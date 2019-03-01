package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.utils.constants.POSTag;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

public class ReactiveRule extends UnigramMethodRule {

	@Override
	public boolean inClass(IProgramElementNode in) {
		super.inClass(in);
		if (! (in instanceof IMethodDecl) ) return false;
		IMethodDecl md = (IMethodDecl)in;
		md.assignStructuralInformation(idSplitter, tagger);
		return makeClassification(md);
	}
	
	protected boolean makeClassification(IMethodDecl md) {
		return super.isEventHandler(md.getParse());
	}
	
	@Override
	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IMethodDecl) { 
			IMethodDecl md = (IMethodDecl)in;

			md.setConstructedSWUM(true);
			parseReactiveName(md);

			if (isINGVerb(md.getParse().endsWith())) {
				//if (isEventHandler(md.getGiven())) System.out.print("XXXINGXXX");
				// TODO update POS tags
				md.getParse().get(md.getParse().size() - 1).setTag(POSTag.VERB_3PS);
				if (md.getParse().size() > 1)
					if (md.getParse().get(md.getParse().size() - 2).getTag() == POSTag.NOUN_MODIFIER)
						md.getParse().get(md.getParse().size() - 2).setTag(POSTag.NOUN);
				return RuleIndicator.ENDS_WITH_ING;
			} else if (isEDVerb(md.getParse().endsWith())) {
				//if (isEventHandler(md.getGiven())) System.out.print("XXXEDXXX");
				// TODO update POS tags
				md.getParse().get(md.getParse().size() - 1).setTag(POSTag.PAST_PARTICIPLE);
				if (md.getParse().size() > 1)
					if (md.getParse().get(md.getParse().size() - 2).getTag() == POSTag.NOUN_MODIFIER)
						md.getParse().get(md.getParse().size() - 2).setTag(POSTag.NOUN);
				return RuleIndicator.ENDS_WITH_ED;
			}
			//}
			return RuleIndicator.SPECIAL;

		} else { return RuleIndicator.UNKOWN; }
	}


}
