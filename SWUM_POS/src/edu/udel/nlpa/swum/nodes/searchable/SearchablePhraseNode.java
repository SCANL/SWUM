/**
 *  SWUM - Copyright (C) 2009 Emily Hill (emily.m.hill@gmail.com)
 *  All rights reserved.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.udel.nlpa.swum.nodes.searchable;

import java.util.ArrayList;
import java.util.HashSet;

import edu.udel.nlpa.swum.nodes.IWordNode;
import edu.udel.nlpa.swum.nodes.IPhraseNode;
import edu.udel.nlpa.swum.nodes.base.PhraseNode;
import edu.udel.nlpa.swum.stem.Stemmer;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.constants.POSTag;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

/**
 * No knowedge of POS
 * @author hill
 *
 */
public class SearchablePhraseNode extends IPhraseNode implements SearchableNode {
		
	public SearchablePhraseNode(String id, IdentifierSplitter idSplitter) {
		super(id, idSplitter);
	}
	
	public SearchablePhraseNode(String id, IdentifierSplitter idSplitter, Stemmer stemmer) {
		String[] name = idSplitter.splitId(id);
		for (String s : name) {
			array.add(new SearchableWord(s, stemmer));
		}
	}
	
	public SearchablePhraseNode(ArrayList<IWordNode> clone, Location location, boolean containsPrep) {
		super(clone, location, containsPrep);
	}

	public SearchablePhraseNode() {
		super();
	}

	public Object clone() {
		return new SearchablePhraseNode((ArrayList<IWordNode>) array.clone(), location, containsPrep);
	}
	
	public IPhraseNode getNewEmpty() {
		return new SearchablePhraseNode();
	}

	protected void init(String[] id) {
		for (String s : id) {
			array.add(new SearchableWord(s));
		}
	}
	
	public String toSearchableString() {
		String s = " ";
		for (IWordNode w : array) {
			s += w.getWord() + " ";
		}
		return s;
	}
	
	public HashSet<String> getWords() {
		HashSet<String> w = new HashSet<String>();
		for (IWordNode iwn : array)
			w.add(iwn.getWord());
		return w;
	}
	
	public HashSet<String> getStemmedWords() {
		HashSet<String> w = new HashSet<String>();
		for (IWordNode iwn : array)
			w.add(((SearchableWord)iwn).getStemmedWord());
		return w;
	}
	
	public HashSet<String> getNonIgnorableStemmedWords() {
		HashSet<String> w = new HashSet<String>();
		for (IWordNode iwn : array)
			w.addAll(((SearchableWord)iwn).getNonIgnorableStemmedWords());
		return w;
	}

	public String toStemmedSearchableString() {
		String s = " ";
		for (IWordNode w : array) { // we know only SearchableWords added
			s += ((SearchableWord)w).toStemmedSearchableString() + " ";
		}
		return s;
	}
	
	public int getHeadPosition(String s) {
		//int pos = this.lastIndexOf(s);
		for (int i = this.size() - 1; i >= 0; i--)
			if (this.get(i).getWord().matches(s)) {
				int head = this.size() - i - 1;
				if (head > 0 &&
						this.get(this.size() - 1).getTag() == POSTag.NOUN_IGNORABLE)
					return head - 1;
				return head;
			}
		return -1;
	}
	
	public int getStemmedHeadPosition(String s) {
		for (int i = this.size() - 1; i >= 0; i--) {
			SearchableWord w = (SearchableWord) this.get(i);
			if (w.getStemmedWord().matches(s)) {
				int head = this.size() - i - 1;
				if (head > 0 &&
						this.get(this.size() - 1).getTag() == POSTag.NOUN_IGNORABLE)
					return head - 1;
				return head;
			}
		}
		return -1;
	}
}
