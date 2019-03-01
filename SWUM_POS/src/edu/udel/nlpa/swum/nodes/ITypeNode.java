package edu.udel.nlpa.swum.nodes;

import edu.udel.nlpa.swum.nodes.base.PhraseNode;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public abstract class ITypeNode extends IProgramElementNode {
	
	protected boolean isPrimitive = false;
	
	public ITypeNode() { super(); }
	
	public ITypeNode(String p) {
		super(p);
	}
	
	public ITypeNode(String p, IdentifierSplitter idSplitter) {
		super(p, idSplitter);
	}
	
	public ITypeNode(String s, IdentifierSplitter idSplitter, ITagger tagger) {
		this(s, idSplitter);
		tagger.tagType(parse);
	}
	
	public ITypeNode(String n, PhraseNode p) {
		super(n, p);
	}
	
	public ITypeNode(String t, Location l, IdentifierSplitter idSplitter,
			ITagger tagger) {
		this(t, idSplitter, tagger);
		location = l;
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}

	public void setPrimitive(boolean isPrimitive) {
		this.isPrimitive = isPrimitive;
	}	

}