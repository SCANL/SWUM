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

public class SWUMSumScore extends SWUMScore {

	public SWUMSumScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
	}
	
	public SWUMSumScore(String q, ISWUMBuilder s) {
		super(q, s);
	}
	
	private static double F_NAME = 0.6;
	private static double F_TYPE1 = 0.2;
	private static double F_TYPE2 = 0.1;
	private static double F_SIG1  = 0.05;
	private static double F_SIG2  = 0.05;

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
			
//			if (count > 0)
//				if (debug_score) score_formula += count + " * (";

			for (SearchableWord q : queryWords.values()) {
				double idf = getIDF(q.getStemmedWord());
				double sqw = 0d;
				
				if (debug_score) score_formula += " + " + idf + " * (";
				
				if (fd.getParse() != null) {
					String t = "";
					if (fd.getType() != null) t = fd.getType().toStemmedSearchableString();
					double lntf = lntf(q.getStemmedWord(), fd.getParse().toStemmedSearchableString(),
							t, fd.getParse().size());
					score = F_NAME * lntf;
					if (debug_score) score_formula += F_NAME + " * " + lntf;
				}
				
				//int c = containsQuery(q.getWord(), sig.toLowerCase());
				if (containsQueryBool(q.getWord(), sig.toLowerCase())) {
					sqw += F_SIG1; // bias in favor of multiple query words?
					if (debug_score) score_formula += " + " + F_SIG1 ;
				}
				
				//c = containsQuery(q.getStemmedWord(), sig.toLowerCase());
				if (containsQueryBool(q.getStemmedWord(), sig.toLowerCase())) {
					sqw += F_SIG2;
					if (debug_score) score_formula += " + " + F_SIG2 ;
				}
				
//				if (fd.getParse() != null)
//					sqw += F_NAME * lntf(q.getStemmedWord(), 
//							fd.getParse().toStemmedSearchableString(), 
//							fd.getParse().size());

				if (fd.getType() != null) {
					if (fd.getOnClass() != null) {
						double head_class = getHeadScore(q.getStemmedWord(),
								fd.getOnClass());
						double head_type = getHeadScore(q.getStemmedWord(),
								fd.getType());
						if (fd.getType().isPrimitive()) { // primitive type
							sqw += F_TYPE1 * head_class;
							// just in case primitive in query 
							sqw += F_TYPE2 * head_type;
							
							if (debug_score) score_formula += " + " + F_TYPE1 +
							" * " + head_class + " + " + F_TYPE2 + " * " + head_type;
						} else { // non-primitive type
							sqw += F_TYPE1 * head_type;
							// class least important if not primitive type
							sqw += F_TYPE2 * head_class;
							
							if (debug_score) score_formula += " + " + F_TYPE1 +
							" * " + head_type + " + " + F_TYPE2 + " * " + head_class;
						}
					}
				} else if (fd.getOnClass() != null) { // fd.getType == null
					double head_class = getHeadScore(q.getStemmedWord(),
							fd.getOnClass());
					sqw += F_TYPE1 * head_class;
					if (debug_score) score_formula += " + " + F_TYPE1 + " * " + head_class;
				}
				
				score += idf * sqw;
				
				if (sqw > 0)
					contains.get(q.getStemmedWord())[0] = idf * sqw;
				
				if (debug_score) score_formula += " )";
			} // for each query word
		} // if contains query words
		
//		if (count > 0) {
//			score *= count; // ensures occurrences of multiple query words goes higher
//							// than just 1 query word in multiple positions
//			if (debug_score) score_formula += " )";
//		}
		if (debug_score) score_formula += " ]]";
		return score;
	}
	
	private static double M_ACT = .3;
	private static double M_DO  = .3;
	private static double M_IO  = .15;
	private static double M_UNK = .15;
	private static double M_SIG1 = .05;
	private static double M_SIG2 = .05;
	
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
			
//			if (count > 0)
//				if (debug_score) score_formula += count + " * (";

			// Run SWUM
			SearchableMethodDecl md = new SearchableMethodDecl(name, mc, stemmer);
			RuleIndicator ri = swum.applyRules(md);
			isReactive = md.isReactive();
			setConstructedSWUM(md);
			setFName(null);
			
			for (SearchableWord q : queryWords.values()) {
				double sqw = 0d;
				double idf = getIDF(q.getStemmedWord());
				
				if (debug_score) score_formula += " + " + idf + " * (";

				//int c = containsQuery(q.getWord(), sig.toLowerCase());
				if (containsQueryBool(q.getWord(), sig.toLowerCase())) {
					sqw += M_SIG1; // bias in favor of multiple query words?
					if (debug_score) score_formula += " + " + M_SIG1 ;
				}

				//c = containsQuery(q.getStemmedWord(), sig.toLowerCase());
				if (containsQueryBool(q.getStemmedWord(), sig.toLowerCase())) {
					sqw += M_SIG2;
					if (debug_score) score_formula += " + " + M_SIG2 ;
				}

				// match query
				if (md.getAction() != null) {
					double hscore = getHeadScore(q.getStemmedWord(), md.getAction());
					sqw += M_ACT * hscore;
					if (debug_score) score_formula += " + " + M_ACT + " * " + hscore;
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
					sqw += M_DO * hscore;
					if (debug_score) score_formula += " + " + M_DO + " * " + hscore;
				}

				if (md.getSecondaryArgs() != null) {
					double hscore = getHeadScore(q.getStemmedWord(), md.getSecondaryArgs());
					sqw += M_IO * hscore;
					if (debug_score) score_formula += " + " + M_IO + " * " + hscore;
				}
				
				if (md.getUnknownArgs() != null) {
					double hscore = getHeadScore(q.getStemmedWord(), md.getUnknownArgs());
					sqw += M_UNK * hscore;
					if (debug_score) score_formula += " + " + M_UNK + " * " + hscore;
				}
				
				score += idf * sqw;
				
				if (sqw > 0)
					contains.get(q.getStemmedWord())[0] = idf * sqw;
				
				if (debug_score) score_formula += ")";
			}
			// TODO use reactive to downplay signature weight?

//			if (count > 0) {
//				score *= count; // ensures occurrences of multiple query words goes higher
//								// than just 1 query word in multiple positions
//				if (debug_score) score_formula += " )";
//			}
			
			if (debug_score) score_formula += " ]]";
		}
		return score;
	}



}
