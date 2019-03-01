package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.HashSet;
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

public class CallGraphVisitor extends JavaElementVisitor {
	protected PrintStream out;
	
	//protected IJavaProject _proj;
	//private String pkg;
	
	protected PrintfFormat pf = new PrintfFormat("%4.20f");
	protected static final DependencyRelationship dependencies = new DependencyRelationship();
	
	public CallGraphVisitor(PrintStream p) {
		super();

		out = p;
		//pkg = jp.getPath().toPortableString().replaceFirst("/", "");
	}

	@Override
	protected boolean visit(IField node) {
//		String sig2 = ContextBuilder.getFileSignature(node);
//
//		//if (s >= 0)
//			//out.println(pf.sprintf(s) + " " + sig2);
//			out.println(pf.sprintf(s) + " " + sig2 );

		return true;
	}
	
	private static String getSig(IMethod node) {
		String sig = ContextBuilder.getEvalSpaceSignature(node, true, "");
		sig = sig.replace("[", "");
		sig = sig.replace("]", "");
		
		while (sig.matches(".*[<>].*"))
			sig = sig.replaceAll("<[^<>]*>", "");
		
		if (sig.contains("$")) {
			String[] split = sig.split("\\$");
			if (split.length > 2) {
				sig = sig.replaceFirst("\\$.*\\$", "|");
				sig = sig.replace("|", "$");
				split = sig.split("\\$");
			}
			
			if (split.length == 2) {
				String before = split[0];
				String after = split[1];
				if (!after.matches("^[0-9].*$")) {
					// Me: net.sf.jabref.BibtexFields$BibtexSingleField.BibtexSingleField(String)
					// D: net.sf.jabref.BibtexSingleField.BibtexSingleField(String)
					before = before.replaceAll("\\.[A-Za-z0-9_]+?$", ".");
					sig = before + after;
					//System.out.println(sig);
				}
			} else {
				//System.err.println("Error: " + sig);
			}
		}
		
		
		return sig;
	}
	
	private void printInfo(String sig, Set<String> links) {
		out.print(sig +": ");
		for (String l : links)
			out.print(l + ", ");
		out.println();
		//out.println(sig1 + " -> " + sig2);
	}
	
	@Override
	protected boolean visit(IMethod node) {
		String sig2 = getSig(node);
		
		//if (!pkg.endsWith(".java")) // then it's a library
//		if (sig2.contains("ImportAction"))
//			System.out.println();
		if (sig2.matches(".*\\$[0-9].*"))
			return true;
//		out.println(sig2); // testing with Dawn's
		
		Set<String> links = new HashSet<String>();
		
		Set<IMethod> c = dependencies.getCallees(node);
		for (IMethod m : c) {
			String pkg = m.getPath().toPortableString();
			if (pkg.endsWith(".java")) { // skip libraries
				String sig = getSig(m);
				if (!sig.matches(".*\\$[0-9].*")) links.add(sig);
				//printInfo(sig2, sig);
			}
		}
		
		c = dependencies.getCHCallers(node);
		for (IMethod m : c) {
			String pkg = m.getPath().toPortableString();
			if (pkg.endsWith(".java")) { // skip libraries
				String sig = getSig(m);
				if (!sig.matches(".*\\$[0-9].*")) links.add(sig);
				//printInfo(sig, sig2);
			}
		}
		
		printInfo(sig2, links);
		
		return true;
	}

//	private void scoreCallHierarchy(IMethod node, double s) {
//		// Do callees NOT in package
//		Set<IMethod> c = dependencies.getCallees(node);
//		for (IMethod m : c) {
//			String pkg = m.getPath().toPortableString();
//			String sig = ContextBuilder.getFileSignature(m);
//			//out.println(pkg);
//			if (!pkg.endsWith(".java")) // then it's a library
//				//if (!pkg.matches(getPathLocation(m))) 
//			{
//				s = bow.score(m);
//				if (s >= 0)
//					out.println(pf.sprintf(-1*s) + " " + sig);
//			} else if (sig.contains("$")) { // it has $ in the name
//				if (s >= 0)
//					out.println(pf.sprintf(s) + " " + sig);
//			}
//		}
//		
//		// Do all callers?
//		c = dependencies.getCHCallers(node);
//		for (IMethod m : c) {
//			String pkg = m.getPath().toPortableString();
//			s = bow.score(m);
//			String sig = ContextBuilder.getFileSignature(m);
//			if (s >= 0) {
//				if (!pkg.endsWith(".java")) {
//					s = -1*s; // then it's a lib
//					out.println(pf.sprintf(s) + " " + sig);
//				} else if (sig.contains("$")) { // it has $ in the name
//					out.println(pf.sprintf(s) + " " + sig);
//				}
//			}
//
//		}
//	}
	
	
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
