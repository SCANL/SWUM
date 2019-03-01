package edu.udel.nlpa.swum.nodes.searchable;

import java.util.HashSet;

import edu.udel.nlpa.swum.nodes.IArgumentNode;
import edu.udel.nlpa.swum.nodes.IEquivalenceNode;
import edu.udel.nlpa.swum.nodes.IWordNode;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.base.ArgumentNode;
import edu.udel.nlpa.swum.nodes.base.EquivalenceNode;
import edu.udel.nlpa.swum.nodes.base.WordNode;
import edu.udel.nlpa.swum.utils.constants.Location;

/**
 * Node wrapper for secondary args
 * @author hill
 *
 */
public class SearchableArgumentNode extends IArgumentNode implements SearchableNode {
		
	public SearchableArgumentNode(SearchableNode a, SearchableWord p) {
		// No guarantees that a is Node -- but all SearchableNodes implement Node too
		super((Node) a, p);
	}

	public String toSearchableString() {
		return ((SearchableWord)prep).toSearchableString() + 
			((SearchableNode)argument).toSearchableString();
	}
	
	public String toStemmedSearchableString() {
		return ((SearchableWord)prep).toStemmedSearchableString() + 
		((SearchableNode)argument).toStemmedSearchableString();
	}

	public int getHeadPosition(String s) {
		return ((SearchableNode)argument).getHeadPosition(s);
	}

	public int getStemmedHeadPosition(String s) {
		return ((SearchableNode)argument).getStemmedHeadPosition(s);
	}

	public HashSet<String> getStemmedWords() {
		return ((SearchableNode)argument).getStemmedWords();
	}
	
	public HashSet<String> getNonIgnorableStemmedWords() {
		return ((SearchableNode)argument).getNonIgnorableStemmedWords();
	}

	public HashSet<String> getWords() {
		return ((SearchableNode)argument).getWords();
	}

}
