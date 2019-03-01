package edu.udel.nlpa.swum.nodes;

import edu.udel.nlpa.swum.nodes.base.ArgumentNode;
import edu.udel.nlpa.swum.nodes.base.EquivalenceNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableArgumentNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableEquivalenceNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableWord;

public class CompositeNodeFactory {

	public static IArgumentNode getArgumentNode(Node a, IWordNode p) {
		IArgumentNode e;
		if (a instanceof SearchableNode && p instanceof SearchableWord)
			e = new SearchableArgumentNode((SearchableNode)a, (SearchableWord)p);
		else // otherwise we know they're both nodes
			e = new ArgumentNode(a, p);
		
		return e;
	}
	
	public static IEquivalenceNode getEquivalenceNode(Node n) {
		IEquivalenceNode e;
		if (n instanceof SearchableNode)
			e = new SearchableEquivalenceNode((SearchableNode)n);
		else
			e = new EquivalenceNode(n);
		
		return e;
	}
}
