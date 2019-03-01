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

import edu.udel.nlpa.swum.nodes.ITypeNode;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public class TypeNode extends ITypeNode {
	
	public TypeNode(String s, IdentifierSplitter idSplitter) {
		super(s, idSplitter);
	}
	
	public TypeNode(String s, Location loc, IdentifierSplitter idSplitter, ITagger tagger) {
		super(s, loc, idSplitter, tagger);
	}

	public TypeNode(String t, IdentifierSplitter idSplitter, ITagger tagger) {
		super(t,idSplitter, tagger);
	}

	@Override
	public void parse(IdentifierSplitter idSplitter) {
		baseParse(idSplitter);
	}
}
