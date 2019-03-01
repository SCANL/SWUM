package edu.udel.nlpa.swum.dora;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.Signature;

import edu.udel.nlpa.swum.scores.AbstractScoreData;
import edu.udel.nlpa.swum.scores.CalculateAllSearchScoreData;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;

/**
 * A node is created for every IMember analyzed, whether or not they are added
 * to the relevant window
 * @author hill
 *
 */
public class DoraNode {
	protected IMember element = null;
	protected NodeType type = NodeType.NO_TYPE;
	protected int id = 0; // for drawing dotty graphs
	protected static int counter = 1;
	protected AbstractScoreData score = null;
	protected int edgesAway = 0;
	protected double pathScore = 1d;
	private static final DecimalFormat df = new DecimalFormat("0.0000");
	protected Map<String, DoraEdge> outEdges = new HashMap<String, DoraEdge>();
	protected Map<String, DoraEdge> inEdges = new HashMap<String, DoraEdge>();
	
	protected boolean goForward = false;
	protected boolean goBackward = false;
	protected boolean wentForward = false;
	protected boolean wentBackward = false;
	protected boolean analyzedFamily = false;
	
	public DoraNode(IMember im) {
		element = im;
		id = counter;
		counter++;
	}
	
	public DoraNode(IMember im, NodeType t) {
		this(im);
		setType(t);
	}
	
	public DoraNode(IMember im, int e) {
		this(im);
		edgesAway = e;
	}
	
	public DoraNode(IMember im, int e, AbstractScoreData s) {
		this(im, e);
		score = s;
	}
	
	public DoraNode(IMember im, int e, NodeType nt) {
		this(im, e);
		setType(nt);
	}
	
	public DoraNode(IMember im, int e, AbstractScoreData s, NodeType nt) {
		this(im, e, nt);
		score = s;
	}
	
	public DoraNode(IMember im, int e, double p,AbstractScoreData s, NodeType nt) {
		this(im, e, s, nt);
		setPathScore(p);
	}

	public NodeType getType() {
		return type;
	}

	/** only upgrade -- don't change from seed */
//	public void setType(NodeType type) {
//		if (!this.type.equals(NodeType.SEED))
//			this.type = type;
//	}
	
	public void setType(NodeType t) {
		if (t.getRelevance() < type.getRelevance())
			return;
		
		type = t;
		
		if (getElement() instanceof IField) {
			if (t.equals(NodeType.NO_TYPE))
				type = NodeType.FIELD;
			else if (t.equals(NodeType.E) || t.equals(NodeType.P)
					|| t.equals(NodeType.K))
				type = NodeType.E_FIELD;
			else if (t.equals(NodeType.LS) || t.equals(NodeType.L))
				type = NodeType.LIBRARY_FIELD;
			else if (t.equals(NodeType.M))
				type = NodeType.RELEVANT_FIELD;
			else if (t.equals(NodeType.C))
				type = NodeType.C_FIELD;
			else if (t.equals(NodeType.S))
				type = NodeType.S_FIELD;
			else if (t.equals(NodeType.SEED))
				type = NodeType.SEED_FIELD;
		}
	}

	public IMember getElement() {
		return element;
	}
	
	public int getId() {
		return id;
	}
	
	public String toString() {
		return getHandle(element);
	}
	
	public String getDotDeclaration() {
		if (score != null)
			return type.drawNode() + 
			id + " [label=\"" + getDotSignature(element)
			+ "\\n" + df.format(score.getScore()) + " (" + type.toString() + ")\"]\n";
		return type.drawNode() + id + " [label=\"" + getDotSignature(element) + "\\n(" + type.toString() + ")\"]\n";
	}

	public AbstractScoreData getScore() {
		return score;
	}

	public void setScore(AbstractScoreData score) {
		this.score = score;
	}

	public int getEdgesAway() {
		return edgesAway;
	}

	// only update if e less
	public void setEdgesAway(int e) {
		if (e < edgesAway)
			edgesAway = e;
	}
	
	public static String getHandle(IMember m) {
		return ContextBuilder.getSignature(m, true);
	}
	
	public String getDotSignature(IMember m) {
		String sig = "";
		String sep = "\\n";
		
		String cl = m.getDeclaringType().getFullyQualifiedName();
		if (cl.length() < 1)
			sig = "";
		else
			sig = cl + "." + sep;
		
		sig += m.getElementName();
		
		if (m.getElementType() == IJavaElement.METHOD)
			sig += getDotSignature((IMethod)m, sep);
		
		return sig;
	}
	
	public String getDotSignature(IMethod m, String sep) {
		String sig = "";
		
		//If the method has parameters
		if (m.getNumberOfParameters() > 0) {		
			String[] types = null;
			sig += sep + "(";

			// Get types
			types = m.getParameterTypes();


			// Pretty print in a string
			for (int i = 0; i < types.length; i++) {
				sig += Signature.getSignatureSimpleName(types[i]);
				if (i < types.length - 1) {
					sig += "," + sep;
				}
			}
			sig += ")";
		} else { // Otherwise
			sig += "()";
		}
		return sig;
	}
	
	public String getSignature() {
		return ContextBuilder.getSignature(element);
	}

	public double getPathScore() {
		return pathScore;
	}

	public void setPathScore(double p) {
		if (p > 1)
			pathScore = 1d;
		else
			pathScore = p;
	}

	public Map<String, DoraEdge> getOutEdges() {
		return outEdges;
	}

	public void addOutEdge(DoraEdge e) {
		outEdges.put(e.toString(), e);
	}

	public Map<String, DoraEdge> getInEdges() {
		return inEdges;
	}

	public void addInEdge(DoraEdge e) {
		inEdges.put(e.toString(), e);
	}

	public boolean shouldGoForward() {
		return goForward;
	}

	public void setGoForward(boolean goForward) {
		this.goForward = goForward;
	}

	public boolean shouldGoBackward() {
		return goBackward;
	}

	public void setGoBackward(boolean goBackward) {
		this.goBackward = goBackward;
	}

	public boolean wentForward() {
		return wentForward;
	}

	public void setWentForward(boolean wentForward) {
		this.wentForward = wentForward;
	}

	public boolean wentBackward() {
		return wentBackward;
	}

	public void setWentBackward(boolean wentBackward) {
		this.wentBackward = wentBackward;
	}

	public boolean hasAnalyzedFamily() {
		return analyzedFamily;
	}

	public void setAnalyzedFamily(boolean analyzedFamily) {
		this.analyzedFamily = analyzedFamily;
	}
	
	
	
}
