package edu.udel.nlpa.swum.scores;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.dora.NodeType;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.searchable.SearchableMethodDecl;
import edu.udel.nlpa.swum.nodes.searchable.SearchableNode;
import edu.udel.nlpa.swum.utils.PrintfFormat;

// TODO Really should be separate implementations for fields & methods
public class CalculateAllSearchScoreData extends AbstractScoreData{
	
	protected SWUMScore swum;
	protected IElementScore bow;
	protected IElementScore dep;
	protected IElementScore depa;
	protected IElementScore sig;
	protected IElementScore name;
	
	
	private double swums = 0d;
	//private double bow_score = 0d;
	private double deps = 0d;
	private double depas = 0d;
	
	private int count = 0;
	private double name_score = 0d;
	private NodeType proposedType = NodeType.NO_TYPE; // proposed type

	


	private double dora = 0d;
	
//	public CalculateSearchScoreData(String q, IJavaProject jp, ISWUMBuilder swumb) {
//		swum = new SWUMConservScore(q, jp, swumb);
//		bow = new BooleanNoSigBOWScore(q, jp, swumb);
//		dep = new DependenceScore(q, jp, swumb);
//		sig = new SignatureBooleanBOWScore(q, jp, swumb);
//	}
//	
//	public CalculateSearchScoreData(CalculateSearchScoreData cssd) {
//		this(cssd.swum, cssd.bow, cssd.dep, cssd.sig);
//	}
	
	public CalculateAllSearchScoreData(DoraScore d, SWUMScore swum2, IElementScore bow2,
			IElementScore dep2, IElementScore sig2, IElementScore name2) {
		super(d);
		swum = swum2;
		bow = bow2;
		dep = dep2;
		sig = sig2;
		name = name2;
	}
	
	public CalculateAllSearchScoreData(DoraScore d, SWUMScore swum2, IElementScore bow2,
			IElementScore dep2, IElementScore depa2, IElementScore sig2, IElementScore name2) {
		super(d);
		swum = swum2;
		bow = bow2;
		dep = dep2;
		depa = depa2;
		sig = sig2;
		name = name2;
	}

	// run first, then access other doubles
	protected double getScores(IMethod node, double B_SWM, double B_SIG,
			double B_DEP, double B_BOW) {
//		dora = 0d;
		swums = swum.score(node);
		bow_score = bow.score(node);
		deps = 0d;
		depas = 0d;
		if (bow_score > 0) {
			deps = dep.score(node);
			depas = depa.score(node);
			
			if (deps < 0)
				deps = 0d;
			if (depas < 0)
				depas = 0d;
		}
		sigs = sig.score(node);
		name_score = name.score(node);
		
		score = Math.max(B_SWM * swums, B_SIG * sigs);
		

//		score += B_DEP * depas;
//		score += Math.max(B_BOW * ( 1d * bows / swum.getContains().keySet().size()),
//				B_DEP * depas);
		score += B_BOW * ( 1d * bow_score / swum.getContains().keySet().size() );

		
		
		reactive = swum.isReactive();
		ctor = swum.isConstructor();
		md = swum.getConstructedSWUM();
		
		//boolean isBinary?
		count = 0;
		
		if (score > 0)
			count = getQueryWordCount(swum.getContains(), bow.getContains());
				
		f = " [[ c:" + count + ", sr:" + pf.sprintf(score) +
			", sw:" + pf.sprintf(swums) +  
			", sg:" + pf.sprintf(sigs) +
			", n:" + pf.sprintf(name_score) +
			", d:" + pf.sprintf(deps) + 
			", da:" + pf.sprintf(depas) + 
			", b:" + pf.sprintf(bow_score) + 
			", r: " + reactive + " ]]";
		return score;
		//return swums;
	}
	
	protected double getScores(IField node, double F_SWM, double F_SIG, double F_SRC) {
		swums = swum.score(node); // if 0, try sig
		bow_score = bow.score(node);
		sigs = sig.score(node);
		score = 0d;
		reactive = false;
		ctor = false;
		deps = 0d;
		depas = 0d;
//		dora = 0d;
		md = null;
		fname = swum.getFName();
		name_score = name.score(node);
		
		count = 0;
		
		if (score > 0)
			count = getQueryWordCount(swum.getContains(), bow.getContains());
		
		score = Math.max(F_SWM * swums, F_SIG * sigs) + F_SRC * bow_score / swum.getContains().keySet().size();
		
		f = " [[ c:" + count + ", sw:" + pf.sprintf(swums) + 
			", sg:" + pf.sprintf(sigs) +
			", n:" + pf.sprintf(name_score) +
			", b:" + pf.sprintf(bow_score) + " ]]";

		return score;
	}
	
	protected int getQueryWordCount(HashMap<String, double[]> sqw, HashMap<String, double[]> dqw) {
		int count = 0;
		
		for (String qw : sqw.keySet()) {
			double s = sqw.get(qw)[0];
			double d = dqw.get(qw)[0];
			if (s > 0 || d > 0)
				count++;
		}
		
		return count;
	}
	


//	public double getSwums() {
//		return swums;
//	}

//	public double getBows() {
//		return bow_score;
//	}

	public double getDeps() {
		return deps;
	}
	
	public double getDepAvg() {
		return depas;
	}

	public int getCount() {
		return count;
	}
	
	/*public double getDoraScore() {
		return doraScore;
	}
	
	public void setDoraScore(double s) {
		doraScore = s;
	}*/
	
	
	public double getNameScore() {
		return name_score;
	}

	public void setName_score(double nameScore) {
		name_score = nameScore;
	}

	public NodeType getProposedType() {
		return proposedType;
	}

	public void setProposedType(NodeType proposedType) {
		this.proposedType = proposedType;
	}

	@Override
	protected double getScores(IMethod node) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected double getScores(IMethod node, Set<Node> actuals) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected double getScores(IField node) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
