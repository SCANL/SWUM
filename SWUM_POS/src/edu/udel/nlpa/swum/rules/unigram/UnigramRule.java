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

import java.util.HashSet;

import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.rules.Rule;
import edu.udel.nlpa.swum.rules.unigram.pos.PCKimmoPOSData;
import edu.udel.nlpa.swum.rules.unigram.pos.UnigramPOSData;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.tagger.UnigramTagger;
import edu.udel.nlpa.swum.utils.LibFileLoader;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.identifiers.ConservativeCamelCaseSplitter;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

/**
 * Base class for unigram-style SWUM rules for METHODS
 * @author hill
 *
 */
public abstract class UnigramRule implements Rule {
	
	// Rules specify Tagger and IdSplitter info
	// Nodes just know how to use those interfaces to construct themselves
	protected static UnigramPOSData pos;
	protected static ITagger tagger;
	protected static IdentifierSplitter idSplitter;
	protected static LibFileLoader loader = LibFileLoader.getInstance();
	
	protected static HashSet<String> specialWords = new HashSet<String>(); // special first words
	protected static HashSet<String> booleanArgVerbs = new HashSet<String>();
	protected static HashSet<String> NPIndicators = new HashSet<String>();
	
	private static HashSet<String> primitiveTypes = new HashSet<String>();
	//private static HashSet<String> multipleArguments = new HashSet<String>();
	
	public UnigramRule() {
		
		pos = new PCKimmoPOSData(); 						// rule specifies POS Data
		tagger = new UnigramTagger(pos);   // and default tagger
		idSplitter = new ConservativeCamelCaseSplitter();
		
		specialWords.add("main");
		specialWords.add("do");
		specialWords.add("go");
		//specialWords.add("handle");
		specialWords.add("process");
		specialWords.add("execute");
		specialWords.add("run");
		
		specialWords.add("remove");
		specialWords.add("delete");
		specialWords.add("pop");
		specialWords.add("dequeue");
		
		specialWords.add("set");
		specialWords.add("reset");
		specialWords.add("enable");
		specialWords.add("replace");
		
		specialWords.add("init");
		specialWords.add("initialize");
		
		specialWords.add("get");
		specialWords.add("return");
		specialWords.add("retrieve");
		specialWords.add("copy");
		specialWords.add("create");
		specialWords.add("construct");
		specialWords.add("clone");
		specialWords.add("duplicate");
		specialWords.add("load");
		specialWords.add("read");
		
		specialWords.add("add");
		specialWords.add("push");
		specialWords.add("append");
		specialWords.add("print");
		specialWords.add("println");
		specialWords.add("write");
		specialWords.add("enqueue");
		// save, store?
		
		specialWords.add("compare");
		specialWords.add("check");
		specialWords.add("assert"); // contains?
		
		specialWords.add("sort");
		specialWords.add("order");
		specialWords.add("search");
		specialWords.add("find");
		
		specialWords.add("start");
		specialWords.add("end");
		specialWords.add("render");
		specialWords.add("force");
		specialWords.add("test");

		
		//-----------------------
		
		NPIndicators.add("size");
		NPIndicators.add("key");
		NPIndicators.add("keys");
		NPIndicators.add("value");
		NPIndicators.add("type");
		NPIndicators.add("id");
		NPIndicators.add("item");
		NPIndicators.add("state");
		NPIndicators.add("length");
		NPIndicators.add("index");
		NPIndicators.add("error");
		NPIndicators.add("name");
		
		/*multipleArguments.add("compare");
		multipleArguments.add("add");
		multipleArguments.add("set");
		multipleArguments.add("copy");*/
		
		booleanArgVerbs.add("check");
		booleanArgVerbs.add("assert");
		booleanArgVerbs.add("add");
		booleanArgVerbs.add("contains");
		booleanArgVerbs.add("print");
		booleanArgVerbs.add("println");
		booleanArgVerbs.add("append");
		booleanArgVerbs.add("add");
		booleanArgVerbs.add("push");
		booleanArgVerbs.add("set"); //queue/dequeue
		
		primitiveTypes.add("byte");
		primitiveTypes.add("short");
		primitiveTypes.add("int");
		primitiveTypes.add("long");
		primitiveTypes.add("float");
		primitiveTypes.add("double");
		primitiveTypes.add("boolean");
		primitiveTypes.add("char");
		primitiveTypes.add("String");
		primitiveTypes.add("string");
		primitiveTypes.add("void");
	}

	public abstract boolean inClass(IProgramElementNode in);
	public abstract RuleIndicator constructSWUM(IProgramElementNode in);
	
	public boolean isPrimitive(String type) {
		return primitiveTypes.contains(type);
	}

}
