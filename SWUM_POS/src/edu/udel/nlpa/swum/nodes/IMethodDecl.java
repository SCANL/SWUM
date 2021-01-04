package edu.udel.nlpa.swum.nodes;

import java.util.ArrayList;

import edu.udel.nlpa.swum.nodes.base.ArgumentNode;
import edu.udel.nlpa.swum.nodes.base.TypeNode;
import edu.udel.nlpa.swum.nodes.base.VariableDecl;
import edu.udel.nlpa.swum.nodes.searchable.SearchableArgumentNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableTypeNode;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.constants.MethodRole;
import edu.udel.nlpa.swum.utils.context.MethodContext;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public abstract class IMethodDecl extends IProgramElementNode {

	protected MethodContext ctx;
	protected MethodRole role;
	
	// Structural Info
	
	protected ArrayList<Node> given; // IVariableDeclaration
	protected ITypeNode onClass;
	protected ArrayList<Node> returns;
	protected ITypeNode returnType;
	
	// Linguistic Info
	
	protected Node action;
	protected Node theme; // how to pt to multiple name nodes?
	protected ArrayList<Node> secondaryArgs; // known IOs (may or may not have prep)
	protected ArrayList<Node> unknownArgs = null; // remaining args, unknown role
	
	// Additional Linguistic Info for checkers
	protected Node agent;
	protected Node condition;
	
	protected Location themeLocation = Location.NONE;
	
	protected double confidence = 0d; // TODO
	
	protected boolean reactive = false;
	protected boolean ctor = false;

	public IMethodDecl(String n) {
		super(n);
	}

	public IMethodDecl(String n, MethodContext c) {
		this(n);
		ctx = c;
	}

	/**
	 * Given a non-empty MethodContext, fills in structural info
	 * Separate from constructor to allow light-weight place holders
	 * @param idSplitter
	 * @param tagger
	 */
	public abstract void assignStructuralInformation(
			IdentifierSplitter idSplitter, ITagger tagger);

	
	public String toString() {
		String a = "";
		String t = "";
		String s = "";
		if (action != null) { a = action.toString(); }
		if (theme != null)  { t = theme.toString(); }
		if (secondaryArgs != null) {
			for (Node n : secondaryArgs) {
				s += n.toString();
			}
		}
		
//		if (unknownArgs != null) {
//			s += "\t++";
//			for (Node n : unknownArgs) {
//				s += " :: " + n.toString();
//			}
//		}
		// everything else is an aux arg
		if(this.getThemeLocation() == Location.ONCLASS) {
			return a + " | " + "["+t+"]" + s;
		} else {
			return a + " | " + t + s;
		}
		
	}
	
	public Location getThemeLocation() {
		return themeLocation;
	}

	public MethodContext getContext() {
		return ctx;
	}

	public void setContext(MethodContext ctx) {
		this.ctx = ctx;
	}

	public ArrayList<Node> getGiven() {
		return given;
	}

	public void setGiven(ArrayList<Node> given) {
		this.given = given;
	}

	public ITypeNode getOnClass() {
		return onClass;
	}

	public void setOnClass(ITypeNode onClass) {
		this.onClass = onClass;
	}

	public ArrayList<Node> getReturns() {
		return returns;
	}

	public void setReturns(ArrayList<Node> returns) {
		this.returns = returns;
	}

	public ITypeNode getReturnType() {
		return returnType;
	}

	public void setReturnType(TypeNode returnType) {
		this.returnType = returnType;
	}

	public Node getAction() {
		return action;
	}

	public void setAction(Node action) {
		this.action = action;
	}

	public Node getTheme() {
		return theme;
	}

	public void setTheme(Node theme) {
		this.theme = theme;
		themeLocation = theme.getLocation();
	}
	
	public void setTheme(IPhraseNode pre, IPhraseNode t) {
		pre.add(t);
		this.theme = pre;
		themeLocation = t.getLocation();
	}

	public void setTheme(Node theme, Location tl) {
		this.theme = theme;
		themeLocation = tl;
	}

	public ArrayList<Node> getSecondaryArgs() {
		return secondaryArgs;
	}

	public void setSecondaryArgs(ArrayList<Node> secondaryArgs) {
		this.secondaryArgs = secondaryArgs;
	}

	public ArrayList<Node> getUnknownArgs() {
		return unknownArgs;
	}

	public void setUnknownArgs(ArrayList<Node> unknownArgs) {
		this.unknownArgs = unknownArgs;
	}
	
	public void addUnknownArg(Node n) {
		if (unknownArgs == null)
			unknownArgs = new ArrayList<Node>();
		unknownArgs.add(n);
	}
	
	public void addUnknownArgs(ArrayList<Node> n) {
		if (unknownArgs == null)
			unknownArgs = n;
		unknownArgs.addAll(n);
	}
	
	public void removeUnknownArg(int i) {
		if (unknownArgs != null)
			unknownArgs.remove(i);
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public MethodRole getRole() {
		return role;
	}

	public void setRole(MethodRole role) {
		this.role = role;
	}
	
	public Node getAgent() {
		return agent;
	}

	public void setAgent(Node agent) {
		this.agent = agent;
	}

	public Node getCondition() {
		return condition;
	}

	public void setCondition(Node condition) {
		this.condition = condition;
	}

	public void setReactive(boolean reactive) {
		this.reactive = reactive;
	}

	public boolean isReactive() {
		return reactive;
	}
	
	public boolean isConstructor() {
		return ctor;
	}
	
	public void setConstructor(boolean c) {
		this.ctor = c;
	}

}