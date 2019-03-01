package edu.udel.nlpa.swum.scores;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.IMember;

public interface IElementScore {
	public double score(IMember node);
	public String getFormula();
	public HashMap<String, double[]> getContains();
	public boolean isReactive();
	public boolean isQueryWord(String s);
	//public boolean isInterface();
}
