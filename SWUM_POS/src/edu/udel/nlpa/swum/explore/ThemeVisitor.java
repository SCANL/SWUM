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

public class ThemeVisitor extends ASTVisitor {
	private IMethod imeth; // visit ast
	private IMethod callee; // target swum
	private boolean in_caller = false;
	
	private Set<int[]> f = new HashSet<int[]>(); // which formal indices are equivalent to the theme
	
	private ArgumentVisitor args;

//	public ThemeVisitor(IMethod caller, IMethod c) {
//		super();
//		MethodContext mc = ContextBuilder.buildMethodContext(((IMethod)c));
//		SearchableMethodDecl md = new SearchableMethodDecl(c.getElementName(), mc, AbstractElementScore.stemmer);
//		RuleIndicator ri = SWUMElementVisitor.swum.applyRules(md);
//		init(caller, c, md);
//	}
	
	public ThemeVisitor(IMethod caller, IMethod c) {
		super();
		init(caller, c);
	}

	private void init(IMethod caller, IMethod c) {
		imeth = caller;
		callee = c;
		args = new ArgumentVisitor(AbstractElementScore.stemmer, AbstractElementScore.idSplitter,
				SWUMElementVisitor.swum, new UnigramTagger(AbstractElementScore.getPos()));
		//initializeF(md);
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
		
	public boolean visit(MethodInvocation node) {
		if (in_caller) {
			IMethod l = (IMethod) node.resolveMethodBinding().getJavaElement();
			if (callee.getHandleIdentifier().equals(l.getHandleIdentifier())) {
				//args.resetTheme(); // just to be safe for reuse
				// we've found what we're looking for -- defer to another visitor
				node.accept(args);
			}
		}
		return false;
	}
	
	public Set<Node> getActualTheme() { return args.getActualTheme(); }

	public class ArgumentVisitor extends ASTVisitor {
		private HashSet<Node> themeWords = new HashSet<Node>();
		public HashSet<Node> getActualTheme() { return themeWords; }
		public void resetTheme() { themeWords = new HashSet<Node>(); }
		private Stemmer stemmer;
		private IdentifierSplitter idSplitter;
		protected ISWUMBuilder swum;
		private ITagger tagger;
		
		public ArgumentVisitor(Stemmer stemmer2,
				IdentifierSplitter idSplitter2, ISWUMBuilder swum2,
				UnigramTagger unigramTagger) {
			stemmer = stemmer2;
			idSplitter = idSplitter2;
			swum = swum2;
			tagger = unigramTagger;
		}
		
		private Set<int[]> idFinSwum(Node t) {
			Set<int[]> f = new HashSet<int[]>();
			if (t instanceof IEquivalenceNode) {
				for (Node n : ((IEquivalenceNode) t).getEquivalentNodes()) {
					if (n instanceof IVariableDecl) {
						f.add(new int[]{((IVariableDecl)n).getPosition()});
					}
				}
			} else if (t instanceof IVariableDecl) {
				f.add(new int[]{((IVariableDecl)t).getPosition()});
			}
			return f;
		}
		
		
		
		// To do add visitors for all the different types
		public boolean visit(SimpleName node) {
			if (node.resolveBinding() != null) {
				IJavaElement m = node.resolveBinding().getJavaElement();
				if (m != null && m.getHandleIdentifier() != null &&
						!m.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
					String name = node.getIdentifier();
					String t = "";
					
					if (m instanceof IField) {
						FieldContext fc = ContextBuilder.buildFieldContext((IField)m);
						SearchableFieldDecl fd = new SearchableFieldDecl(name, fc, stemmer);
						swum.applyRules(fd);
						
						themeWords.add(fd);
					} else if (m instanceof ILocalVariable) {
						SearchableVariableDecl svd = new SearchableVariableDecl(name, ContextBuilder.getVariableType(((ILocalVariable)m).getTypeSignature()), stemmer,
								idSplitter, tagger);
						themeWords.add(svd);
//					} else if (m instanceof IMethod) {
//						MethodContext mc = ContextBuilder.buildMethodContext(((IMethod)m));
//						
//						// Run SWUM
//						SearchableMethodDecl md = new SearchableMethodDecl(name, mc, stemmer);
//						RuleIndicator ri = swum.applyRules(md);
//
//						themeWords.add(md.getTheme()); // TODO -- nest
					} else if (m instanceof IType || m instanceof ITypeParameter) {
						SearchableTypeNode tn = new SearchableTypeNode(name, stemmer, idSplitter, tagger);
						themeWords.add(tn);
					}
				}
			}
			
			return false;
		}
		
		public boolean visit(MethodInvocation node) {

			IJavaElement m = node.resolveMethodBinding().getJavaElement();
			if (m != null && m.getHandleIdentifier() != null) {
				String name = node.getName().getIdentifier();

				MethodContext mc = ContextBuilder.buildMethodContext(((IMethod)m));

				// Run SWUM
				SearchableMethodDecl md = new SearchableMethodDecl(name, mc, stemmer);
				RuleIndicator ri = swum.applyRules(md);
				
				// figure out if we need to check class or formals for theme

				
				if (md.getThemeLocation() == Location.ONCLASS) {
					if (node.getExpression() != null)
						node.getExpression().accept(this);
				} else if (md.getTheme() instanceof IEquivalenceNode ||
						md.getTheme() instanceof IVariableDecl) { // check if it's formal
					// if formals equiv, check actuals
					if (node.arguments() != null) {
						Set<int[]>f = idFinSwum(md.getTheme());
						List<Expression> a = node.arguments();
						for (int[] i : f) {
							if (a.size() > i[0]) // error check
								(a.get(i[0])).accept(this);
						}
					}
				} else if (md.getThemeLocation() == Location.NAME)
					themeWords.add(md.getTheme());
			}
			
			return false;
		}

	}
}
