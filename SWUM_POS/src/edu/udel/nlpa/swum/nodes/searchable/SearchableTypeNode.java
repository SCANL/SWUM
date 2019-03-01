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

import edu.udel.nlpa.swum.nodes.ITypeNode;
import edu.udel.nlpa.swum.stem.Stemmer;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public class SearchableTypeNode extends ITypeNode implements SearchableNode {
	
	public SearchableTypeNode(String p) {
		super(p);
	}

	public SearchableTypeNode(String t, Stemmer stemmer, IdentifierSplitter idSplitter, ITagger tagger) {
		this(t);
		if (stemmer != null)
			parse(idSplitter, stemmer);
		else
			parse(idSplitter);
		tagger.tagType(parse);
	}

	public SearchableTypeNode(String t, Location l, Stemmer stemmer,
			IdentifierSplitter idSplitter, ITagger tagger) {
		this(t, stemmer, idSplitter, tagger);
		location = l;
	}

	@Override
	public void parse(IdentifierSplitter idSplitter) {
		if (parse == null)
			parse = new SearchablePhraseNode(name, idSplitter);
	}
	
	public void parse(IdentifierSplitter idSplitter, Stemmer stemmer) {
		if (parse == null)
			parse = new SearchablePhraseNode(name, idSplitter, stemmer);
	}

	public String toSearchableString() {
		return ((SearchablePhraseNode)parse).toSearchableString();
	}
	
	public String toStemmedSearchableString() {
		return ((SearchablePhraseNode)parse).toStemmedSearchableString();
	}
	
	public int getHeadPosition(String s) {
		return ((SearchablePhraseNode)parse).getHeadPosition(s);
	}

	public int getStemmedHeadPosition(String s) {
		return ((SearchablePhraseNode)parse).getStemmedHeadPosition(s);
	}
	
	public HashSet<String> getNonIgnorableStemmedWords() {
		return ((SearchablePhraseNode)parse).getNonIgnorableStemmedWords();
	}

	public HashSet<String> getStemmedWords() {
		return ((SearchablePhraseNode)parse).getStemmedWords();
	}

	public HashSet<String> getWords() {
		return ((SearchablePhraseNode)parse).getWords();
	}
	
	
}
