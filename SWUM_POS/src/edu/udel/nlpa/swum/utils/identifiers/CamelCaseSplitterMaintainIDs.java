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

public class CamelCaseSplitterMaintainIDs implements IdentifierSplitter {

	public String[] splitId(String id) {
		id = basicSplit(id);
		id = id.trim();
		return id.split("\\s+");
	}
	
	public String splitIdIntoSpaces(String id) {
		id = basicSplit(id);
		return id.toLowerCase();
	}

	public String basicSplit(String id) {
		//# remove punctuation
		
		id = id.replaceAll("[^a-zA-Z0-9]", " ");
		
		String orig = id;
				
		id = " " + id + " ";
		
		// Separate numbers not already sep. by underscore
		id = id.replaceAll("([a-zA-Z])([0-9])", "$1 $2");
		id = id.replaceAll("([0-9])([a-zA-Z])", "$1 $2");
				
		// default camel case
		id = id.replaceAll("([a-z])([A-Z])", "$1 $2");
		
		// Issue: plural acronyms
		// APIs: AP Is or APIs?
		id = id.replaceAll("([A-Z])([A-Z][a-z])", "$1 $2");

		//# Remove leading and trailing space, and any 2+ space -> 1
		//id = id.toLowerCase();
		id = id.replaceAll("\\s+", " ");
		
		return orig + " " + id;
	}


}
