package edu.udel.nlpa.swum.scores;

import org.eclipse.jdt.core.IJavaProject;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.nodes.searchable.SearchableWord;

public class BooleanBOWScore extends BOWScore {

	public BooleanBOWScore(String q, ISWUMBuilder s) {
		super(q, s);
	}

	public BooleanBOWScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
	}

	protected double bowScore(String src, String sig) {
		return bowScore(src);
	}
	
	protected double bowScore(String src) {
		double score = 0d;
		if (debug_score) score_formula = " [[ ";
		
		src = stem(idSplitter.splitIdIntoSpaces(src));
		for (SearchableWord q : queryWords.values()) {
			double idf = getLinearIDF(q.getStemmedWord());
			if (containsQueryBool(q, src)) {
				score += idf;
				if (debug_score) score_formula += q.getWord() + " : " + pf.sprintf(idf) + ", ";
				contains.get(q.getStemmedWord())[0] = idf;
			}
		}
		if (debug_score) score_formula += " ]]";
		//score /= 1d * queryWords.size();
		return score;
	}

}
