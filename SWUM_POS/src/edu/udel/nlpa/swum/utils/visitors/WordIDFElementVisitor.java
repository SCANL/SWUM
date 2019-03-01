package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import edu.udel.nlpa.swum.builders.UnigramBuilder;
import edu.udel.nlpa.swum.scores.AbstractElementScore;
import edu.udel.nlpa.swum.scores.BooleanBOWScore;
import edu.udel.nlpa.swum.scores.BooleanNoSigBOWScore;
import edu.udel.nlpa.swum.scores.IElementScore;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public class WordIDFElementVisitor extends JavaElementVisitor {

	protected PrintStream out;
	HashMap<String, int[]> terms = new HashMap<String, int[]>();
	protected IElementScore bow; // just to make sure dfl initialized

	public WordIDFElementVisitor(String q, IJavaProject jp, PrintStream p) {
		super();
		out = p;
		bow = new BooleanBOWScore(q, jp, new UnigramBuilder());
		// TODO Need to decouple IDF Info from SWUM Scores
	}

	@Override
	protected boolean visit(IClassFile node) {
		return false;
	}

	@Override
	protected boolean visit(IMethod node) {
		String s = getSource(node);

		//int words = 0;
		String[] words = AbstractElementScore.idSplitter.splitId(s);
		for (String w : words) {
			w = w.toLowerCase();
			w = AbstractElementScore.stemmer.stem(w);

			if (terms.containsKey(w))
				terms.get(w)[0]++;
			else 
				terms.put(w, new int[]{1});

		}

		calculate();
		return true;
	}
	
	protected String getSource(IMember node) {
		String src = null;
		try {
			src = node.getSource();
		} catch (JavaModelException e) { e.printStackTrace(); }
		if (src == null) ContextBuilder.getSignature(node);
		if (src == null) src = node.getElementName();
		return src;
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
			double idf = AbstractElementScore.getLogIDF(t);
			int tf = terms.get(t)[0];
			double tfidf = tf * idf;
			out.println(tfidf + " " + t + " [" +tf + ", " + idf+ "]");
		}
	}



}
