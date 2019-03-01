package edu.udel.nlpa.swum.dora;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;

//import edu.udel.nlpa.swum.dora.view.GrappaDrawer;
import edu.udel.nlpa.swum.utils.LibFileLoader;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

/**
 * One edge call graph with no scoring or query filtering
 * @author hill
 *
 */
public class ProgramGraph {
	//protected Collection<IMember> seed = new HashSet<IMember>();
	protected Map<String, DoraNode> nodes = new HashMap<String, DoraNode>();
	protected Map<String, DoraEdge> edges = new HashMap<String, DoraEdge>();
	protected static final String tempDirString = 
		LibFileLoader.getInstance().getLead("out/");
	
//	public ProgramGraph(Collection<IMember> s) {
//		seed = s;
//	}
//	
//	public ProgramGraph(IMember s) {
//		addSeed(s);
//	}
//
//	public void addSeed(IMember s) {
//		seed.add(s);
//	}
	
	//abstract public void construct();
	
	public void addNode(DoraNode f) {
		nodes.put(f.toString(), f);
	}
	
	public void removeNode(DoraNode f) {
		nodes.remove(f.toString());
	}
	
	public DoraNode getNode(String f) {
		return nodes.get(f);
	}
	
	public DoraNode getNode(IMember m) {
		return nodes.get(DoraNode.getHandle(m));
	}
	
	public boolean containsNode(String t) {
		return nodes.containsKey(t);
	}
	
	public boolean containsNode(IMember m) {
		return nodes.containsKey(DoraNode.getHandle(m));
	}
	
	public boolean containsNode(DoraNode n) {
		return nodes.containsKey(n.toString());
	}
	
	public boolean addEdge(DoraNode f, DoraNode t) {
		return addEdge(f, t, EdgeType.PLAIN);
	}
	
	public boolean addEdge(DoraNode f, DoraNode t, EdgeType type) {
		DoraEdge e = new DoraEdge(f, t, type);
		return !updateStructures(f, t, e);
	}
	
	public boolean addEdge(DoraNode f, DoraNode t, EdgeType type, double score) {
		DoraEdge e = new DoraEdge(f, t, type, score);
		return !updateStructures(f, t, e);
	}
	
	public boolean addEdge(DoraNode f, DoraNode t, EdgeType type, double score, boolean written, boolean read) {
		DoraEdge e = new DoraEdge(f, t, type, score);
		e.setWritten(written);
		e.setRead(read);
		return !updateStructures(f, t, e);
	}
	
	public void removeEdge(DoraEdge f) {
		nodes.remove(f.toString());
	}

	private boolean updateStructures(DoraNode f, DoraNode t, DoraEdge e) {
		boolean update = false;
		if (edges.containsKey(e.toString())) {
			DoraEdge orig = edges.get(e.toString());
			if (e.getScore() > orig.getScore())
				orig.setScore(e.getScore());
			if (orig.getEdgeType() != EdgeType.CONNECT && orig.getEdgeType() != EdgeType.INITIALIZER)
				orig.setEdgeType(e.getEdgeType());
			orig.setWritten(e.isWritten() || orig.isWritten());
			orig.setRead(e.isRead() || orig.isRead());
			e = orig;
			update = true;
		} else {
			edges.put(e.toString(), e);
		}
		nodes.put(t.toString(), t);
		nodes.put(f.toString(), f);
		f.addOutEdge(e);
		t.addInEdge(e);
		return update;
	}
	
	public void addDoubleEdge(DoraNode f, DoraNode t, EdgeType type, double score) {
		addEdge(f, t, type, score);
		addEdge(t, f, type, score);
	}
	
	public void addDoubleEdge(DoraNode f, DoraNode t, EdgeType type) {
		addEdge(f, t, type);
		addEdge(t, f, type);
	}


	/**
	 * TODO use GEF ZEST for visualization
	 */
	public void display(String fname) {
		outputDisplay(tempDirString + fname +".dot");
		outputSearchNodes(tempDirString + fname +".out");
	}
	
	public void outputDisplay(String fname) {
		FileOutputStream out = null;
		PrintStream p = null;
		try {		
			out=new FileOutputStream(fname);
			p=new PrintStream( out );

			// print out dot file
			//System.out.println("Outputting to " + fname);
			
			// Dot file header
			p.println("digraph rw { \n");
			p.println("#\tgraph[page=\"8.5,11\",size=\"7.5,10\",ratio=fill,center=1];\n");
			
			// Node types
			drawNodes(p);
			
			p.println("\n");
			
			// Edges
			drawEdges(p);
			
			// Dot file footer
			p.println("\n}");

			p.flush();
			p.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// display to user
		//new GrappaDrawer(fname, tempDirString + "rw.grappa", getRWNodes());
	}
	
	public void outputSearchNodes(String fname) {
		FileOutputStream out = null;
		PrintStream p = null;
		try {		
			out=new FileOutputStream(fname);
			p=new PrintStream( out );

			for (DoraNode n : nodes.values()) {
				p.println(ContextBuilder.getSignature(n.getElement()));
				//p.println( ContextBuilder.getEvalSignature(n.getElement(), true) );
			}
			
			p.flush();
			p.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void drawEdges(PrintStream p) {
		for(DoraEdge e : edges.values())
			p.println(e.toDotString());		
	}

	private void drawNodes(PrintStream p) {
		for (DoraNode n : nodes.values()) {
			p.println(n.getDotDeclaration());
		}
	}

	public Map<String, DoraNode> getNodes() {
		return nodes;
	}

	public Map<String, DoraEdge> getEdges() {
		return edges;
	}
	
//	private HashMap<String, IMember> getRWNodes() {
//		HashMap<String, IMember> n = new HashMap<String, IMember>();
//		for (DoraNode node : nodes.values())
//				n.put(String.valueOf(node.getId()), node.getElement());
//		return n;
//	}
	
	
}
