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

import edu.udel.nlpa.swum.nodes.IFieldDecl;
import edu.udel.nlpa.swum.nodes.IPhraseNode;
import edu.udel.nlpa.swum.nodes.ITypeNode;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.base.FieldDecl;
import edu.udel.nlpa.swum.stem.Stemmer;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.constants.ProgramElementType;
import edu.udel.nlpa.swum.utils.context.FieldContext;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public class SearchableFieldDecl extends IFieldDecl implements SearchableNode {

	protected Stemmer stemmer = null;
	
	public SearchableFieldDecl(String n, FieldContext ctx) {
		super(n, ctx);
	}
	
	public SearchableFieldDecl(String n, FieldContext ctx, Stemmer stem) {
		super(n, ctx);
		stemmer = stem;
	}
	
	@Override
	public void parse(IdentifierSplitter idSplitter) {
		if (parse == null) {
			if (stemmer == null)
				parse = new SearchablePhraseNode(name, idSplitter);
			else
				parse = new SearchablePhraseNode(name, idSplitter, stemmer);
		}
	}

	/**
	 * Given a non-empty FieldContext, fills in structural info
	 * Separate from constructor to allow light-weight place holders
	 * @param idSplitter
	 * @param tagger
	 */
	public void assignStructuralInformation(IdentifierSplitter idSplitter, ITagger tagger) {
		type = new SearchableTypeNode(ctx.getType(), stemmer, idSplitter, tagger);
		onClass = new SearchableTypeNode(ctx.getDeclaringClass(), stemmer, idSplitter, tagger);

	}
	
	public SearchablePhraseNode getParse() {
		return (SearchablePhraseNode) parse;
	}
	
	public SearchableTypeNode getType() {
		return (SearchableTypeNode) type;
	}
	
	public SearchableTypeNode getOnClass() {
		return (SearchableTypeNode) onClass;
	}
	
	public String toSearchableString() { // no class yet
		return ((SearchablePhraseNode)parse).toSearchableString() + 
			((SearchableTypeNode)type).toSearchableString();
	}
	
	public String toStemmedSearchableString() {
		return ((SearchablePhraseNode)parse).toStemmedSearchableString() + 
		((SearchableTypeNode)type).toStemmedSearchableString();
	}
	
	public int getHeadPosition(String s) {
		int head = ((SearchablePhraseNode)parse).getHeadPosition(s);
		if (head == -1)
			return ((SearchableTypeNode)type).getHeadPosition(s);
		
		int headt = ((SearchableTypeNode)type).getHeadPosition(s);
		if (headt == -1)
			return head;
		else
			return Math.min(headt, head);
	}

	public int getStemmedHeadPosition(String s) {
		int head = ((SearchablePhraseNode)parse).getStemmedHeadPosition(s);
		if (head == -1)
			return ((SearchableTypeNode)type).getStemmedHeadPosition(s);
		
		int headt = ((SearchableTypeNode)type).getStemmedHeadPosition(s);
		if (headt == -1)
			return head;
		else
			return Math.min(headt, head);
	}

	public HashSet<String> getStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		sw.addAll(((SearchablePhraseNode)parse).getStemmedWords());
		sw.addAll(((SearchableTypeNode)type).getStemmedWords());
		return sw;
	}

	public HashSet<String> getWords() {
		HashSet<String> sw = new HashSet<String>();
		sw.addAll(((SearchablePhraseNode)parse).getWords());
		sw.addAll(((SearchableTypeNode)type).getWords());
		return sw;
	}

	public HashSet<String> getNonIgnorableStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		sw.addAll(((SearchablePhraseNode)parse).getNonIgnorableStemmedWords());
		sw.addAll(((SearchableTypeNode)type).getNonIgnorableStemmedWords());
		return sw;
	}
}
