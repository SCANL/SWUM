package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.builders.UnigramBuilder;
import edu.udel.nlpa.swum.nodes.base.FieldDecl;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.scores.AbstractElementScore;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;
import edu.udel.nlpa.swum.utils.context.FieldContext;
import edu.udel.nlpa.swum.utils.context.MethodContext;

public class WordCountElementVisitor extends JavaElementVisitor {

	private PrintStream out;
	private static int i = 0;
	
	public WordCountElementVisitor(PrintStream p) {
		super();
		out = p;
	}
	
	@Override
	protected boolean visit(IClassFile node) {
		return false;
	}
	
	@Override
	protected boolean visit(IMethod node) {
		count(node);
		return true;
	}

	private void count(IMember node) {
		String src = getSource(node);
		int count = stem(AbstractElementScore.idSplitter.splitIdIntoSpaces(src)).size();
		int lines = 0;
		out.println(i + " " + count + " " + lines + " " +  node.getElementName());
		i++;
	}

	@Override
	protected boolean visit(IField node) {
		count(node);
		return true;
	}
	
	protected int getApproxStmts(String src) {
		int lines = 0;
		
		if (src != null && !src.equals("")) {
			for (char c : src.toCharArray()) {
				if (c == ';' || c == '}' || c == '{')
					lines++;
			}
		}
		
		return lines;
	}
	
	protected String getSource(IMember node) {
		String src = null;
		try {
			src = node.getSource();
		} catch (JavaModelException e) { e.printStackTrace(); }
		if (src == null) src = node.getElementName();
		return src;
	}
	
	protected HashSet<String> stem(String splitId) {
		HashSet<String> words = new HashSet<String>();
		for (String i : splitId.split("\\s+")) {
			words.add(AbstractElementScore.stemmer.stem(i));
		}
		return words;
	}

}
