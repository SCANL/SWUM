package edu.udel.nlpa.swum.scores;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.nodes.IWordNode;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.searchable.SearchableEquivalenceNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableFieldDecl;
import edu.udel.nlpa.swum.nodes.searchable.SearchableMethodDecl;
import edu.udel.nlpa.swum.nodes.searchable.SearchableNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchablePhraseNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableWord;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.constants.POSTag;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;
import edu.udel.nlpa.swum.utils.context.FieldContext;
import edu.udel.nlpa.swum.utils.context.MethodContext;

public class SWUMConservScore extends SWUMScore {
	
	public SWUMConservScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
	}
	
	public SWUMConservScore(String q, ISWUMBuilder s) {
		super(q, s);
	}

	@Override
	protected double swumFieldScore(IField node, String sig) {
		return swumFieldScore(node);
	}
	
	public double score(ILocalVariable node) {
		String name = node.getElementName();
		String type = ContextBuilder.getType(node.getTypeSignature());
		return swumVariableScore(name, type);
	}
	
	public double score(IType node) {
		return swumTypeScore(node.getElementName());
	}
	
	public double score(ITypeParameter node) {
		return swumTypeScore(node.getElementName());
	}
	
	private static double MM_ACT = 1d;
	private static double MM_DO  = 1d;
	private static double MM_IO  = 0.5; // for now, only in name
	private static double MM_UNK = 0.25; // really low
	/*private static double MM_SIG1 = 0.25;
	private static double MM_SIG2 = 0.25;*/
	
	protected double swumMethodScore(IMethod node, String sig) {
		return swumMethodScore(node, new HashSet<Node>(), sig);
	}

	protected double swumMethodScore(IMethod node, Set<Node> actuals, String sig) {
		double score = 0d;
		//String sig = getSignature(node);
		String name = node.getElementName();
		if (debug_score) score_formula = " [[";
		
		// count that matches id splitter & stemmer
		//int count = containsQuery(stem(idSplitter.splitIdIntoSpaces(sig)));
		//int count = M_SIG * containsQuery(stem(idSplitter.splitIdIntoSpaces(sig)));

		//if (count > 0 || containsQuery(sig.toLowerCase()) > 0) {
			MethodContext mc = ContextBuilder.buildMethodContext(node);
			
			// Run SWUM
			SearchableMethodDecl md = new SearchableMethodDecl(name, mc, stemmer);
			RuleIndicator ri = swum.applyRules(md);
			isReactive = md.isReactive();
			isCtor = md.isConstructor();
			setConstructedSWUM(md);
			setFName(null);

			for (SearchableWord q : queryWords.values()) {
				double idf = getLinearIDF(q.getStemmedWord());
				double ac = 0d;
				double d0 = 0d;
				double io = 0d;
				double un = 0d;
				
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
					double alpha = MM_DO;
					if (md.getThemeLocation() == Location.ONCLASS &&
							!nameContainsQuery(md) )
						alpha = MM_UNK;
					d0 += alpha * hscore;
					if (debug_score) score_formula += ", " + alpha + " * " + hscore;
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
				
				double max = getMax(ac, d0, io, un);
				score += idf * max;
				
				if (max > 0)
					contains.get(q.getStemmedWord())[0] = idf * max;
				
				if (debug_score) score_formula += ")";
		//	}
			
			if (debug_score) score_formula += " ]]";
		}
		return score;
	}
	
	private boolean nameContainsQuery(SearchableMethodDecl md) {
		ArrayList<IWordNode> name = md.getParse().getPhrase();
		for (IWordNode w : name) {
			if (w instanceof SearchableWord) {
				if ( queryWords.containsKey(
						((SearchableWord)w).getStemmedWord() ) )
					return true;
			}
		}
		return false; // TODO change to true to remove impact
	}

	private double swumFieldScore(IField node) {
		String sig = ContextBuilder.getSignature(node);
		String name = node.getElementName();
		int count = containsQuery(stem(idSplitter.splitIdIntoSpaces(sig)));
		
		// count that overcomes poor splitting
		if (count > 0 || containsQuery(sig.toLowerCase()) > 0) { // for efficiency
			FieldContext fc = ContextBuilder.buildFieldContext(node);
			SearchableFieldDecl fd = new SearchableFieldDecl(name, fc, stemmer);
			RuleIndicator ri = swum.applyRules(fd);
			SearchablePhraseNode type = null;
			
			if (fd.getType() != null) {
				if (fd.getOnClass() != null) {
					if (fd.getType().isPrimitive()) { // primitive type
						type = (SearchablePhraseNode) fd.getOnClass().getParse();
					} else { // non-primitive type
						type = (SearchablePhraseNode) fd.getType().getParse();
					}
				}
			} else if (fd.getOnClass() != null) {
				type = (SearchablePhraseNode) fd.getOnClass().getParse();
			}
			
			return swumFieldScore(fd.getParse(), type, sig);
		}
		return 0d;
	}
	
	private double swumTypeScore(String type) {
		String sig = type;
		SearchablePhraseNode t = new SearchablePhraseNode(type, idSplitter, stemmer);
		int count = containsQuery(t.toStemmedSearchableString());
		if (count > 0 || containsQuery(sig.toLowerCase()) > 0)
			return swumFieldScore(null, t, sig);
		return 0d;
	}
	
	private static double FM_NAME = 1d;
	private static double FM_TYPE1 = 0.5;
	/*private static double FM_TYPE2 = 0d;
	private static double FM_SIG1  = 0d;
	private static double FM_SIG2  = 0d;*/
	
	private double swumVariableScore(String name, String type) {
		String sig = type + " " + name;
		SearchablePhraseNode n = new SearchablePhraseNode(name, idSplitter, stemmer);
		SearchablePhraseNode t = new SearchablePhraseNode(type, idSplitter, stemmer);
		int count = containsQuery(t.toStemmedSearchableString() + " " + n.toStemmedSearchableString());
		if (count > 0 || containsQuery(sig.toLowerCase()) > 0)
			return swumFieldScore(n,t, sig);
		return 0d;
	}

	private double swumFieldScore(SearchablePhraseNode name, SearchablePhraseNode type, String sig) {
		double score = 0d;
		if (debug_score) score_formula = " [[";
		setConstructedSWUM(null);
		HashSet<String> fname = new HashSet<String>();
		if (name != null)
			fname.addAll(name.getStemmedWords());

		// assume contains some query word
		for (String qw : queryWords.keySet()) {
			SearchableWord q = queryWords.get(qw);
			double idf = getLinearIDF(q.getStemmedWord());
			double sqw = 0d;
			double t1 = 0d;

			if (debug_score) score_formula += " + " + idf + " * max(";

			if (name != null) {
				String t = "";
				if (type != null) t = type.toStemmedSearchableString();
				double head = getHeadScore(q.getStemmedWord(), name);
				double lntf = lntf(q.getStemmedWord(), name.toStemmedSearchableString(), t, name.size());
				//double lntf = getHeadScore(q.getStemmedWord(), name);
				sqw += FM_NAME * (lntf + head) / 2d;
				if (debug_score) score_formula += FM_NAME + " * " + lntf + " + " + head;
				
				//if (lntf + head > 0)
				//	fname.add(q.getStemmedWord());
			}

			if (type != null) {
				double head_type = getHeadScore(q.getStemmedWord(), type);
				t1 = FM_TYPE1 * head_type;
				if (debug_score) score_formula += ", " + FM_TYPE1 + " * " + head_type;
			}
			double max = Math.max(sqw, t1);
			score += idf * max;
			
			if (max > 0)
				contains.get(qw)[0] = idf * max;

			if (debug_score) score_formula += " )";
			
		} // for each query word
		
		setFName(fname);
		
		if (debug_score) score_formula += " ]]";
		return score;
	}



	
}
