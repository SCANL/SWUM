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
import java.util.HashSet;

import edu.udel.nlpa.swum.nodes.CompositeNodeFactory;
import edu.udel.nlpa.swum.nodes.IArgumentNode;
import edu.udel.nlpa.swum.nodes.IEquivalenceNode;
import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IPhraseNode;
import edu.udel.nlpa.swum.nodes.IVariableDecl;
import edu.udel.nlpa.swum.nodes.IWordNode;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.ArgumentNode;
import edu.udel.nlpa.swum.nodes.base.EquivalenceNode;
import edu.udel.nlpa.swum.nodes.base.FieldDecl;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.nodes.base.PhraseNode;
import edu.udel.nlpa.swum.nodes.base.TypeNode;
import edu.udel.nlpa.swum.nodes.base.VariableDecl;
import edu.udel.nlpa.swum.nodes.base.WordNode;
import edu.udel.nlpa.swum.rules.Rule;
import edu.udel.nlpa.swum.rules.unigram.pos.PCKimmoPOSData;
import edu.udel.nlpa.swum.rules.unigram.pos.UnigramPOSData;
import edu.udel.nlpa.swum.tagger.ITagger;
import edu.udel.nlpa.swum.tagger.UnigramTagger;
import edu.udel.nlpa.swum.utils.IgnorableWords;
import edu.udel.nlpa.swum.utils.LibFileLoader;
import edu.udel.nlpa.swum.utils.PositionalFrequencies;
import edu.udel.nlpa.swum.utils.constants.POSTag;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.constants.Location;
import edu.udel.nlpa.swum.utils.identifiers.ConservativeCamelCaseSplitter;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

/**
 * Base class for unigram-style SWUM rules for METHODS
 * @author hill
 *
 */
public abstract class UnigramMethodRule extends UnigramRule implements Rule {
	
	private static PositionalFrequencies pf = PositionalFrequencies.getInstance();

	/**
	 * WARNING: side affect of calling this method is
	 * to strip potential preamble and tag prepositions, based
	 * on Rule's preTag implementation in Tagger
	 * 
	 * Only implemented for method declarations so far.
	 */
	public boolean inClass(IProgramElementNode in) {
		if (in instanceof IMethodDecl) {
			IMethodDecl md = (IMethodDecl)in;
			
			// parse separate from creation so splitter can live in rule
			md.parse(idSplitter);
			tagger.preTag(in);
			
			return makeClassification(md);
		} 
		return false;
	}
	
	protected abstract boolean makeClassification(IMethodDecl md);
	public abstract RuleIndicator constructSWUM(IProgramElementNode in);

	protected IPhraseNode getVP(IPhraseNode name, IPhraseNode pre) {
		IPhraseNode phrase = pre;
		for (int i = 0; i < name.size(); i++) {
			POSTag tag = name.get(i).getTag();
			if (tag == POSTag.VERB || tag == POSTag.VERB_MODIFIER || 
					tag == POSTag.VERB_PARTICLE || tag == POSTag.NON_VERB || 
					tag == POSTag.VERB_IGNORABLE) {
				phrase.add(name.get(i));
			}			
		}
		
		return phrase;
	}
	
	protected IPhraseNode getNP(IPhraseNode name) {		
		return getNP(name, 0);
	}
	
	protected IPhraseNode getNP(IPhraseNode name, int start) {
		IPhraseNode phrase = name.getNewEmpty();
		
		for (int i = start; i < name.size(); i++) {
			POSTag tag = name.get(i).getTag();
			if (tag == POSTag.NOUN || tag == POSTag.NOUN_MODIFIER || tag == POSTag.DETERMINER
				 || tag == POSTag.PRONOUN || tag == POSTag.NOUN_IGNORABLE
				 || tag == POSTag.DIGIT || tag == POSTag.PREAMBLE) {
				phrase.add(name.get(i));
			} else if (tag == POSTag.PREPOSITION) { return phrase; }
		}
		
		return phrase;
	}
	
	// boolean checker indicators
	protected boolean isChecker(String id) {
		return pos.is3rdPersonSingularVerb(id) || pos.is3rdPersonIrregularVerb(id) ||
			   pos.isModalVerb(id) /*|| id.matches("check")*/;
	}
	
	protected boolean isPrep(String id) {
		return pos.isPreposition(id);
	}
	
	protected boolean isPrepositionalPhrase(IPhraseNode parse) {
		for (IWordNode w : parse.getPhrase()) {
			if (w.getTag() == POSTag.PREPOSITION) {
				return true;
			}
		}
		return false;
	}
	
	protected void parseBaseVerbName(IMethodDecl md) {
		// assumes preamble stripped
		IPhraseNode parse = md.getParse();
		
		// TODO what if starts with adverb?!?
		
		// if 1 or fewer words, assume verb
		if (parse.size() <= 1) {
			if (pos.isIgnorableVerb(parse.beginsWith()) || pos.isModalVerb(parse.beginsWith())) // added 6/19/11
				parse.get(0).setTag(POSTag.VERB_IGNORABLE);
			else
				parse.get(0).setTag(POSTag.VERB);
			return;
		}
		
		// Otherwise, the second word might be the verb
		int i = 0; // assume first word is verb unless...
		i = checkForIngorableVerb(parse, i);
		//i = checkForIngorableVerb(parse, i); // TODO I've seen a max of 2 ignorable
		
		if (i < parse.size()) {	
			if (pos.isAdverb(parse.beginsWith(i)) 
					&& parse.beginsWith(i).endsWith("ly") // TODO verify!!!
					&& !pos.isDeterminer(parse.beginsWith(i))
					&& !pos.isPronoun(parse.beginsWith(i))
					&& !pos.isPreposition(parse.beginsWith(i))) { // TODO test
				if (i + 1 < parse.size() && pos.isPotentialVerb(parse.beginsWith(i+1))) { // if we can set the following word to be verb
					parse.get(i).setTag(POSTag.VERB_MODIFIER);
					i++;
				}
			}
		}
		
		if (pos.isIgnorableVerb(parse.beginsWith(i)) || pos.isModalVerb(parse.beginsWith(i)))  // added 6/19/11
			parse.get(i).setTag(POSTag.VERB_IGNORABLE);
		else
			parse.get(i).setTag(POSTag.VERB);
		
		// should have ID'ed main verb by now
			
		int begin = i+1;
		
		if (begin < parse.size()) {		
			if ( pos.isVerbParticle(parse.beginsWith(i), parse.beginsWith(begin)) ) { // TODO test
				parse.get(begin).setTag(POSTag.VERB_PARTICLE);
				begin++;
			} /*else if (pos.isAdverb(parse.beginsWith(begin)) 
					//&& parse.beginsWith(begin).endsWith("ly") // TODO verify!!!
					&& !pos.isDeterminer(parse.beginsWith(begin))
					&& !pos.isPronoun(parse.beginsWith(begin))
					&& !pos.isPreposition(parse.beginsWith(begin))) { // TODO test
				parse.get(begin).setTag(POSTag.VERB_MODIFIER);
				begin++;
			}*/  // VM's have to come before verb (counter -- lookupLocally(Name)
		}

		if (begin < parse.size()) { // rest are objects or preps
			int prep = findFirstPreposition(parse, begin);
			if (prep == -1)
				tagger.tagNounPhrase(begin, parse.size() - 1, parse);
			else { // we found a prep: could be VX?PY?(f?)
				boolean noX = false;
				boolean noY = false;
				if (begin == prep) // no X
					noX = true;
				else
					tagger.tagNounPhrase(begin, prep - 1, parse);
				// TODO tag rest!
				begin = prep+1;
				if (begin == parse.size()) { // no Y
					noY = true;
				} else if (begin < parse.size())
					tagger.tagNounPhrase(begin, parse.size() - 1, parse);
			}
		}
	}

	private int checkForIngorableVerb(IPhraseNode parse, int i) {
		if (i < parse.size() - 1 && // make sure last word in name is verb 
				( pos.isIgnorableVerb(parse.beginsWith(i)) &&
				( pf.getVerbProbability(parse.beginsWith(i+1)) 
					> pf.getNounProbability(parse.beginsWith(i+1)) )
					|| // modal added 6/19/11
					pos.isModalVerb(parse.beginsWith(i)) )
		) {
			parse.get(i).setTag(POSTag.VERB_IGNORABLE);
			i++;
		}
		return i;
	}
	
	protected int findFirstPreposition(IPhraseNode parse, int begin) {
		for (int i = begin; i < parse.size(); i++)
			if (parse.get(i).getTag() == POSTag.PREPOSITION)
				return i;
		return -1;
	}
	
	/*protected int findFirstBoolArg(MethodDecl md) {
		for (int i = 0; i < md.getGiven().size(); i++) {
			Node n = md.getGiven().get(i);
			if (n instanceof VariableDecl &&
					((VariableDecl)n).getType().getName().matches("boolean") )
				return i;
		}
		return -1;
	}
	
	// TO DO need to search Unknown args?
	// OR add used in SWUM already boolean?
	protected int findFirstNonBoolArg(MethodDecl md) {
		for (int i = 0; i < md.getGiven().size(); i++) {
			Node n = md.getGiven().get(i);
			if (n instanceof VariableDecl &&
					! ((VariableDecl)n).getType().getName().matches("boolean") )
				return i;
		}
		return -1;
	}*/
	
	protected boolean hasAllBooleanArgs(IMethodDecl md) {
		for (int i = 0; i < md.getGiven().size(); i++) {
			Node n = md.getGiven().get(i);
			if (n instanceof VariableDecl &&
					! ((IVariableDecl)n).getType().getName().matches("boolean") )
				return false;
		}
		return true;
	}
	
	protected void setDefaultActionAndThemes(IMethodDecl md) {
		
		// set action
		md.setAction(this.getVP(md.getParse(), md.getPreamble()));
		
		setPrepThemeAndArgs(md);

		// set theme
		//setTheme(md);

		// set secondary args
		//setSecondaryArgs(md);

	}
	
	protected void setPrepThemeAndArgs(IMethodDecl md) {
		// setup
		ArrayList<Node> unusedArgs = new ArrayList<Node>();
		populateUnknownArgs(md, unusedArgs);
		int p = findFirstPreposition(md.getParse(), 0); // > -1 for prep
		IPhraseNode nameTheme = getNP(md.getParse());
		boolean checkDO = false; // check DO in name for overlap
		boolean checkIO = false; // check IO in name for overlap

		// Assign args
		if (p > -1) { // if there's a prep in name
			IWordNode prep = md.getParse().get(p);
			IPhraseNode io = getNP(md.getParse(), p+1);
			
			// set IO or P -> NM
			if (!io.isEmpty()) { // io in name
				IArgumentNode arg = CompositeNodeFactory.getArgumentNode(io, prep);
				setIO(md, arg);
				checkIO = true; // don't check overlap until theme found
			} else if (md.getUnknownArgs() != null 
					&& !md.getUnknownArgs().isEmpty()) { // or IO = f
				// NOT working?
				IArgumentNode arg = CompositeNodeFactory.getArgumentNode(md.getUnknownArgs().get(0), 
						prep);
				setIO(md, arg);
			} else { // P -> NM
				handleNoPrepArg(md, p);
				nameTheme = getNP(md.getParse()); // reset name theme for DO
			}
			
			// set theme
			// set second to ensure IO hunt doesn't pick class
			if (!nameTheme.isEmpty()) { // if theme in name
				setThemeToName(md, nameTheme);
				checkDO = true;
			} else { // otherwise, class (C)
				md.setTheme(md.getOnClass());
			}
			
		} else { // no prep in name -- set theme only
			if (!nameTheme.isEmpty()) { // if theme in name
				setThemeToName(md, nameTheme);
				checkDO = true;
			} else { // otherwise take first in {f, C}
				// guarantees unknown args non-empty
				md.addUnknownArg(md.getOnClass());
				md.setTheme(md.getUnknownArgs().get(0));
				md.removeUnknownArg(0);
			}
		}
		
		// Find equivalences
		if ((checkDO || checkIO) && md.getUnknownArgs() != null 
				&& !md.getUnknownArgs().isEmpty()) 
			checkOverlap(md, checkDO, checkIO);

		// cleanup
		// add ununsed args to unknown, even boolean
		md.addUnknownArgs(unusedArgs);
		//if (!md.getReturnType().isPrimitive()) // TODO change to just void
		if (!md.getReturnType().getName().matches("void"))
			md.addUnknownArg(md.getReturnType());
		
		/* Note: adding class as unused arg indep of checkIO
		 * if checkIO = true, and not checkDO, then do will be class
		 * if check IO = true and checkDO, then this will be executed
		 * if no prep & DO not in name, class will already be on unused args
		 * list for finding theme.
		 */
		if (checkDO) // if DO in name, add class to args
			md.addUnknownArg(md.getOnClass()); // don't want class in overlap
	}

	private void setThemeToName(IMethodDecl md, IPhraseNode nameTheme) {
		nameTheme.setLocation(Location.NAME);
		md.setTheme(nameTheme);
		//md.setThemeLocation(Location.NAME);
	}

	private void populateUnknownArgs(IMethodDecl md, ArrayList<Node> unusedArgs) {
		// depending on verb, add all formals to unknown - pick from there
		if (booleanArgVerbs.contains(md.getParse().beginsWith())) {
			md.setUnknownArgs(new ArrayList<Node>(md.getGiven()));
		} else { // only add non-bool args
			for (Node n : md.getGiven()) {
				if (n instanceof IVariableDecl) {
					if (((IVariableDecl)n).getType().getName().matches("boolean"))
						unusedArgs.add(n);
					else
						md.addUnknownArg(n);
				}
			}
		}
	}

	// TODO -- is this correct?
	private void handleNoPrepArg(IMethodDecl md, int p) {
		md.getParse().get(p).setTag(POSTag.NOUN_MODIFIER);
	}

	// TODO
	private void checkOverlap(IMethodDecl md, boolean checkDO, boolean checkIO) {
		if (md.getParse().beginsWith().matches("set"))
			return; // special case
		
		IPhraseNode theme = null;
		IArgumentNode arg = null;
		
		ArrayList<Node> unknownArgs = md.getUnknownArgs();
		boolean[] don = new boolean[unknownArgs.size()];
		boolean[] ion = new boolean[unknownArgs.size()];
		boolean overlappingDO = false;
		boolean overlappingIO = false;
		
		// get DO word from name
		String wordDO = "";
		if (checkDO) { // name theme
			theme = (IPhraseNode) md.getTheme();
			wordDO = theme.endsWith();
		}
		
		// get IO word from name
		String wordIO = "";
		if (checkIO) { // name theme
			arg = (IArgumentNode) md.getSecondaryArgs().get(0);
			IPhraseNode argn = (IPhraseNode) arg.getArgument();
			wordIO = argn.endsWith();
			if (wordDO.matches(wordIO))
				return; // no equivalence if multiple overlap
		}
		
		// Find overlap
		for (int i = 0; i < unknownArgs.size(); i++) { // Node n : unknownArgs
			if (unknownArgs.get(i) instanceof IVariableDecl) { // as it should be
				IVariableDecl var = (IVariableDecl) unknownArgs.get(i);
				IPhraseNode name = var.getParse();
				IPhraseNode type = var.getType().getParse();
				
				if (checkDO) {
					don[i] = processOverlap(name, wordDO);
					don[i] |= processOverlap(type, wordDO);
				}
				
				if (checkIO) {
					ion[i] = processOverlap(name, wordIO);
					ion[i] |= processOverlap(type, wordIO);
				}
				
				if (don[i] && ion[i])
					return; // no equivalence if multiple overlap
				
				if (don[i]) overlappingDO = true;
				else if (ion[i]) overlappingIO = true;

			}
		}
		
		// Create overlap in SWUM
		if (overlappingDO) {
			IEquivalenceNode equivdo = CompositeNodeFactory.getEquivalenceNode(theme);
			md.setUnknownArgs(createEquivalence(equivdo, don, unknownArgs));
			md.setTheme(equivdo); // reset in md
		} 

		if (overlappingIO) {
			Node oldArg = arg.getArgument();
			IEquivalenceNode equivio = CompositeNodeFactory.getEquivalenceNode(oldArg);
			md.setUnknownArgs(createEquivalence(equivio, ion, unknownArgs));
			arg.setArgument(equivio); // reset in md (seconday ArgumentNode)
		}
	}


	private ArrayList<Node> createEquivalence(IEquivalenceNode equivdo, boolean[] don,
			ArrayList<Node> unknownArgs) {
		ArrayList<Node> newArgs = new ArrayList<Node>(unknownArgs);
		int nequiv = 0; // number of equivalences (index offset)
		for (int i = 0; i < don.length; i++) {
			if (don[i]) {
				equivdo.addEquivalentNode(unknownArgs.get(i));
				newArgs.remove(i - nequiv);
				nequiv++;
			}
		}
		return newArgs;
	}

	private boolean processOverlap(IPhraseNode name, String word) {
		// Process param -- just check last 2 words
		boolean ret = false;
		if (word.matches(name.endsWith()))
			ret = true;
		else
			ret = false;
		
		int head = name.size() - 1;
		
		if (ret == false && head > 0) {
			if (word.matches(name.get(head-1).getWord()) &&
					IgnorableWords.getInstance().isIgnorableHeadWord(name.endsWith()))
				ret = true;
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private void researchPrepIOClasses(IMethodDecl md, IPhraseNode nameTheme,
			IPhraseNode io) {
		if (nameTheme.isEmpty()) {
			if (md.getGiven().isEmpty()) {
				if (io.isEmpty())
					System.out.print("XXXX VP() ");
				else
					System.out.print("XXXX VPY() ");
			} else {
				if (io.isEmpty())
					System.out.print("XXXX VP(p) ");
				else
					System.out.print("XXXX VPY(p) ");
			}
		} else if (io.isEmpty()) {
			if (md.getGiven().isEmpty())
				System.out.print("XXXX VXP() ");
			else
				System.out.print("XXXX VXP(p) ");
		}
	}

	/*private void setIOToFormal(MethodDecl md, WordNode prep, int a) {
		ArgumentNode param = new ArgumentNode(md.getGiven().get(a), prep);
		param.setLocation(Location.FORMAL);
		setIO(md, param);
	}*/

	private void setIO(IMethodDecl md, IArgumentNode arg) {
		ArrayList<Node> secondaryArgs = new ArrayList<Node>();
		secondaryArgs.add(arg);
		md.setSecondaryArgs(secondaryArgs);
	}
	
	/**
	 * Does the leading word start with a noun and not a particle?
	 */
	protected boolean startsNounPhrase(String word) {
		return NPIndicators.contains(word) ||
			   pos.isNoun(word)  ||
			   pos.isDeterminer(word) ||
			   pos.isPronoun(word) ||
			   pos.isAdjective(word);
	}
	
	protected boolean isEventHandler(IPhraseNode parse) {
		return 
		(isNonBaseVerb(parse.endsWith()) 
				&& !parse.beginsWith().matches("get")
				&& !parse.beginsWith().matches("set"))
		;//|| parse.beginsWith().matches("on"); // ok undefined for preamble
	}
	
	protected boolean isEventHandler(ArrayList<Node> given) {
		if (given == null || given.isEmpty())
			return false;
		for (Node n : given) {
			if (n instanceof IVariableDecl) {
				IVariableDecl vd = (IVariableDecl)n;
				if (vd.getType().getParse().endsWith().matches("event") /*||
						vd.getType().getParse().endsWith().matches("action")*/)
					return true;
			}
		}
		return false;
	}
	
	protected boolean isNonBaseVerb(String word) {
		//return (word.endsWith("ing") || word.endsWith("ed"));
		return isINGVerb(word) || 
				isEDVerb(word);
	}

	protected boolean isINGVerb(String word) {
		return pos.isPresentParticiple(word); // ing
	}
	
	protected boolean isEDVerb(String word) {
		return pos.isPastTense(word) || //ed
				pos.isPastParticiple(word);  // en
	}
	
	protected boolean isSpecialCase(String id) {
		return specialWords.contains(id) || id.matches("^[0-9].*");
	}

	protected void parseReactiveName(IMethodDecl md) {
		//md.setAction(new WordNode("handle", POSTag.VERB)); // EEK
		md.setAction(md.getParse().get(0).getNewWord("[handle]", POSTag.VERB));
		
		tagger.tagNounPhrase(md.getParse());
		
		md.setTheme(md.getPreamble(), md.getParse());
		setDefaultUnknownArgs(md);
		md.setReactive(true);
	}

	protected void setDefaultUnknownArgs(IMethodDecl md) {
		md.setUnknownArgs(md.getGiven());
		md.addUnknownArg(md.getOnClass());
		if (!md.getReturnType().isPrimitive())
			md.addUnknownArg(md.getReturnType());
	}
	
	/*protected boolean takesMultipleArguments(String id) {
		return multipleArguments.contains(id);
	}*/

}
