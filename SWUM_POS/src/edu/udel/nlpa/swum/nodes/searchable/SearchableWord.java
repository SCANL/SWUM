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

import java.util.HashSet;

import edu.udel.nlpa.swum.nodes.IWordNode;
import edu.udel.nlpa.swum.stem.Stemmer;
import edu.udel.nlpa.swum.utils.constants.POSTag;

public class SearchableWord extends IWordNode implements SearchableNode {

//	private IWordNode word;
	private String stemmedWord = "";
	//private static Stemmer stemmer = new MStem();
	
	public SearchableWord(String w) {
		super(w);
		stemmedWord = w; // default
	}
	
	public SearchableWord(String w, POSTag t) {
		super(w,t);
		stemmedWord = w;
	}
	
	public SearchableWord(String w, Stemmer stemmer) {
		super(w);
		stemmedWord = stemmer.stem(word);
	}
	
	protected void copy(IWordNode w) {
		super.copy(w);
		if (w instanceof SearchableWord)
			stemmedWord = ((SearchableWord)w).getStemmedWord();
	}
	
	/*public SearchableWord(String w, POSTag t, double c) {
		word = new WordNode(w, t, c);
		// stemmedWord = stemmed(w)
	}
	
	public SearchableWord(String w, POSTag t) {
		this(w, t, 0.0);
	}
	
	public SearchableWord() {
		this("");
	}
	
	public SearchableWord(String w, String tag) {
		word = new WordNode(w, tag);
		// stemmedWord = stemmed(w)
	}
	
	public SearchableWord(IWordNode w) {
		word = w;
		// stemmedWord = stemmed(w)
	}*/
	
	public String getStemmedWord() {
		return stemmedWord;
	}
	
	public String toSearchableString() {
		return " " + word + " ";
	}
	
	public String toStemmedSearchableString() {
		return " " + stemmedWord + " ";
	}
	
	public int getHeadPosition(String s) {
		if (s.matches(word))
			return 0;
		else
			return -1;
	}

	public int getStemmedHeadPosition(String s) {
		if (s.matches(stemmedWord))
			return 0;
		else
			return -1;
	}

	@Override
	public IWordNode getNewWord(String w, POSTag t) {
		return new SearchableWord(w, t);
	}

	public HashSet<String> getStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		sw.add(stemmedWord);
		return sw;
	}
	
	public HashSet<String> getNonIgnorableStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		// if tag isn't ignorable
		if (tag != POSTag.NOUN_IGNORABLE && tag != POSTag.VERB_IGNORABLE)
			sw.add(stemmedWord);
		return sw;
	}

	public HashSet<String> getWords() {
		HashSet<String> sw = new HashSet<String>();
		sw.add(word);
		return sw;
	}
}
