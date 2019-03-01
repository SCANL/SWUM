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

package edu.udel.nlpa.swum.builders;

import java.util.ArrayList;

import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.rules.Rule;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

/**
 * Applies rules in order to a given program element node. SWUM is constructed
 * for first matching rule.
 * 
 * Be careful of waste in the rules -- if the classifiers aren't lightweight,
 * then each classifier will be applying the same conditions (wastefully)
 * @author hill
 *
 */
public abstract class RuleListSWUMBuilder implements ISWUMBuilder {
	protected ArrayList<Rule> rules = new ArrayList<Rule>();
	
	public RuleListSWUMBuilder() {
		defineRuleSet();
	}
	
	/**
	 * Applies rules in order to a given program element node. SWUM is constructed
	 * for first matching rule.
	 */
	public RuleIndicator applyRules(IProgramElementNode in) {
		for (Rule r : rules) {
			if (r.inClass(in))
				return r.constructSWUM(in);
		}
		return null;
	}
	
	public abstract void defineRuleSet();
}
