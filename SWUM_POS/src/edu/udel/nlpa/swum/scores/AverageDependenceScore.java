package edu.udel.nlpa.swum.scores;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.*;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.explore.DependencyRelationship;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public class AverageDependenceScore extends AbstractElementScore {

	protected static final DependencyRelationship dependencies = new DependencyRelationship();
	private SWUMConservScore swum;

	public AverageDependenceScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
		swum = new SWUMConservScore(q, jp, super.swum);
	}

	
	@Override
	public double score(IField node) {
		resetContains();
		return 0d; 
	}

	@Override
	public double score(IMethod node) {
		resetContains();
		double score = getAvgUses(node);
		return score;
	}
	

	
	private static double A_METHOD = 1d;
	private static double A_FIELD  = 1d;
	private static double A_LOCAL  = 1d;
	private static double A_TYPE   = 1d; // in essence, these are being weighed
	private static double A_TPARAM = 1d; // by 0.05 by SWUMConserv
	private static double A_INIT   = 0d;
	
	private double getAvgUses(IMethod node) {
		double avg = 0d;
		int total = 0;
		
		String src = null;
		try {
			if (node.getCompilationUnit() != null)
				src = node.getCompilationUnit().getSource();
		} catch (JavaModelException e1) { e1.printStackTrace(); }
		
		if (src == null)
			return -1d;
		// else
		
		HashSet<IJavaElement> c = dependencies.getJavaElementUses(node);

		resetContains();
		
		if (debug_score) score_formula = " [[ avg(";
		
		for (IJavaElement m : c) {
			// var use
			double score = 0d;
			String t = "";
			total++;
			
			if (m instanceof IField) {
				try {
					t = ContextBuilder.getType(((IField)m).getTypeSignature());
				} catch (JavaModelException e) { e.printStackTrace(); }
				score = A_FIELD * swum.score((IField)m);
			} else if (m instanceof ILocalVariable) {
				t = ContextBuilder.getVariableType(((ILocalVariable)m).getTypeSignature());
				score = A_LOCAL * swum.score((ILocalVariable)m);
			}  else if (m instanceof IMethod) {
				t= ((IMethod)m).getDeclaringType().getElementName();
				score = A_METHOD * swum.score((IMethod)m);
			} else if (m instanceof IType) {
				score = A_TYPE * swum.score((IType)m);
			} else if (m instanceof ITypeParameter) {
				score = A_TPARAM * swum.score((ITypeParameter)m);
			} else if (m instanceof IInitializer) {
				System.out.println("IINITIALIZER: " + m.getHandleIdentifier());
			}
			avg += score;
			if (score > 0) {
				if (debug_score) score_formula += t + " " + m.getElementName() + 
				" : " + pf.sprintf(score) + ", ";
			}
		}

		if (debug_score) score_formula += ") ]]";
		
		if (total > 0)
			avg = avg / (1d * total * queryWords.size());
		
		return avg;
	}
	
}
