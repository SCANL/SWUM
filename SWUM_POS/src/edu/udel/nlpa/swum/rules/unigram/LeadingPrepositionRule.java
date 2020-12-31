package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.nodes.base.WordNode;
import edu.udel.nlpa.swum.utils.constants.POSTag;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

public class LeadingPrepositionRule extends UnigramMethodRule {

	@Override
	protected boolean makeClassification(IMethodDecl md) {
		return isPrep(md.getParse().beginsWith());
	}
	
	@Override
	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IMethodDecl) { 
			IMethodDecl md = (IMethodDecl)in;
			
			// Now fill in SWUM
			md.assignStructuralInformation(idSplitter, tagger);
			
			md.getParse().get(0).setTag(POSTag.PREPOSITION);
			tagger.tagNounPhrase(1, md.getParse().size() - 1, md.getParse());
			if (md.getPreamble().isEmpty())
				md.setTheme(md.getParse()); // TODO fix
			else
				md.setTheme(md.getPreamble(), md.getParse());
			md.setConstructedSWUM(true);
			
			// TODO : make name proper secondaryArg, 
			// and in SEARCH account for if theme is null
			
			String prep = md.getParse().beginsWith();
			
			
			
			//if (prep.matches("to")) //{
			//	md.setAction(new WordNode("convert", POSTag.VERB));
			//} else if (prep.matches("on")) // event handler should come first
			if (prep.matches("to") || prep.matches("from"))
				md.setAction(md.getParse().get(0).getNewWord("[convert]", POSTag.VERB));
				//md.setAction(new WordNode("convert", POSTag.VERB));
			else if (prep.matches("on") || prep.matches("before") ||
					prep.matches("after")) {// event handler should come first
				parseReactiveName(md);
				return RuleIndicator.PREP;
			}
			
			setDefaultUnknownArgs(md);
			
			return RuleIndicator.PREP;
			
		} else { return RuleIndicator.UNKOWN; }
	}


}
