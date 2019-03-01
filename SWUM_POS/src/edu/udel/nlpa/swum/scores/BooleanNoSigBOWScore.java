package edu.udel.nlpa.swum.scores;

import org.eclipse.jdt.core.IJavaProject;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.nodes.searchable.SearchableWord;

public class BooleanNoSigBOWScore extends BooleanBOWScore {

	public BooleanNoSigBOWScore(String q, ISWUMBuilder s) {
		super(q, s);
	}

	public BooleanNoSigBOWScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
	}

	protected double bowScore(String src, String sig) {
		// remove sig from src
		String[] sigs = sig.split("[^0-9_A-Za-z]");
		for (String r : sigs)
			src = src.replaceFirst(r, "");
		
		return super.bowScore(src, sig);
	}

}
