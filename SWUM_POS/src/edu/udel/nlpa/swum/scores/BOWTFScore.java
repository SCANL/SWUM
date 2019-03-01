package edu.udel.nlpa.swum.scores;

import java.util.HashMap;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;

public class BOWTFScore extends BOWScore {

	public BOWTFScore(String q, ISWUMBuilder s) {
		super(q, s);
	}

	public BOWTFScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
	}

	protected double bowScore(String src, String sig) {
		return bowScore(src);
	}
	
	protected double bowScore(String src) {
		double score = 0d;
		if (debug_score) score_formula = " [[";

		int words = 0;
		if (containsQueryBool(src.toLowerCase())) {
			// get tf
			String[] str = idSplitter.splitId(src);
			HashMap<String, int[]> terms = new HashMap<String, int[]>();
			for (String w : str) {
				w = w.toLowerCase();
				w = stemmer.stem(w);
				words++;
				if (queryWords.containsKey(w)) {
					if (terms.containsKey(w))
						terms.get(w)[0]++;
					else 
						terms.put(w, new int[]{1});
				}
			}

			for (String t : terms.keySet()) {
				double idf = getLogIDF(t); // ln
				int tf = terms.get(t)[0];
				//TODO if tf = 0 increment

				double max = 0;
				if (tf > 0)
					max = 1 + Math.log(1d * tf); // ln
				max *= idf;
				contains.get(t)[0] = max;
				score += max;
				
				if (debug_score) score_formula += t+ ": ("+tf +","+ idf +") ";
			}
		}
		if (debug_score) score_formula += score;
		//score /= Math.log10(1d * words);

		if (debug_score) score_formula += " ]]";
		return score;
	}

}
