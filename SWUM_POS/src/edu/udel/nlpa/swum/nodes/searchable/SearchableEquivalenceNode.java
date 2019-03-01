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

import edu.udel.nlpa.swum.nodes.IEquivalenceNode;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.base.EquivalenceNode;
import edu.udel.nlpa.swum.utils.constants.Location;

public class SearchableEquivalenceNode extends IEquivalenceNode implements SearchableNode {
	
	public SearchableEquivalenceNode(SearchableNode n) {
		super((Node) n);
	}
	
	// NO GUARANTEE
	public void addEquivalentNode(SearchableNode n) {
		if (equivalentNodes == null)
			equivalentNodes = new ArrayList<Node>();
		equivalentNodes.add((Node)n);
	}

	public String toSearchableString() {
		String s = "";
		for (Node n : equivalentNodes) {
			s += ((SearchableNode)n).toSearchableString();
		}
		return s;
	}

	public String toStemmedSearchableString() {
		String s = "";
		for (Node n : equivalentNodes) {
			s += ((SearchableNode)n).toStemmedSearchableString();
		}
		return s;
	}
	
	public int getHeadPosition(String s) {
		int min = -1;
		for (Node n : equivalentNodes) {
			int t = ((SearchableNode)n).getHeadPosition(s);
			if (t > -1 && min > -1)
				min = Math.min(min, t);
			else if (t > -1 && min == -1)
				min = t;
		}
		return min;
	}

	public int getStemmedHeadPosition(String s) {
		int min = -1;
		for (Node n : equivalentNodes) {
			int t = ((SearchableNode)n).getStemmedHeadPosition(s);
			if (t > -1 && min > -1)
				min = Math.min(min, t);
			else if (t > -1 && min == -1)
				min = t;
		}
		return min;
	}
	
	public HashSet<String> getStemmedWords() {
		HashSet<String> sw = new HashSet<String>();

		for (Node n : equivalentNodes)
			sw.addAll(((SearchableNode)n).getStemmedWords());
		
		return sw;
	}
	
	public HashSet<String> getNonIgnorableStemmedWords() {
		HashSet<String> sw = new HashSet<String>();

		for (Node n : equivalentNodes)
			sw.addAll(((SearchableNode)n).getNonIgnorableStemmedWords());
		
		return sw;
	}
	
	public HashSet<String> getWords() {
		HashSet<String> sw = new HashSet<String>();

		for (Node n : equivalentNodes)
			sw.addAll(((SearchableNode)n).getWords());
		
		return sw;
	}

}
