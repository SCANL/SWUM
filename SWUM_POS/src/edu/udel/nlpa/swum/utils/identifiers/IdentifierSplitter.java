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

package edu.udel.nlpa.swum.utils.identifiers;

public interface IdentifierSplitter {
	
	/**
	 * Takes a camel-cased identifier an outputs a 
	 * string array of tokens -- maintaining case
	 * information.
	 * 
	 * @param id: camel-cased identifier to be split
	 * @return a string array of tokens
	 */
	public abstract String[] splitId(String id);
	
	/**
	 * Takes a camel-cased identifier an outputs a
	 * space-delimited string of tokens -- NOT maintaiing
	 * case information.
	 * 
	 * @param id: camel-cased identifier to be split
	 * @return a space-delimited string of tokens
	 */
	public abstract String splitIdIntoSpaces(String id);
}
