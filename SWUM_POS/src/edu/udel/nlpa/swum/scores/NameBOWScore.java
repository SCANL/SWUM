package edu.udel.nlpa.swum.scores;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.nodes.searchable.SearchableWord;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public class NameBOWScore extends BooleanBOWScore {

	public NameBOWScore(String q, ISWUMBuilder s) {
		super(q, s);
	}

	public NameBOWScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
	}
	
	@Override
	public double score(IField node) {
		resetContains();
		return bowScore(node.getElementName());
	}

	@Override
	public double score(IMethod node) {
		resetContains();
		return bowScore(node.getElementName());
	}
	
	@Override
	protected double bowScore(String src) {
		return super.bowScore(src);
	}

}
