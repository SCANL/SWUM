package edu.udel.nlpa.swum.nodes;

import edu.udel.nlpa.swum.utils.constants.Location;

public abstract class IArgumentNode implements Node {

	protected Node argument;
	protected IWordNode prep;
	// TODO: role of arg with verb
	
	protected IArgumentNode(Node a, IWordNode p) {
		argument = a;
		prep = p;
	}
	
	//public abstract IArgumentNode createNewArgumentNode(Node a, IWordNode p);

	public Node getArgument() {
		return argument;
	}
	
	public void setArgument(Node argument) {
		this.argument = argument;
	}
	
	public IWordNode getPreposition() {
		return prep;
	}
	
	public void setPreposition(IWordNode prep) {
		this.prep = prep;
	}
	
	public Location getLocation() {
		return argument.getLocation();
	}
	
	public String toString() {
		return prep + " " + argument;
	}

}