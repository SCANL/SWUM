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

/**
 * TODO Don't know what information this needs yet
 * WORK IN PROGRESS
 * @author hill
 *
 */
public interface UnigramPOSData {
	public boolean isPreposition(String word);
	public boolean isNoun(String word);
	public boolean isAdjective(String word);
	public boolean isAdverb(String word);
	public boolean isDeterminer(String word);
	public boolean isPronoun(String word);
	public boolean isPastTense(String word);
	public boolean isPastParticiple(String word);
	public boolean isPresentParticiple(String word);
	public boolean isVerbParticle(String verb, String word);
	public boolean is3rdPersonSingularVerb(String word);
	public boolean is3rdPersonIrregularVerb(String word);
	public boolean isModalVerb(String word);
	public boolean isIgnorableVerb(String word);
	public boolean isIgnorableHeadWord(String word);
	public boolean isTwoLetterDictionaryWord(String word);
	public boolean isPotentialVerb(String word);
	public boolean isGeneralVerb(String word);
	public boolean isEventWord(String word);
	public boolean isSideEffectWord(String word);
}
