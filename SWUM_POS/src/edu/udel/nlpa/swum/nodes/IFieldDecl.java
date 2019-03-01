package edu.udel.nlpa.swum.nodes;

import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.context.FieldContext;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public abstract class IFieldDecl extends IProgramElementNode {
	
	protected FieldContext ctx;
	
	// Structural Info
	protected ITypeNode type;
	protected ITypeNode onClass;
	
	public IFieldDecl(String n) {
		super(n);
	}

	public IFieldDecl(String n, FieldContext ctx) {
		super(n);
		this.ctx = ctx;
	}
	
	public abstract void assignStructuralInformation(IdentifierSplitter idSplitter, ITagger tagger);
	
	public ITypeNode getType() {
		return type;
	}

	public void setType(ITypeNode type) {
		this.type = type;
	}
	
	public ITypeNode getOnClass() {
		return onClass;
	}

	public void setOnClass(ITypeNode onClass) {
		this.onClass = onClass;
	}

	public String toString() {
		return "[ " + type.toString() + "- " + super.toString() + "]";
	}


}