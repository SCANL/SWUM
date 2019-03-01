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

package edu.udel.nlpa.swum.nodes.base;

import java.util.ArrayList;

import edu.udel.nlpa.swum.nodes.IPhraseNode;
import edu.udel.nlpa.swum.nodes.IWordNode;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.rules.unigram.pos.UnigramPOSData;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.constants.POSTag;
import edu.udel.nlpa.swum.utils.identifiers.ConservativeCamelCaseSplitter;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

/**
 * No knowledge of POS
 * @author hill
 *
 */
public class PhraseNode extends IPhraseNode implements Node {
	
	public PhraseNode(String id, IdentifierSplitter idSplitter) {
		super(id, idSplitter);
	}
	
	public PhraseNode(ArrayList<IWordNode> array, Location location, boolean containsPrep) {
		super(array, location, containsPrep);
	}

	public PhraseNode() {
		super();
	}

	public Object clone() {
		return new PhraseNode((ArrayList<IWordNode>) array.clone(), location, containsPrep);
	}

	@Override
	public IPhraseNode getNewEmpty() {
		return new PhraseNode();
	}
	
	protected void init(String[] id) {
		for (String s : id) {
			array.add(new WordNode(s));
		}
	}

	
}
