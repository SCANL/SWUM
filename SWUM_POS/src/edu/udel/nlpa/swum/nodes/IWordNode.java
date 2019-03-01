package edu.udel.nlpa.swum.nodes;

import edu.udel.nlpa.swum.nodes.base.WordNode;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.constants.POSTag;

public abstract class IWordNode implements Node {

	protected String word;
	protected POSTag tag;
	protected double confidence;
	protected boolean all_caps = false;
	
	public IWordNode(String w, POSTag t, double c) {
		word = w.toLowerCase();
		if (isAllCaps(w))
			all_caps = true;
		tag = t;
		confidence = c;
	}
	
	public IWordNode(String w, POSTag t) {
		this(w, t, 0.0);
	}
	
	public IWordNode(String w, String tag) {
		this(w);
		this.setTag(WordNode.getPOS(tag));
	}
	
	public IWordNode(String w) {
		this(w, POSTag.UNKNOWN);
	}
	
	public IWordNode() {
		this("");
	}
	
	public abstract IWordNode getNewWord(String w, POSTag t);
	
	protected void copy(IWordNode node) {
		word = node.getWord();
		tag = node.getTag();
		all_caps = node.allCaps();
	}
	
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public POSTag getTag() {
		return tag;
	}

	public void setTag(POSTag tag) {
		this.tag = tag;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	public boolean allCaps() {
		return all_caps;
	}
	
	public String toString() {
		String w = word;
		if (all_caps) w = w.toUpperCase();
		return w + " (" + tag.toString() + ")";
	}
	
	public boolean equals(Object other) {
		if (other instanceof WordNode) {
			IWordNode o = (IWordNode) other;
			return (word.equals(o.getWord()) && tag == o.getTag());
		}
		return false;
	}
	
	protected boolean isAllCaps(String name) {
		return name.matches("^[A-Z0-9_]+$");
	}
	
	public static POSTag getPOS(String pos) {
		if (pos.matches("N"))
			return POSTag.NOUN;
		else if (pos.matches("NM"))
			return POSTag.NOUN_MODIFIER;
		else if (pos.matches("V"))
			return POSTag.VERB;
		else if (pos.matches("PP"))
			return POSTag.PAST_PARTICIPLE;
		else if (pos.matches("V3PS"))
			return POSTag.VERB_3PS;
		else if (pos.matches("VING"))
			return POSTag.VERB_ING;
		else if (pos.matches("VM"))
			return POSTag.VERB_MODIFIER;
		else if (pos.matches("VP"))
			return POSTag.VERB_PARTICLE;
		else if (pos.matches("P"))
			return POSTag.PREPOSITION;
		else if (pos.matches("D"))
			return POSTag.DIGIT;
		else if (pos.matches("DT"))
			return POSTag.DETERMINER;
		else if (pos.matches("PRE"))
			return POSTag.PREAMBLE;
		
		return POSTag.UNKNOWN;
	}

	public Location getLocation() { return Location.NONE; }

}