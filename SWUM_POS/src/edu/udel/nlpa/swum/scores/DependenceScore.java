package edu.udel.nlpa.swum.scores;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.*;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.explore.DependencyRelationship;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public class DependenceScore extends AbstractElementScore {

	protected static final DependencyRelationship dependencies = new DependencyRelationship();
	private SWUMConservScore swum;

	public DependenceScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
		swum = new SWUMConservScore(q, jp, super.swum);
	}

	
	@Override
	public double score(IField node) {
		resetContains();
		return 0d; //getMaxScoredFieldUser(node);
	}

	@Override
	public double score(IMethod node) {
		resetContains();
		//double score = calcLineDataDep(node); // inconclusive
		double score = getStructuralDependences(node);
		return score;
	}
	
	private double getStructuralDependences(IMethod node) {
		double max = 
			//getAvgFieldUses(node);
			//getMaxFieldUses(node);
			//getMaxVarUses(node);
			getMaxUses(node);

		// project refs takes a LONG time
//		c = dependencies.getProjectReferences(node, _proj);
//		for (IMember m : c) {
//			// potential caller
//		}
//
//		c = dependencies.getCallees(node);
//		for (IMember m : c) {
//			// potential callee
//		}
//		

//		
//		c = dependencies.getMemberInterfaces(node);
//		for (IMember m : c) {
//			// dual - inherits
//		}
		
		return max;
	}

	/*private double getMaxFieldUses(IMethod node) {
		double max = 0d;
		HashSet<IMember> c = dependencies.getFieldUses(node);
		if (debug_score) score_formula = " [[ max(";
		for (IMember m : c) {
			// potential field use
			double score = swum.score(m);
			if (debug_score) score_formula += m.getElementName() + ":" + score + ", ";
			if (score > max) {
				max = score;
			}
		}
		if (debug_score) score_formula += ") ]]";
		return max;
	}*/
	
	/*private double getMaxVarUses(IMethod node) {
		double max = 0d;
		HashSet<IJavaElement> c = dependencies.getVarUses(node);
		
		resetMaxes();
		
		if (debug_score) score_formula = " [[ max(";
		
		for (IJavaElement m : c) {
			// var use
			double score = 0d;
			String t = "";
			if (m instanceof IField) {
				try {
					t = ContextBuilder.getType(((IField)m).getTypeSignature());
				} catch (JavaModelException e) { e.printStackTrace(); }
				score = swum.score((IField)m);
			} else if (m instanceof ILocalVariable) {
				t = ContextBuilder.getVariableType(((ILocalVariable)m).getTypeSignature());
				score = swum.score((ILocalVariable)m);
			}
			if (debug_score) score_formula += t + " " + m.getElementName() + 
				" : " + pf.sprintf(score) + ", ";
			
			if (score > 0) {
				for (String qw : swum.getContains()) {
					if (score > maxes.get(qw)[0]) {
						maxes.get(qw)[0] = score;
					}
				}
			}
		}
		
		for (String qw : maxes.keySet()) {
			max += maxes.get(qw)[0] / queryWords.size(); // normalize by length?
		}
		
		if (debug_score) score_formula += ") ]]";
		return max;
	}*/
	
	/*private double getAvgFieldUses(IMethod node) {
		double score = 0d;
		int tot = 0;
		HashSet<IMember> c = dependencies.getFieldUses(node);
		if (debug_score) score_formula = " [[ avg(";
		for (IMember m : c) {
			// potential field use
			double t = swum.score(m);
			score += t;
			tot++;
			if (debug_score) score_formula += m.getElementName() + ":" + t + ", ";
		}
		if (debug_score) score_formula += ") ]]";
		if (tot > 0)
			score /= tot;
		return score;
	}*/
	
	private static double A_METHOD = 1d;
	private static double A_FIELD  = 1d;
	private static double A_LOCAL  = 1d;
	private static double A_TYPE   = 1d; // in essence, these are being weighed
	private static double A_TPARAM = 1d; // by 0.05 by SWUMConserv
	private static double A_INIT   = 0d;
	
	private double getMaxUses(IMethod node) {
		double max = 0d;
		
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
		
		if (debug_score) score_formula = " [[ max(";
		
		for (IJavaElement m : c) {
			// var use
			double score = 0d;
			String t = "";
			
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
			
			if (score > 0) {
				if (debug_score) score_formula += t + " " + m.getElementName() + 
				" : " + pf.sprintf(score) + ", ";
				for (String qw : swum.getContains().keySet()) {
//					if (swum.getContains().get(qw)[0] > contains.get(qw)[0]) {
//						contains.get(qw)[0] = swum.getContains().get(qw)[0];
//					}
//					if (score > contains.get(qw)[0]) {
//						contains.get(qw)[0] = score;
//					}
					// if the qw appeared in member, AND higher score
					if (swum.getContains().get(qw)[0] > 0 && score > contains.get(qw)[0]) {
						contains.get(qw)[0] = score;
					}
				}
			}
		}
		
		for (String qw : contains.keySet()) {
			max += contains.get(qw)[0] / queryWords.size(); // normalize by length?
		}
		
		if (debug_score) score_formula += ") ]]";
		return max;
	}
	
}
