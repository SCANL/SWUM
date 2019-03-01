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

import edu.udel.nlpa.swum.nodes.IVariableDecl;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

/**
 * Variable Declaration
 * @author hill
 *
 */
public class VariableDecl extends IVariableDecl {

	public VariableDecl(String n, String t, IdentifierSplitter idSplitter,
			ITagger tagger) {
		super(n, t, idSplitter, tagger);
	}
	
	public VariableDecl(String n, String t, Location l, int pos,
			IdentifierSplitter idSplitter, ITagger tagger) {
		super(n,t,l, pos, idSplitter,tagger);
	}
	
	public VariableDecl(String n, String t, Location l,
			IdentifierSplitter idSplitter, ITagger tagger) {
		super(n,t,l,idSplitter,tagger);
	}

	public void parse(IdentifierSplitter idSplitter) {
		baseParse(idSplitter);
	}

	@Override
	public void initTypeName(String t, IdentifierSplitter idSplitter,
			ITagger tagger) {
		typeName = new TypeNode(t, idSplitter, tagger);
		
	}

}
