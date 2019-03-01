package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.udel.nlpa.swum.dora.DoraEdge;
import edu.udel.nlpa.swum.dora.DoraNode;
import edu.udel.nlpa.swum.dora.EdgeType;
import edu.udel.nlpa.swum.dora.NodeType;
import edu.udel.nlpa.swum.dora.ProgramGraph;
import edu.udel.nlpa.swum.explore.DependencyRelationship;
import edu.udel.nlpa.swum.explore.FieldInitializerVisitor;
import edu.udel.nlpa.swum.explore.ThemeVisitor;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.searchable.SearchableMethodDecl;
import edu.udel.nlpa.swum.scores.AbstractElementScore;
import edu.udel.nlpa.swum.scores.AbstractScoreData;
import edu.udel.nlpa.swum.scores.AbstractScoreData;
import edu.udel.nlpa.swum.scores.CalculateAllSearchScoreData;
import edu.udel.nlpa.swum.scores.CalculateSWUMSearchScoreData;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public class SWUMEval1DoraVisitor extends SWUMSearchVisitor {

	
	private HashMap<String,String> seeds = null;

	private HashSet<String> analyzedList = new HashSet<String>();
	protected static final DependencyRelationship dependencies = new DependencyRelationship();
	private String fname;
	String pkg;
	
	private double max_sig    = 1d;
//	private double max_body   = 1d;
//	private double max_combo  = 1d;
//	private double max_fcombo = 1d;
//	private double max_fsig   = 1d;
	
	private int tp = 0;
	private int fp = 0;
	private int fn = 0;
	private int tn = 0;
	
	private static boolean noK = false;
	
	private boolean debug = true;
	
	public SWUMEval1DoraVisitor(String q, IJavaProject jp, PrintStream p) {
		super(q, jp, p);
		fname = q.replaceAll("\\s+", "_");
		pkg = jp.getPath().toPortableString().replaceFirst("/", "");
		out.println("# " + pkg);
		//fname = "experiment/concerns/" + fname;
	}
	
	public SWUMEval1DoraVisitor(String q, IJavaProject jp, PrintStream p, HashMap<String,String> s, double[] maxes) {
		this(q, jp, p);
		seeds = s;
		max_sig = maxes[0];
//		max_body = maxes[1];
//		max_combo = maxes[2];
//		max_fcombo = maxes[3];
	}
	
//	@Override
//	protected boolean visit(IField node) {
//		String sig2 = ContextBuilder.getSignature(node);
//		
//		if (seeds == null || seeds.contains(sig2)) {
//			seedList.put(DoraNode.getHandle(node), node);
//			//handleFieldUsers(node, sig2);
//		}
//		return true;
//	}
	
	@Override
	protected boolean visit(IMethod node) {
		String sig = ContextBuilder.getSignature(node);
		String sig2 = getRankName(node);
		
		//System.out.println ( getPathLocation(node) + " " + sig2);
		//ProgramGraph pg = new ProgramGraph();
		if (seeds.containsKey(sig2) && !analyzedList.contains(sig)) {
			DoraNode t = createOrGetNode(node, 0);
			// explore seeds in all directions
			t.setGoBackward(true);
			t.setGoForward(true);
			// Always explore seeds and add to nodes
			t.setType(NodeType.SEED);
			// do I want to count tp fp fn tn while I'm at it? Let's just see if it runs...
			explore(t, t.getEdgesAway());
			
			System.out.println("----" + sig2 + " " + tp + " " + fp + " " + fn + " " + tn);
			tp = 0;
			fp = 0;
			fn = 0;
			tn = 0;
			analyzedList.add(sig);
		}
		
		return true;
	}
	
	public static String getRankName(IMethod m) {
		
		String str = "";
		str += m.getDeclaringType().getFullyQualifiedName() + "." +
		m.getElementName() + "_";
		
		String types[] = m.getParameterTypes();
		
		str += types.length;
		
		/*for (int i = 0; i < types.length; i++) {
			str += Signature.getSignatureSimpleName(types[i]);
			if (i < types.length - 1) {
				str += "_";
			}
		}*/
		
		return str;
	}

	private HashMap<IMethod, int[]> getLastCallees(IMethod node) {
		HashMap<IMethod, int[]> c = new HashMap<IMethod, int[]>();
		Set<IMethod> lc = dependencies.getLastCallees(node);
		Set<IMethod> all = dependencies.getCallees(node);
		for (IMethod m : all)
			c.put(m, new int[]{0});
		for (IMethod m : lc) {
			if (c.containsKey(m))
				c.get(m)[0] = 1;
			else
				c.put(m, new int[]{1});
		}
		return c;
	}



//	private boolean getNameOverlap(SearchableMethodDecl sswum,
//			SearchableMethodDecl cswum) {
//		HashSet<String> sName = sswum.getNameNonIgnorableStemmedWords();
//		HashSet<String> cName = cswum.getNameNonIgnorableStemmedWords();
//		
//		return checkOverlap(sName, cName);
//	}
	
	private double getNameOverlapOnQueryTerm(SearchableMethodDecl sswum,
			SearchableMethodDecl cswum) {
		HashSet<String> sName = sswum.getNameStemmedWords();
		HashSet<String> cName = cswum.getNameStemmedWords();
		
		return checkOverlapOnQueryTerm(sName, cName);
	}
	
	// TODO NoAbbrev -- needs to be smarter
	private double getATOverlapOnQueryTerm(SearchableMethodDecl sswum,
			SearchableMethodDecl cswum) {
		HashSet<String> sName = sswum.getActionStemmedWords();
		sName.addAll(sswum.getThemeStemmedWords());
		HashSet<String> cName = cswum.getActionStemmedWords();
		cName.addAll(cswum.getThemeStemmedWords());
		
		return checkOverlapOnQueryTerm(sName, cName);
	}
	
//	private boolean getSigOverlap(SearchableMethodDecl sswum,
//			SearchableMethodDecl cswum) {
//		HashSet<String> sName = sswum.getSigStemmedWords();
//		HashSet<String> cName = cswum.getSigStemmedWords();
//		
//		return checkOverlap(sName, cName);
//	}
//	
//	private boolean getNameMatch(SearchableMethodDecl sswum,
//			SearchableMethodDecl cswum) {
//		return sswum.toStemmedSearchableString()
//			.matches(cswum.toStemmedSearchableString());
//	}

	private boolean getThemeAndActionOverlap(SearchableMethodDecl sswum,
			SearchableMethodDecl cswum) {
		HashSet<String> sAction = sswum.getActionStemmedWords();
		HashSet<String> sTheme  = sswum.getThemeStemmedWords();
		HashSet<String> cAction = cswum.getActionStemmedWords();
		HashSet<String> cTheme  = cswum.getThemeStemmedWords();
		
		boolean action = checkOverlap(sAction, cAction);
		boolean theme = checkOverlap(sTheme, cTheme);
		
		return action && theme;
	}
	
//	private boolean getThemeOrActionOverlap(SearchableMethodDecl sswum,
//			SearchableMethodDecl cswum) {
//		HashSet<String> sAction = sswum.getActionNonIgnorableStemmedWords();
//		HashSet<String> sTheme  = sswum.getThemeNonIgnorableStemmedWords();
//		HashSet<String> cAction = cswum.getActionNonIgnorableStemmedWords();
//		HashSet<String> cTheme  = cswum.getThemeNonIgnorableStemmedWords();
//		
//		boolean action = checkOverlap(sAction, cAction);
//		boolean theme = checkOverlap(sTheme, cTheme);
//		
//		return action || theme;
//	}

	private boolean checkOverlap(HashSet<String> sAction, HashSet<String> cAction) {
		for (String s : sAction)
			if (cAction.contains(s))
				return true;
		return false;
	}
	
	private double Q_WEIGHT  = 1d;
	private double NQ_WEIGHT = 0.5;
	private double checkOverlapOnQueryTerm(HashSet<String> sAction, HashSet<String> cAction) {
		double olap = 0d;
		double total = 0d;
		double score = 0d;
		for (String s : sAction) {
			double cnt = NQ_WEIGHT * AbstractElementScore.getLinearIDF(s);
			if (swum.isQueryWord(s))
				cnt = Q_WEIGHT * AbstractElementScore.getLinearIDF(s);
			
			if (cAction.contains(s))
				olap += cnt;
			
			total += cnt;

		}
		
		for (String s : cAction) {
			if (!sAction.contains(s)) {
				if (swum.isQueryWord(s))
					total += Q_WEIGHT * AbstractElementScore.getLinearIDF(s);
				else
					total += NQ_WEIGHT * AbstractElementScore.getLinearIDF(s);
			}
		}
			
		if (total > 0)
			score = olap / total;
		return score;
	}
	
	protected int getApproxStmts(IMethod m) {
		String src = "";
		
		try {
			src = m.getSource();
		} catch (JavaModelException e) { e.printStackTrace(); }
		
		return getApproxStmts(src);
	}
	protected int getApproxStmts(String src) {
		int lines = 0;
		
		if (src != null && !src.equals("")) {
			for (char c : src.toCharArray()) {
				if (c == ';' || c == '}' || c == '{')
					lines++;
			}
		}
		
		return lines;
	}

//	private double calculateFanIn(int callee_fanin) {
//		 double fanin = 0d;
//		if (callee_fanin > 0)
//			fanin = 1d / (Math.log(callee_fanin) + 1d);
//		return fanin;
//	}
	
	private double calculateFanIn10(int callee_fanin) {
		 double fanin = 0d;
		if (callee_fanin > 0)
			fanin = 1d / (Math.log10(callee_fanin) + 1d);
		return fanin;
	}

	
	private double t_caller = 0.51;
	
	private void explore(DoraNode sd, int edgesAway) {
		if (debug) out.println("\n--" + sd.getSignature());
		
		if (sd.getElement() instanceof IMethod) {
			AbstractScoreData current = getScore(sd);
			
				findCallers(sd, edgesAway, current);

			// explore family
//			if(!sd.hasAnalyzedFamily() && sd.getType() != NodeType.K && 
//					!sd.getElement().isBinary() && !sd.getScore().isConstructor()) {
//				findFamily(sd, edgesAway, pg, current);
//				sd.setAnalyzedFamily(true);
//			}

				findCallees(sd, edgesAway, (CalculateSWUMSearchScoreData)current);

		} /*else if (sd.getElement() instanceof IField) {
			CalculateAllSearchScoreData current = (CalculateAllSearchScoreData)getScore(sd);
			// field accessors
			findFieldAccessors(sd, edgesAway, pg, current);
			sd.setWentBackward(true);
		}*/

	}
	

	private AbstractScoreData getScore(DoraNode sd) {
		AbstractScoreData current = null;
		if (sd.getScore() == null) {
			if (sd.getElement() instanceof IMethod) {
			current = new CalculateSWUMSearchScoreData(dora, swum, name, bow);
			((CalculateSWUMSearchScoreData)current).
				gatherScores(sd.getElement());
			} else if (sd.getElement() instanceof IField) {
				current = new CalculateAllSearchScoreData(dora, swum, bow, dep, sig, name);
				((CalculateAllSearchScoreData)current).
				gatherScores((IField)sd.getElement(), F_SWM, F_SIG, F_SRC);
			}
			sd.setScore(current);
		} else {
			current = sd.getScore();
		}
		return current;
	}

	private void findFamily(DoraNode sd, int edgesAway, 
			AbstractScoreData current) {
		Set<IMember> c = dependencies.getMemberInterfaces(sd.getElement());
		if (c.size() > 0) {
			if (debug) out.print("\nFamily:\n");
			for (IMember m : c) {
				//out.println("\ti " + ContextBuilder.getSignature(m));
				
				//if (s > 0) { // add node going forward
				if (!ContextBuilder.getSignature(m).equals(sd.getSignature())) {
					DoraNode d;

					if (m.isBinary()) {
						d = createOrGetNode(m, edgesAway + 1, sd.getScore(), NodeType.LS);
					} else {
						d = createOrGetNode(m, edgesAway + 1, sd.getScore(), sd.getType());
						d.setGoBackward(sd.shouldGoBackward());
						d.setGoForward(sd.shouldGoForward());
					}
					
						
						out.println("\t" + sd.getType().toString() + " " + 
								getRankName((IMethod)m) + " " + pf.sprintf(sd.getScore().getScore()));
				}
			}
		}
	}
	
	private void findCallers(DoraNode sd, int edgesAway, 
			AbstractScoreData callee) {
		Set<IMethod> c = dependencies.getCHCallersInProject((IMethod)sd.getElement(), pkg);
		if (c.size() > 0) {
			if (debug) out.print("\nCallers:\n");
			for (IMethod m : c) {
				CalculateSWUMSearchScoreData caller = new CalculateSWUMSearchScoreData(dora, swum, name, bow);
				caller.gatherScores(m);
				double sig_callee = callee.getScore() / max_sig;
				double sig_caller = caller.getScore() / max_sig;
				
				int last = 0;
				HashMap<IMethod, int[]> cr = getLastCallees(m);
				if (cr.containsKey(m))
					last = cr.get(m)[0];
				
				int caller_fanout = cr.size();
				int callee_fanin = c.size();
				
				boolean and = getThemeAndActionOverlap(caller.getConstructedSWUM(), 
						callee.getConstructedSWUM());

				double fanin = Math.max(calculateFanIn10(callee_fanin),
						calculateFanIn10(caller_fanout));
					
				double qname = getNameOverlapOnQueryTerm(caller.getConstructedSWUM(), 
						callee.getConstructedSWUM());
				double qo = qname;

				double l = calculateFanIn10(2);
				if (last == 1)
					l = 1d;
				
				double damp = Math.max(l, fanin);
				
				double qat = 0d;
					qat = getATOverlapOnQueryTerm(caller.getConstructedSWUM(), 
						callee.getConstructedSWUM());
				if (and && !sd.getElement().isBinary() && !caller.isReactive())
					qat = 1d;
				
				// m 
				//double wrt_query = sig_caller;// + 0.1 * sig;
				double wrt_qo = 0d;
				if (qo > 0)
					wrt_qo = damp * (qo + sig_caller) * 0.5;
				
				double wrt_qat = 0d;
				double wrt_qat2 = 0d;
				if (qat > 0) {
					wrt_qat = damp * (qat + sig_caller) * 0.5;
					wrt_qat2 = damp * qat;
				}
				
				double lavg = 0d;
				if (sig_callee > 0 && sig_caller > 0) {
					lavg = 0.5 * (sig_callee + sig_caller);
				}
				
				boolean isP = caller.getNameScore() == 0 && caller.isReactive() && !m.isBinary();
				String firstWord = caller.getConstructedSWUM().getParse().beginsWith();
				boolean isK = AbstractElementScore.getPos().isGeneralVerb(firstWord) && !m.isBinary();
				
				NodeType type = NodeType.NO_TYPE;
				double s = 0d;
				boolean forward = false;
				boolean backward = false;
				
				//s = wrt_qat;
				if (sd.getType() == NodeType.K) {
					if (wrt_qo > 0.75 || wrt_qat > 0.75 || wrt_qat2 > 0.75) {
						type = NodeType.K;
						s = wrt_qat;
						//if (sig_callee > 0)
						//if (callee_fanin == 1) // Lost 8 FPs & 1 TP
						// ISSUE: is this arbitrary?
						// Idea: if K has few callers, keep going, o/w general
						if (callee_fanin < 3) // lose 3 FP
							backward = true;
					} else if (isP) {
						type = NodeType.P;
					}
					s = wrt_qat2;
				} else { // don't go backward from "s", so can only be "M"
					if (sig_caller > t_caller) { // 0.7?
						if (m.isBinary()) {
							type = NodeType.L;
							//forward = true;
							backward = true;
						} else {
							type = NodeType.M;
							forward = true;
							backward = true;
						}
						s = sig_caller;
						
//				} else if (wrt_qat > 0.7 || wrt_qo > 0.6) { // old
					} else if (wrt_qat > 0.4 || wrt_qo > 0.4) { // new
						// can always upgrade to 0.5
						if (m.isBinary()) {
							type = NodeType.L;
							//forward = true;
							backward = true;
						} else {
							type = NodeType.M;
							forward = true;
							backward = true;
						}
						s = wrt_qat;
					} else if (sd.getElement().isBinary() && !m.isBinary() &&
							lavg > 0) {
						// we're hoping to weave back into user code
						type = NodeType.K;
						backward = true;
						s = lavg;
					} else if (isP) {
						type = NodeType.P;
					} else if (isK) {
						type = NodeType.K;
						if (sig_callee > 0)
							backward = true;
					}
				}
				
				String s2 = getRankName(m);
				if (type != NodeType.NO_TYPE) { // add node going forward
					//type = NodeType.M;
					DoraNode d = createOrGetNode(m, edgesAway + 1, caller, type);
					d.setGoForward(forward || d.shouldGoForward());
					d.setGoBackward(backward || d.shouldGoBackward());
					
					if (seeds.containsKey(s2) && !seeds.get(s2).matches("PN")) {
						if (noK && (type == NodeType.K || type == NodeType.P))
							fn++;
						else
							tp++;
					} else {
						if (noK && (type == NodeType.K || type == NodeType.P))
							tn++;
						else
							fp++;
					}
				} else {
					if (seeds.containsKey(s2) && !seeds.get(s2).matches("PN")) {
						fn++;
					} else {
						tn++;
					}
				}
				
				out.println("\t" + type.toString() + " " + 
						getRankName(m) + " " + pf.sprintf(s));
			}
		}
	}
	
/*	private void findFields(DoraNode sd, int edgesAway, ProgramGraph pg,
			AbstractScoreData current) {
		Set<IField> c = dependencies.getFieldUses(sd.getElement());
		if (c.size() > 0) {
			if (debug) out.print("\nFields:\n");
			for (IField m : c) {
				
				CalculateAllSearchScoreData use = getFieldScores(m);
				double sig_caller = current.getScore() / max_sig;
				double combo = use.getScore() / max_fcombo;
				double maxq = 0d;
				boolean written = getFieldWritten(m, sd.getElement());
				boolean read = true;
				
				if (written) // only need to check for read if written
					read = getFieldRead(m, sd.getElement());
				
				if (use.getFieldNameStemmedWords() != null) {
					HashSet<String> sName = current.getConstructedSWUM().getNameStemmedWords();
					HashSet<String> tName = current.getConstructedSWUM().getThemeNoAbbrevStemmedWords();
					tName.addAll(sName);
				
					maxq = Math.max( checkOverlapOnQueryTerm(sName, 
							use.getFieldNameStemmedWords()), 
									 checkOverlapOnQueryTerm(tName, 
							use.getFieldNameStemmedWords()) );
				}
				
				double wrt_query = combo + 0.1 * sig_caller;
				double wrt_qo = 0d;
				if (maxq > 0)
					wrt_qo = (maxq + combo + sig_caller) / 3d;
				
				NodeType type = NodeType.NO_TYPE;
				double s = 0d;
				//boolean forward = false;
				boolean backward = false;
				
				if (wrt_query > 0.8) { // 0.5?
					type = NodeType.RELEVANT_FIELD;
					if (m.isBinary())
						type = NodeType.LIBRARY_FIELD;
					s = wrt_query;
					backward = true;
				} else if (wrt_qo > 0.6) {
					type = NodeType.RELEVANT_FIELD; // NodeType.FIELD ?
					if (m.isBinary())
						type = NodeType.LIBRARY_FIELD;
					s = wrt_qo;
					backward = true;
				}		

				//s = written ? 1 : 0;
//				type = NodeType.NO_TYPE;
//				if (written) {
//					type = NodeType.FIELD;
//					s = wrt_query;
//				}
				
				if (type != NodeType.NO_TYPE) { // add node going forward
					
//					if (written)
//						backward = true;
					
					//type = NodeType.M;
					DoraNode d = createOrGetNode(pg, m, edgesAway + 1, use, type);
					// don't undo settings if already major
					d.setGoBackward(backward || d.shouldGoBackward());
					
					//add edge
					//pg.addEdge(sd, d, EdgeType.PLAIN);
					pg.addEdge(sd, d, EdgeType.PLAIN, s, written, read);
					addToWorkList(d);
				} else if (pg.containsNode(m)) { // in concern
					// add edge
					DoraNode d = pg.getNode(m);
					pg.addEdge(sd, d, EdgeType.PLAIN, s);
				}
//				} else if (combo > 0) {
//					//if (handleVisited(m, caller)) {
//					//if (handleVisited(m, sd)) {
//					String h = DoraNode.getHandle(m);
//					if (visited.containsKey(h)) {
//						visited.get(h)[0] += sig_caller;
//						if (visited.get(h)[0] > 1) {
//							DoraNode d = createOrGetNode(pg, m, edgesAway + 1, use, NodeType.FIELD);
//							pg.addEdge(sd, d, EdgeType.CONNECT,visited.get(h)[0]);
//						}
//						//pg.addEdge(sd, visited.get(DoraNode.getHandle(m)), EdgeType.CONNECT, 0);
//						//addToWorkList(d);
//					} else
//						visited.put(h, new double[]{sig_caller});
//				}
				
				out.println("\t" + type.toString() + " " + 
						ContextBuilder.getSignature(m) + " " + pf.sprintf(s));
			}
		}
	}
	
	private boolean getFieldRead(IField f, IMember m) {
		boolean w = false;
		String src = "";
		try {
			src = m.getSource();
		} catch (JavaModelException e) { }
		
		if (src != null && !src.matches("")) {
			src = src.replaceAll("\\n", " ");
			if (src.matches("^.*" + f.getElementName() + "\\s*[^= ].*"))
				w = true;
		}
		return w;
	}

	private boolean getFieldWritten(IField f, IMember m) {
		boolean w = false;
		String src = "";
		try {
			src = m.getSource();
		} catch (JavaModelException e) { }
		
		if (src != null && !src.matches("")) {
			src = src.replaceAll("\\n", " ");
			if (src.matches("^.*" + f.getElementName() + "\\s*=[^=].*"))
				w = true;
		}
		return w;
	}*/

//	private void findFieldAccessors(DoraNode sd, int edgesAway, ProgramGraph pg,
//			CalculateAllSearchScoreData field) {
//		Set<IMember> c = dependencies.getProjectReferences(sd.getElement(), _proj);
//		if (c.size() > 0) {
//			if (debug) out.print("\nField Accessors:\n");
//			for (IMember m : c) {
//				CalculateSWUMSearchScoreData caller = new CalculateSWUMSearchScoreData(swum, name, bow);
//				caller.gatherScores(m);
//				
//				boolean written = getFieldWritten((IField)sd.getElement(), m);
//				boolean read = true;
//				
//				if (written) // only need to check for read if written
//					read = getFieldRead((IField)sd.getElement(), m);
//				
//				double sig = caller.getSwums() / max_sig;
//				double fscore = field.getScore() / max_fcombo;
//				
//				double maxq = 0d;
//				
//				if (field.getFieldNameStemmedWords() != null) {
//					HashSet<String> sName = caller.getConstructedSWUM().getNameStemmedWords();					
//					HashSet<String> tName = caller.getConstructedSWUM().getThemeNoAbbrevStemmedWords();
//					tName.addAll(sName);
//				
//					maxq = Math.max( checkOverlapOnQueryTerm(sName, 
//							field.getFieldNameStemmedWords()), 
//									 checkOverlapOnQueryTerm(tName, 
//							field.getFieldNameStemmedWords()) );
//				}
//				
//				double wrt_caller = 0d;
//				if (maxq > 0)
//					wrt_caller = (maxq + fscore + sig) / 3d;
//				
//				NodeType type = NodeType.NO_TYPE;
//				double s = 0d;
//				boolean forward = false;
//				boolean backward = false;
//				
//				if (wrt_caller > 0.6) {
//					if (wrt_caller > 0.7) {
//						type = NodeType.M;
//						if (m.isBinary())
//							type = NodeType.L;
//					} else {
//						type = NodeType.S;
//						if (m.isBinary())
//							type = NodeType.LS;
//					}
//					s = wrt_caller;
//					//forward = true; //?
//					//if (written)
//						backward = true; // TODO if field accessor not callee of m, e?
//				}
//				
//				
//				
//				//s = written ? 1 : 0;
////				s = wrt_caller;
////				type = NodeType.NO_TYPE;
////				if (written) {
////					type = NodeType.C;
////					
////				}
//				
//				
//				boolean isInit = caller.isConstructor();
//				if (!isInit) {
//					HashSet<String> verbs = caller.getConstructedSWUM().getActionWords();
//					isInit |= verbs.contains("init");
//					isInit |= verbs.contains("create");
//					isInit |= verbs.contains("creates");
//					isInit |= verbs.contains("setup");
//					isInit |= verbs.contains("initialize");
//					isInit |= verbs.contains("initializes");
//					isInit |= verbs.contains("construct");
//					isInit |= verbs.contains("constructs");
//					isInit |= verbs.contains("assign");
//					isInit |= verbs.contains("assigns");
//					isInit |= verbs.contains("instantiate");
//					isInit |= verbs.contains("instantiates");	
//				}
//				
//				// only for m & seed fields
//				if (isInit && written && type == NodeType.NO_TYPE) {
//					type = NodeType.I;
//					forward = true; // to look up supporting?
//				}
//
//				DoraNode d = null;
//				//if (s > 0) { 
//				if (type != NodeType.NO_TYPE) { // add node going forward
//						//type = NodeType.M;
//						d = createOrGetNode(pg, m, edgesAway + 1, caller, type);
//						d.setGoForward(forward || d.shouldGoForward());
//						d.setGoBackward(backward || d.shouldGoBackward());
//						
//						//add edge
//						//pg.addEdge(sd, d, EdgeType.PLAIN);
//						pg.addEdge(d, sd, EdgeType.PLAIN, s, written, read);
//						addToWorkList(d);
//				} else if (pg.containsNode(m)) { // in concern
//					// add edge
//					d = pg.getNode(m);
//					pg.addEdge(d, sd, EdgeType.PLAIN, s);
//				}
////				} else {
////					//if (handleVisited(m, caller)) {
////					if (handleVisited(m, sd)) {
////						d = createOrGetNode(pg, m, edgesAway + 1, caller, NodeType.C);
////						pg.addEdge(d, sd, EdgeType.CONNECT, s, written);
////						pg.addEdge(visited.get(DoraNode.getHandle(m)), sd, EdgeType.CONNECT, 0);
////					}
////				}
//				
//				out.println("\t" + type.toString() + " " + 
//						ContextBuilder.getSignature(m) + " " + pf.sprintf(s));
//				
//				if (d != null && d.getType() == NodeType.I) {
//					// Need to find initializing methods
//					findFieldInitializer(sd, d, pg);
//				}
//			}
//		}
//	}

//	private void findFieldInitializer(DoraNode field, DoraNode caller,
//			ProgramGraph pg) {
//		FieldInitializerVisitor fv = new FieldInitializerVisitor((IField)field.getElement(), (IMethod)caller.getElement());
//		
//		try {
//			// Create AST
//			ASTParser lParser = ASTParser.newParser(AST.JLS3); 
//			lParser.setKind(ASTParser.K_COMPILATION_UNIT);
//			lParser.setSource(caller.getElement().getCompilationUnit()); // set source
//			lParser.setResolveBindings(true); // set bindings
//
//			CompilationUnit cu = (CompilationUnit) lParser.createAST(null);
//			cu.accept(fv);
//		} catch (Exception e) { }
//		
//		for (IMember m : fv.getInitializers()) {
//			DoraNode d = createOrGetNode(pg, m, caller.getEdgesAway() + 1, null, NodeType.I);
//			d.setGoForward(true);
//			d.setGoBackward(d.shouldGoBackward());
//
//			//add edge
//			pg.addEdge(caller, d, EdgeType.PLAIN, 0);
//			pg.addEdge(d, field, EdgeType.INITIALIZER, 0);
//			addToWorkList(d);
//		}		
//	}

	private void findCallees(DoraNode sd, int edgesAway, 
			CalculateSWUMSearchScoreData current) {
		HashMap<IMethod, int[]> c = getLastCallees((IMethod)sd.getElement());
		//Set<IMethod> c = dependencies.getCallees((IMethod)sd.getElement());
		if (c.size() > 0) {
			if (debug) out.print("\nCallees:\n");
			for (IMethod m : c.keySet()) {
				int caller_fanout = c.size();
				int callee_fanin = 0;
				if (!m.isBinary()) callee_fanin = dependencies.getCHCallersInProject(m).size();
				
				Set<Node> actuals = null;
				
				
				CalculateSWUMSearchScoreData callee = new CalculateSWUMSearchScoreData(dora, swum, name, bow);
				callee.gatherScores(m, actuals);
				
				double sig_callee = callee.getSwums() / max_sig /*+ 0.1 * callee.getBowScore() / max_body*/;
				double sig_caller = current.getSwums() / max_sig;
				double act_callee = sig_callee;
				
				if (!sd.getElement().isBinary()) {
					actuals = getActualTheme((IMethod)sd.getElement(), m);

					callee.gatherScores(m, actuals);
					act_callee = callee.getSwums() / max_sig /*+ 0.1 * callee.getBowScore() / max_body*/;
				}

				boolean and = getThemeAndActionOverlap(current.getConstructedSWUM(), 
						callee.getConstructedSWUM());
				
				double qname = getNameOverlapOnQueryTerm(current.getConstructedSWUM(), 
						callee.getConstructedSWUM());
				double qo = 0d;
				if (callee.getNameScore() > 0)
					qo = qname;
				
				boolean isO = AbstractElementScore.getPos().isSideEffectWord(
						callee.getConstructedSWUM().getParse().beginsWith());

				double fanin = Math.max(calculateFanIn10(callee_fanin),
						calculateFanIn10(caller_fanout));
				
				double l = calculateFanIn10(2);
				if (c.get(m)[0] == 1)
					l = 1d;
				double damp = Math.max(l, fanin);
				
				double qat = 0d;
				if (sig_callee > 0) {
					qat = getATOverlapOnQueryTerm(current.getConstructedSWUM(), 
						callee.getConstructedSWUM());
					if (and && !m.isBinary() && !callee.isReactive())
						qat = 1d;
				}
				
				double wrt_qo = 0d;
				double wrt_qos = 0d;
				if (qo > 0) {
					wrt_qo = damp * (qo + sig_caller) * 0.5;
					wrt_qos = damp * qo;
				}
				
				double wrt_qat = 0d;
				double wrt_qats = 0d;
				if (qat > 0) {
					wrt_qat = damp * (qat + sig_caller) * 0.5;
					wrt_qats = damp * qat;
				}

				NodeType type = NodeType.NO_TYPE;
				double s = 0d;
				boolean forward = false;
				boolean backward = false;
				
				//s = wrt_qo;
				// o
				if (isO && act_callee > 0) {
					s = act_callee;
					type = NodeType.E;
				} else if (act_callee > 0.9 || qo == 1 || qat == 1) { // perfect overlap
					if (sd.getType() == NodeType.SEED)
						type = NodeType.M;
					else
						type = sd.getType();
					
					if (m.isBinary()) {
						if (type == NodeType.M)
							type = NodeType.L;
						else
							type = NodeType.LS;
						forward = sd.shouldGoForward();
					} else {
						//type = sd.getType(); // if qo == 1, node type = type of parent, regardless of match
						forward = sd.shouldGoForward();
						backward = sd.shouldGoBackward();
					}
					s = 1d;
				// can't be just actuals, sig must contain  query word too
				} else if (act_callee > 0.6 && sig_callee > 0) { // 0.5? && name > 0?
					type = NodeType.S;
					if (m.isBinary())
						type = NodeType.LS;
					s = act_callee;
					forward = true;
					//backward = true;
				} else if (wrt_qo > 0.5) { // 0.6?
					type = NodeType.S;
					forward = true;
					//backward = true;
					if (m.isBinary())
						type = NodeType.LS;
					s = wrt_qo;
				} else if (wrt_qat > 0.7) {
					type = NodeType.S;
					if (m.isBinary())
						type = NodeType.LS;
					forward = true;
					//backward = true;
					s = wrt_qat;
				} else if (sd.getType() == NodeType.S && wrt_qats > 0.6) {
					type = NodeType.S;
					if (m.isBinary())
						type = NodeType.LS;
					forward = true;
					s = wrt_qats;
				} else if (sd.getType() == NodeType.I && act_callee > t_caller) {
					// Need same lower threshold as callers when expecting
					// little overlap b/w names -- it's like exploring callees
					// from p/k's
					if (m.isBinary()) {
						type = NodeType.L;
						forward = true; // keep going in this direction
						//backward = true;
					} else {
						type = NodeType.M;
						forward = true;
						backward = true;
					}
					s = act_callee;
				}
				
				if (type == NodeType.LS) {
					forward = false;
					// if the actions or the first words don't overlap...
//					if (!checkOverlap(current.getConstructedSWUM().getNameStemmedWords(),
//							callee.getConstructedSWUM().getNameStemmedWords()) )
//						type = NodeType.NO_TYPE; // libs can be greedy
					// only want to include if overlapping action or theme?
				}
				// if L action doesn't overlap, downgrade to LS??
				
//				s = sig_callee;
//				type = NodeType.S;
				
				String s2 = getRankName(m);
				if (type != NodeType.NO_TYPE) { // add node going forward
	
					DoraNode d = createOrGetNode(m, edgesAway + 1, callee, type);

					// if it's already set, don't undo
					d.setGoForward(forward || d.shouldGoForward());
					d.setGoBackward(backward || d.shouldGoBackward());
					
					
					if (seeds.containsKey(s2) && !seeds.get(s2).matches("PN")) {
						tp++;
					} else {
						fp++;
					}
				} else {
					if (seeds.containsKey(s2) && !seeds.get(s2).matches("PN")) {
						fn++;
					} else {
						tn++;
					}
				}
				
				out.println("\t" + type.toString() + " " + 
						getRankName(m) + " " + pf.sprintf(s));
			}
		}
	}

	//private boolean handleVisited(IMember m, AbstractScoreData callee) {
//	private boolean handleVisited(IMember m, DoraNode caller) {
//		return false;
//		String h = DoraNode.getHandle(m);
//		if (visited.containsKey(h))
//			return true;
//		//visited.put(h, callee);
//		visited.put(h, caller);
//		return false;
//	}

	private Set<Node> getActualTheme(IMethod node, IMethod m) {
		ThemeVisitor tv = new ThemeVisitor(node, m);

		try {
			// Create AST
			ASTParser lParser = ASTParser.newParser(AST.JLS3); 
			lParser.setKind(ASTParser.K_COMPILATION_UNIT);
			lParser.setSource(node.getCompilationUnit()); // set source
			lParser.setResolveBindings(true); // set bindings

			CompilationUnit cu = (CompilationUnit) lParser.createAST(null);
			cu.accept(tv);
		} catch (Exception e) { }

		return tv.getActualTheme();
	}
	
	
	private DoraNode createOrGetNode(IMember sd, int e) {
		return new DoraNode(sd, e);
	}
	
	private DoraNode createOrGetNode(IMember sd, int e, 
			AbstractScoreData score, NodeType nt) {
		return new DoraNode(sd, e, score, nt);
	}
	
//	private DoraNode createOrGetNode(ProgramGraph pg, IMember sd, int e, double pscore,
//			AbstractScoreData score, NodeType nt) {
//		String handle = DoraNode.getHandle(sd);
//		DoraNode t;
//		if (pg.containsNode(handle)) {
//			t = pg.getNode(handle);
//			// assume score equiv
//			if (e < t.getEdgesAway()) {
//				t.setEdgesAway(e);
//			//if (nt.getRelevance() > t.getType().getRelevance())
//				t.setType(nt);
//			//if (pscore > t.getPathScore())
//				t.setPathScore(pscore);
//			}
//		} else {
//			t = new DoraNode(sd, e, pscore, score, nt);
//			pg.addNode(t);
//		}
//			
//		return t;
//	}
	

	
	public String toString() {
		return super.swum.getContains().keySet().toString();
	}
}
