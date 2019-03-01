package edu.udel.nlpa.swum.scores;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;


public class DoraScore extends AbstractElementScore {
	
	public DoraScore(String q) {
		super(q);
	}

	public double doraScore(IMember im) {
		if (im == null)
			return 0.0;
		int isBinary = 0;
		double singleNameFreq = 0d;
		double singleStmtFreq = 0d;
		int length = 0;
		boolean t = true;
		String src = "";
		
		String name = im.getElementName();
		
		
		if (im.isBinary()) isBinary = 1;
		
		try {
			src = im.getSource();
		} catch (JavaModelException e) { e.printStackTrace(); }
		
		if (src != null && !src.equals("")) {

			length = getApproxStmts(src);

			// for each word in name and src
			singleStmtFreq = getDoraIDF(src);
			
		}
		
		if (name != null && !name.equals("")) {
			singleNameFreq = getDoraIDF(name);
		}
		
		return logit(-0.5 + -2.5*isBinary + 1*singleNameFreq + 
				  			 0.5*( singleStmtFreq/(length+1d) )
			);
	}

	private double getDoraIDF(String src) {
		double score = 0d;
		
		int numDocs = dfl.getNumDocs();
		boolean t = true;
		HashMap<String, int[]> terms = getMap(idSplitter.splitIdIntoSpaces(src));
		for (String word : queryWords.keySet()) {
		//for (String word : terms.keySet()) {
			if (terms.containsKey(queryWords.get(word).getStemmedWord())) { // only for words in query
				int tf = terms.get(word)[0];
				int df = dfl.getDF(word);
				double idf = 0;
				if (df != 0) idf = Math.log(numDocs/((double)df));
				score += tf*idf;
			}
		}
		return score;
	}
	
	public HashMap<String, int[]> getMap(String str) {
		HashMap<String, int[]> terms = new HashMap<String, int[]>();
		if (str != null && !str.equals("")) {
			for (String t : str.split("\\s")) {
				//if (Activator.getPreference(PreferenceConstants.P_STEM))
					t = stemmer.stem(t); // TODO NOT Porter's
				if (!t.matches("^\\s*$")) {
					if (terms.containsKey(t))
						terms.get(t)[0]++;
					else
						terms.put(t, new int[]{1});
				}
			}
		}
		return terms;
	}
	
	private double logit(double d) {
		//s = exp(d) / (1 + exp(d));
		return Math.exp(d) / (1 + Math.exp(d));
	}

	@Override
	public double score(IMethod node) {
		return doraScore(node);
	}

	@Override
	public double score(IField node) {
		return doraScore(node);
	}

}
