package edu.udel.nlpa.swum.explore;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;

import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.nodes.IEquivalenceNode;
import edu.udel.nlpa.swum.nodes.IVariableDecl;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.base.EquivalenceNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableFieldDecl;
import edu.udel.nlpa.swum.nodes.searchable.SearchableMethodDecl;
import edu.udel.nlpa.swum.nodes.searchable.SearchableTypeNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableVariableDecl;
import edu.udel.nlpa.swum.scores.AbstractElementScore;
import edu.udel.nlpa.swum.stem.Stemmer;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.tagger.UnigramTagger;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;
import edu.udel.nlpa.swum.utils.context.FieldContext;
import edu.udel.nlpa.swum.utils.context.MethodContext;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;
import edu.udel.nlpa.swum.utils.visitors.SWUMElementVisitor;

public class FieldInitializerVisitor extends ASTVisitor {
	private IMethod imeth; // visit ast
	private IField field; // target swum
	private boolean in_caller = false;
	private boolean in_init = false;
	
	private Set<IMethod> init = new HashSet<IMethod>(); 

	public FieldInitializerVisitor(IField f, IMethod caller) {
		super();
		imeth = caller;
		field = f;
	}

	public boolean visit(MethodDeclaration node) {
		if (node.resolveBinding() != null) {
			IMethod im = (IMethod) node.resolveBinding().getJavaElement();
			if (im.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
				in_caller = true;
			}
		}
		return in_caller;
	}

	public void endVisit(MethodDeclaration node) {
		if (node.resolveBinding() != null) {
			IMethod im = (IMethod) node.resolveBinding().getJavaElement();
			if (im.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
				in_caller = false;
			}
		}
	}
		
	public boolean visit(Assignment node) {
		if (in_caller) {
			
			if (node.getLeftHandSide().toString().matches("\\s*" + field.getElementName() + "\\s*")) {
				in_init = true;
				node.getRightHandSide().accept(this);
				in_init = false;
			}
		}
		return false;
	}
	
	public boolean visit(MethodInvocation node) {
		if (in_caller && in_init) {
			IMethod l = (IMethod) node.resolveMethodBinding().getJavaElement();
			if (l != null)
				init.add(l);
		}	
		return false;
	}
	
	public boolean visit(ClassInstanceCreation node) {
		if (in_caller && in_init) {
			IMethod l = (IMethod) node.resolveConstructorBinding().getJavaElement();
			if (l != null)
				init.add(l);
		}	
		return false;
	}
	
	public Set<IMethod> getInitializers() { return init; }

}
