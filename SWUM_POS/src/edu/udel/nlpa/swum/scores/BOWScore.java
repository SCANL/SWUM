package edu.udel.nlpa.swum.scores;

import java.util.HashMap;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public abstract class BOWScore extends AbstractElementScore {

	public BOWScore(String q, ISWUMBuilder s) {
		super(q, s);
	}

	public BOWScore(String q, IJavaProject jp, ISWUMBuilder s) {
		super(q, jp, s);
	}
	
	@Override
	public double score(IField node) {
		resetContains();
		return bowScore(getSource(node), ContextBuilder.getSignature(node));
	}

	@Override
	public double score(IMethod node) {
		resetContains();
		return bowScore(getSource(node), ContextBuilder.getSignature(node));
	}

	protected String getSource(IMember node) {
		String src = null;
		try {
			src = node.getSource();
		} catch (JavaModelException e) { e.printStackTrace(); }
		if (src == null) src = node.getElementName();
		return src;
	}

	protected abstract double bowScore(String src, String sig);

}
