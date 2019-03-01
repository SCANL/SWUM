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

import java.util.HashMap;

/**
 * Model for singleton classes
 * @author hill
 *
 */
public class PositionalFrequencies {
	
	private static PositionalFrequencies ref;
	private static HashMap<String, int[]> freq; 
	
	private static int FIRST  = 0;
	private static int MIDDLE = 1;
	private static int LAST   = 2;
	private static int ONLY   = 3;
	private static int TOTAL  = 4;
	
	private PositionalFrequencies() { 
		freq = new HashMap<String, int[]>();
	}
	
	public void addFrequency(String word, int first, int middle, int last, int only) {
		//int[] f = {first, middle, last, only};
		int[] f = new int[5];
		f[FIRST] = first;
		f[MIDDLE] = middle;
		f[LAST] = last;
		f[ONLY] = only;
		f[TOTAL] = first + middle + last + only;
		freq.put(word, f);
	}
	
	public int getFirstFrequency(String word) {
		return getFrequency(word, FIRST);
	}
	
	public int getMiddleFrequency(String word) {
		return getFrequency(word, MIDDLE);
	}
	
	public int getLastFrequency(String word) {
		return getFrequency(word, LAST);
	}
	
	public int getOnlyFrequency(String word) {
		return getFrequency(word, ONLY);
	}
	
	public int getTotalFrequency(String word) {
		return getFrequency(word, TOTAL);
	}
	
	private int getFrequency(String word, int index) {
		if (!freq.containsKey(word))
			return 0;
		return freq.get(word)[index];
	}
	
	public double getVerbProbability(String word) {
		double p = 100d;
		p *= (getFrequency(word, FIRST) + getFrequency(word, ONLY));
		
		int tot = getFrequency(word, TOTAL);
		if (tot > 0)
			p /= tot;
		else
			p = 0d;
		
		return p;
	}
	
	public double getNounProbability(String word) {
		double p = 100d;
		p *= (getFrequency(word, MIDDLE) + getFrequency(word, LAST));
		
		int tot = getFrequency(word, TOTAL);
		if (tot > 0)
			p /= tot;
		else
			p = 0d;
		
		return p;
	}

	public static PositionalFrequencies getInstance() {
		if (ref == null) {
			ref = new PositionalFrequencies();
			ref = LibFileLoader.getInstance().readPositionalFrequencyFile("v_positional_freq.txt");
		}
		return ref;
	}

	public Object clone() throws CloneNotSupportedException { 
		throw new CloneNotSupportedException(); 
	}
}
