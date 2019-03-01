package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

public class ConstructorRule extends UnigramMethodRule {

	@Override
	public boolean inClass(IProgramElementNode in) {
		return in instanceof IMethodDecl &&
			makeClassification((IMethodDecl)in);
	}
	
	@Override
	protected boolean makeClassification(IMethodDecl md) {
		return md.getContext().isConstructor();
	}
	
	@Override
	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IMethodDecl) { 
			IMethodDecl md = (IMethodDecl)in;
			
			// TODO: could re-write like the others
			// but gives alternate example
			
			// Now parse & split
			md.parse(idSplitter);
			//tagger.preTag(in);
			tagger.tagNounPhrase(md.getParse());
			
			// Now fill in SWUM
			md.assignStructuralInformation(idSplitter, tagger);
			md.setTheme(md.getParse());
			md.setUnknownArgs(md.getGiven());
			
			// TODO: How do we want to fill in the action?!?
			md.setConstructor(true);
			md.setConstructedSWUM(true);
			return RuleIndicator.CONSTRUCTOR;
			
		} else { return RuleIndicator.UNKOWN; }
	}

}
