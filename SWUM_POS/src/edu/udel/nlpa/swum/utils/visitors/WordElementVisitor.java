package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;

import edu.udel.nlpa.swum.builders.UnigramBuilder;
import edu.udel.nlpa.swum.scores.AbstractElementScore;
import edu.udel.nlpa.swum.scores.BooleanNoSigBOWScore;
import edu.udel.nlpa.swum.scores.IElementScore;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public class WordElementVisitor extends JavaElementVisitor {

	protected PrintStream out;
	private HashSet<String> seeds = null;
	HashMap<String, int[]> terms = new HashMap<String, int[]>();
	protected IElementScore bow;
	
	public WordElementVisitor(String q, IJavaProject jp, PrintStream p, HashSet<String> s, double[] maxes) {
		super();
		out = p;
		seeds = s;
		bow = new BooleanNoSigBOWScore(q, jp, new UnigramBuilder());
		// TODO Need to decouple IDF Info from SWUM Scores
	}
	
	@Override
	protected boolean visit(IClassFile node) {
		return false;
	}
	
	@Override
	protected boolean visit(IMethod node) {
		String s = ContextBuilder.getSignature(node);
		String seval = ContextBuilder.getEvalSignature(node);
		if (seeds.contains(seval)) {
			//out.println(s);
			seeds.remove(seval); // make sure only analyze once
			int words = 0;
			
			for (String w : AbstractElementScore.idSplitter.splitId(s)) {
				w = w.toLowerCase();
				w = AbstractElementScore.stemmer.stem(w);

				if (terms.containsKey(w))
					terms.get(w)[0]++;
				else 
					terms.put(w, new int[]{1});

			}
			
		}
		return true;
	}

	@Override
	protected boolean visit(IField node) {
//		FieldContext fc = ContextBuilder.buildFieldContext(node);
//		FieldDecl fd = new FieldDecl(node.getElementName(), fc);
//		//RuleIndicator ri = 
//			swum.applyRules(fd);
//		
//		System.out.println(fd + "::" + ContextBuilder.getSignature(node));
//		return true;
		return false;
	}
	
	public void calculate() {
		for (String t : terms.keySet()) {
			double idf = AbstractElementScore.getLinearIDF(t);
			int tf = terms.get(t)[0];
			double tfidf = tf * idf;
			out.println(tfidf + " " + t + " [" +tf + ", " + idf+ "]");
		}
	}
	
	

}
