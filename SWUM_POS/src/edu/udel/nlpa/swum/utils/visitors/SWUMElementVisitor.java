package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.builders.UnigramBuilder;
import edu.udel.nlpa.swum.nodes.base.FieldDecl;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;
import edu.udel.nlpa.swum.utils.context.FieldContext;
import edu.udel.nlpa.swum.utils.context.MethodContext;

public class SWUMElementVisitor extends JavaElementVisitor {

	public static ISWUMBuilder swum;
	private PrintStream out;
	
	public SWUMElementVisitor() {
		super();
		swum = new UnigramBuilder();
		out = System.out;
	}
	
	public SWUMElementVisitor(PrintStream p) {
		this();
		out = p;
	}
	
	@Override
	protected boolean visit(IClassFile node) {
		return false;
	}
	
	@Override
	protected boolean visit(IMethod node) {
		MethodContext mc = ContextBuilder.buildMethodContext(node);
		MethodDecl md = new MethodDecl(node.getElementName(), mc);
		RuleIndicator ri = 
			swum.applyRules(md); // getSignature(node) vs getFileSignature?
		out.println(ri + "::" + md + "::" + ContextBuilder.getFileSignature(node));
//		out.println(ContextBuilder.getFileSignature(node));
		return true;
	}

	@Override
	protected boolean visit(IField node) {
		FieldContext fc = ContextBuilder.buildFieldContext(node);
		FieldDecl fd = new FieldDecl(node.getElementName(), fc);
		RuleIndicator ri = 
			swum.applyRules(fd);
		
		out.println(ri + "::" +fd + "::" + ContextBuilder.getFileSignature(node));
//		out.println(ContextBuilder.getFileSignature(node));
		return true;
	}

}
