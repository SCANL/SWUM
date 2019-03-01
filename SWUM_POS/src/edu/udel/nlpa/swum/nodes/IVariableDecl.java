package edu.udel.nlpa.swum.nodes;

import edu.udel.nlpa.swum.nodes.base.TypeNode;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;


public abstract class IVariableDecl extends IProgramElementNode {

	protected ITypeNode typeName;
	protected int position = 0;
	
	public IVariableDecl(String n, String t, IdentifierSplitter idSplitter, ITagger tagger) {
		this(n, idSplitter);
		initTypeName(t, idSplitter, tagger);
		tagger.tagVariableName(parse);
	}
	
	public IVariableDecl(String n, String t, Location l,
			IdentifierSplitter idSplitter, ITagger tagger) {
		this(n,t,idSplitter,tagger);
		location = l;
	}
	public IVariableDecl(String n, String t, Location l, int pos,
			IdentifierSplitter idSplitter, ITagger tagger) {
		this(n,t,l,idSplitter,tagger);
		position = pos;
	}
	
	public IVariableDecl(String n, IdentifierSplitter idSplitter) {
		super(n, idSplitter);
	}
	
	public IVariableDecl(String n) {
		super(n);
	}
	
	public abstract void initTypeName(String t, IdentifierSplitter idSplitter, ITagger tagger);

	public ITypeNode getType() {
		return typeName;
	}

	public void setType(ITypeNode type) {
		this.typeName = type;
	}
	
	public String toString() {
		return "[ " + typeName.toString() + "- " + super.toString() + "]";
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	

}