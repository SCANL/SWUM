package edu.udel.nlpa.swum.scores;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;

import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.searchable.SearchableMethodDecl;
import edu.udel.nlpa.swum.utils.PrintfFormat;

public abstract class AbstractScoreData {

	protected boolean reactive = false;
	protected boolean ctor = false;
	protected double score = 0d;
	protected SearchableMethodDecl md;
	protected HashSet<String> fname;
	protected String f = "";
	protected double doraScore = 0d;
	protected DoraScore dora;
	protected double bow_score = 0d;
	protected double sigs = 0d;
	
	public AbstractScoreData(DoraScore d) {
		dora = d;
	}
	
	protected static PrintfFormat pf = new PrintfFormat("%.2f");
	
	public double gatherScores(IMethod node) {
		calculateDoraScore(node);
		return getScores(node);
	}
	
	public double gatherScores(IField node) {
		calculateDoraScore(node);
		return getScores(node);
	}
	
	public double gatherScores(IMethod node, Set<Node> actuals) {
		calculateDoraScore(node);
		return getScores(node, actuals);
	}
	
	public double gatherScores(IField node, double F_SWM, double F_SIG, double F_SRC) {
		calculateDoraScore(node);
		return getScores(node, F_SWM, F_SIG, F_SRC);
	}
	
	public double gatherScores(IMethod node, double B_SWM, double B_SIG,
			double B_DEP, double B_BOW) {
		calculateDoraScore(node);
		return getScores(node, B_SWM, B_SIG, B_DEP, B_BOW);
	}
	
	protected abstract double getScores(IMethod node, double B_SWM, double B_SIG, double B_DEP, double B_BOW);
	protected abstract double getScores(IField node, double F_SWM, double F_SIG, double F_SRC);	
	protected abstract double getScores(IMethod node);
	protected abstract double getScores(IMethod node, Set<Node> actuals);
	protected abstract double getScores(IField node);

	public double gatherScores(IMember m) {
		calculateDoraScore(m);
		if (m instanceof IMethod)
			return gatherScores((IMethod)m);
		else if (m instanceof IField)
			return gatherScores((IField)m);
		return 0d;
	}

	private void calculateDoraScore(IMember m) {
		doraScore = dora.doraScore(m);
	}
	
	public boolean isReactive() {
		return reactive;
	}

	public boolean isConstructor() {
		return ctor;
	}

	public double getScore() {
		//return score;
		return score; // only if you want no SWUM scoring
	}
	
	public double getSScore() {
		return score;
	}

	public SearchableMethodDecl getConstructedSWUM() {
		return md;
	}

	public HashSet<String> getFieldNameStemmedWords() {
		return fname;
	}
	
	public String getF() {
		return f;
	}
	
	public void setF(String f) {
		this.f = f;
	}
	public double getDoraScore() {
		return doraScore;
	}
	public void setDoraScore(double doraScore) {
		this.doraScore = doraScore;
	}
	
	public void setScore(double s) {
		score = s;
	}
	
	public double getBowScore() {
		return bow_score;
	}
	
	public double getSwums() {
		return score;
	}
	
	public double getSigs() {
		return sigs;
	}

}