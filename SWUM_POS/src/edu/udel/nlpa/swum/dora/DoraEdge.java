package edu.udel.nlpa.swum.dora;

import java.text.DecimalFormat;

public class DoraEdge {
	private DoraNode from;
	private DoraNode to;
	private EdgeType type = EdgeType.PLAIN;
	private double score = 0d;
	private static final DecimalFormat df = new DecimalFormat("0.00");
	private boolean written = false;
	private boolean read = false; // true should be default
	
	public DoraEdge(DoraNode f, DoraNode t, EdgeType ty) {
		this(f, t);
		type = ty;
	}
	
	public DoraEdge(DoraNode f, DoraNode t, EdgeType ty, double s) {
		this(f, t, ty);
		score = s;
	}
	
	public DoraEdge(DoraNode f, DoraNode t) {
		from = f;
		to = t;
	}

	public DoraNode getFromNode() {
		return from;
	}

	public DoraNode getToNode() {
		return to;
	}
	
	public String toDotString() {
		return from.getId() + " -> " + to.getId() + " " + type.drawNode() 
		 + " [label=\"" + df.format(score) + "\"]"
		;
	}
	
	public String toString() {
		return from.getSignature() + " -> " + to.getSignature();
	}
	
	public EdgeType getEdgeType() { return type; }
	public void setEdgeType(EdgeType t) { type = t; }

	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}
	
	public boolean isWritten() {
		return written;
	}

	public void setWritten(boolean written) {
		this.written = written;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}
	
}
