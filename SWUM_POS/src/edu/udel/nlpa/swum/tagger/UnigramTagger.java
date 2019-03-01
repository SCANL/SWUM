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

package edu.udel.nlpa.swum.tagger;

import java.util.HashSet;

import edu.udel.nlpa.swum.nodes.IWordNode;
import edu.udel.nlpa.swum.nodes.IPhraseNode;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.PhraseNode;
import edu.udel.nlpa.swum.rules.unigram.pos.UnigramPOSData;
import edu.udel.nlpa.swum.utils.IgnorableWords;
import edu.udel.nlpa.swum.utils.LibFileLoader;
import edu.udel.nlpa.swum.utils.PositionalFrequencies;
import edu.udel.nlpa.swum.utils.constants.POSTag;

public class UnigramTagger implements ITagger {

	//private static HashSet<String> possiblePreamble = new HashSet<String>();
	private HashSet<String> ignorableHeadWords = IgnorableWords.getInstance().getIgnorableHeadWords();
	private static PositionalFrequencies pf = PositionalFrequencies.getInstance();

	public UnigramPOSData pos;

	static {
		
//		possiblePreamble.add("jj");
//		possiblePreamble.add("jb");
//		possiblePreamble.add("ejb");
//		possiblePreamble.add("m");
		
	}
	
	public UnigramTagger(UnigramPOSData p) {
		pos = p;
	}
	
	public void preTag(IProgramElementNode node) {
		tagDigits(node.getParse());
		stripPreamble(node);
		tagPrepositions(node.getParse());
	}

	public void tagType(IPhraseNode parse) {
		tagNounPhrase(parse);
		
	}

	public void tagVariableName(IPhraseNode parse) {
		tagNounPhrase(parse);
	}
	
	// TODO don't return concrete PhraseNode
	public void stripPreamble(IProgramElementNode in) {
		if (in.getPreamble() != null)
			return;
		
		int i = 0; // verb start position
		int last_i = -1; // force one iteration
		//if (in.getParse().size() > 1) {
		while (in.getParse().size() > 1 && last_i != i) {
			// Check for preamble
			
			// TODO this could be while to strip off multiple prefixes & digit combos
			last_i = i;
			
			// first nab digits
			i = nabDigits(in, i);
			
			if (in.getParse().size() > i+1) {

				// then get pre
				String pre = in.getParse().beginsWith(i);

				if ( pre.length() == 1 || 
						(pre.length() == 2 && !pos.isTwoLetterDictionaryWord(pre)) ||
						(pre.length()  < 5 && !pre.matches(".*[gs]et.*") && 
								!pos.isPotentialVerb(pre) &&
								pf.getOnlyFrequency(pre) == 0 && 
								pf.getFirstFrequency(pre) > 0
						)
				) {
					in.getParse().get(i).setTag(POSTag.PREAMBLE);
					i++;
				}
			}
			
			//i = nabDigits(in, i); // will be handled by loop
		}
		
		// If preamble is holder, makes getVP easier
		IPhraseNode pn = in.getParse().getNewEmpty();
		if ( i > 0 ) {
			for (int j = 0; j < i; j++) {
				pn.add(in.getParse().get(0));
				in.getParse().remove(0);
			}
		}
		in.setPreamble(pn);
	}

	private int nabDigits(IProgramElementNode in, int i) {
		while (i < in.getParse().size() && in.getParse().get(i).getTag() == POSTag.DIGIT)
			i++;
		return i;
	}
	
	public void tagPrepositions(IPhraseNode node) {
		for (IWordNode w : node.getPhrase()) {
			if (pos.isPreposition(w.getWord())) {
				w.setTag(POSTag.PREPOSITION);
				//node.setContainsPrep(true);
			}
		}
	}
	
	public void tagDigits(IPhraseNode node) {
		for (IWordNode w : node.getPhrase()) {
			if (w.getWord().matches("\\d+")) {
				w.setTag(POSTag.DIGIT);
			}
		}
	}
	
	public void tagNounPhrase(IPhraseNode node) {
		tagNounPhrase(0, node.size() - 1, node);
	}
	
//	public void tagReactive(IPhraseNode node) {
//		tagNounPhrase(0, node.size() - 1, node);
//	}

	public void tagNounPhrase(int startIndex, int stopIndex, IPhraseNode node) {
		//tagDigits(); in ProgramElement
		int i = stopIndex;
		while (node.get(i).getTag() == POSTag.DIGIT) i--;
		
		// tag end
		if (i >= startIndex) {
			if (pos.isDeterminer(node.beginsWith(i)))
				node.get(i).setTag(POSTag.DETERMINER);
			else if (pos.isPronoun(node.beginsWith(i)))
				node.get(i).setTag(POSTag.PRONOUN);
			else {
				if (ignorableHeadWords.contains(node.beginsWith(i)))
					node.get(i).setTag(POSTag.NOUN_IGNORABLE);
				else
					node.get(i).setTag(POSTag.NOUN);
			}
		}
		i--;
		
		// tag rest
		for (int j = i; j >= startIndex; j--) {
			if (pos.isDeterminer(node.beginsWith(j)))
				node.get(j).setTag(POSTag.DETERMINER);
			else if (pos.isPronoun(node.beginsWith(j)))
				node.get(j).setTag(POSTag.PRONOUN);
			else if (node.get(j).getTag() != POSTag.DIGIT)
				node.get(j).setTag(POSTag.NOUN_MODIFIER);
		}
	}
	
	public HashSet<String> getIgnorableHeadWords() {
		return ignorableHeadWords;
	}

	public void setIgnorableHeadWords(HashSet<String> ignorableHeadWords) {
		this.ignorableHeadWords = ignorableHeadWords;
	}

}
