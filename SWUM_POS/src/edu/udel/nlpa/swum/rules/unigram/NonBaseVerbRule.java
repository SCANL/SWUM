package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.utils.constants.MethodRole;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

public class NonBaseVerbRule extends UnigramMethodRule {

	@Override
	protected boolean makeClassification(IMethodDecl md) {
		return isNonBaseVerb(md.getParse().beginsWith());
	}
	
	@Override
	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IMethodDecl) { 
			IMethodDecl md = (IMethodDecl)in;
			
			// Now fill in SWUM
			md.assignStructuralInformation(idSplitter, tagger);

			parseBaseVerbName(md);
			setDefaultActionAndThemes(md);
			md.setConstructedSWUM(true);
			
			if (super.isINGVerb(md.getParse().beginsWith()))
				return RuleIndicator.ING_VERB;
			else if (super.isEDVerb(md.getParse().beginsWith()))
				return RuleIndicator.ED_VERB;
			
			return RuleIndicator.SPECIAL;
			
		} else { return RuleIndicator.UNKOWN; }
	}


}
