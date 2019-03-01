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

package edu.udel.nlpa.swum.rules.unigram;

import java.util.ArrayList;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.nodes.base.PhraseNode;
import edu.udel.nlpa.swum.nodes.base.TypeNode;
import edu.udel.nlpa.swum.nodes.base.VariableDecl;
import edu.udel.nlpa.swum.rules.Rule;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.tagger.UnigramTagger;
import edu.udel.nlpa.swum.utils.constants.MethodRole;
import edu.udel.nlpa.swum.utils.constants.POSTag;
import edu.udel.nlpa.swum.utils.constants.ProgramElementType;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.context.MethodContext;
import edu.udel.nlpa.swum.utils.identifiers.ConservativeCamelCaseSplitter;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public class BaseVerbRule extends UnigramMethodRule {
		
	/**
	 * Base Verb assuming no prior rules and no prepositions.
	 * WARNING: side affect of calling this method is
	 * to strip potential preamble and tag prepositions, based
	 * on Rule's preTag implementation in Tagger
	 * 
	 * Only implemented for method declarations so far.
	 */
	/*public boolean inClass(ProgramElementNode in) {
		if (in instanceof MethodDeclaration) {
			MethodDeclaration md = (MethodDeclaration)in;
			if (md.getContext().isConstructor()) return false;
			
			// parse separate from creation so splitter can live in rule
			md.parse(idSplitter);
			tagger.preTag(in);
			return makeClassification(md);
		}
		return false;
	}*/

	protected boolean makeClassification(IMethodDecl md) {
		String word = md.getParse().beginsWith();
		
		// TODO: Change to be syntactic detectors
		if (isChecker(word)) return false;
		if (isSpecialCase(word)) return false;
		if (isEventHandler(md.getParse()) ||
				isEventHandler(md.getGiven())) return false;
		if (startsNounPhrase(word)) return false;
		if (isPrepositionalPhrase(md.getParse())) return false;
		if (isNonBaseVerb(word)) return false;
		
		return true;
	}
	
	/**
	 * Base Verb -> V [VP|VM]? (DT|NM)* N?
	 */
	public RuleIndicator constructSWUM(IProgramElementNode in) {
		// rule only applies to method declarations/invocations
		if (in instanceof IMethodDecl) { 
			IMethodDecl md = (IMethodDecl)in;
			
			parseBaseVerbName(md); // Now it's parsed & split
			
			// Identify Method role
			determineMethodRole(md);
			
			md.assignStructuralInformation(idSplitter, tagger);
			setDefaultActionAndThemes(md);
			md.setConstructedSWUM(true);
			
			return RuleIndicator.BASE_VERB;
			
		} else { return RuleIndicator.UNKOWN; }
	}

	private void determineMethodRole(IMethodDecl md) {
		if (md.getContext().getType().matches("void"))
			md.setRole(MethodRole.ACTION);
		else
			md.setRole(MethodRole.FUNCTION);

		if (md.getRole().equals(MethodRole.FUNCTION) && 
				md.getParse().beginsWith().matches("get"))
			md.setRole(MethodRole.GETTER);
		else if (md.getRole().equals(MethodRole.ACTION) &&
				md.getParse().beginsWith().matches("set"))
			md.setRole(MethodRole.SETTER);
	}

}
