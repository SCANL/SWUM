package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import edu.udel.nlpa.swum.explore.DependencyRelationship;
import edu.udel.nlpa.swum.scores.BOWTFScore;
import edu.udel.nlpa.swum.scores.CalculateSWUMSearchScoreData;
import edu.udel.nlpa.swum.scores.IElementScore;
import edu.udel.nlpa.swum.utils.PrintfFormat;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public class TFIDFSearchVisitor extends SWUMElementVisitor {
	protected PrintStream out;
	
	protected IElementScore bow;
	protected IJavaProject _proj;
	//private String pkg;
	
	protected PrintfFormat pf = new PrintfFormat("%4.20f");
	protected static final DependencyRelationship dependencies = new DependencyRelationship();
	
	public TFIDFSearchVisitor(String q, IJavaProject jp, PrintStream p) {
		super();
		_proj = jp;
		out = p;
		bow = new BOWTFScore(q, jp, super.swum);
		//pkg = jp.getPath().toPortableString().replaceFirst("/", "");
	}

	@Override
	protected boolean visit(IField node) {
		String sig2 = ContextBuilder.getRhinoSignature(node); //ContextBuilder.getFileSignature(node);
		
		double s = bow.score(node); 
		String debug = bow.getFormula();
		//if (s >= 0)
			//out.println(pf.sprintf(s) + " " + sig2);
			out.println(pf.sprintf(s) + " " + sig2 + " " + debug);

		return true;
	}
	
	@Override
	protected boolean visit(IMethod node) {
		String sig2 = ContextBuilder.getRhinoSignature(node); //ContextBuilder.getFileSignature(node);
		
		double s = bow.score(node); 
		String debug = bow.getFormula();
		//if (s >= 0)
			//out.println(pf.sprintf(s) + " " + sig2);
			out.println(pf.sprintf(s) + " " + sig2 + " " + debug);
		
		return true;
	}

	private void scoreCallHierarchy(IMethod node, double s) {
		// Do callees NOT in package
		Set<IMethod> c = dependencies.getCallees(node);
		for (IMethod m : c) {
			String pkg = m.getPath().toPortableString();
			String sig = ContextBuilder.getFileSignature(m);
			//out.println(pkg);
			if (!pkg.endsWith(".java")) // then it's a library
				//if (!pkg.matches(getPathLocation(m))) 
			{
				s = bow.score(m);
				if (s >= 0)
					out.println(pf.sprintf(-1*s) + " " + sig);
			} else if (sig.contains("$")) { // it has $ in the name
				if (s >= 0)
					out.println(pf.sprintf(s) + " " + sig);
			}
		}
		
		// Do all callers?
		c = dependencies.getCHCallers(node);
		for (IMethod m : c) {
			String pkg = m.getPath().toPortableString();
			s = bow.score(m);
			String sig = ContextBuilder.getFileSignature(m);
			if (s >= 0) {
				if (!pkg.endsWith(".java")) {
					s = -1*s; // then it's a lib
					out.println(pf.sprintf(s) + " " + sig);
				} else if (sig.contains("$")) { // it has $ in the name
					out.println(pf.sprintf(s) + " " + sig);
				}
			}

		}
	}
	
	
//	public static String getType(String type) {
//		return getFieldType(type);
//	}
//	
//	public static String getVariableType(String type) {
//		return getFieldType(type.replaceAll("/", "."));
//	}
//	
//	public static String getFieldType(String type) {
//		String t = Signature.getSignatureSimpleName(type);
//		t.replaceAll("[\\[\\]]", ""); // replace with array??
//		return t;
//	}
	
	public static String getPathLocation(IMember node) {
		String pkg = node.getPath().toPortableString();
		if (pkg.endsWith(".java")) {
			pkg = pkg.replaceAll("^/([A-Za-z0-9_]+)/.*", "$1");
		} else {
			pkg = "lib";
		}
		return pkg;
	}
	
}
