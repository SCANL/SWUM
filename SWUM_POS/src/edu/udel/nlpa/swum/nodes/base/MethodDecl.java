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

import edu.udel.nlpa.swum.nodes.IArgumentNode;
import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.ITypeNode;
import edu.udel.nlpa.swum.nodes.IVariableDecl;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.utils.constants.MethodRole;
import edu.udel.nlpa.swum.utils.constants.ProgramElementType;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.context.MethodContext;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

/** Method Declaration **/
public class MethodDecl extends IMethodDecl {

	public MethodDecl(String n) {
		super(n);
	}
	
	public MethodDecl(String n, MethodContext c) {
		super(n, c);
	}
	
	public void parse(IdentifierSplitter idSplitter) {
		baseParse(idSplitter);
	}
	
	public void assignStructuralInformation(IdentifierSplitter idSplitter, ITagger tagger) {
		parse.setLocation(Location.NAME);
		returnType = new TypeNode(ctx.getType(), Location.RETURN, idSplitter, tagger);
		
		returns = new ArrayList<Node>();
		returns.add(returnType);
		
		given = new ArrayList<Node>();
		
		if (!ctx.getFormals().isEmpty()) {

			int i = 0;
			for (String f : ctx.getFormals()) {
				String[] s = f.split("\\s+");
				if (s.length == 2) {
					given.add(new VariableDecl(s[1], s[0], Location.FORMAL, i, idSplitter, tagger));
				}
				i++;
			}
		}
		onClass = new TypeNode(ctx.getDeclaringClass(), Location.ONCLASS, idSplitter, tagger);
	}
	
	
}
