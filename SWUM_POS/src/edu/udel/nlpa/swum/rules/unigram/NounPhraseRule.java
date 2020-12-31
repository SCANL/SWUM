package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.nodes.base.WordNode;
import edu.udel.nlpa.swum.utils.constants.POSTag;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

public class NounPhraseRule extends UnigramMethodRule {

	protected boolean makeClassification(IMethodDecl md) {
		return startsNounPhrase(md.getParse().beginsWith());
	}
	
	@Override
	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IMethodDecl) { 
			IMethodDecl md = (IMethodDecl)in;
			
			md.setConstructedSWUM(true);
			
			// Now fill in SWUM
			md.assignStructuralInformation(idSplitter, tagger);
			
			if (md.getContext().getType().matches("void")) {
				parseReactiveName(md);
				return RuleIndicator.VOID_NP;
			} else {
				tagger.tagNounPhrase(md.getParse());
				md.setTheme(md.getPreamble(), md.getParse());
				//md.setAction(new WordNode("get", POSTag.VERB)); // TODO EEK
				md.setAction(md.getParse().get(0).getNewWord("[get]", POSTag.VERB));
				setDefaultUnknownArgs(md);
				return RuleIndicator.NOUN_PHRASE;
			}
			
		} else { return RuleIndicator.UNKOWN; }
	}


}
