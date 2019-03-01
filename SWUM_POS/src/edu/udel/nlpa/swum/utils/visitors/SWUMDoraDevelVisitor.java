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
import edu.udel.nlpa.swum.scores.CalculateAllSearchScoreData;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public class SWUMDoraDevelVisitor extends SWUMSearchVisitor {

	private HashSet<String> seeds = null;
	private HashMap<String, IMember> seedList = new HashMap<String, IMember>();
	private HashMap<String, IMember> libSeedList = new HashMap<String, IMember>();
	private HashSet<String> analyzedList = new HashSet<String>();
	protected static final DependencyRelationship dependencies = new DependencyRelationship();
	private String fname;
	
	private double max_sig    = 1d;
	private double max_body   = 1d;
	private double max_combo  = 1d;
	private double max_fcombo = 1d;
	private double max_fsig   = 1d;
	
	public SWUMDoraDevelVisitor(String q, IJavaProject jp, PrintStream p) {
		super(q, jp, p);
		fname = q.replaceAll("\\s+", "_");
	}
	
	public SWUMDoraDevelVisitor(String q, IJavaProject jp, PrintStream p, HashSet<String> s, double[] maxes) {
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
			//System.out.println("\t" + sig2);
		}
		
		return true;
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleInheritance(DoraNode node) {
		//out.println("\n--" + node.getSignature() + " " + node.getScore().getF());
		return handleInheritance((IMethod)node.getElement(), (CalculateAllSearchScoreData)node.getScore());
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleInheritance(IMethod node, String sig2) {
		CalculateAllSearchScoreData score = gatherScores(node);
		//out.println("\n--" + sig2 + " " + score.getF());
		return handleInheritance(node, score);
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleInheritance(IMethod node, CalculateAllSearchScoreData start) {

		Set<IMember> c = dependencies.getMemberInterfaces(node);
		
		HashMap<IMethod, CalculateAllSearchScoreData> nonz = 
			new HashMap<IMethod, CalculateAllSearchScoreData>();
		
		if (c.size() > 0) {
			out.print("\nFamily:\n");
			for (IMember i : c) {
				if (i instanceof IMethod && !i.isBinary()) {
					IMethod m = (IMethod) i;
					CalculateAllSearchScoreData caller = gatherScores(m);
					double s = analyzeFamily(start, caller);
					out.println("\t" + pf.sprintf(s) + " " + 
							ContextBuilder.getSignature(m) + " " + caller.getF());
					caller.setDoraScore(s);
					nonz.put(m, caller);
				}
			}
		}
		
		return nonz;
	}
	
	private double analyzeFamily(CalculateAllSearchScoreData start, 
			CalculateAllSearchScoreData fam) {
		double s = 0d;
		double sig = fam.getSwums() / max_sig;
		double combo = fam.getScore() / max_combo;
		
		double SCALE = 1d; // to compete with other scores?
		
		double csm = SIG_SWUM * start.getSwums() / max_sig;
		if (start.getDeps() > 0)
			csm += BDY_SWUM * start.getDeps() / max_body;
		
		s = combo * SCALE;
		
		double qname = getNameOverlapOnQueryTerm(start.getConstructedSWUM(), 
				fam.getConstructedSWUM());

		s = qname;
		if (fam.getNameScore() > 0)
			s = 1d; // action and theme must overlap
		else
			s = 0d;
		
		s = 1d;
		
		String f = "[[ q:" + pf.sprintf(qname) + 
		//", sm:" + pf.sprintf(s) + 
		", sr:" + pf.sprintf(combo) + 
		", sw:" + pf.sprintf(sig) +  
		", cs:" + pf.sprintf(csm) +
		" ]]";
		
		fam.setF(f);
		
		
		
		return s;
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleCallers(DoraNode node) {
		//out.println("\n--" + node.getSignature() + " " + node.getScore().getF());
		return handleCallers((IMethod)node.getElement(), (CalculateAllSearchScoreData)node.getScore());
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleCallers(IMethod node, String sig2) {
		CalculateAllSearchScoreData score = gatherScores(node);
		//out.println("\n--" + sig2 + " " + score.getF());
		return handleCallers(node, score);
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleCallers(IMethod node, CalculateAllSearchScoreData score) {
		//CalculateSearchScoreData callee = gatherScores(node);
		HashMap<IMethod, CalculateAllSearchScoreData> nonz = 
			new HashMap<IMethod, CalculateAllSearchScoreData>();

		//int fo = dependencies.getCallees(node).size();

		// Callers
		Set<IMethod> c = dependencies.getCHCallersInProject(node);
		if (c.size() > 0) {
			out.print("\nCallers: " + c.size() + "\n");
			for (IMethod m : c) {
				CalculateAllSearchScoreData caller = gatherScores(m);
				//int fo = dependencies.getCallees(m).size();
				//int fi = dependencies.getCHCallersInProject(m).size();
//				int l = getApproxStmts(m);
//				double s = analyzeCaller(caller, score, c.size(), 1, 1, l,
//						m.isBinary());
				
				// analyzeCallR(CalculateSearchScoreData caller, 
				//CalculateSearchScoreData callee,
				//int last, int callee_fanin, int caller_fanout, 
				//boolean callee_binary) {

				int last = 0;
				HashMap<IMethod, int[]> cr = getLastCallees(m);
				if (cr.containsKey(node))
					last = cr.get(node)[0];
				double s = analyzeCallR(caller, score, last, c.size(),
						cr.size(), m.isBinary());
				
				

				out.println("\t" + pf.sprintf(s) + " " + 
						ContextBuilder.getSignature(m) + " " + 
						caller.getF()
						);
				
				caller.setDoraScore(s);
				nonz.put(m, caller);
			}
		}
		
		return nonz;
	}

	private HashMap<IMethod, CalculateAllSearchScoreData> handleCallees(DoraNode node) {
		//out.println("\n--" + node.getSignature() + " " + node.getScore().getF());
		return handleCallees((IMethod)node.getElement(), (CalculateAllSearchScoreData)node.getScore());
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleCallees(IMethod node, String sig2) {
		CalculateAllSearchScoreData score = gatherScores(node);
		out.println("\n--" + sig2 + " " + score.getF());
		return handleCallees(node, score);
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleCallees(IMethod node, CalculateAllSearchScoreData score) {
		// Callees
		HashMap<IMethod, int[]> c = getLastCallees(node);
		
		HashMap<IMethod, CalculateAllSearchScoreData> nonz = 
			new HashMap<IMethod, CalculateAllSearchScoreData>();
		
		if (c.size() > 0) {
			out.print("\nCallees:\n");
			for (IMethod m : c.keySet()) {
				int calls = 0;
				int callers = 0;
				if (!m.isBinary()) callers = dependencies.getCHCallersInProject(m).size();
				//if (!m.isBinary()) calls = dependencies.getCallees(m).size();
				CalculateAllSearchScoreData callee = gatherScores(m);
				double s = analyzeCall(score, callee, c.get(m)[0], callers, 
						c.size(), m.isBinary());
				
				//if (m.isBinary())
				out.println("\t" + pf.sprintf(s) + " " + 
				ContextBuilder.getSignature(m) + " " + 
				callee.getF());

				callee.setDoraScore(s);
				nonz.put(m, callee);
			}
		}
		
		return nonz;
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
	
	double SIG_SWUM = 1d;
	double BDY_SWUM = 0.5;
	double LAST = 0.15;
	double AND_OLAP = 0.4;
	double SIG_OLAP = 0.1;
	double BINARY = 0.2;
	double FANIN = 0.15;

	// m callee, sswum caller
	private double analyzeCall(CalculateAllSearchScoreData caller, 
			CalculateAllSearchScoreData callee,
			int last, int callee_fanin, int caller_fanout, boolean callee_binary) {
		double sig = callee.getSwums() / max_sig;
		double sig_caller = caller.getSwums() / max_sig;
		double body = callee.getDeps() / max_body;
		double da = callee.getDepAvg() / max_body;
		double combo = callee.getScore() / max_combo;
		double combo_caller = caller.getScore() / max_combo;
		double s = 0d;
		
		boolean and = getThemeAndActionOverlap(caller.getConstructedSWUM(), 
				callee.getConstructedSWUM());
//						&& sigs > 0;
//		boolean name = getNameOverlap(caller.getConstructedSWUM(), 
//				callee.getConstructedSWUM());
//		boolean or = getThemeOrActionOverlap(caller.getConstructedSWUM(), 
//				callee.getConstructedSWUM());
//		boolean sigo = getSigOverlap(caller.getConstructedSWUM(), 
//				callee.getConstructedSWUM());
		
		double fanin = Math.max(calculateFanIn10(callee_fanin),
				calculateFanIn10(caller_fanout));
						
		double qname = getNameOverlapOnQueryTerm(caller.getConstructedSWUM(), 
				callee.getConstructedSWUM());
		double qo = 0d;
		if (callee.getNameScore() > 0) {
			qo = qname;
//			if (and && !callee_binary && !callee.isReactive())
//				qo = 1d;
		} else
			qo = 0d;
		

		boolean isO = AbstractElementScore.getPos().isSideEffectWord(
				callee.getConstructedSWUM().getParse().beginsWith());

		
		double l = calculateFanIn10(2);
		if (last == 1)
			l = 1d;
		double damp = Math.max(l, fanin);
		
		double qat = 0d;
		if (sig > 0) {
			qat = getATOverlapOnQueryTerm(caller.getConstructedSWUM(), 
				callee.getConstructedSWUM());
			if (and && !callee_binary && !callee.isReactive())
				qat = 1d;
		}
		
		double wrt_query = sig;
		double wrt_qo = 0d;
		if (qo > 0)
			wrt_qo = damp * (qo + sig_caller) * 0.5;
			// s = (sig + qo * damp) / 2d;
		
		//s = wrt_query;
		double wrt_qat = 0d;
		if (qat > 0)
			wrt_qat = damp * (qat + sig_caller) * 0.5;

		// o
		if (isO && sig > 0)
			s = 0.25;
		else if (sig > 0.8) // wrt_query > 
			s = 1d; // 1d major
		else if (qo == 1 || qat == 1) // perfect overlap
			s = 0.75; // if qo == 1, node type = type of parent, regardless of match
		else if (wrt_qo > 0.55 || wrt_qat > 0.7) 
			s = 0.5d; // s
		else
			s = 0d;
		
		//s = wrt_qat;
		
		String f = "[[ q:" + pf.sprintf(qo) + 
		", qat:" + pf.sprintf(qat) + 
		", s:" + pf.sprintf(wrt_query) + 
		", c:" + pf.sprintf(wrt_qo) + 
		", a:" + pf.sprintf(wrt_qat) +
		", l:" + last +
//		", sr:" + pf.sprintf(combo) +
//		", srr:" + pf.sprintf(combo_caller) +
		", sw:" + pf.sprintf(sig) +  
		", swr:" + pf.sprintf(sig_caller) +
		//", sg:" + pf.sprintf(sigs) +
		//", cs:" + pf.sprintf(csm) +
		", n:" + pf.sprintf(callee.getNameScore()) +
		//", d:" + pf.sprintf(body) + 
//		", da:" + pf.sprintf(da) + 
//		", dn:" + pf.sprintf(callee.getDepAvg()) +
		//", b:" + pf.sprintf(bows) + 
		//", r: " + reactive +
		", fi:" + pi.sprintf(callee_fanin) +
		", fo:" + pi.sprintf(caller_fanout) +
		", o:" + isO +
		", a:" + and +  
		", b: " + callee_binary +
		" ]]";
		
		callee.setF(f);
		
		return s;
	}
	
	private double analyzeCallR(CalculateAllSearchScoreData caller, 
			CalculateAllSearchScoreData callee,
			int last, int callee_fanin, int caller_fanout, boolean callee_binary) {
		double sig_caller = caller.getSwums() / max_sig;
		double sig = callee.getSwums() / max_sig;
		double da = caller.getDepAvg() / max_body;
		double combo = caller.getScore() / max_combo;
		double s = 0d;
		
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
		if (and && !callee_binary && !caller.isReactive())
			qat = 1d;
		
		// m 
		double wrt_query = sig_caller;// + 0.1 * sig;
		double wrt_qo = 0d;
		if (qo > 0)
			wrt_qo = damp * (qo + sig_caller) * 0.5;
			//wrt_caller = (qo * damp + sig_caller) / 2d;
		
		double wrt_qat = 0d;
		if (qat > 0)
			wrt_qat = damp * (qat + sig_caller) * 0.5;
		
		//boolean initsMField = false;
		
		boolean isP = caller.getNameScore() == 0 && caller.isReactive();
		String firstWord = caller.getConstructedSWUM().getParse().beginsWith();
		boolean isK = AbstractElementScore.getPos().isGeneralVerb(firstWord);

		s = qat * damp;
		
		// k -> k/p
//		if (qo * damp > 0.75 || qat * damp > 0.75)
//			s = 0.15;
//		else if (isP)
//			s = 0.25;
//		else
//			s = 0d;
		
//		if (isP || isK)
//			s = 0d;
		
		// callers m -> m/s/c
//		if (wrt_caller > 0.6) // wrt_query > 0.65
//			s = 1d; // 1d major		
//		else if (wrt_query > 0.7) // necessary?
//			s = 0.75;
//		else if (isP)
//			s = 0.25;
//		else if (isK)
//			s = 0.15;
//		else
//			s = 0;
			
		String f = "[[ q:" + pf.sprintf(qname) + 
		//", sm:" + pf.sprintf(sm) + 
		", qat:" + pf.sprintf(qat) + 
		", l:" + last +
		", s:" + pf.sprintf(wrt_query) + 
		", c:" + pf.sprintf(wrt_qo) + 
		", sr:" + pf.sprintf(combo) +
		", sw:" + pf.sprintf(sig_caller) +  
		//", sg:" + pf.sprintf(sigs) +
		//", cs:" + pf.sprintf(csm) +
		", n:" + pf.sprintf(caller.getNameScore()) +
		//", d:" + pf.sprintf(body) + 
		", da:" + pf.sprintf(da) + 
		", dn:" + pf.sprintf(caller.getDepAvg()) +
		//", b:" + pf.sprintf(bows) + 
		//", r: " + reactive +
		", fo:" + pi.sprintf(caller_fanout) +
		", fi:" + pi.sprintf(callee_fanin) +
		", i:" + pf.sprintf(fanin) +
		", a:" + and +  
		", b: " + callee_binary +
		" ]]";
		
		caller.setF(f);
		
		return s;
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
	
	private HashMap<IField, CalculateAllSearchScoreData> handleFields(DoraNode node) {
		//out.println("\n--" + node.getSignature() + " " + node.getScore().getF());
		return handleFields((IMethod)node.getElement(), (CalculateAllSearchScoreData)node.getScore());
	}
	
	private HashMap<IField, CalculateAllSearchScoreData> handleFields(IMethod node, String sig2) {
		CalculateAllSearchScoreData score = gatherScores(node);
		//out.println("\n--" + sig2 + " " + score.getF());
		return handleFields(node, score);
	}
	
	private HashMap<IField, CalculateAllSearchScoreData> handleFields(IMethod node, CalculateAllSearchScoreData score) {
		// Field uses
		Set<IField> c = dependencies.getFieldUses(node);
		
		HashMap<IField, CalculateAllSearchScoreData> nonz = 
			new HashMap<IField, CalculateAllSearchScoreData>();
		
		if (c.size() > 0) {
			out.print("\nFields:\n");
			for (IField f : c) {
					CalculateAllSearchScoreData use = gatherScores(f);
					double s = analyzeFieldUse(use, score);
					out.println("\t" + pf.sprintf(s) + " " + 
							ContextBuilder.getSignature(f) + " " +  
							use.getF());
					
					use.setDoraScore(s);
					nonz.put(f, use);
			}
		}
		
		return nonz;
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleFieldUsers(DoraNode node) {
		//out.println("\n--" + node.getSignature() + " " + node.getScore().getF());
		return handleFieldUsers((IField)node.getElement(), (CalculateAllSearchScoreData)node.getScore());
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleFieldUsers(IField node, String sig2) {
		CalculateAllSearchScoreData score = gatherScores(node);
		//out.println("\n--" + sig2 + " " + score.getF());
		return handleFieldUsers(node, score);
	}
	
	private HashMap<IMethod, CalculateAllSearchScoreData> handleFieldUsers(IField node, CalculateAllSearchScoreData score) {
		// Field users
		Set<IMember> c = dependencies.getProjectReferences(node, _proj);
		
		HashMap<IMethod, CalculateAllSearchScoreData> nonz = 
			new HashMap<IMethod, CalculateAllSearchScoreData>();
		
		if (c.size() > 0) {
			out.print("\nUsers:\n");
			for (IMember m : c) {
				if (m instanceof IMethod) {
					IMethod mu = (IMethod) m;
					CalculateAllSearchScoreData caller = gatherScores(mu);
					double s = analyzeFieldUser(score, caller, c.size());
					out.println("\t" + pf.sprintf(s) + " " + 
							ContextBuilder.getSignature(mu) + " " +  
							caller.getF());
					
					caller.setDoraScore(s);
					nonz.put(mu, caller);
				}
			}
		}
		
		return nonz;
	}
	
	private double analyzeFieldUser(CalculateAllSearchScoreData field, CalculateAllSearchScoreData caller, int users) {
		double s = 0;
		double sig = caller.getSwums() / max_sig;
		//double body = caller.getBows() / max_body;
		double combo = caller.getScore() / max_combo;
		double fscore = field.getScore() / max_fcombo;
		boolean sname = false;
		//double qname = 0d;
		double da = caller.getDepAvg() / max_body;

//		if (field.getFieldNameStemmedWords() != null) {
//			HashSet<String> sName = caller.getConstructedSWUM().getNameStemmedWords();
//			sname = checkOverlap(sName, field.getFieldNameStemmedWords());
//			qname = checkOverlapOnQueryTerm(sName, 
//					field.getFieldNameStemmedWords());
//		}
		
		double maxq = 0d;
		
		if (field.getFieldNameStemmedWords() != null) {
			HashSet<String> sName = caller.getConstructedSWUM().getNameStemmedWords();
			sname = checkOverlap(sName, field.getFieldNameStemmedWords());
			maxq = Math.max( checkOverlapOnQueryTerm(sName, 
					field.getFieldNameStemmedWords()), 
							 checkOverlapOnQueryTerm(caller.getConstructedSWUM().getThemeStemmedWords(), 
					field.getFieldNameStemmedWords()) );
			maxq = Math.max(checkOverlapOnQueryTerm(caller.getConstructedSWUM().getThemeNoAbbrevStemmedWords(), 
					field.getFieldNameStemmedWords()), maxq);
			sName.addAll(caller.getConstructedSWUM().getThemeNoAbbrevStemmedWords());
			maxq = Math.max(checkOverlapOnQueryTerm(sName, 
					field.getFieldNameStemmedWords()), maxq);
		}

		double fanin = calculateFanIn(users);
		
		//s = (sig + qname) / 2d;
		//s = Math.max(sig, qname);
		
		double wrt_query = fscore + 0.1 * sig;
		double wrt_caller = 0d;
		if (maxq > 0)
			wrt_caller = (maxq + fscore + sig) / 3d;

		s = sig;
		
 
//		if (wrt_caller > 0.6)
//			s = 1d; // for m
//		else
//			s = 0;
		
		
		String f = "[[ q:" + pf.sprintf(maxq) +
//		", sm:" + pf.sprintf(sm) +
		", s:" + pf.sprintf(wrt_query) + 
		", c:" + pf.sprintf(wrt_caller) + 
		", sr:" + pf.sprintf(combo) +
		", sw:" + pf.sprintf(sig) +  
		//", sg:" + pf.sprintf(sigs) +
//		", cs:" + pf.sprintf(csm) +
		", n:" + pf.sprintf(caller.getNameScore()) +
		", d:" + pf.sprintf(da) + 
		//", b:" + pf.sprintf(bows) + 
		//", r: " + reactive +
		", f:" + users +
		", i:" + pf.sprintf(fanin) +
//		", b: " + callee_binary +
		" ]]";
		
		caller.setF(f);
		
		return s;
	}

	private double analyzeFieldUse(CalculateAllSearchScoreData use, CalculateAllSearchScoreData caller) {
		double sig = use.getSwums() / max_sig;
		double sig_caller = caller.getSwums() / max_sig;
		//double body = use.getDeps() / max_body;
		double combo = use.getScore() / max_fcombo;
		double s = 0d;
		boolean name = false;
		double maxq = 0d;
		
		if (use.getFieldNameStemmedWords() != null) {
			HashSet<String> sName = caller.getConstructedSWUM().getNameStemmedWords();
			name = checkOverlap(sName, use.getFieldNameStemmedWords());
			maxq = Math.max( checkOverlapOnQueryTerm(sName, 
					use.getFieldNameStemmedWords()), 
							 checkOverlapOnQueryTerm(caller.getConstructedSWUM().getThemeStemmedWords(), 
					use.getFieldNameStemmedWords()) );
			maxq = Math.max(checkOverlapOnQueryTerm(caller.getConstructedSWUM().getThemeNoAbbrevStemmedWords(), 
					use.getFieldNameStemmedWords()), maxq);
			sName.addAll(caller.getConstructedSWUM().getThemeNoAbbrevStemmedWords());
			maxq = Math.max(checkOverlapOnQueryTerm(sName, 
					use.getFieldNameStemmedWords()), maxq);
		}
				
		double names = use.getNameScore();
		double NAME_OLAP = 0.1;
//		if (body < 0) body = 0;
//		double sm = SIG_SWUM * sig;
//		sm += BDY_SWUM * body;
		
		s = combo;
		if (name)
			s += SIG_OLAP;
		
		double wrt_query = combo + 0.1 * sig_caller;
		double wrt_qo = 0d;
		if (maxq > 0)
			wrt_qo = (maxq + combo + sig_caller) / 3d;

		s = combo; // > 0.6
		
		//s = (qname + sig) / 2d;
		//s = wrt_query;
		
////		if (wrt_query > 0.8) // > 0.85?
////			s = 1d; 
//		else if (wrt_caller > 0.65)
//			s = 0.5d; // c?
//		else
//			s = 0;
		
		String f = "[[ q:" + pf.sprintf(maxq) + 
//		", sm:" + pf.sprintf(sm) + 
		", s:" + pf.sprintf(wrt_query) + 
		", c:" + pf.sprintf(wrt_qo) + 
		", sr:" + pf.sprintf(combo) +
		", sw:" + pf.sprintf(sig) +  
		//", sg:" + pf.sprintf(sigs) +
//		", cs:" + pf.sprintf(csm) +
//		", n:" + pf.sprintf(callee.getNameScore()) +
//		", d:" + pf.sprintf(body) + 
		//", b:" + pf.sprintf(bows) + 
		//", r: " + reactive +
//		", f:" + callee_fanin +
//		", i:" + pf.sprintf(fanin) +
//		", b: " + callee_binary +
		" ]]";
		
		use.setF(f);
		

		return s;
	}
	
	/*private double analyzeCaller(CalculateSearchScoreData caller, 
			CalculateSearchScoreData callee, int callee_fanin, 
			int caller_fanout, int caller_fanin, int lines, boolean caller_binary) {
		double sig = caller.getSwums() / max_sig;
		double body = caller.getDeps() / max_body;
		double combo = caller.getSearchScore() / max_combo;
		double s = 0d;
		
		boolean and = getThemeAndActionOverlap(caller.getConstructedSWUM(), 
					callee.getConstructedSWUM());
		boolean sigo = getSigOverlap(caller.getConstructedSWUM(), 
					callee.getConstructedSWUM());
//		boolean name = getNameOverlap(caller.getConstructedSWUM(), 
//				callee.getConstructedSWUM());
	
		double fanin = calculateFanIn(callee_fanin);
		
		if (body < 0) body = 0;
		
		double qname = getNameOverlapOnQueryTerm(caller.getConstructedSWUM(), 
				callee.getConstructedSWUM());

		double qo = qname;
		if (and  && !caller_binary && !caller.isReactive())
			qo = 1d;

		//if (caller.getNameScore() > 0)
		//	s = qo;
		
		boolean initsMField = false;
		
		boolean isP = caller.getNameScore() == 0 && 
			(caller.isReactive() /*|| caller_fanin == 0);
		
		String firstWord = caller.getConstructedSWUM().getParse().beginsWith();
		boolean isK = 	//AbstractElementScore.getPos().isEventWord(firstWord) ||
						//caller_fanout > 20 ||
						//(caller.isConstructor() && initsMField)
						AbstractElementScore.getPos().isGeneralVerb(firstWord)
						//|| lines >= 50
						(
							(//AbstractElementScore.getPos().isIgnorableVerb(firstWord) ||
								AbstractElementScore.getPos().isGeneralVerb(firstWord) ) &&
							(caller.getConstructedSWUM().getThemeWords().contains("action") ||
								caller.getConstructedSWUM().getThemeWords().contains("event") )
						;
		
		s = (sig + qo) / 2d;
		//double li = 1 - calculate
		if (isP)
			s = 2d; // p
		else if (isK)
			s = 1d;
		else if (lines >= 50 && s > 0)
			s = 0.5;
		else
			s = 0d;
		
		// caller p/k identification
//		if (sig > 0 || caller.isReactive())
//			s = fanin;
//		if (caller.isConstructor())
//			s = 1d;
//		else
//			s = 0d;
		
		String f = "[[ q:" + pf.sprintf(qname) + 
		//", sm:" + pf.sprintf(sm) + 
		", sr:" + pf.sprintf(combo) +
		", sw:" + pf.sprintf(sig) +  
		", n:" + pf.sprintf(caller.getNameScore()) +
		", d:" + pf.sprintf(body) + 
		", da:" + pf.sprintf(caller.getDepAvg() / max_body) + 
		//", b:" + pf.sprintf(caller.getBows() / max_body) +
		", fo:" + pi.sprintf(caller_fanout) +
		", fi:" + pi.sprintf(caller_fanin) +
		", li:" + pi.sprintf(lines) +
		//", i:" + pf.sprintf(fanin) +
		", a: " + and +
		", r: " + caller.isReactive() +
		", b: " + caller_binary +
		" ]]";
		
		caller.setF(f);

		return s;
	}*/
	
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
		
		exploreSet(pg, libSeedList, NodeType.L);
		
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
	
	private void addToLibList(IMethod m) {
		libSeedList.put(DoraNode.getHandle(m), m);
	}
	
	private double t_explore = 0.74;
	private double t_display = 0.24;
	private int    t_distance = 1;
	//private double t_field_user = t_display;
	
	private void explore(DoraNode sd, int edgesAway, ProgramGraph pg) {
		if (sd.getElement() instanceof IMethod) {
			// Handle callees
			exploreCallees(sd, edgesAway, pg);
			
			// Handle inheritance
			exploreInheritance(sd, edgesAway, pg);
			// doesn't work for binary?
			
			// Handle field uses
			exploreFields(sd, edgesAway, pg);
			
			// Handle callers
			exploreCallers(sd, edgesAway, pg);
			
		} else if (sd.getElement() instanceof IField) {
			// Handle field users
			//exploreFieldUsers(sd, edgesAway, pg);
		}
	}
	
	private void exploreFieldUsers(DoraNode sd, int edgesAway, ProgramGraph pg) {
		HashMap<IMethod, CalculateAllSearchScoreData> users = handleFieldUsers(sd);
		
		for (IMethod m : users.keySet()) {
			
			CalculateAllSearchScoreData score = users.get(m);
			double pscore = scoreDampen(score.getDoraScore(), edgesAway, sd.getPathScore());

			// TODO check that edges away within threshold
			
			// set type
			if (pscore > 
					thresholdDampen(t_explore, edgesAway) ) {
				// TODO add to worklist
				DoraNode c = createOrGetNode(pg, m, edgesAway + 1, pscore, score,
						NodeType.M);
				addToWorkList(c);
				
				//add edge
				pg.addEdge(c, sd, EdgeType.PLAIN, score.getDoraScore());
			} else if (pscore > 
					thresholdDampen(t_display, edgesAway) ) {
				DoraNode c = createOrGetNode(pg, m, edgesAway + 1, pscore, score,
						NodeType.E);
				
				//add edge
				pg.addEdge(c, sd, EdgeType.PLAIN, pscore);
			} else if (pg.containsNode(m))
				pg.addEdge(pg.getNode(m), sd, EdgeType.PLAIN, pscore);
		} 
	}
	
	private double scoreDampen(double score, int e, double p) {
//		double d = p;
//		if (d > 1) d = 1;
//		return score * d;// - 0.1 * e;
		return score;
	}
	
	private double thresholdDampen(double t, int e) {
		return t;// + 0.1 * e;
	}

	private void exploreFields(DoraNode sd, int edgesAway, ProgramGraph pg) {
		HashMap<IField, CalculateAllSearchScoreData> callees = handleFields(sd);
		
		for (IField m : callees.keySet()) {
			
			CalculateAllSearchScoreData score = callees.get(m);
			double pscore = scoreDampen(score.getDoraScore(), edgesAway, sd.getPathScore());

			// TODO check that edges away within threshold
			
			// set type
			if (pscore > 
					thresholdDampen(t_explore, edgesAway)) {
				// TODO add to worklist
				
				DoraNode c = createOrGetNode(pg, m, edgesAway + 1, pscore, score, NodeType.M);
				addToWorkList(c);
				
				//add edge
				pg.addEdge(sd, c, EdgeType.PLAIN, score.getDoraScore());
			} else if (pscore >  
					thresholdDampen(t_display, edgesAway)) {
				DoraNode c = createOrGetNode(pg, m, edgesAway + 1, pscore, 
						score, NodeType.E);
				
				//add edge
				pg.addEdge(sd, c, EdgeType.PLAIN, pscore);
			} else if (pg.containsNode(m))
				pg.addEdge(sd, pg.getNode(m), EdgeType.PLAIN, pscore);

		}
	}
	
	private void exploreInheritance(DoraNode sd, int edgesAway, ProgramGraph pg) {
		HashMap<IMethod, CalculateAllSearchScoreData> callees = handleInheritance(sd);
		
		for (IMethod m : callees.keySet()) {
			//if (!m.isBinary()) { // don't add binary inheritance
				CalculateAllSearchScoreData score = callees.get(m);
				double pscore = scoreDampen(score.getDoraScore(), edgesAway, sd.getPathScore());

				// TODO check that edges away within threshold
				if (!m.isBinary()) {

					if (pscore > 
							thresholdDampen(t_explore, edgesAway)) {

						DoraNode c = createOrGetNode(pg, m, edgesAway + 1, 
								pscore, score, sd.getType());
						addToWorkList(c);
						
						//add edge both ways
						pg.addDoubleEdge(sd, c, EdgeType.IMPLEMENT, pscore);
					} else if (pscore > 
							thresholdDampen(t_display, edgesAway)) {
						DoraNode c = createOrGetNode(pg, m, edgesAway + 1, 
								pscore, score, sd.getType());

						//add edge both ways
						pg.addDoubleEdge(sd, c, EdgeType.IMPLEMENT, pscore);
					} else if (pg.containsNode(m))
						pg.addDoubleEdge(pg.getNode(m), sd, EdgeType.IMPLEMENT, pscore);
				}
		}
	}

	private void exploreCallees(DoraNode sd, int edgesAway, ProgramGraph pg) {
		HashMap<IMethod, CalculateAllSearchScoreData> callees = handleCallees(sd);
		
		for (IMethod m : callees.keySet()) {
			String h = DoraNode.getHandle(m);
			String h2 = ContextBuilder.getSignature(m);
			if (m.isBinary() && seeds.contains(h2) && !workList.containsKey(h)
					&& !analyzedList.contains(h))
					addToLibList(m);
			
			CalculateAllSearchScoreData score = callees.get(m);
			double pscore = scoreDampen(score.getDoraScore(), edgesAway, sd.getPathScore());

			// TODO check that edges away within threshold
			
			// set type
			if (pscore >  
					thresholdDampen(t_explore, edgesAway)) {
				// TODO add to worklist
				DoraNode c;

				if (m.isBinary())
					c = createOrGetNode(pg, m, edgesAway + 1, pscore, score, NodeType.L);
				else {
					c = createOrGetNode(pg, m, edgesAway + 1, pscore, score, NodeType.M);
					addToWorkList(c);
				}
				
				//add edge
				pg.addEdge(sd, c, EdgeType.PLAIN, pscore);
			} else if (pscore >  
					thresholdDampen(t_display, edgesAway)) {
				DoraNode c;
				
				if (m.isBinary())
					c = createOrGetNode(pg, m, edgesAway + 1, pscore, score, NodeType.L);
				else {
					c = createOrGetNode(pg, m, edgesAway + 1, pscore, score, 
							NodeType.E);
					//addToWorkList(c);
				}
				
				//add edge
				pg.addEdge(sd, c, EdgeType.PLAIN, pscore);
			} else if (pg.containsNode(m))
				pg.addEdge(sd, pg.getNode(m), EdgeType.PLAIN, pscore);
		}
	}
	
	private void exploreCallers(DoraNode sd, int edgesAway, ProgramGraph pg) {
		HashMap<IMethod, CalculateAllSearchScoreData> callers = handleCallers(sd);
		
		for (IMethod m : callers.keySet()) {
			String h = DoraNode.getHandle(m);
			String h2 = ContextBuilder.getSignature(m);
			if (m.isBinary() && seeds.contains(h2) && !workList.containsKey(h)
					&& !analyzedList.contains(h))
					addToLibList(m);
			
			CalculateAllSearchScoreData score = callers.get(m);
			double pscore = scoreDampen(score.getDoraScore(), edgesAway, sd.getPathScore());
			// TODO check that edges away within threshold
			
			// set type
			if (pscore >  
					thresholdDampen(t_explore, edgesAway)) {
				// TODO add to worklist

				DoraNode c;

				if (m.isBinary())
					c = createOrGetNode(pg, m, edgesAway + 1, pscore, score, NodeType.L);
				else {
					c = createOrGetNode(pg, m, edgesAway + 1, pscore, score, NodeType.M);
					addToWorkList(c);
				}
				
				//add edge
				pg.addEdge(c, sd, EdgeType.PLAIN, pscore);
			} else if (pscore >  
					thresholdDampen(t_display, edgesAway)) {
				DoraNode c;
				
				if (m.isBinary())
					c = createOrGetNode(pg, m, edgesAway + 1, pscore, score, 
							NodeType.L);
				else {
					c = createOrGetNode(pg, m, edgesAway + 1, pscore, score, 
							NodeType.K);
					//addToWorkList(c);
				}
				
				//add edge
				pg.addEdge(c, sd, EdgeType.PLAIN, pscore);
			} else if (pg.containsNode(m))
				pg.addEdge(pg.getNode(m), sd, EdgeType.PLAIN, pscore);
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
			t = new DoraNode(sd, e, gatherScores(sd));
			
		return t;
	}
	
	private DoraNode createOrGetNode(ProgramGraph pg, IMember sd, int e, double pscore,
			CalculateAllSearchScoreData score, NodeType nt) {
		String handle = DoraNode.getHandle(sd);
		DoraNode t;
		if (pg.containsNode(handle)) {
			t = pg.getNode(handle);
			// assume score equiv
			if (e < t.getEdgesAway()) {
				t.setEdgesAway(e);
			//if (nt.getRelevance() > t.getType().getRelevance())
				t.setType(nt);
			//if (pscore > t.getPathScore())
				t.setPathScore(pscore);
			}
		} else {
			t = new DoraNode(sd, e, pscore, score, nt);
			pg.addNode(t);
		}
			
		return t;
	}
}
