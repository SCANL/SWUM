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

package edu.udel.nlpa.swum.utils.constants;

public enum POSTag {
	NOUN {
		public String toString() { return "N"; }
		/*public boolean isNP() { return true; }
		public boolean isVerb() { return false; }*/
	}, 
	NOUN_IGNORABLE { // for ignorable head words
		public String toString() { return "NI"; }
	}, 
	NOUN_PLURAL { // for plural nouns TODO
		public String toString() { return "NPL"; }
	},
	NOUN_MODIFIER { // noun or adj
		public String toString() { return "NM"; }
	}, 
	NON_VERB { // adj, adv, noun occurring BEFORE verb in name
		public String toString() { return "NV"; }
	},
	VERB_IGNORABLE {
		public String toString() { return "VI"; }
	},
	VERB {
		public String toString() { return "V"; }
	},
	PAST_PARTICIPLE { // -ed -en
		public String toString() { return "PP"; }
	},
	VERB_3PS { // -s
		public String toString() { return "V3PS"; }
	},
	VERB_ING { // -ing
		public String toString() { return "VING"; }
	},
	/*VERB_BASE { // infinitive
		public String toString() { return "VB"; }
	},*/
	VERB_MODIFIER { // adv
		public String toString() { return "VM"; }
	},
	VERB_PARTICLE { // tear down, 
		public String toString() { return "VP"; }
	},
	PREPOSITION {
		public String toString() { return "P"; }
	},
	DIGIT {
		public String toString() { return "D"; }
	},
	DETERMINER {
		public String toString() { return "DT"; }
	},
	PRONOUN {
		public String toString() { return "PR"; }
	},
	PREAMBLE {
		public String toString() { return "PRE"; }
	},
	UNKNOWN {
		public String toString() { return "UN"; }
	}
}
