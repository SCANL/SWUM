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

/** Decorator for Nodes 
 * Implementations MUST ALSO conform to interface Node
 */
public interface SearchableNode {
	
	//public Node getNode();
	
	/**
	 * Space delimited (with extra spaces on either side)
	 * @return searchable string
	 */
	public String toSearchableString();
	
	/**
	 * Space delimited (with extra spaces on either side)
	 * containing stemmed words
	 * @return searchable string
	 */
	public String toStemmedSearchableString();
	
	/**
	 * Number of places from head position that s occurs
	 * in this node's associated word or phrase.
	 * 
	 * Example: Node phrase "auction server"
	 * getHeadPosition("auction") returns 1
	 * 
	 * Example: Node phrase "auction entry",
	 * where entry is an ignorable head word
	 * getHeadPosition("auction") returns 0
	 * 
	 * @param s - string to look for
	 * @return Number of places from head, -1 if s 
	 * occurs nowhere in phrase, accounting for 
	 * ignorable head words
	 */
	public int getHeadPosition(String s);
	
	/**
	 * Number of places from head position that s occurs
	 * in this node's associated word or phrase.
	 * 
	 * Example: Node phrase "auction server"
	 * getHeadPosition("auction") returns 1
	 * 
	 * Example: Node phrase "auction entry",
	 * where entry is an ignorable head word
	 * getHeadPosition("auction") returns 0
	 * 
	 * @param s - stemmed string to look for
	 * @return Number of places from head, -1 if s 
	 * occurs nowhere in phrase, accounting for 
	 * ignorable head words
	 */
	public int getStemmedHeadPosition(String s);
	
	public HashSet<String> getWords();
	public HashSet<String> getStemmedWords();
	public HashSet<String> getNonIgnorableStemmedWords();
}
