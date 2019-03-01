package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

public class EmptyNameRule extends UnigramMethodRule {

	/*@Override
	public boolean inClass(IProgramElementNode in) {
		return in instanceof IMethodDecl &&
			makeClassification((IMethodDecl)in);
	}*/
	
	@Override
	protected boolean makeClassification(IMethodDecl md) {
		return md.getParse().isEmpty();
	}
	
	@Override
	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IMethodDecl) { 
			IMethodDecl md = (IMethodDecl)in;

			// Now parse & split
			//md.parse(idSplitter);
			//tagger.tagNounPhrase(md.getParse());
			
			// Now fill in SWUM
			md.assignStructuralInformation(idSplitter, tagger);
			md.setTheme(md.getPreamble());
			
			// TODO: How do we want to fill in the action?!?
			
			md.setConstructedSWUM(true);
			return RuleIndicator.EMPTY;
			
		} else { return RuleIndicator.UNKOWN; }
	}

}
