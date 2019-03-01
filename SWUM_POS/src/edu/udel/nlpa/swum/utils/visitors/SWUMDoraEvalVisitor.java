package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.*;

import edu.udel.nlpa.swum.dora.DoraNode;
import edu.udel.nlpa.swum.dora.EdgeType;
import edu.udel.nlpa.swum.dora.NodeType;
import edu.udel.nlpa.swum.dora.ProgramGraph;
import edu.udel.nlpa.swum.explore.DependencyRelationship;
import edu.udel.nlpa.swum.nodes.searchable.SearchableMethodDecl;
import edu.udel.nlpa.swum.scores.AbstractElementScore;
import edu.udel.nlpa.swum.scores.AbstractScoreData;
import edu.udel.nlpa.swum.scores.AbstractScoreData;
import edu.udel.nlpa.swum.scores.CalculateSWUMSearchScoreData;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public class SWUMDoraEvalVisitor extends SWUMSearchVisitor {

	private HashSet<String> seeds = null;
	private HashMap<String, IMember> seedList = new HashMap<String, IMember>();
	//private HashMap<String, IMember> libSeedList = new HashMap<String, IMember>();
	private HashSet<String> analyzedList = new HashSet<String>();
	protected static final DependencyRelationship dependencies = new DependencyRelationship();
	private String fname;
	
	private double max_sig    = 1d;
	private double max_body   = 1d;
	private double max_combo  = 1d;
	private double max_fcombo = 1d;
	private double max_fsig   = 1d;
	
	private boolean debug = true;
	
	public SWUMDoraEvalVisitor(String q, IJavaProject jp, PrintStream p) {
		super(q, jp, p);
		fname = q.replaceAll("\\s+", "_");
		fname = "experiment/concerns/" + fname;
	}
	
	public SWUMDoraEvalVisitor(String q, IJavaProject jp, PrintStream p, HashSet<String> s, double[] maxes) {
		this(q, jp, p);
		seeds = s;
//		max_sig = maxes[0];
//		max_body = maxes[1];
//		max_combo = maxes[2];
//		max_fcombo = maxes[3];
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
		
		//System.out.println ( getPathLocation(node) + " " + sig2);
		
		if (seeds == null || seeds.contains(sig2)) {
			seedList.put(DoraNode.getHandle(node), node);
		}
		
		return true;
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
		
		//out.println("\n--" + sig2 + " " + score.getF());
//		out.println("\t" + pf.sprintf(s) + " " + 
//				ContextBuilder.getSignature(m) + 
//				" [[ l:" + c.get(m)[0] + " ]] " + callee.getF());
		
		pg.display(fname);
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
			pg.addNode(t);
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
	
//	private void addToLibList(IMethod m) {
//		libSeedList.put(DoraNode.getHandle(m), m);
//	}
	
	private int    t_distance = 1;
	
	private void explore(DoraNode sd, int edgesAway, ProgramGraph pg) {
		if (debug) out.println("\n--" + getSig(sd.getElement()));
		if (sd.getElement() instanceof IMethod) {
			AbstractScoreData current;

			if (sd.getScore() == null) {
				current = new CalculateSWUMSearchScoreData(dora, swum, name);
				((CalculateSWUMSearchScoreData)current).
				gatherScores((IMethod)sd.getElement());
				sd.setScore(current);
			} else {
				current = sd.getScore();
			}

			findCallers(sd, edgesAway, pg);

			//findFamily(sd, edgesAway, pg);

			//findCallees(sd, edgesAway, pg);

			// field uses
			//findFields(sd, edgesAway, pg);
		}

	}

	private void findFamily(DoraNode sd, int edgesAway, ProgramGraph pg) {
		Set<IMember> c = dependencies.getMemberInterfaces(sd.getElement());
		AbstractScoreData current = sd.getScore();
		if (c.size() > 0) {
			if (debug) out.print("\nFamily:\n");
			for (IMember m : c) {
				out.println("\tq " + getSig(m));
				
				//if (s > 0) { // add node going forward
					DoraNode d;

					if (m.isBinary())
						d = createOrGetNode(pg, m, edgesAway + 1, null, NodeType.L);
					else {
						d = createOrGetNode(pg, m, edgesAway + 1, null, NodeType.NO_TYPE);
					}
					
					sd.setAnalyzedFamily(true);
					
					//add all family
					//if (current.getScore() > 0) {
						pg.addDoubleEdge(sd, d, EdgeType.IMPLEMENT);
						addToWorkList(d);
					//}
			}
		}
	}
	
	private void findCallers(DoraNode sd, int edgesAway, ProgramGraph pg) {
		Set<IMethod> c = dependencies.getCHCallersInProject((IMethod)sd.getElement());
		AbstractScoreData current = sd.getScore();
		if (c.size() > 0) {
			if (debug) out.print("\nCallers:\n");
			for (IMethod m : c) {
				
				
				CalculateSWUMSearchScoreData caller = new CalculateSWUMSearchScoreData(dora, swum, name);
				caller.gatherScores(m);
				double qname = getNameOverlapOnQueryTerm(caller.getConstructedSWUM(), 
						current.getConstructedSWUM());
				
				DoraNode d;

				if (m.isBinary())
					d = createOrGetNode(pg, m, edgesAway + 1, null, NodeType.L);
				else {
					d = createOrGetNode(pg, m, edgesAway + 1, null, NodeType.NO_TYPE);
				}

				//add edge
				pg.addEdge(d, sd, EdgeType.PLAIN);

				if (caller.getScore() > 0 || qname > 0) { // or overlap?
					addToWorkList(d);
					out.println("\tq " + getSig(m));
				} else {
					out.println("\tn " + getSig(m));
				}
			}
		}
	}
	
	private String getSig(IMember m) {
		return ContextBuilder.getSignature(m, true);
	}
	
	private void findFields(DoraNode sd, int edgesAway, ProgramGraph pg) {
		Set<IField> c = dependencies.getFieldUses(sd.getElement());
		AbstractScoreData current = sd.getScore();
		if (c.size() > 0) {
			if (debug) out.print("\nFields:\n");
			for (IField m : c) {
				
				
				double qname = 0d;
				
				CalculateSWUMSearchScoreData use = new CalculateSWUMSearchScoreData(dora, swum, name);
				use.gatherScores(m);
				if (use.getFieldNameStemmedWords() != null) {
					HashSet<String> sName = current.getConstructedSWUM().getNameStemmedWords();
					qname = checkOverlapOnQueryTerm(sName, use.getFieldNameStemmedWords());
				}
				
				//add edge
				if (use.getScore() > 0 || qname > 0) { // or overlap?
					out.println("\tq " + getSig(m));
					
					DoraNode d;

					if (m.isBinary())
						d = createOrGetNode(pg, m, edgesAway + 1, null, NodeType.LIBRARY_FIELD);
					else {
						d = createOrGetNode(pg, m, edgesAway + 1, null, NodeType.FIELD);
					}

					sd.setAnalyzedFamily(true);
					pg.addEdge(sd, d, EdgeType.PLAIN);
					addToWorkList(d);
				} else {
					out.println("\tn " + getSig(m));
				}
			}
		}
	}

	private void findCallees(DoraNode sd, int edgesAway, ProgramGraph pg) {
		//HashMap<IMethod, int[]> c = getLastCallees((IMethod)sd.getElement());
		Set<IMethod> c = dependencies.getCallees((IMethod)sd.getElement());
		AbstractScoreData current = sd.getScore();
		if (c.size() > 0) {
			if (debug) out.print("\nCallees:\n");
			for (IMethod m : c) {
				CalculateSWUMSearchScoreData callee = new CalculateSWUMSearchScoreData(dora, swum, name);
				callee.gatherScores(m);
				double qname = getNameOverlapOnQueryTerm(callee.getConstructedSWUM(), 
						current.getConstructedSWUM());
				
				

				if (callee.getScore() > 0 || qname > 0) { // or overlap? -- too many?
					DoraNode d;

					out.println("\tq " + getSig(m));

					if (m.isBinary())
						d = createOrGetNode(pg, m, edgesAway + 1, null, NodeType.L);
					else {
						d = createOrGetNode(pg, m, edgesAway + 1, null, NodeType.NO_TYPE);
					}
					
					pg.addEdge(sd, d, EdgeType.PLAIN);
					addToWorkList(d);
				} else {
					out.println("\tn " + getSig(m));
				}
			}
		}
	}
	
	
	private DoraNode createOrGetNode(ProgramGraph pg, IMember sd, int e) {
		String handle = DoraNode.getHandle(sd);
		DoraNode t;
		if (pg.containsNode(handle)) {
			t =  pg.getNode(handle);
			if (t.getEdgesAway() > e)
				t.setEdgesAway(e);
		} else
			t = new DoraNode(sd, e);//, gatherScores(sd));
			
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
	
	private DoraNode createOrGetNode(ProgramGraph pg, IMember sd, int e, 
			AbstractScoreData score, NodeType nt) {
		String handle = DoraNode.getHandle(sd);
		DoraNode t;
		if (pg.containsNode(handle)) {
			t = pg.getNode(handle);
			// assume score equiv
			if (e < t.getEdgesAway())
				t.setEdgesAway(e);
			t.setType(nt);
		} else {
			t = new DoraNode(sd, e, score, nt);
			pg.addNode(t);
		}
			
		return t;
	}
	
	public String toString() {
		return super.swum.getContains().keySet().toString();
	}
}
