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

public class SWUMTopDoraVisitor extends SWUMSearchVisitor {

	
	private Set<String> seeds = null;
	private HashMap<String, IMember> seedList = new HashMap<String, IMember>();
	//private HashMap<String, IMember> libSeedList = new HashMap<String, IMember>();
	//private HashMap<String, AbstractScoreData> visited = new HashMap<String, AbstractScoreData>();
	private HashMap<String, double[]> visited = new HashMap<String, double[]>();

	private HashSet<String> analyzedList = new HashSet<String>();
	protected static final DependencyRelationship dependencies = new DependencyRelationship();
	private String fname;
	private String altdir = "dora_results/5edge/tsdora50/";
	String pkg;
	
	private int    t_distance = 5; // 10
	private double t_very_greedy = 0.51;
	//private double t_greedy = 0.51;
	private double t_greedy = 0.6;
	private double t_conserv = 0.75; // high overlap + sig_caller * damp
	private int    t_lowfanin = 3;
	private double intercept = 0d;//0.5;
		
	private boolean debug = true;
	
	public SWUMTopDoraVisitor(String q, IJavaProject jp, PrintStream p) {
		super(q, jp, p);
		fname = altdir + q.replaceAll("\\s+", "_");
		pkg = jp.getPath().toPortableString().replaceFirst("/", "");
		out.println("# " + pkg);
		//fname = "experiment/concerns/" + fname;
	}
	
	public SWUMTopDoraVisitor(String q, IJavaProject jp, PrintStream p, Set<String> s, double[] maxes) {
		this(q, jp, p);
		seeds = s;
	}
	
	@Override
	protected boolean visit(IField node) {
		String sig2 = ContextBuilder.getSignature(node);
		
		if (seeds == null || seeds.contains(sig2)) {
			seedList.put(DoraNode.getHandle(node), node);
			//handleFieldUsers(node, sig2);
		}
		return true;
	}
	
	@Override
	protected boolean visit(IMethod node) {
		String sig2 = ContextBuilder.getSignature(node);
		sig2 = SWUMEval1DoraVisitor.getRankName(node);
		//System.out.println ( getPathLocation(node) + " " + sig2);
		
		if (seeds == null || seeds.contains(sig2)) {
			seedList.put(DoraNode.getHandle(node), node);
		}
		
		return true;
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



	private boolean getNameOverlap(SearchableMethodDecl sswum,
			SearchableMethodDecl cswum) {
		HashSet<String> sName = sswum.getNameNonIgnorableStemmedWords();
		HashSet<String> cName = cswum.getNameNonIgnorableStemmedWords();
		
		return checkOverlap(sName, cName);
	}
	
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
	
	private boolean getSigOverlap(SearchableMethodDecl sswum,
			SearchableMethodDecl cswum) {
		HashSet<String> sName = sswum.getSigStemmedWords();
		HashSet<String> cName = cswum.getSigStemmedWords();
		
		return checkOverlap(sName, cName);
	}
	
	private boolean getNameMatch(SearchableMethodDecl sswum,
			SearchableMethodDecl cswum) {
		return sswum.toStemmedSearchableString()
			.matches(cswum.toStemmedSearchableString());
	}

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
	
	private boolean getThemeOrActionOverlap(SearchableMethodDecl sswum,
			SearchableMethodDecl cswum) {
		HashSet<String> sAction = sswum.getActionNonIgnorableStemmedWords();
		HashSet<String> sTheme  = sswum.getThemeNonIgnorableStemmedWords();
		HashSet<String> cAction = cswum.getActionNonIgnorableStemmedWords();
		HashSet<String> cTheme  = cswum.getThemeNonIgnorableStemmedWords();
		
		boolean action = checkOverlap(sAction, cAction);
		boolean theme = checkOverlap(sTheme, cTheme);
		
		return action || theme;
	}

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

	private double calculateFanIn(int callee_fanin) {
		 double fanin = 0d;
		if (callee_fanin > 0)
			fanin = 1d / (Math.log(callee_fanin) + 1d);
		return fanin;
	}
	
	private double t_damp = 1d / (Math.log10(2) + 1d);
	private double calculateFanIn10(int callee_fanin) {
		 double fanin = 0d;
		if (callee_fanin > 0)
			fanin = 1d / (Math.log10(callee_fanin) + 1d);
		return fanin;
	}
	
	private HashMap<String, DoraNode> workList;
	public void exploreSeedSet() {
		ProgramGraph pg = new ProgramGraph();
		
		exploreSet(pg, seedList, NodeType.SEED);
		//exploreSet(pg, libSeedList, NodeType.LS);
		
		reviseSet(pg);
			
		pg.display(fname);
	}
	
	private void reviseSet(ProgramGraph pg) {
		HashSet<DoraNode> removeNodes = new HashSet<DoraNode>();
		HashSet<DoraEdge> removeEdges = new HashSet<DoraEdge>();
		
		for (DoraNode d : pg.getNodes().values()) {
			if (d.getElement() instanceof IField) { // for each field
				doubleCheckAccessors(d, pg); // add any p/k links
				int written = 0;
				int read = 0;
				int count = 0;
				
				// make sure any field accessors link with the rest of the concern
				// o/w remove them
				for (DoraEdge e : d.getInEdges().values()) {
					if (e.getEdgeType() != EdgeType.INITIALIZER)
						count++;
					if (e.isWritten())
						written++;
					if (e.isRead())
						read++;
					// if this is the only edge to this node
					if ( e.getEdgeType() == EdgeType.PLAIN &&
							e.getFromNode().getInEdges().isEmpty() &&
							//e.getFromNode().getOutEdges().size() == 1 &&
							e.getFromNode().getType() != NodeType.SEED) {
						
						HashSet<String> action = 
							e.getFromNode().getScore().getConstructedSWUM().getActionWords();
						
						// first, double check callers not in concern
						if (!doubleCheckHasOtherLinks(e.getFromNode(), pg) && e.getFromNode().getType() != NodeType.I 
								&& (action.contains("get") || action.contains("is"))) { // conserv
							// remove this node & edge
							removeEdges.add(e);
							removeNodes.add(e.getFromNode());
							count--;
							out.println("-- " + e.getFromNode().getType() + " " + 
									ContextBuilder.getSignature(e.getFromNode().getElement()));
							out.println("-- " + ContextBuilder.getSignature(e.getFromNode().getElement()) + " -> " + 
									ContextBuilder.getSignature(d.getElement()));
							if (e.isWritten())
								written--;
							if (e.isRead())
								read--;

						}
					}
				}
				
				// if only written to within concern, not read -- then side effect
				if (written == count) {
					if (read > 0 && count > 1) // it's written & read in multiple methods
						d.setType(NodeType.C_FIELD);
					else // it's only written to (never read) or it's read/written within the same 1 method
						d.setType(NodeType.E_FIELD);
					out.println("** " + d.getType() + " " + ContextBuilder.getSignature(d.getElement()));
				// o/w a communicator
				} else if (count == 1) { // if read by 1 method only
					if (d.getType() != NodeType.SEED_FIELD)
						d.setType(NodeType.S_FIELD);
				} else { //if (d.getType() != NodeType.SEED_FIELD) { // only M detectors?

					// otherwise if not seed, it's a communicator!
					
					// It doesn't depend on what type the field is, but on what type the METHOD is!
					if (d.getType() != NodeType.SEED_FIELD)
						d.setType(NodeType.C_FIELD);
					
					// re-check in edges for getters & setters (c)
					for (DoraEdge e : d.getInEdges().values()) {
						if ( e.getEdgeType() == EdgeType.PLAIN && !removeEdges.contains(e)) {
							HashSet<String> action = 
								e.getFromNode().getScore().getConstructedSWUM().getActionWords();
							
							// if it's not super important
							if ( e.getFromNode().getType() != NodeType.SEED
									 //&& e.getFromNode().getType() != NodeType.M
									) {

								// Make sure it's really a setter for this field
								if (action.contains("set") && e.isWritten()) {
									e.getFromNode().setType(NodeType.C);
								} else if (action.contains("get") || 
										action.contains("is") ) {
									// Make sure it's really a getter

									try {
										String src = e.getFromNode().getElement().getSource();
										src = src.replaceAll("\\n", " ");
										//if (src.matches("^.*"+node.getElementName()+"\\s*\\([^)]*\\)\\s*\\{[^;]*;[^;]*\\}\\s*$"))
										if (src.matches("^.*"+e.getFromNode().getElement().getElementName()+"\\s*\\([^)]*\\)\\s*\\{[^;]*;[^;]*\\}\\s*$"))
											e.getFromNode().setType(NodeType.C);
									} catch (JavaModelException e1) { 
										// if no src, give benefit of doubt
										e.getFromNode().setType(NodeType.C);
									}
								}
								
								if (e.getFromNode().getType() == NodeType.C) {
									out.println("** " + e.getFromNode().getType() + " " + 
											ContextBuilder.getSignature(e.getFromNode().getElement()));
									makeFamilyCommunicators(e.getFromNode(), pg);
								}
								// TODO if reset as C, make sure family C too
							}		
						}
					}
				}
			} else if (d.getElement() instanceof IMethod && !d.getElement().isBinary()) {
				doubleCheckHasOtherLinks(d, pg);
			}
		} // for each concern element
		
		// TODO doesn't work
		for (DoraEdge e : removeEdges)
			pg.removeEdge(e);
		
		for (DoraNode d : removeNodes)
			pg.removeNode(d);

	}
	
	private void makeFamilyCommunicators(DoraNode sd, ProgramGraph pg) {
		for (DoraEdge e : sd.getInEdges().values()) {
			if (e.getEdgeType() == EdgeType.IMPLEMENT && e.getFromNode().getType() != NodeType.SEED) {
				e.getFromNode().setType(NodeType.C);
				out.println("** " + e.getFromNode().getType() + " " + 
						ContextBuilder.getSignature(e.getFromNode().getElement()));
			}
		}
		for (DoraEdge e : sd.getOutEdges().values()) {
			if (e.getEdgeType() == EdgeType.IMPLEMENT && e.getToNode().getType() != NodeType.SEED) {
				e.getToNode().setType(NodeType.C);
				out.println("** " + e.getToNode().getType() + " " + 
						ContextBuilder.getSignature(e.getToNode().getElement()));
			}
		}
	}

	private void doubleCheckAccessors(DoraNode sd, ProgramGraph pg) {
		Set<IMember> c = dependencies.getProjectReferences(sd.getElement(), _proj);
		for (IMember m : c) {
			if (pg.containsNode(m)) { // in concern
				// add edge
				DoraNode d = pg.getNode(m);
				if (pg.addEdge(d, sd, EdgeType.PLAIN, 0d))
					out.println("++ " + ContextBuilder.getSignature(d.getElement()) + " -> " + ContextBuilder.getSignature(sd.getElement()));
			}
		}
	}

	private boolean doubleCheckHasOtherLinks(DoraNode sd, ProgramGraph pg) {
		boolean hasLink = false;
		Set<IMethod> c = dependencies.getCHCallersInProject((IMethod)sd.getElement(), pkg);
		for (IMethod m : c) {
			if (pg.containsNode(m)) { // in concern
				// add edge
				DoraNode d = pg.getNode(m);
				if (pg.addEdge(d, sd, EdgeType.PLAIN, 0d))
					out.println("++ " + ContextBuilder.getSignature(d.getElement()) + " -> " + ContextBuilder.getSignature(sd.getElement()));
				hasLink = true;
			}
		}
		
		c = dependencies.getCallees((IMethod)sd.getElement());
		for (IMethod m : c) {
			if (pg.containsNode(m)) { // in concern
				// add edge
				DoraNode d = pg.getNode(m);
				if (pg.addEdge(sd, d, EdgeType.PLAIN, 0d))
					out.println("++ " + ContextBuilder.getSignature(sd.getElement()) + " -> " + ContextBuilder.getSignature(d.getElement()));
				hasLink = true;
			}
		}
		
//		Set<IField> f = dependencies.getFieldUses(sd.getElement());
//		for (IField m : f) {
//			if (pg.containsNode(m)) { // in concern
//				// add edge
//				DoraNode d = pg.getNode(m);
//				pg.addEdge(sd, d, EdgeType.PLAIN, 0d);
//				hasLink = true;
//			}
//		}
		
		return hasLink;
	}

	private void exploreSet(ProgramGraph pg, HashMap<String, IMember> list, NodeType init) {
		workList = new HashMap<String, DoraNode>();
		
		// init work list with seed set
		for (IMember sd : list.values()) {
			DoraNode t = createOrGetNode(pg, sd, 0);
			// explore seeds in all directions
			t.setGoBackward(true);
			t.setGoForward(true);
			// Always explore seeds and add to nodes
			t.setType(init);
			addToWorkList(t);
			//out.println("\n--" + t.getSignature() + " " + t.getScore().getF());
		}
		
		// explore workList
		
		while (!workList.isEmpty()) {
			String handle = workList.keySet().iterator().next();
			DoraNode sd = workList.get(handle);
			
			// Make sure we haven't already analyzed it
			if (!analyzedList.contains(handle) && sd.getEdgesAway() < t_distance) {
				explore(sd, sd.getEdgesAway(), pg);
			}
			
			// Remove from work list
			workList.remove(handle);
			analyzedList.add(handle);
		}
	}

	private void addToWorkList(DoraNode t) {
		workList.put(t.toString(), t);
	}
	

	
	private void explore(DoraNode sd, int edgesAway, ProgramGraph pg) {
		if (debug) out.println("\n--" + sd.getSignature());
		
		if (sd.getElement() instanceof IMethod) {
			AbstractScoreData current = getScore(sd);
			
			// callers
			if (sd.shouldGoBackward() && !sd.wentBackward()) {
				findCallers(sd, edgesAway, pg, current);
				sd.setWentBackward(true);
			}

			// explore family
			if(!sd.hasAnalyzedFamily() && sd.getType() != NodeType.K && 
					!sd.getElement().isBinary() && !sd.getScore().isConstructor()) {
				findFamily(sd, edgesAway, pg, current);
				sd.setAnalyzedFamily(true);
			}

			if (sd.shouldGoForward() && !sd.wentForward()) {
				// callees
				findCallees(sd, edgesAway, pg, (CalculateSWUMSearchScoreData)current);
				
				// field uses
				findFields(sd, edgesAway, pg, current);
				
				sd.setWentForward(true);
			}

		} else if (sd.getElement() instanceof IField) {
			CalculateAllSearchScoreData current = (CalculateAllSearchScoreData)getScore(sd);
			// field accessors
			findFieldAccessors(sd, edgesAway, pg, current);
			sd.setWentBackward(true);
		}

	}
	
	private CalculateAllSearchScoreData getFieldScores(IField f) {
		CalculateAllSearchScoreData use = new CalculateAllSearchScoreData(dora, swum, bow, dep, sig, name);
		use.gatherScores(f, F_SWM, F_SIG, F_SRC);
		return use;
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

	private void findFamily(DoraNode sd, int edgesAway, ProgramGraph pg,
			AbstractScoreData current) {
		Set<IMember> c = dependencies.getMemberInterfaces(sd.getElement());
		if (c.size() > 0) {
			if (debug) out.print("\nFamily:\n");
			for (IMember m : c) {
				//out.println("\ti " + ContextBuilder.getSignature(m));
				if (m instanceof IMethod) {
					CalculateSWUMSearchScoreData callee = new CalculateSWUMSearchScoreData(dora, swum, name, bow);
					callee.gatherScores((IMethod)m);
					double sig = logit(callee.getDoraScore() - intercept);

					//if (s > 0) { // add node going forward
					if (!ContextBuilder.getSignature(m).equals(sd.getSignature()) &&
							sig > t_very_greedy) {
						DoraNode d;

						if (m.isBinary()) {
							d = createOrGetNode(pg, m, edgesAway + 1, sd.getScore(), NodeType.LS);
						} else {
							if (sd.getType() == NodeType.SEED)
								d = createOrGetNode(pg, m, edgesAway + 1, sd.getScore(), NodeType.M);
							else
								d = createOrGetNode(pg, m, edgesAway + 1, sd.getScore(), sd.getType());
							d.setGoBackward(sd.shouldGoBackward());
							d.setGoForward(sd.shouldGoForward());
						}



						//add edge
						//if (current.getScore() > 0) {
						pg.addDoubleEdge(sd, d, EdgeType.IMPLEMENT);
						addToWorkList(d);
						//}

						out.println("\t" + sd.getType().toString() + " " + 
								ContextBuilder.getSignature(m) + " " + pf.sprintf(sd.getScore().getScore()));
					}
				}
			}
		}
	}
	
	private void findCallers(DoraNode sd, int edgesAway, ProgramGraph pg,
			AbstractScoreData callee) {
		Set<IMethod> c = dependencies.getCHCallersInProject((IMethod)sd.getElement(), pkg);
		if (c.size() > 0) {
			if (debug) out.print("\nCallers:\n");
			for (IMethod m : c) {
				CalculateSWUMSearchScoreData caller = new CalculateSWUMSearchScoreData(dora, swum, name, bow);
				caller.gatherScores(m);
				double sig_callee = logit(callee.getDoraScore() - intercept);// / max_sig;
				double sig_caller = logit(caller.getDoraScore() - intercept);// / max_sig;
				
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
				if (last == 1 || m.isBinary()) // don't dampen libraries for backward API weaving
					l = 1d;
				
				double damp = Math.max(l, fanin);
				
				double qat = 0d;
					qat = getATOverlapOnQueryTerm(caller.getConstructedSWUM(), 
						callee.getConstructedSWUM());
				if (and && !sd.getElement().isBinary() && !caller.isReactive())
					qat = 1d;
				
				// m 
				double wrt_query = sig_caller;// + 0.1 * sig;
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
				if (callee.getSScore() > 0 && caller.getSScore() > 0) {
					lavg = 0.5 * (callee.getSScore() + callee.getSScore());
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
					// want to be really sure because could always be a boundary
					if (wrt_qo > t_conserv || wrt_qat > t_conserv || wrt_qat2 > t_conserv) {
						type = NodeType.K;
						s = wrt_qat;
						//if (sig_callee > 0)
						//if (callee_fanin == 1) // Lost 8 FPs & 1 TP
						// ISSUE: is this arbitrary?
						// Idea: if K has few callers, keep going, o/w general
						if (callee_fanin < t_lowfanin) // lose 3 FP
							backward = true;
					} else if (isP) {
						type = NodeType.P;
					}
					s = wrt_qat2;
				} else { // don't go backward from "s", so can only be "M"
					if (sig_caller > t_very_greedy) { // 0.7? 0.51? // t_conserv?
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
					} else if (wrt_qat > t_very_greedy || wrt_qo > t_very_greedy) { // new 0.4?
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
						if (sig_callee > 0) // unnecessary, but left in as sanity check
							backward = true;
					}
				}
				
				
				if (type != NodeType.NO_TYPE) { // add node going forward
					//type = NodeType.M;
					DoraNode d = createOrGetNode(pg, m, edgesAway + 1, caller, type);
					d.setGoForward(forward || d.shouldGoForward());
					d.setGoBackward(backward || d.shouldGoBackward());
					
					//add edge
					//pg.addEdge(sd, d, EdgeType.PLAIN);
					pg.addEdge(d, sd, EdgeType.PLAIN, s);
					
					if (type != NodeType.P)
						addToWorkList(d);
				} else if (pg.containsNode(m)) { // in concern
					// add edge
					DoraNode d = pg.getNode(m);
					pg.addEdge(d, sd, EdgeType.PLAIN, s);
				}
				
				out.println("\t" + type.toString() + " " + 
						ContextBuilder.getSignature(m) +
						" " + pf.sprintf(sig_caller) +
						" " + pf.sprintf(wrt_qo));
			}
		}
	}
	
	private void findFields(DoraNode sd, int edgesAway, ProgramGraph pg,
			AbstractScoreData current) {
		Set<IField> c = dependencies.getFieldUses(sd.getElement());
		if (c.size() > 0) {
			if (debug) out.print("\nFields:\n");
			for (IField m : c) {
				boolean t = false;
				CalculateAllSearchScoreData use = getFieldScores(m);
				double sig_caller = logit(current.getDoraScore() - intercept);// / max_sig;
				double combo = logit(use.getDoraScore() - intercept);// / max_fcombo;
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
				
				double wrt_query = combo + 0.1 * sig_caller; // TODO should this be just sig here?
				double wrt_qo = 0d;
				if (maxq > 0)
					wrt_qo = (maxq + combo + sig_caller) / 3d;
				
				NodeType type = NodeType.NO_TYPE;
				double s = 0d;
				//boolean forward = false;
				boolean backward = false;
				
				if (wrt_query > t_greedy) { // 0.5? // 0.8?
					type = NodeType.RELEVANT_FIELD;
					if (m.isBinary())
						type = NodeType.LIBRARY_FIELD;
					s = wrt_query;
					if (wrt_query > t_conserv)
						backward = true;
				} else if (wrt_qo > t_greedy) {
					type = NodeType.RELEVANT_FIELD; // NodeType.FIELD ?
					if (m.isBinary())
						type = NodeType.LIBRARY_FIELD;
					s = wrt_qo;
					backward = true; // get communicators this way?
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
						ContextBuilder.getSignature(m) +  
						" " + pf.sprintf(combo)
						 + " " + pf.sprintf(wrt_qo));
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
	}

	private void findFieldAccessors(DoraNode sd, int edgesAway, ProgramGraph pg,
			CalculateAllSearchScoreData field) {
		Set<IMember> c = dependencies.getProjectReferences(sd.getElement(), _proj);
		if (c.size() > 0) {
			if (debug) out.print("\nField Accessors:\n");
			for (IMember m : c) {
				if (m instanceof IMethod) {
					CalculateSWUMSearchScoreData caller = new CalculateSWUMSearchScoreData(dora, swum, name, bow);
					caller.gatherScores(m);

					boolean written = getFieldWritten((IField)sd.getElement(), m);
					boolean read = true;

					if (written) // only need to check for read if written
						read = getFieldRead((IField)sd.getElement(), m);

					double sig = logit(caller.getDoraScore() - intercept);
					double fscore = logit(field.getDoraScore() - intercept);

					double maxq = 0d;

					if (field.getFieldNameStemmedWords() != null) {
						HashSet<String> sName = caller.getConstructedSWUM().getNameStemmedWords();					
						HashSet<String> tName = caller.getConstructedSWUM().getThemeNoAbbrevStemmedWords();
						tName.addAll(sName);

						maxq = Math.max( checkOverlapOnQueryTerm(sName, 
								field.getFieldNameStemmedWords()), 
								checkOverlapOnQueryTerm(tName, 
										field.getFieldNameStemmedWords()) );
					}

					double wrt_caller = 0d;
					if (maxq > 0)
						wrt_caller = (maxq + fscore + sig) / 3d;

					NodeType type = NodeType.NO_TYPE;
					double s = 0d;
					boolean forward = false;
					boolean backward = false;

					if (wrt_caller > t_greedy) {
						if (wrt_caller > t_conserv) { // kinda meaningless for exploration
							type = NodeType.M;
							if (m.isBinary())
								type = NodeType.L;
							//backward = true;
						} else {
							type = NodeType.S;
							if (m.isBinary())
								type = NodeType.LS;
						}
						s = wrt_caller;
						//forward = true; //?
						//if (written)
						backward = true; // TODO if field accessor not callee of m, e?
					}



					//s = written ? 1 : 0;
					//				s = wrt_caller;
					//				type = NodeType.NO_TYPE;
					//				if (written) {
					//					type = NodeType.C;
					//					
					//				}


					boolean isInit = caller.isConstructor();
					if (!isInit) {
						HashSet<String> verbs = caller.getConstructedSWUM().getActionWords();
						isInit |= verbs.contains("init");
						isInit |= verbs.contains("create");
						isInit |= verbs.contains("creates");
						isInit |= verbs.contains("setup");
						isInit |= verbs.contains("initialize");
						isInit |= verbs.contains("initializes");
						isInit |= verbs.contains("construct");
						isInit |= verbs.contains("constructs");
						isInit |= verbs.contains("assign");
						isInit |= verbs.contains("assigns");
						isInit |= verbs.contains("instantiate");
						isInit |= verbs.contains("instantiates");	
					}

					// only for m & seed fields
					if (isInit && written && type == NodeType.NO_TYPE) {
						type = NodeType.I;
						forward = true; // to look up supporting?
					}

					DoraNode d = null;
					//if (s > 0) { 
					if (type != NodeType.NO_TYPE) { // add node going forward
						//type = NodeType.M;
						d = createOrGetNode(pg, m, edgesAway + 1, caller, type);
						d.setGoForward(forward || d.shouldGoForward());
						d.setGoBackward(backward || d.shouldGoBackward());

						//add edge
						//pg.addEdge(sd, d, EdgeType.PLAIN);
						pg.addEdge(d, sd, EdgeType.PLAIN, s, written, read);
						addToWorkList(d);
					} else if (pg.containsNode(m)) { // in concern
						// add edge
						d = pg.getNode(m);
						pg.addEdge(d, sd, EdgeType.PLAIN, s);
					}
					//				} else {
					//					//if (handleVisited(m, caller)) {
					//					if (handleVisited(m, sd)) {
					//						d = createOrGetNode(pg, m, edgesAway + 1, caller, NodeType.C);
					//						pg.addEdge(d, sd, EdgeType.CONNECT, s, written);
					//						pg.addEdge(visited.get(DoraNode.getHandle(m)), sd, EdgeType.CONNECT, 0);
					//					}
					//				}

					out.println("\t" + type.toString() + " " + 
							ContextBuilder.getSignature(m) + 
							" " + pf.sprintf(sig)
							 + " " + pf.sprintf(wrt_caller));

					if (d != null && d.getType() == NodeType.I) {
						// Need to find initializing methods
						findFieldInitializer(sd, d, pg);
					}
				}
			}
		}
	}

	private void findFieldInitializer(DoraNode field, DoraNode caller,
			ProgramGraph pg) {
		FieldInitializerVisitor fv = new FieldInitializerVisitor((IField)field.getElement(), (IMethod)caller.getElement());
		
		try {
			// Create AST
			ASTParser lParser = ASTParser.newParser(AST.JLS3); 
			lParser.setKind(ASTParser.K_COMPILATION_UNIT);
			lParser.setSource(caller.getElement().getCompilationUnit()); // set source
			lParser.setResolveBindings(true); // set bindings

			CompilationUnit cu = (CompilationUnit) lParser.createAST(null);
			cu.accept(fv);
		} catch (Exception e) { }
		
		for (IMember m : fv.getInitializers()) {
			DoraNode d = createOrGetNode(pg, m, caller.getEdgesAway() + 1, null, NodeType.I);
			d.setGoForward(true);
			d.setGoBackward(d.shouldGoBackward());

			//add edge
			pg.addEdge(caller, d, EdgeType.PLAIN, 0);
			pg.addEdge(d, field, EdgeType.INITIALIZER, 0);
			addToWorkList(d);
		}		
	}

	private void findCallees(DoraNode sd, int edgesAway, ProgramGraph pg,
			CalculateSWUMSearchScoreData current) {
		HashMap<IMethod, int[]> c = getLastCallees((IMethod)sd.getElement());
		//Set<IMethod> c = dependencies.getCallees((IMethod)sd.getElement());
		if (c.size() > 0) {
			if (debug) out.print("\nCallees:\n");
			for (IMethod m : c.keySet()) {
				int caller_fanout = c.size();
				int callee_fanin = 0;
				if (!m.isBinary()) callee_fanin = dependencies.getCHCallersInProject(m).size();
				
				//Set<Node> actuals = null;
				
				
				CalculateSWUMSearchScoreData callee = new CalculateSWUMSearchScoreData(dora, swum, name, bow);
				callee.gatherScores(m);
				
				double sig_score = callee.getSwums();
				double sig_callee = logit(callee.getDoraScore() - intercept); // max_sig /*+ 0.1 * callee.getBowScore() / max_body*/;
				double sig_caller = logit(current.getDoraScore() - intercept); // max_sig;
				double act_callee = sig_callee;
				
//				if (!sd.getElement().isBinary()) {
//					actuals = getActualTheme((IMethod)sd.getElement(), m);
//
//					callee.gatherScores(m, actuals);
//					act_callee = callee.getSwums() / max_sig /*+ 0.1 * callee.getBowScore() / max_body*/;
//				}

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
				
				double l = t_damp;
				if (c.get(m)[0] == 1)
					l = 1d;
				double damp = Math.max(l, fanin);
				// if fanin, fanout or last = 1 o/w t_damp
				
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
				} else if (act_callee > t_very_greedy && sig_score > 0) { // 0.6? 0.5? && name > 0?
					type = NodeType.S;
					if (m.isBinary())
						type = NodeType.LS;
					forward = true;
					s = act_callee;
					if (sd.getType() == NodeType.S && act_callee < t_greedy)
						forward = false;
					//backward = true;
				} else if (wrt_qo > t_very_greedy) { // 0.6?
					type = NodeType.S;
					forward = true; // if parent M or S with score > t_medium
					if (sd.getType() == NodeType.S && wrt_qo < t_greedy)
						forward = false;
					//backward = true;
					if (m.isBinary())
						type = NodeType.LS;
					s = wrt_qo;
				} else if (wrt_qat > t_very_greedy) {
					type = NodeType.S;
					if (m.isBinary())
						type = NodeType.LS;
					forward = true; // if parent M or S with score > t_medium
					if (sd.getType() == NodeType.S && wrt_qat < t_greedy)
						forward = false;
					//backward = true;
					s = wrt_qat;
				} else if (sd.getType() == NodeType.S && wrt_qats > t_very_greedy) {
					type = NodeType.S;
					if (m.isBinary())
						type = NodeType.LS;
					forward = true; // if parent M or S with score > t_medium
					if (sd.getType() == NodeType.S && wrt_qats < t_greedy)
						forward = false;
					s = wrt_qats;
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
				
				if (type != NodeType.NO_TYPE) { // add node going forward
	
					DoraNode d = createOrGetNode(pg, m, edgesAway + 1, callee, type);

					// if it's already set, don't undo
					d.setGoForward(forward || d.shouldGoForward());
					d.setGoBackward(backward || d.shouldGoBackward());
					
					//add edge
					pg.addEdge(sd, d, EdgeType.PLAIN, s);
					addToWorkList(d);
				} else if (pg.containsNode(m)) { // in concern
					// add edge
					DoraNode d = pg.getNode(m);
					pg.addEdge(sd, d, EdgeType.PLAIN, s);
				}
				
				out.println("\t" + type.toString() + " " + 
						ContextBuilder.getSignature(m) 
						+ " " + pf.sprintf(act_callee)
						+ " " + pf.sprintf(wrt_qo));
			}
		}
	}

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
	
	
	private DoraNode createOrGetNode(ProgramGraph pg, IMember sd, int e) {
		String handle = DoraNode.getHandle(sd);
		DoraNode t;
		if (pg.containsNode(handle)) {
			t =  pg.getNode(handle);
			if (t.getEdgesAway() > e)
				t.setEdgesAway(e);
		} else {
			t = new DoraNode(sd, e);//, gatherScores(sd));
			pg.addNode(t);
		}
			
		return t;
	}
	
	private DoraNode createOrGetNode(ProgramGraph pg, IMember sd, int e, 
			AbstractScoreData score, NodeType nt) {
		String handle = DoraNode.getHandle(sd);
		DoraNode t;
		if (pg.containsNode(handle)) {
			t = pg.getNode(handle);
			if (score != null && 
					(t.getScore() == null || t.getScore().getScore() < score.getScore()))
				t.setScore(score);
			if (e < t.getEdgesAway())
				t.setEdgesAway(e);
			t.setType(nt);
		} else {
			t = new DoraNode(sd, e, score, nt);
			pg.addNode(t);
		}
			
		return t;
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
	

	private double logit(double d) {
		//s = exp(d) / (1 + exp(d));
		//return Math.exp(d) / (1 + Math.exp(d));
		return d;
	}
	
	public String toString() {
		return super.swum.getContains().keySet().toString();
	}
}
