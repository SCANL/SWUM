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

package edu.udel.nlpa.swum.rules.unigram.pos;

import java.util.HashMap;
import java.util.HashSet;

import edu.udel.nlpa.swum.utils.IgnorableWords;
import edu.udel.nlpa.swum.utils.LibFileLoader;


public class PCKimmoPOSData implements UnigramPOSData {

	private static LibFileLoader loader = LibFileLoader.getInstance();
	
	private HashSet<String> prepositions = new HashSet<String>();
	private String prepfile = "my.preps2.txt";
	
	private HashSet<String> verbs3ps = new HashSet<String>();
	private HashSet<String> verbs3pirr = new HashSet<String>();
	private String v3psfile = "V_3PS.txt";
	private String v3psirrfile = "v3p-irr.txt";
	
	private HashSet<String> modalVerbs = new HashSet<String>();
	private String modalfile = "modal_verbs.txt";
	
	private HashSet<String> ingVerbs = new HashSet<String>();
	private String ingfile = "ING.txt";
	
	private HashSet<String> pastTenseVerbs = new HashSet<String>();
	private String edfile = "ED.txt";
	private String edirrfile = "ED-IRR.txt";
	
	private HashSet<String> pastParticipleVerbs = new HashSet<String>();
	private String enfile = "EN.txt";
	private String enirrfile = "EN-IRR.txt";
	
	private HashSet<String> potentialVerbs = new HashSet<String>();
	private String vfile = "V.txt";
	
	private HashSet<String> onlyNouns = new HashSet<String>();
	private String onfile = "O_N.txt";
	
	private HashSet<String> adjectives = new HashSet<String>();
	private String adjfile = "AJ.txt";
	
	private HashSet<String> adverbs = new HashSet<String>();
	private String advfile = "AV.txt";
	
	private HashSet<String> determiners = new HashSet<String>();
	private String detfile = "DT.txt";
	
	private HashSet<String> pronouns = new HashSet<String>();
	private String prnfile = "PRNS.txt";
	
	private HashSet<String> ignorableVerbs = new HashSet<String>();
	private String ivfile = "ignorable_verbs.txt";
	
	private HashSet<String> generalVerbs = new HashSet<String>();
	private String gvfile = "general_verbs.txt";
	
	private HashSet<String> eventWords = new HashSet<String>();
	private String ewfile = "event_words.txt";
	
	private HashSet<String> sideEffectWords = new HashSet<String>();
	private String sefile = "side_effects.txt";
	
	private HashSet<String> twoDict = new HashSet<String>();
	private String dict2file = "dict.2.txt";
	
	// particle, verb
	private HashMap<String,HashSet<String>> verbParticles = new HashMap<String,HashSet<String>>();
	private String vpfile = "VP.txt";
	
	public PCKimmoPOSData() {
		
		loader.readFileIntoHash(dict2file, twoDict);
		loader.readFileIntoHash(prepfile, prepositions);
		loader.readFileIntoHash(vfile, potentialVerbs);
		loader.readFileIntoHash(onfile, onlyNouns);
		loader.readFileIntoHash(ingfile, ingVerbs);
		loader.readFileIntoHash(edfile, pastTenseVerbs);
		loader.readFileIntoHash(edirrfile, pastTenseVerbs);
		loader.readFileIntoHash(enfile, pastParticipleVerbs);
		loader.readFileIntoHash(enirrfile, pastParticipleVerbs);
		loader.readFileIntoHash(adjfile, adjectives);
		loader.readFileIntoHash(advfile, adverbs);
		loader.readFileIntoHash(detfile, determiners);
		loader.readFileIntoHash(prnfile, pronouns);
		loader.readFileIntoNestedMap(vpfile, verbParticles);
		loader.readFileIntoHash(v3psfile, verbs3ps);
		loader.readFileIntoHash(v3psirrfile, verbs3pirr);
		loader.readFileIntoHash(modalfile, modalVerbs);
		loader.readFrequencyFileIntoHash(ivfile, ignorableVerbs);
		loader.readFileIntoHash(gvfile, generalVerbs);
		loader.readFileIntoHash(ewfile, eventWords);
		loader.readFileIntoHash(sefile, sideEffectWords);
	}
	
	public boolean isPreposition(String word) {
		return prepositions.contains(word);
	}
	
	public boolean isNoun(String word) {
		/* return siga.isNP(w)
		|| ( !posa.isVerb(w) && posa.isOnlyNoun(w) && !w.matches("fire") &&
				!w.matches("^.*up$") && !w.matches("^.*out$"))
		|| (l == 1 && w.matches("count"))
		; */
		
		// If it's not potentially a verb, it's likely a noun, and 
		// it's not an unsplit verb particle
		return !potentialVerbs.contains(word) && onlyNouns.contains(word)
			&& !word.matches("^.*(up|down|out)$");
		
		// Popular NP indicators to learn:
		/*NPIndicators.add("size");
		NPIndicators.add("key");
		NPIndicators.add("keys");
		NPIndicators.add("value");
		NPIndicators.add("type");
		NPIndicators.add("id");
		NPIndicators.add("item");
		NPIndicators.add("state");
		NPIndicators.add("length");
		NPIndicators.add("index");
		NPIndicators.add("error");
		NPIndicators.add("name");*/
	}


	public boolean isVerbParticle(String verb, String word) {
		if (!verbParticles.containsKey(word))
			return false;
		if (verbParticles.get(word).contains(verb))
			return true;
		return false;
	}
	
	public boolean isPastParticiple(String word) {
		return pastParticipleVerbs.contains(word);
	}

	public boolean isPastTense(String word) {
		return pastTenseVerbs.contains(word);
	}

	public boolean isPresentParticiple(String word) {
		return ingVerbs.contains(word);
	}

	public boolean isAdjective(String word) {
		return !potentialVerbs.contains(word) && 
			adjectives.contains(word);
	}
	
	public boolean isAdverb(String word) {
		return adverbs.contains(word);
	}

	public boolean isDeterminer(String word) {
		return determiners.contains(word);
	}
	
	public boolean isPronoun(String word) {
		return pronouns.contains(word);
	}

	public boolean is3rdPersonSingularVerb(String word) {
		return verbs3ps.contains(word);
	}
	
	public boolean is3rdPersonIrregularVerb(String word) {
		return verbs3pirr.contains(word);
	}

	public boolean isModalVerb(String word) {
		return modalVerbs.contains(word);
	}

	public boolean isIgnorableVerb(String word) {
		return ignorableVerbs.contains(word);
	}
	
	public boolean isGeneralVerb(String word) {
		return generalVerbs.contains(word);
	}
	
	public boolean isEventWord(String word) {
		return eventWords.contains(word);
	}
	
	public boolean isSideEffectWord(String word) {
		return sideEffectWords.contains(word);
	}

	public boolean isTwoLetterDictionaryWord(String word) {
		return twoDict.contains(word);
	}

	public boolean isPotentialVerb(String word) {
		return potentialVerbs.contains(word);
	}

	public boolean isIgnorableHeadWord(String word) {
		return IgnorableWords.getInstance().isIgnorableHeadWord(word);
	}

}
