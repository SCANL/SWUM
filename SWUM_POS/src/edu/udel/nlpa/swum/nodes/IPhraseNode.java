package edu.udel.nlpa.swum.nodes;

import java.util.ArrayList;
import java.util.HashSet;

import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public abstract class IPhraseNode implements Node, Cloneable {
	
	//private static final long serialVersionUID = 1L;
	protected boolean containsPrep = false;
	protected Location location = Location.NONE;
	protected ArrayList<IWordNode> array = new ArrayList<IWordNode>();
	
	public IPhraseNode() { } 
	
	public IPhraseNode(String[] id) {
		init(id);
	}
	
	public IPhraseNode(String id, IdentifierSplitter idSplitter) {
		init(idSplitter.splitId(id));
	}
	
	public IPhraseNode(ArrayList<IWordNode> a, Location l, boolean prep) {
		containsPrep = prep;
		location = l;
		array = a;
	}
	
	protected abstract void init(String[] id);
	
	public abstract Object clone();
	
	protected void copy(IPhraseNode node) {
		array = node.array;
		location = node.location;
		containsPrep = node.containsPrep;
	}
	
	/**
	 * Gets a new empty copy of a phrase node of the same type
	 * @return
	 */
	public abstract IPhraseNode getNewEmpty();
	
	public String toString() {
		String s = "";
		for (IWordNode w : array) {
			s += w.toString() + " ";
		}
		return s;
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	public ArrayList<IWordNode> getPhrase() { return array; }
	public IWordNode get(int i) { return array.get(i); }
	public int size() { return array.size(); }
	public void add(IWordNode w) { array.add(w); }
	public boolean isEmpty() { return array.isEmpty(); }

	// Special getters
	public String beginsWith() { return array.get(0).getWord(); }
	public String beginsWith(int i) { return array.get(i).getWord(); }
	public String endsWith() { return array.get(array.size() - 1).getWord(); }

	public void remove(int i) { array.remove(i); }

	public void add(IPhraseNode theme) {
//		if (array.isEmpty()) {
//			copy(theme);
//		} else {
			for (IWordNode w : theme.getPhrase())
				array.add(w);
		//}
	}

}