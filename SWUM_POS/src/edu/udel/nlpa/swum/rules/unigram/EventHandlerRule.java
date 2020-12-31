package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.nodes.base.WordNode;
import edu.udel.nlpa.swum.utils.constants.POSTag;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

public class EventHandlerRule extends UnigramMethodRule {

	@Override
	public boolean inClass(IProgramElementNode in) {
		super.inClass(in);
		if (! (in instanceof IMethodDecl) ) return false;
		IMethodDecl md = (IMethodDecl)in;
		md.assignStructuralInformation(idSplitter, tagger);
		return makeClassification(md);
	}
	
	@Override
	protected boolean makeClassification(IMethodDecl md) {
		return super.isEventHandler(md.getGiven());
	}
	
	@Override
	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IMethodDecl) { 
			IMethodDecl md = (IMethodDecl)in;

			md.setConstructedSWUM(true);
			//md.setAction(new WordNode("handle", POSTag.VERB)); // TODO
			md.setAction(md.getParse().get(0).getNewWord("[handle]", POSTag.VERB));

			tagger.tagNounPhrase(md.getParse());
			md.setTheme(md.getPreamble(), md.getParse());
			
			setDefaultUnknownArgs(md);
			md.setReactive(true);

			if (isEventHandler(md.getGiven())) {
				return RuleIndicator.EVENT_PARAM;
			}

			return RuleIndicator.SPECIAL;
			
		} else { return RuleIndicator.UNKOWN; }
	}


}
