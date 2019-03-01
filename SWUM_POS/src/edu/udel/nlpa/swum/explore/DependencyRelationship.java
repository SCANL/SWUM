package edu.udel.nlpa.swum.explore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;


public class DependencyRelationship {
	
	protected static final CallingRelationship callHierarchy = new EclipseCallHierarchy();
	
	/**
	 *  Gets field references and method callers (up)
	 */
	public HashSet<IMember> getProjectReferences(IMember m, IJavaProject proj) {
		// Get the search pattern
	    SearchPattern pattern = SearchPattern.createPattern(m, IJavaSearchConstants.REFERENCES);

	    // Get the search scope
	    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {proj});

	    // Get the search requestor
	    ReferenceMatchCollector requestor = new ReferenceMatchCollector();

	    // Search
	    SearchEngine searchEngine = new SearchEngine();
	    try {
			searchEngine.search(pattern, 
					new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, 
					scope, requestor, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		/*if (m instanceof IMethod &&
				Activator.getPreference(PreferenceConstants.P_TYPES)) {
			HashMap<IMember,EdgeType> all = requestor.getResults();
			all.putAll(getMemberInterfaces(m));
			return all;
		} else {*/
			return requestor.getResults();
		//}
	}
	
	/**
	 *  Gets method callees (down)
	 *  ONLY for a method!!!!
	 */
	public HashMap<IMethod, int[]> getNumberedCallees(IMethod m) {
		return callHierarchy.getCallees(m);
	}
	
	public Set<IMethod> getCallees(IMethod m) {
		return callHierarchy.getCallees(m).keySet();
	}
	
	public HashSet<IMethod> getLastCallees(IMethod m) {
		String src = null;
		try {
			if (m.getCompilationUnit() != null)
				src = m.getCompilationUnit().getSource();
		} catch (JavaModelException e) { e.printStackTrace(); }
		if (src != null) {
			LastCallVisitor viz = new LastCallVisitor(m);

			// Create AST
			ASTParser lParser = ASTParser.newParser(AST.JLS3); 
			lParser.setKind(ASTParser.K_COMPILATION_UNIT);
			lParser.setSource(m.getCompilationUnit()); // set source
			lParser.setResolveBindings(true); // set bindings

			CompilationUnit cu = (CompilationUnit) lParser.createAST(null);
			if (cu != null)
				cu.accept(viz);

			return viz.getLast();
		} else {
			HashSet<IMethod> last = new HashSet<IMethod>();
//			HashMap<IMethod, int[]> c = callHierarchy.getCallees(m);
//			int size = c.size() - 1;
//			// last ditch effort
//			for (IMethod im : c.keySet())
//				if (c.get(m)[0] >= size)
//					last.add(m);
			
			return last;
		}
	}
	
	public HashSet<IMember> getCallers(IMethod m) {
		return getProjectReferences(m, m.getJavaProject());
	}
	
	public HashMap<IMethod, int[]> getNumberedCallers(IMethod m) {
		return callHierarchy.getCallers(m);
	}
	
	public Set<IMethod> getCHCallers(IMethod m) {
		return callHierarchy.getCallers(m).keySet();
	}
	
	public Set<IMethod> getCHCallersInProject(IMethod m, IMember pkg) {
		return getCHCallersInProject(m, getPathLocation(pkg));
	}
	
	public Set<IMethod> getCHCallersInProject(IMethod m) {
		return getCHCallersInProject(m, getPathLocation(m));
	}
	
	public Set<IMethod> getCHCallersInProject(IMethod m, String pkg) {
		Set<IMethod> c = new HashSet<IMethod>();
		
		for (IMethod n : callHierarchy.getCallers(m).keySet()) {
			if (pkgOK(pkg, getPathLocation(n)))
				c.add(n);
		}
		return c;
	}
	
	public static String getPathLocation(IMember node) {
		String pkg = node.getPath().toPortableString();
		if (pkg.endsWith(".java")) {
			pkg = pkg.replaceAll("^/([A-Za-z0-9_]+)/.*", "$1");
		} else {
			pkg = "lib";
		}
		return pkg;
	}
	
	private boolean pkgOK(String pkg_src, String pkg_dest) {
		return pkg_src.matches(pkg_dest) || pkg_dest.matches("lib");
	}

	
	/**
	 *  Gets field uses
	 *  ONLY for a method!!!!
	 */
	public HashSet<IMember> getLocalFieldUses(IMember m) {
		HashSet<IMember> all = new HashSet<IMember>();
		if (m instanceof IMethod) {

			// First step, get fields declared in compilation unit
			HashSet<IMember> fields = getDeclaredFields(m);

			LocalFieldVisitor viz = new LocalFieldVisitor(m, fields);

			// Create AST
			ASTParser lParser = ASTParser.newParser(AST.JLS3); 
			lParser.setKind(ASTParser.K_COMPILATION_UNIT);
			lParser.setSource(m.getCompilationUnit()); // set source
			lParser.setResolveBindings(true); // set bindings

			CompilationUnit cu = (CompilationUnit) lParser.createAST(null);
			cu.accept(viz);
			
			return viz.getAccessedFields();
			
			//if (Activator.getPreference(PreferenceConstants.P_TYPES))
				//all.putAll(this.getMemberInterfaces(m));
				//all.putAll(this.getMemberImplementors(m));
		}
		return all;
	}
	
	public HashSet<IJavaElement> getVarUses(IMember m) {
		HashSet<IJavaElement> all = new HashSet<IJavaElement>();
		if (m instanceof IMethod) {

			VarVisitor viz = new VarVisitor(m);

			// Create AST
			ASTParser lParser = ASTParser.newParser(AST.JLS3); 
			lParser.setKind(ASTParser.K_COMPILATION_UNIT);
			lParser.setSource(m.getCompilationUnit()); // set source
			lParser.setResolveBindings(true); // set bindings

			CompilationUnit cu = (CompilationUnit) lParser.createAST(null);
			cu.accept(viz);
			
			return viz.getAccessedFields();
		}
		return all;
	}
	
	public HashSet<IJavaElement> getJavaElementUses(IMember m) {
		HashSet<IJavaElement> all = new HashSet<IJavaElement>();
		if (m instanceof IMethod) {
			String src = null;
			try {
				if (m.getCompilationUnit() != null)
					src = m.getCompilationUnit().getSource();
			} catch (JavaModelException e) { e.printStackTrace(); }
			if (src != null) {
				JEVisitor viz = new JEVisitor(m);

				// Create AST
				ASTParser lParser = ASTParser.newParser(AST.JLS3); 
				lParser.setKind(ASTParser.K_COMPILATION_UNIT);
				lParser.setSource(m.getCompilationUnit()); // set source
				lParser.setResolveBindings(true); // set bindings

				CompilationUnit cu = (CompilationUnit) lParser.createAST(null);
				cu.accept(viz);

				return viz.getAccessed();
			}
		}
		return all;
	}
	
	public HashSet<IField> getFieldUses(IMember m) {
		HashSet<IField> all = new HashSet<IField>();
		if (m instanceof IMethod) {
			String src = null;
			try {
				if (m.getCompilationUnit() != null)
					src = m.getCompilationUnit().getSource();
			} catch (JavaModelException e) { e.printStackTrace(); }
			if (src != null) {
				FieldVisitor viz = new FieldVisitor(m);

				// Create AST
				ASTParser lParser = ASTParser.newParser(AST.JLS3); 
				lParser.setKind(ASTParser.K_COMPILATION_UNIT);
				lParser.setSource(m.getCompilationUnit()); // set source
				lParser.setResolveBindings(true); // set bindings

				CompilationUnit cu = (CompilationUnit) lParser.createAST(null);
				cu.accept(viz);

				return viz.getAccessed();
			}
		}
		return all;
	}
	
	/**
	 *  interface & overidden methods (up)
	 */
	public HashSet<IMember> getMemberInterfaces(IMember m) {
		HashSet<IMember> results = new HashSet<IMember>();
		
		if (m instanceof IMethod) {
			//System.out.println(m);
			// Get the search requestor
		    ReferenceMatchCollector requestor = new ReferenceMatchCollector();
			
		    // Get the search pattern
		    SearchPattern pattern_fields = 
		    	SearchPattern.createPattern(m, 
		    			IJavaSearchConstants.DECLARATIONS | 
		    			IJavaSearchConstants.IGNORE_DECLARING_TYPE | 
		    			IJavaSearchConstants.IGNORE_RETURN_TYPE
		    			//IJavaSearchConstants.DECLARATIONS -- only one edge away?
		    			);

		    // Get the search scope

		    // Search
		    SearchEngine searchEngine = new SearchEngine();
		    try {
		    	IJavaSearchScope scope = 
		    		SearchEngine.createHierarchyScope(m.getDeclaringType());
				searchEngine.search(pattern_fields, 
						new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, 
						scope, requestor, null);
				
			} catch (Exception e) { e.printStackTrace();}
			
			HashSet<IMember> temp = requestor.getResults();
			for (IMember t : temp) {
				if (!t.getHandleIdentifier().equals(m.getHandleIdentifier())) {
					results.add(t);
				}
			}
		}
		return results;
	}
	
	/**
	 *  overriding & implementing methods (down)
	 */
	/*public HashMap<IMember,EdgeType> getMemberImplementors(IMember m) {
		HashMap<IMember,EdgeType> results = new HashMap<IMember,EdgeType>();
		
		if (m instanceof IMethod) {
			
			// Get the search requestor
		    ReferenceMatchCollector requestor = new ReferenceMatchCollector(EdgeType.IMPLEMENT);
			
		    // Get the search pattern
		    SearchPattern pattern_fields = 
		    	SearchPattern.createPattern(m, IJavaSearchConstants.DECLARATIONS);

		    // Get the search scope

		    // Search
		    SearchEngine searchEngine = new SearchEngine();
		    try {
		    	IJavaSearchScope scope = SearchEngine.createHierarchyScope(m.getDeclaringType());
				searchEngine.search(pattern_fields, 
						new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, 
						scope, requestor, null);
				
			} catch (Exception e) { e.printStackTrace();}
			HashMap<IMember,EdgeType> temp = requestor.getResults();
			for (IMember t : temp.keySet()) {
				if (!t.getHandleIdentifier().equals(m.getHandleIdentifier()))
					results.put(t, temp.get(t));
			}
		}
		
		return results;
	}*/

	private HashSet<IMember> getDeclaredFields(IMember m) {
		// Get the search requestor
	    ReferenceMatchCollector requestor = new ReferenceMatchCollector();
		
	    // Get the search pattern
	    SearchPattern pattern_fields = 
	    	SearchPattern.createPattern("*", IJavaSearchConstants.FIELD, 
	    			IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);

	    // Get the search scope
	    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {m.getCompilationUnit()});

	    // Search
	    SearchEngine searchEngine = new SearchEngine();
	    try {
			searchEngine.search(pattern_fields, 
					new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, 
					scope, requestor, null);
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return requestor.getResults();
	}

	
	// TODO: do we want inaccurate matches?
	public class ReferenceMatchCollector extends SearchRequestor {
		
		private HashSet<IMember> results = new HashSet<IMember>();
		
		public ReferenceMatchCollector() {
			super();
		}

		public HashSet<IMember> getResults() { return results; }
		
		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
				if (match.getElement() instanceof IField || 
						match.getElement() instanceof IMethod)
					results.add((IMember)match.getElement());
				/*else // initializers
					System.err.println("Ooops: " + match.getElement().getClass());*/
			} /*else 
				System.err.println("Inaccurate: " + match.getElement());*/
				
		}
		
	}
	
	public class LocalFieldVisitor extends ASTVisitor {
		private HashMap<String,IMember> fields = new HashMap<String,IMember>();
		private HashSet<IMember> accessed = new HashSet<IMember>();
		private IMember imeth;
		
		private boolean in = false;
		
		public LocalFieldVisitor(IMember m, HashSet<IMember> f) {
			super();
			imeth = m;
			
			for (IMember im : f)
				fields.put(im.getHandleIdentifier(), im);
		}
		
		public boolean visit(MethodDeclaration node) {
			if (node.resolveBinding() != null) {
				IMethod im = (IMethod) node.resolveBinding().getJavaElement();
				if (im.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
					in = true;
					return true;
				}
			}
			return false;
		}
		
		public void endVisit(MethodDeclaration node) {
			if (node.resolveBinding() != null) {
				IMethod im = (IMethod) node.resolveBinding().getJavaElement();
				if (im.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
					in = false;
				}
			}
		}
		
		// TODO only works for bound fields
		public boolean visit(SimpleName node) {
			if (in) {
				if (node.resolveBinding() != null) {
					IJavaElement je = node.resolveBinding().getJavaElement();
					if (je != null && je.getHandleIdentifier() != null &&
							fields.containsKey(je.getHandleIdentifier()))
						accessed.add(fields.get(je.getHandleIdentifier()));
				}
			}
			return false;
		}
		
		public HashSet<IMember> getAccessedFields() { return accessed; }
	}
	
	public class VarVisitor extends ASTVisitor {
		private HashSet<IJavaElement> accessed = new HashSet<IJavaElement>();
		private IMember imeth;
		
		private boolean in = false;
		
		public VarVisitor(IMember m) {
			super();
			imeth = m;
		}
		
		public boolean visit(MethodDeclaration node) {
			if (node.resolveBinding() != null) {
				IMethod im = (IMethod) node.resolveBinding().getJavaElement();
				if (im.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
					in = true;
					return true;
				}
			}
			return false;
		}
		
		public void endVisit(MethodDeclaration node) {
			if (node.resolveBinding() != null) {
				IMethod im = (IMethod) node.resolveBinding().getJavaElement();
				if (im.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
					in = false;
				}
			}
		}
		
		// TODO only works for bound fields
		public boolean visit(SimpleName node) {
			if (in) {
				if (node.resolveBinding() != null) {
					IJavaElement je = node.resolveBinding().getJavaElement();
					if (je != null && je.getHandleIdentifier() != null &&
							(je instanceof IField || je instanceof ILocalVariable))
						accessed.add(je);
				}
			}
			return false;
		}
		
		public HashSet<IJavaElement> getAccessedFields() { return accessed; }
	}
	
	public class JEVisitor extends ASTVisitor {
		private HashSet<IJavaElement> accessed = new HashSet<IJavaElement>();
		private IMember imeth;
		
		private boolean in = false;
		
		public JEVisitor(IMember m) {
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
		
		// TODO IInitializers?
		public boolean visit(SimpleName node) {
			if (in) {
				if (node.resolveBinding() != null) {
					IJavaElement je = node.resolveBinding().getJavaElement();
					if (je != null && je.getHandleIdentifier() != null &&
							!je.getHandleIdentifier().equals(imeth.getHandleIdentifier()))
						accessed.add(je);
				}
			}
			return false;
		}
		
		public HashSet<IJavaElement> getAccessed() { return accessed; }
	}

	public class FieldVisitor extends ASTVisitor {
		private HashSet<IField> accessed = new HashSet<IField>();
		private IMember imeth;
		
		private boolean in = false;
		
		public FieldVisitor(IMember m) {
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
		
		public boolean visit(SimpleName node) {
			if (in) {
				if (node.resolveBinding() != null) {
					IJavaElement je = node.resolveBinding().getJavaElement();
					if (je != null && je.getHandleIdentifier() != null &&
							je instanceof IField)
						accessed.add((IField)je);
				}
			}
			return false;
		}
		
		public HashSet<IField> getAccessed() { return accessed; }
	}

}
