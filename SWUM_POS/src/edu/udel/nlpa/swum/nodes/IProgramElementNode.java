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

package edu.udel.nlpa.swum.nodes;

import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.PhraseNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchablePhraseNode;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;


/**
 * IdentifierSplitter set here -- NO NODE INSTANTIATION
 * @author hill
 *
 */
public abstract class IProgramElementNode implements Node {
	
	protected String name = "";
	protected IPhraseNode preamble = null;
	protected IPhraseNode parse = null;
	protected Location location = Location.NONE;
	protected boolean constructedSWUM = false; // true if Rule.constructSWUM called on node
	protected double score = 0d;
	
	public IProgramElementNode() { } 
	
	public IProgramElementNode(String p) {
		name = p;
	}
	
	public IProgramElementNode(String p, IdentifierSplitter idSplitter) {
		this(p);
		parse(idSplitter);
	}
	
	public IProgramElementNode(String n, PhraseNode p) {
		name = n;
		parse = p;
	}
	
	public abstract void parse(IdentifierSplitter idSplitter);
	
	public String getName() {
		return name;
	}

	public void setName(String n) {
		this.name = n;
	}
	
	public String toString() {
		return parse.toString();
	}

	public IPhraseNode getParse() {
		return parse;
	}

	public void setParse(PhraseNode parse) {
		this.parse = parse;
	}

	public IPhraseNode getPreamble() {
		return preamble;
	}

	public void setPreamble(IPhraseNode preamble) {
		this.preamble = preamble;
	}

	public boolean isConstructedSWUM() {
		return constructedSWUM;
	}

	public void setConstructedSWUM(boolean constructedSWUM) {
		this.constructedSWUM = constructedSWUM;
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	/** Slightly breaking separation in the interest of keeping
	 * related code in one place -- no copy/paste
	 * @param idSplitter
	 */
	protected void baseParse(IdentifierSplitter idSplitter) {
		if (parse == null)
			parse = new PhraseNode(name, idSplitter);
	}
	
	public double getScore() { return score; }
	public void setScore(double s) { score = s; }
}
