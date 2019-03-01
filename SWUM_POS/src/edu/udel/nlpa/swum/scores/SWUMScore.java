package edu.udel.nlpa.swum.scores;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.searchable.SearchableMethodDecl;
import edu.udel.nlpa.swum.nodes.searchable.SearchableWord;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public abstract class SWUMScore extends AbstractElementScore {

	public SWUMScore(String q, ISWUMBuilder s) {
		super(q, s);
	}
	
	public SWUMScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
	}
	
	@Override
	public double score(IMethod node) {
		resetContains();
		return swumMethodScore(node, ContextBuilder.getSignature(node));
	}
	
	public double score(IMethod node, Set<Node> actualTheme) {
		resetContains();
		return swumMethodScore(node, actualTheme, ContextBuilder.getSignature(node));
	}

	@Override
	public double score(IField node) {
		resetContains();
		return swumFieldScore(node, ContextBuilder.getSignature(node));
	}
	
	protected abstract double swumFieldScore(IField node, String sig);
	protected abstract double swumMethodScore(IMethod node, String sig);
	protected abstract double swumMethodScore(IMethod node, Set<Node> actuals, String sig);
	//protected abstract double swumMethodScore(IMethod node, String sig, Set<String> actualTheme);
	
	protected double getMax(double sqw, double t1, double t2, double s1, double s2) {
		double max = Math.max(sqw, t1);
		max = Math.max(max, t2);
		max = Math.max(max, s1);
		max = Math.max(max, s2);
		return max;
	}
	
	protected double getMax(double sqw, double t1, double t2, double s1) {
		double max = Math.max(sqw, t1);
		max = Math.max(max, t2);
		max = Math.max(max, s1);
		return max;
	}
	
	protected double getMax(double sqw, double s3, double t1, double t2, double s1, double s2) {
		double max = Math.max(sqw, t1);
		max = Math.max(max, t2);
		max = Math.max(max, s1);
		max = Math.max(max, s2);
		max = Math.max(max, s3);
		return max;
	}
	
	private SearchableMethodDecl constructedSWUM = null;

	public SearchableMethodDecl getConstructedSWUM() {
		return constructedSWUM;
	}
	
	public void setConstructedSWUM(SearchableMethodDecl constructedSWUM) {
		this.constructedSWUM = constructedSWUM;
	}
	
	private HashSet<String> fname;

	public void setFName(HashSet<String> n) {
		fname = n;
	}

	public HashSet<String> getFName() {
		return fname;
	}


}
