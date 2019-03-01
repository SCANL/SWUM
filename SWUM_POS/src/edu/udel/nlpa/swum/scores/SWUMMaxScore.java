package edu.udel.nlpa.swum.scores;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.searchable.SearchableEquivalenceNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableFieldDecl;
import edu.udel.nlpa.swum.nodes.searchable.SearchableMethodDecl;
import edu.udel.nlpa.swum.nodes.searchable.SearchableNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableWord;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;
import edu.udel.nlpa.swum.utils.context.FieldContext;
import edu.udel.nlpa.swum.utils.context.MethodContext;

public class SWUMMaxScore extends SWUMScore {

	public SWUMMaxScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
	}
	
	public SWUMMaxScore(String q, ISWUMBuilder s) {
		super(q, s);
	}
	
	private static double MM_ACT = 1d;
	private static double MM_DO  = 1d;
	private static double MM_IO  = 0.5;
	private static double MM_UNK = 0.5;
	private static double MM_SIG1 = 0.25;
	private static double MM_SIG2 = 0.25;
	
	protected double swumMethodScore(IMethod node, String sig) {
		return swumMethodScore(node, new HashSet<Node>(), sig);
	}

	protected double swumMethodScore(IMethod node, Set<Node> actuals, String sig) {

		double score = 0d;
		//String sig = getSignature(node);
		String name = node.getElementName();
		if (debug_score) score_formula = " [[";
		
		// count that matches id splitter & stemmer
		int count = containsQuery(stem(idSplitter.splitIdIntoSpaces(sig)));
		//int count = M_SIG * containsQuery(stem(idSplitter.splitIdIntoSpaces(sig)));

		if (count > 0 || containsQuery(sig.toLowerCase()) > 0) {
			MethodContext mc = ContextBuilder.buildMethodContext(node);
			
			// Run SWUM
			SearchableMethodDecl md = new SearchableMethodDecl(name, mc, stemmer);
			RuleIndicator ri = swum.applyRules(md);
			isReactive = md.isReactive();
			setConstructedSWUM(md);
			setFName(null);
			
			for (SearchableWord q : queryWords.values()) {
				double idf = getIDF(q.getStemmedWord());
				double ac = 0d;
				double d0 = 0d;
				double io = 0d;
				double un = 0d;
				double s1 = 0d;
				double s2 = 0d;
				
				if (debug_score) score_formula += " + " + idf + " * max(";

				// match query
				if (md.getAction() != null) {
					double hscore = getHeadScore(q.getStemmedWord(), md.getAction());
					ac += MM_ACT * hscore;
					if (debug_score) score_formula += MM_ACT + " * " + hscore;
				}

				if (md.getTheme() != null) {
					Node t = md.getTheme();
					if (actuals != null && !actuals.isEmpty() && md.getTheme() instanceof SearchableNode) {
						SearchableEquivalenceNode sen = new SearchableEquivalenceNode((SearchableNode)md.getTheme());
						for (Node n : actuals)
							sen.addEquivalentNode(n);
						t = sen;
					}
					double hscore = getHeadScore(q.getStemmedWord(), t);
					d0 += MM_DO * hscore;
					if (debug_score) score_formula += ", " + MM_DO + " * " + hscore;
				}

				if (md.getSecondaryArgs() != null) {
					double hscore = getHeadScore(q.getStemmedWord(), md.getSecondaryArgs());
					io += MM_IO * hscore;
					if (debug_score) score_formula += ", " + MM_IO + " * " + hscore;
				}
				
				if (md.getUnknownArgs() != null) {
					double hscore = getHeadScore(q.getStemmedWord(), md.getUnknownArgs());
					un += MM_UNK * hscore;
					if (debug_score) score_formula += ", " + MM_UNK + " * " + hscore;
				}
				
				//int c = containsQuery(q.getWord(), sig.toLowerCase());
				if (containsQueryBool(q.getWord(), sig.toLowerCase())) {
					s1 += MM_SIG1; // bias in favor of multiple query words?
					if (debug_score) score_formula += ", " + MM_SIG1 ;
				}

				//c = containsQuery(q.getStemmedWord(), sig.toLowerCase());
				if (containsQueryBool(q.getStemmedWord(), sig.toLowerCase())) {
					s2 += MM_SIG2;
					if (debug_score) score_formula += ", " + MM_SIG2 ;
				}
				double max = getMax(ac, d0, io, un, s1, s2);
				score += idf * max;
				
				if (max > 0)
					contains.get(q.getStemmedWord())[0] = idf * max;
				
				if (debug_score) score_formula += ")";
			}
			
			if (debug_score) score_formula += " ]]";
		}
		return score;
	}
	
	private static double FM_NAME = 1d;
	private static double FM_TYPE1 = 0.75;
	private static double FM_TYPE2 = 0.5;
	private static double FM_SIG1  = 0.25;
	private static double FM_SIG2  = 0.25;

	protected double swumFieldScore(IField node, String sig) {
		String name = node.getElementName();
		double score = 0d;
		if (debug_score) score_formula = " [[";
		
		// count that matches id splitter & stemmer
		int count = containsQuery(stem(idSplitter.splitIdIntoSpaces(sig)));
		
					  // count that overcomes poor splitting
		if (count > 0 || containsQuery(sig.toLowerCase()) > 0) { // for efficiency
			FieldContext fc = ContextBuilder.buildFieldContext(node);
			SearchableFieldDecl fd = new SearchableFieldDecl(name, fc, stemmer);
			RuleIndicator ri = swum.applyRules(fd);
			
			for (SearchableWord q : queryWords.values()) {
				double idf = getIDF(q.getStemmedWord());
				double sqw = 0d;
				double t1 = 0d;
				double t2 = 0d;
				double s1 = 0d;
				double s2 = 0d;
				
				if (debug_score) score_formula += " + " + idf + " * max(";
				
				if (fd.getParse() != null) {
					String t = "";
					if (fd.getType() != null) t = fd.getType().toStemmedSearchableString();
					double lntf = lntf(q.getStemmedWord(), fd.getParse().toStemmedSearchableString(),
							t, fd.getParse().size());
					sqw += FM_NAME * lntf;
					if (debug_score) score_formula += FM_NAME + " * " + lntf;
				}
				
				if (fd.getType() != null) {
					if (fd.getOnClass() != null) {
						double head_class = getHeadScore(q.getStemmedWord(),
								fd.getOnClass());
						double head_type = getHeadScore(q.getStemmedWord(),
								fd.getType());
						if (fd.getType().isPrimitive()) { // primitive type
							t1 += FM_TYPE1 * head_class;
							// just in case primitive in query 
							t2 += FM_TYPE2 * head_type;
							
							if (debug_score) score_formula += ", " + FM_TYPE1 +
							" * " + head_class + ", " + FM_TYPE2 + " * " + head_type;
						} else { // non-primitive type
							t1 += FM_TYPE1 * head_type;
							// class least important if not primitive type
							t2 += FM_TYPE2 * head_class;
							
							if (debug_score) score_formula += ", " + FM_TYPE1 +
							" * " + head_type + ", " + FM_TYPE2 + " * " + head_class;
						}
					}
				} else if (fd.getOnClass() != null) { // fd.getType == null
					double head_class = getHeadScore(q.getStemmedWord(),
							fd.getOnClass());
					t1 += FM_TYPE1 * head_class;
					if (debug_score) score_formula += ", " + FM_TYPE1 + " * " + head_class;
				}
				
				//int c = containsQuery(q.getWord(), sig.toLowerCase());
				if (containsQueryBool(q.getWord(), sig.toLowerCase())) {
					s1 += FM_SIG1; // bias in favor of multiple query words?
					if (debug_score) score_formula += ", " + FM_SIG1 ;
				}
				
				//c = containsQuery(q.getStemmedWord(), sig.toLowerCase());
				if (containsQueryBool(q.getStemmedWord(), sig.toLowerCase())) {
					s2 += FM_SIG2;
					if (debug_score) score_formula += ", " + FM_SIG2 ;
				}
				
				double max = getMax(sqw, t1, t2, s1, s2);
				score += idf * max;
				
				if (max > 0)
					contains.get(q.getStemmedWord())[0] = idf * max;
				
				if (debug_score) score_formula += " )";
			} // for each query word
		} // if contains query words
		
		if (debug_score) score_formula += " ]]";
		return score;
	}


}
