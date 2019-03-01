package edu.udel.nlpa.swum.utils.visitors;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.internal.core.JavaElement;

import edu.udel.nlpa.swum.explore.DependencyRelationship.LocalFieldVisitor;

/**
 * IJavaElement Visitor
 * If visit returns true, visitChildren will investigate children
 * Otherwise, visiting stops.
 * A traversal must begin with a call to "startVisit"
 * @author hill
 *
 */
public abstract class AbstractJavaElementVisitor {

	public void startVisiting(IMethod node) { 
		visit(node);
		HashSet<IJavaElement> todo = null;
		String src = "";
		try {
			src = node.getSource().replaceAll("\\n", " ");
		} catch (JavaModelException e) { e.printStackTrace(); }
		
		if (src.matches(".*new\\s+[A-Za-z0-9_]+\\([^)]*\\)\\s+\\{.*") || src.matches(".*class\\s+[A-Za-z0-9_]+.*?\\{.*")) {
			//System.out.println("!!Going to explore " + node.getElementName());
			MethodVisitor viz = new MethodVisitor(node);

			// Create AST
			ASTParser lParser = ASTParser.newParser(AST.JLS3); 
			lParser.setKind(ASTParser.K_COMPILATION_UNIT);
			lParser.setSource(node.getCompilationUnit()); // set source
			lParser.setResolveBindings(true); // set bindings

			CompilationUnit cu = (CompilationUnit) lParser.createAST(null);
			cu.accept(viz);
			todo = viz.getDeclared();
		}

		if (todo != null)
			for (IJavaElement j : todo)
				startVisiting(j);
	}

	public void startVisiting(IField node) { 
		visit(node);
	}
	
	public void startVisiting(IInitializer node) { 
		visit(node);
	}
	
	public void startVisiting(IType node) { 
		if (visit(node)) {
			try {
				for (IType t : node.getTypes())
					startVisiting(t);
			} catch (JavaModelException e) { e.printStackTrace(); }
			try {
				for (IMethod t : node.getMethods())
					startVisiting(t);
			} catch (JavaModelException e) { e.printStackTrace(); }
			try {
				for (IField t : node.getFields())
					startVisiting(t);
			} catch (JavaModelException e) { e.printStackTrace(); }
			try {
				for (IInitializer t : node.getInitializers())
					startVisiting(t);
			} catch (JavaModelException e) { e.printStackTrace(); }			
		}
	}
	
	public void startVisiting(IPackageDeclaration node) { 
		visit(node);
	}
	
	public void startVisiting(IImportDeclaration node) { 
		visit(node);
	}
	
	public void startVisiting(ICompilationUnit node) { 
		if (visit(node)) {
			try {
				for (IType t : node.getAllTypes())
					startVisiting(t);
			} catch (JavaModelException e) { e.printStackTrace(); }

			try {
				for (IPackageDeclaration p : node.getPackageDeclarations())
					startVisiting(p);
			} catch (JavaModelException e) { e.printStackTrace(); }

			try {
				for (IImportDeclaration i : node.getImports())
					startVisiting(i);
			} catch (JavaModelException e) { e.printStackTrace(); }
		}
	}
	
	// TODO untested & possibly excessive
	public void startVisiting(IClassFile node) {
		//if (node.isStructureKnown())
		IType t = node.getType();
		if (t != null)
			startVisiting(t);
	}
	
	public void startVisiting(IPackageFragment node) {
		if (visit(node)) {
			try {
				for (ICompilationUnit icu : node.getCompilationUnits())
					startVisiting(icu);
			} catch (JavaModelException e) { e.printStackTrace(); }
			/*try {
				for (IClassFile icf : node.getClassFiles())
					startVisiting(icf);
			} catch (JavaModelException e) { e.printStackTrace(); }*/
		}
	}
	
	public void startVisiting(IPackageFragmentRoot node) {
		if (visit(node)) {
			try {
				for (IJavaElement icu : node.getChildren()) {
					System.err.println(icu.getElementType());
					startVisiting((IPackageFragment)icu);
				}
			} catch (JavaModelException e) { e.printStackTrace(); }
			/*try {
				for (IClassFile icf : node.getClassFiles())
					startVisiting(icf);
			} catch (JavaModelException e) { e.printStackTrace(); }*/
		}
	}
	
	public void startVisiting(IJavaProject node) {
		if (visit(node)) {
			try {
				for (IPackageFragment ipf : node.getPackageFragments())
					startVisiting(ipf);
			} catch (JavaModelException e) { e.printStackTrace(); }
		}
	}
	
	protected abstract boolean visit(IMethod node);
	protected abstract boolean visit(IField node);
	protected abstract boolean visit(IInitializer node);
	protected abstract boolean visit(IType node);
	protected abstract boolean visit(IPackageDeclaration node);
	protected abstract boolean visit(IImportDeclaration node);
	protected abstract boolean visit(ICompilationUnit node);
	protected abstract boolean visit(IClassFile node);
	protected abstract boolean visit(IPackageFragment node);
	protected abstract boolean visit(IPackageFragmentRoot node);
	protected abstract boolean visit(IJavaProject node);
	
	public void startVisiting(IJavaElement node) {
		if (node instanceof IJavaProject)
			startVisiting((IJavaProject)node);
		else if (node instanceof IPackageFragment)
			startVisiting((IPackageFragment)node);
		else if (node instanceof ICompilationUnit)
			startVisiting((ICompilationUnit)node);
		else if (node instanceof IType)
			startVisiting((IType)node);
		else if (node instanceof IMethod)
			startVisiting((IMethod)node);
		else if (node instanceof IField)
			startVisiting((IField)node);
		else if (node instanceof IInitializer)
			startVisiting((IInitializer)node);
		else if (node instanceof IClassFile)
			startVisiting((IPackageFragment)node);
		else if (node instanceof IImportDeclaration)
			startVisiting((IImportDeclaration)node);
		else if (node instanceof IPackageDeclaration)
			startVisiting((IPackageDeclaration)node);
		else if (node instanceof IPackageFragmentRoot)
			startVisiting((IPackageFragmentRoot)node);
		else
			System.err.println("ERROR: visiting type" + node.getClass());
	}
	
	public class MethodVisitor extends ASTVisitor {
		private HashSet<IJavaElement> accessed = new HashSet<IJavaElement>();
		private IMethod imeth;
		
		private boolean in = false;
		
		public MethodVisitor(IMethod m) {
			super();
			imeth = m;
		}
		
		public boolean visit(MethodDeclaration node) {
			if (node.resolveBinding() != null) {
				IMethod im = (IMethod) node.resolveBinding().getJavaElement();
				if (im.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
					in = true;
				}
			}
			return in;
		}
		
		public void endVisit(MethodDeclaration node) {
			if (node.resolveBinding() != null) {
				IMethod im = (IMethod) node.resolveBinding().getJavaElement();
				if (im.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
					in = false;
				}
			}
		}
		
		public boolean visit(AnonymousClassDeclaration node) {
			if (in && node.resolveBinding() != null)
				accessed.add(node.resolveBinding().getJavaElement());
			return false;
		}
		
		public boolean visit(TypeDeclarationStatement node) {
			if (in && node.resolveBinding() != null)
				accessed.add(node.resolveBinding().getJavaElement());
			return false;
		}
		
		
		public HashSet<IJavaElement> getDeclared() { return accessed; }
	}

}
