package edu.udel.nlpa.swum.nodes;

import java.util.ArrayList;

import edu.udel.nlpa.swum.utils.constants.Location;


public abstract class IEquivalenceNode implements Node {

	protected ArrayList<Node> equivalentNodes = null;
	
	public IEquivalenceNode(Node n) {
		addEquivalentNode(n);
	}
	
	//public abstract IEquivalenceNode createNewEquivalenceNode(Node n);
	
	//public abstract void addEquivalentNode(Node n);
	public void addEquivalentNode(Node n) {
		if (equivalentNodes == null)
			equivalentNodes = new ArrayList<Node>();
		equivalentNodes.add(n);
	}
	
	/**
	 * Gets a new empty copy of a node of the same type
	 * @return
	 */
	//public abstract IEquivalenceNode getNewNode(Node n);
	
	public ArrayList<Node> getEquivalentNodes() {
		return equivalentNodes;
	}

	public void setEquivalentNodes(ArrayList<Node> equivalentNodes) {
		this.equivalentNodes = equivalentNodes;
	}
	
	/**
	 * Returns location of first item in equivalent node array
	 */
	public Location getLocation() {
		if (equivalentNodes.isEmpty())
			return Location.NONE;
		else
			return equivalentNodes.get(0).getLocation();
	}
	
	public String toString() {
		String s = equivalentNodes.toString();
		s = s.replaceAll("\\[", "").replaceAll("\\]", "").split(",")[0].strip();
		return s;
	}

}