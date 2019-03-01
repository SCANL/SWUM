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

package edu.udel.nlpa.swum.utils;

import java.util.HashSet;

/**
 * Model for singleton classes
 * @author hill
 *
 */
public class IgnorableWords {
	protected LibFileLoader loader = LibFileLoader.getInstance();
	protected HashSet<String> formalHeadWords = new HashSet<String>();
	private String hfile = "formal.head_words.txt";
	
	private IgnorableWords() { 
		loader.readFrequencyFileIntoHash(hfile, formalHeadWords);
	}

	public static IgnorableWords getInstance() {
		if (ref == null)
			ref = new IgnorableWords();		
		return ref;
	}

	public Object clone() throws CloneNotSupportedException { 
		throw new CloneNotSupportedException(); 
	}

	private static IgnorableWords ref;

	public HashSet<String> getIgnorableHeadWords() {
		return formalHeadWords;
	}
	
	public boolean isIgnorableHeadWord(String w) {
		return formalHeadWords.contains(w);
	}
	
	
}
