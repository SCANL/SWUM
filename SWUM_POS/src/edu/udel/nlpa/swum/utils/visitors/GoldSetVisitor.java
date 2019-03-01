package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.udel.nlpa.swum.dora.DoraEdge;
import edu.udel.nlpa.swum.dora.DoraNode;
import edu.udel.nlpa.swum.dora.EdgeType;
import edu.udel.nlpa.swum.dora.NodeType;
import edu.udel.nlpa.swum.dora.ProgramGraph;
import edu.udel.nlpa.swum.explore.DependencyRelationship;
import edu.udel.nlpa.swum.explore.FieldInitializerVisitor;
import edu.udel.nlpa.swum.explore.ThemeVisitor;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.searchable.SearchableMethodDecl;
import edu.udel.nlpa.swum.scores.AbstractElementScore;
import edu.udel.nlpa.swum.scores.AbstractScoreData;
import edu.udel.nlpa.swum.scores.AbstractScoreData;
import edu.udel.nlpa.swum.scores.CalculateAllSearchScoreData;
import edu.udel.nlpa.swum.scores.CalculateSWUMSearchScoreData;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

public class GoldSetVisitor extends SWUMSearchVisitor {

	
	private HashMap<String, String> seeds = null;
	private HashMap<String, IMember> seedList = new HashMap<String, IMember>();

	private HashSet<String> analyzedList = new HashSet<String>();
	protected static final DependencyRelationship dependencies = new DependencyRelationship();
	private String fname;
	String pkg;
	
	private boolean debug = true;
	
	public GoldSetVisitor(String q, IJavaProject jp, PrintStream p) {
		super(q, jp, p);
		fname = q.replaceAll("\\s+", "_");
		pkg = jp.getPath().toPortableString().replaceFirst("/", "");
		//out.println("# " + pkg);
		//fname = "experiment/concerns/" + fname;
	}
	
	public GoldSetVisitor(String q, IJavaProject jp, PrintStream p, HashMap<String,String> s) {
		this(q, jp, p);
		seeds = s;
	}
	
	@Override
	protected boolean visit(IField node) {
		String sig2 = ContextBuilder.getSignature(node);
		
		if (seeds.containsKey(sig2)) {
			seedList.put(DoraNode.getHandle(node), node);
		}
		return true;
	}
	
	@Override
	protected boolean visit(IMethod node) {
		String sig2 = ContextBuilder.getSignature(node);
		
		if (seeds.containsKey(sig2)) {
			seedList.put(DoraNode.getHandle(node), node);
		}
		
		return true;
	}
	
	public void exploreSeedSet() {
		ProgramGraph pg = new ProgramGraph();
		
		exploreSet(pg, seedList);
		
		for (DoraNode sd : pg.getNodes().values()) {
			if (sd.getElement() instanceof IField) {
				doubleCheckAccessors(sd, pg);
			} else {
				doubleCheckHasOtherLinks(sd, pg);
			}
		}
			
		pg.display(fname);
	}
	

	private void doubleCheckAccessors(DoraNode sd, ProgramGraph pg) {
		Set<IMember> c = dependencies.getProjectReferences(sd.getElement(), _proj);
		for (IMember m : c) {
			if (pg.containsNode(m)) { // in concern
				// add edge
				DoraNode d = pg.getNode(m);
				if (pg.addEdge(d, sd, EdgeType.PLAIN, 0d))
					out.println("++ " + ContextBuilder.getSignature(d.getElement()) + " -> " + ContextBuilder.getSignature(sd.getElement()));
			}
		}
	}

	private boolean doubleCheckHasOtherLinks(DoraNode sd, ProgramGraph pg) {
		boolean hasLink = false;
		Set<IMethod> c = dependencies.getCHCallersInProject((IMethod)sd.getElement(), pkg);
		for (IMethod m : c) {
			if (pg.containsNode(m)) { // in concern
				// add edge
				DoraNode d = pg.getNode(m);
				if (pg.addEdge(d, sd, EdgeType.PLAIN, 0d))
					out.println("++ " + ContextBuilder.getSignature(d.getElement()) + " -> " + ContextBuilder.getSignature(sd.getElement()));
				hasLink = true;
			}
		}
		
		c = dependencies.getCallees((IMethod)sd.getElement());
		for (IMethod m : c) {
			if (pg.containsNode(m)) { // in concern
				// add edge
				DoraNode d = pg.getNode(m);
				if (pg.addEdge(sd, d, EdgeType.PLAIN, 0d))
					out.println("++ " + ContextBuilder.getSignature(sd.getElement()) + " -> " + ContextBuilder.getSignature(d.getElement()));
				hasLink = true;
			}
		}
		
		Set<IField> f = dependencies.getFieldUses(sd.getElement());
		for (IField m : f) {
			if (pg.containsNode(m)) { // in concern
				// add edge
				DoraNode d = pg.getNode(m);
				pg.addEdge(sd, d, EdgeType.PLAIN, 0d);
				hasLink = true;
			}
		}
		
		return hasLink;
	}

	private void exploreSet(ProgramGraph pg, HashMap<String, IMember> list) {
		
		// init work list with seed set
		for (IMember sd : list.values()) {
			DoraNode t = createOrGetNode(pg, sd, 0);
			// explore seeds in all directions
			t.setGoBackward(true);
			t.setGoForward(true);
			// Always explore seeds and add to nodes
			t.setType(getNodeType(t));
			//out.println("\n--" + t.getSignature() + " " + t.getScore().getF());
		}
		
	}
	
	private NodeType getNodeType(DoraNode d) {
		String s = ContextBuilder.getSignature(d.getElement());
		// don't need to worry about fields -- set type takes care of
		NodeType t = NodeType.NO_TYPE;
		
		if (seeds.containsKey(s)) {
			s = seeds.get(s);
			if (s.matches("m"))
				t = NodeType.M;
			else if (s.matches("s"))
				t = NodeType.S;
			else if (s.matches("l"))
				t = NodeType.L;
			else if (s.matches("c"))
				t = NodeType.C;
			else if (s.matches("e"))
				t = NodeType.E;
			else if (s.matches("p"))
				t = NodeType.P;
			else if (s.matches("k"))
				t = NodeType.K;
			else if (s.matches("i"))
				t = NodeType.I;
		}
		return t;
	}

	private DoraNode createOrGetNode(ProgramGraph pg, IMember sd, int e) {
		String handle = DoraNode.getHandle(sd);
		DoraNode t;
		if (pg.containsNode(handle)) {
			t =  pg.getNode(handle);
			if (t.getEdgesAway() > e)
				t.setEdgesAway(e);
		} else {
			t = new DoraNode(sd, e);//, gatherScores(sd));
			pg.addNode(t);
		}
			
		return t;
	}
	
	public String toString() {
		return super.swum.getContains().keySet().toString();
	}
}
