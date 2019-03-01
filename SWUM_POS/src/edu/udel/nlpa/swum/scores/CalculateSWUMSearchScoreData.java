package edu.udel.nlpa.swum.scores;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;

import edu.udel.nlpa.swum.nodes.Node;

// TODO Really should be separate implementations for fields & methods
public class CalculateSWUMSearchScoreData extends AbstractScoreData {
	
	protected SWUMScore swum;
	protected IElementScore name;
	protected IElementScore bow = null;
	private double name_score = 0d;
	
	
	public CalculateSWUMSearchScoreData(DoraScore d, SWUMScore swum2, IElementScore name2, IElementScore bow2) {
		super(d);
		swum = swum2;
		name = name2;
		bow = bow2;
	}
	
	public CalculateSWUMSearchScoreData(DoraScore d, SWUMScore swum2, IElementScore name2) {
		super(d);
		swum = swum2;
		name = name2;
	}

	// run first, then access other doubles
	protected double getScores(IMethod node) {
		return getScores(node, new HashSet<Node>());
	}
	
	protected double getScores(IMethod node, Set<Node> actuals) {
//		dora = 0d;
		score = swum.score(node, actuals);
		name_score = name.score(node);		
		if (bow != null)
			bow_score = bow.score(node);
		
		reactive = swum.isReactive();
		ctor = swum.isConstructor();
		md = swum.getConstructedSWUM();
		
		return score;
		//return swums;
	}
	
	protected double getScores(IField node) {
		score = swum.score(node); // if 0, try sig
		reactive = false;
		ctor = false;
		md = null;
		fname = swum.getFName();
		name_score = name.score(node);
		
		return score;
	}
	
	public double getSwums() {
		return score;
	}

	public double getNameScore() {
		return name_score;
	}


	@Override
	protected double getScores(IMethod node, double B_SWM, double B_SIG,
			double B_DEP, double B_BOW) {
		return getScores(node);
	}

	@Override
	protected double getScores(IField node, double F_SWM, double F_SIG,
			double F_SRC) {
		return getScores(node);
	}
	
	
}
