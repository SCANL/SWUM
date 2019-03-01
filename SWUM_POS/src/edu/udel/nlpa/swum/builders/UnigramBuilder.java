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

import edu.udel.nlpa.swum.rules.unigram.*;

public class UnigramBuilder extends RuleListSWUMBuilder {

	@Override
	public void defineRuleSet() {
		// add other rules
		rules.add(new FieldRule());
		rules.add(new ConstructorRule()); // must be first because rest apply preamble tag?
		rules.add(new EmptyNameRule());
		rules.add(new CheckerRule()); // for now follows base verb rule
		rules.add(new SpecialCaseRule()); // for now follows base verb rule
		rules.add(new ReactiveRule());
		rules.add(new EventHandlerRule()); // how to construct SWUM?
		rules.add(new NounPhraseRule());
		rules.add(new LeadingPrepositionRule());
		rules.add(new NonBaseVerbRule()); // for now follows base verb rule
		rules.add(new DefaultBaseVerbRule());	// what about preps?
	}

}
