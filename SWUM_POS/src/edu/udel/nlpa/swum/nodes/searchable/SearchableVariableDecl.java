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

import edu.udel.nlpa.swum.nodes.IVariableDecl;
import edu.udel.nlpa.swum.nodes.base.PhraseNode;
import edu.udel.nlpa.swum.nodes.base.TypeNode;
import edu.udel.nlpa.swum.stem.Stemmer;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.constants.ProgramElementType;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

/**
 * Variable Declaration
 * @author hill
 *
 */
public class SearchableVariableDecl extends IVariableDecl implements SearchableNode {
	
	protected Stemmer stemmer = null;
	
	public SearchableVariableDecl(String n, String t, Stemmer stem,
			IdentifierSplitter idSplitter, ITagger tagger) {
		super(n);
		stemmer = stem;
		parse(idSplitter);
		initTypeName(t, idSplitter, tagger);
		tagger.tagVariableName(parse);
		//super(n, t, idSplitter, tagger);
	}

	public SearchableVariableDecl(String n, String t, Location l, Stemmer stem,
			IdentifierSplitter idSplitter, ITagger tagger) {
		this(n,t,stem,idSplitter,tagger);
		location = l;
	}
	
	public SearchableVariableDecl(String n, String t, Location l, int p, Stemmer stem,
			IdentifierSplitter idSplitter, ITagger tagger) {
		this(n,t,l,stem,idSplitter,tagger);
		position = p;
	}

	@Override
	public void parse(IdentifierSplitter idSplitter) {
		if (parse == null)
			parse = new SearchablePhraseNode(name, idSplitter, stemmer);
	}
	
	@Override
	public void initTypeName(String t, IdentifierSplitter idSplitter, ITagger tagger) {
		typeName = new SearchableTypeNode(t, stemmer, idSplitter, tagger);
	}
	
	public String toSearchableString() {
		return ((SearchableTypeNode)typeName).toSearchableString() + 
			((SearchablePhraseNode)parse).toSearchableString();
	}
	
	public String toStemmedSearchableString() {
		return ((SearchableTypeNode)typeName).toStemmedSearchableString() + 
		((SearchablePhraseNode)parse).toStemmedSearchableString();
	}
	
	public int getHeadPosition(String s) {
		int head = ((SearchablePhraseNode)parse).getHeadPosition(s);
		if (head == -1)
			return ((SearchableTypeNode)typeName).getHeadPosition(s);
		
		int headt = ((SearchableTypeNode)typeName).getHeadPosition(s);
		if (headt == -1)
			return head;
		else
			return Math.min(headt, head);
	}

	public int getStemmedHeadPosition(String s) {
		int head = ((SearchablePhraseNode)parse).getStemmedHeadPosition(s);
		if (head == -1)
			return ((SearchableTypeNode)typeName).getStemmedHeadPosition(s);
		
		int headt = ((SearchableTypeNode)typeName).getStemmedHeadPosition(s);
		if (headt == -1)
			return head;
		else
			return Math.min(headt, head);
	}

	public HashSet<String> getNonIgnorableStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		sw.addAll(((SearchablePhraseNode)parse).getNonIgnorableStemmedWords());
		sw.addAll(((SearchableTypeNode)typeName).getNonIgnorableStemmedWords());
		return sw;
	}

	public HashSet<String> getStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		sw.addAll(((SearchablePhraseNode)parse).getStemmedWords());
		sw.addAll(((SearchableTypeNode)typeName).getStemmedWords());
		return sw;
	}

	public HashSet<String> getWords() {
		HashSet<String> sw = new HashSet<String>();
		sw.addAll(((SearchablePhraseNode)parse).getWords());
		sw.addAll(((SearchableTypeNode)typeName).getWords());
		return sw;
	}


}
