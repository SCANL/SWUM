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

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IVariableDecl;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.scores.AbstractElementScore;
import edu.udel.nlpa.swum.stem.Stemmer;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.constants.MethodRole;
import edu.udel.nlpa.swum.utils.constants.ProgramElementType;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.context.MethodContext;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

/** Method Declaration **/
public class SearchableMethodDecl extends IMethodDecl implements SearchableNode {
	
	protected Stemmer stemmer = null;
	
	public SearchableMethodDecl(String n) {
		super(n);
	}
	
	public SearchableMethodDecl(String n, MethodContext c) {
		super(n, c);
	}
	
	public SearchableMethodDecl(String n, MethodContext c, Stemmer stem) {
		super(n, c);
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
	 * Given a non-empt MethodContext, fills in structural info
	 * Separate from constructor to allow light-weight place holders
	 * @param idSplitter
	 * @param tagger
	 */
	public void assignStructuralInformation(IdentifierSplitter idSplitter, ITagger tagger) {
		parse.setLocation(Location.NAME);
		returnType = new SearchableTypeNode(ctx.getType(), Location.RETURN, stemmer, idSplitter, tagger);
		
		returns = new ArrayList<Node>();
		returns.add(returnType);
		
		given = new ArrayList<Node>();
		
		if (!ctx.getFormals().isEmpty()) {
			int i = 0;
			for (String f : ctx.getFormals()) {
				String[] s = f.split("\\s+");
				if (s.length == 2) {
					given.add(new SearchableVariableDecl(s[1], s[0], Location.FORMAL, i, stemmer, idSplitter, tagger));
				}
				i++;
			}
		}
		onClass = new SearchableTypeNode(ctx.getDeclaringClass(), Location.ONCLASS, stemmer, idSplitter, tagger);
	}

	public int getHeadPosition(String s) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Node getNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getStemmedHeadPosition(String s) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String toSearchableString() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toStemmedSearchableString() {
		return ((SearchableNode)parse).toStemmedSearchableString();
	}

	public HashSet<String> getStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		
		sw.addAll(getActionStemmedWords());
		
		sw.addAll(getThemeStemmedWords());

		return sw;
	}
	
	public HashSet<String> getSigStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		
		sw.addAll(getActionStemmedWords());
		sw.addAll(getThemeStemmedWords());
		sw.addAll(getSecondaryArgsStemmedWords());
		sw.addAll(getUnknownArgsStemmedWords());

		return sw;
	}
	
	public HashSet<String> getSecondaryArgsStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		if (secondaryArgs != null && !secondaryArgs.isEmpty())
			for (Node n : secondaryArgs)
				if (n instanceof SearchableNode)
					sw.addAll(((SearchableNode)n).getStemmedWords());

		return sw;
	}
	
	public HashSet<String> getUnknownArgsStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		if (unknownArgs != null && !unknownArgs.isEmpty())
			for (Node n : unknownArgs)
				if (n instanceof SearchableNode)
					sw.addAll(((SearchableNode)n).getStemmedWords());

		return sw;
	}
	
	public HashSet<String> getActionStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		
		if (action instanceof SearchableNode)
			sw = ((SearchableNode)action).getStemmedWords();

		return sw;
	}
	
	public HashSet<String> getThemeStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		
		if (theme instanceof SearchableNode)
			sw = ((SearchableNode)theme).getStemmedWords();

		return sw;
	}
	
	public HashSet<String> getThemeNoAbbrevStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		// TODO fix and make part of Searchable Nodes
		if (theme instanceof SearchableVariableDecl) {
			SearchableVariableDecl svd = (SearchableVariableDecl) theme;
			sw = getNoAbbrevVariable(svd);
		} else if (theme instanceof SearchableFieldDecl) {
			SearchableFieldDecl svd = (SearchableFieldDecl) theme;
			sw = getNoAbbrevField(svd);
		} else if (theme instanceof SearchableEquivalenceNode) {
			for (Node n : ((SearchableEquivalenceNode)theme).getEquivalentNodes()) {
				if (n instanceof SearchableNode) {
					if (n instanceof SearchableVariableDecl)
						sw.addAll(getNoAbbrevVariable((SearchableVariableDecl) n));
					else if (theme instanceof SearchableFieldDecl)
						sw.addAll(getNoAbbrevField((SearchableFieldDecl) n));
					else
						sw.addAll(((SearchableNode)n).getStemmedWords());
				}
			}
		} else {
			sw = ((SearchableNode)theme).getStemmedWords();
		}

		return sw;
	}

	// TODO : if the first or last words are abbreviations, don't add to word list
	private HashSet<String> getNoAbbrevField(SearchableFieldDecl svd) {
		HashSet<String> sw;
		sw = ((SearchableNode)svd.getType()).getStemmedWords();
		if (!AbstractElementScore.isMultiWordAbbreviation(((SearchableNode)svd.getParse()).toSearchableString().trim(), 
				((SearchableNode)svd.getType().getParse()).toSearchableString().trim()))
			sw.addAll(((SearchableNode)svd.getParse()).getStemmedWords());
		return sw;
	}

	private HashSet<String> getNoAbbrevVariable(SearchableVariableDecl svd) {
		HashSet<String> sw;
		sw = ((SearchableNode)svd.getType()).getStemmedWords();
		// if the name isn't an abbrev, add to words
		if (!AbstractElementScore.isMultiWordAbbreviation(((SearchableNode)svd.getParse()).toSearchableString().trim(), 
				((SearchableNode)svd.getType().getParse()).toSearchableString().trim()))
			sw.addAll(((SearchableNode)svd.getParse()).getStemmedWords());
		return sw;
	}
	
	public HashSet<String> getNameStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		
		if (parse instanceof SearchableNode)
			sw = ((SearchableNode)parse).getStemmedWords();

		return sw;
	}
	
	public HashSet<String> getNameNonIgnorableStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		
		if (parse instanceof SearchableNode)
			sw = ((SearchableNode)parse).getNonIgnorableStemmedWords();

		return sw;
	}
	
	
	public HashSet<String> getNonIgnorableStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		
		sw.addAll(getActionNonIgnorableStemmedWords());
		
		sw.addAll(getThemeNonIgnorableStemmedWords());

		return sw;
	}
	
	public HashSet<String> getActionNonIgnorableStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		
		if (action instanceof SearchableNode)
			sw = ((SearchableNode)action).getNonIgnorableStemmedWords();

		return sw;
	}
	
	public HashSet<String> getThemeNonIgnorableStemmedWords() {
		HashSet<String> sw = new HashSet<String>();
		
		if (theme instanceof SearchableNode)
			sw = ((SearchableNode)theme).getNonIgnorableStemmedWords();

		return sw;
	}

	public HashSet<String> getWords() {
		HashSet<String> sw = new HashSet<String>();
		
		sw.addAll(getActionWords());
		
		sw.addAll(getThemeWords());

		return sw;
	}
	
	public HashSet<String> getActionWords() {
		HashSet<String> sw = new HashSet<String>();
		
		if (action instanceof SearchableNode)
			sw = ((SearchableNode)action).getWords();

		return sw;
	}
	
	public HashSet<String> getThemeWords() {
		HashSet<String> sw = new HashSet<String>();
		
		if (theme instanceof SearchableNode)
			sw = ((SearchableNode)theme).getWords();

		return sw;
	}

}
