package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.dora.NodeType;
import edu.udel.nlpa.swum.explore.DependencyRelationship;
import edu.udel.nlpa.swum.explore.FieldInitializerVisitor;
import edu.udel.nlpa.swum.explore.ThemeVisitor;
import edu.udel.nlpa.swum.nodes.searchable.SearchableMethodDecl;
import edu.udel.nlpa.swum.scores.*;
import edu.udel.nlpa.swum.utils.PrintfFormat;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;
import edu.udel.nlpa.swum.utils.context.MethodContext;

public class SWUMSearchVisitor extends SWUMElementVisitor {
	protected PrintStream out;
	
	protected SWUMScore swum;
	protected IElementScore bow;
	protected IElementScore dep;
	protected IElementScore dep_avg;
	protected IElementScore sig;
	protected IElementScore name;
	protected IJavaProject _proj;
	protected DoraScore dora;
	
	protected PrintfFormat pf = new PrintfFormat("%.2f");
	protected PrintfFormat pi = new PrintfFormat("%3d");
	//protected CalculateSearchScoreData scoreData;
		
	/*public SWUMSearchVisitor(String q) {
		this(q, System.out);
	}
	
	public SWUMSearchVisitor(String q, PrintStream o) {
		super();
		out = o;
		swum = new SWUMMaxScore(q, super.swum);
		bow = new BOWScore(q, super.swum);
		dep = new DependenceScore(q, super.swum);
	}*/
	
	public SWUMSearchVisitor(String q, IJavaProject jp, PrintStream p) {
		super();
		_proj = jp;
		out = p;
		swum = new SWUMConservScore(q, jp, super.swum);
		bow = new BooleanNoSigBOWScore(q, jp, super.swum);
		//bow = new BOWTFScore(q, jp, super.swum);
		dep = new DependenceScore(q, jp, super.swum);
		dep_avg = new AverageDependenceScore(q, jp, super.swum);
		sig = new SignatureBooleanBOWScore(q, jp, super.swum);
		name = new NameBOWScore(q, jp, super.swum);
		dora = new DoraScore(q);
		
		//scoreData = new CalculateSearchScoreData(swum, bow, dep, sig);
	}
	
	protected static double F_SWM = 1d;
	protected static double F_SIG = 0.05;
	//protected static double F_DEP = 0d;
	protected static double F_SRC = 0.1;
	protected static double F_CNT = 0d;

	@Override
	protected boolean visit(IField node) {
//		String sig2 = ContextBuilder.getSignature(node);
//		CalculateAllSearchScoreData score = gatherScores(node);
//
//		out.printf("%.5f::f::::%s\n", score.getScore(), 
//				sig2 + score.getF());

		return true;
	}
	
	protected static double B_SWM = 1d;
	protected static double B_DEP = 0.1; 
	protected static double B_BOW = 0.1; 
	protected static double B_SIG = 0.05; 
	protected static double B_CNT = 1d; 

	
	// for reactive
	protected static double R_SWM = 0.25;
	protected static double R_DEP = 1d; // for reactive
	protected static double R_BOW = 0.25;
	
	@Override
	protected boolean visit(IMethod node) {
		String sig2 = ContextBuilder.getFileSignature(node);
		//String seval = ContextBuilder.getEvalSignature(node);
//		CalculateSearchScoreData score = gatherScores(node);
//		
//		out.printf("%.5f::m::::%s\n", score.getSearchScore(), 
//				sig2 + score.getF());
		
		
//		double s = swum.score(node);
//		out.printf("%.5f::m::::%s\n", s, 
//				sig2 + swum.getFormula());
		
		double s = swum.score(node); //calculateSWUMSearchScore(node);
		out.println(s + " " + sig2);
		
		//System.out.println(SWUMEval1DoraVisitor.getRankName(node));
				
		return true;
	}
	
	protected CalculateAllSearchScoreData gatherScores(IMember node) {
		if (node instanceof IMethod)
			return gatherScores((IMethod)node);
		else if (node instanceof IField)
			return gatherScores((IField)node);
		return null;
	}
	
	protected CalculateAllSearchScoreData gatherScores(IMethod node) {
		CalculateAllSearchScoreData cssd = new CalculateAllSearchScoreData(dora, swum, bow, dep, dep_avg, sig, name);
		cssd.gatherScores(node, B_SWM, B_SIG, B_DEP, B_BOW);
		return cssd;
	}
	
	protected CalculateAllSearchScoreData gatherScores(IField node) {
		CalculateAllSearchScoreData cssd = new CalculateAllSearchScoreData(dora, swum, bow, dep, sig, name);
		cssd.gatherScores(node, F_SWM, F_SIG, F_SRC);
		return cssd;
	}
	
	protected double calculateSWUMSearchScore(IMethod node) {
		double score = 0d;
		double swums = swum.score(node);
//		return swums;
		double bows = bow.score(node);
		double sigs = sig.score(node);
		score = Math.max(B_SWM * swums, B_SIG * sigs);
		score += B_BOW * ( 1d * bows / swum.getContains().keySet().size() );
		return score;
	}
	
	protected double calculateBOWSearchScore(IMethod node) {
		return bow.score(node);
	}
	
	protected double calculateSWUMBodySearchScore(IMethod node) {
		CalculateAllSearchScoreData ssd = gatherScores(node);
		return ssd.getDeps();
	}
	
}
