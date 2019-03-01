package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.utils.constants.MethodRole;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

public class SpecialCaseRule extends UnigramMethodRule {

	protected boolean makeClassification(IMethodDecl md) {
		return isSpecialCase(md.getParse().beginsWith());
	}
	
	@Override
	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IMethodDecl) { 
			IMethodDecl md = (IMethodDecl)in;
			
			// Now fill in SWUM
			md.assignStructuralInformation(idSplitter, tagger);

			if (md.getParse().size() == 1 && 
					(md.getParse().beginsWith().matches("run") ||
							md.getParse().beginsWith().matches("main")) ||
							md.getParse().beginsWith().matches("test")) //{
				md.setReactive(true);
			//	parseReactiveName(md); // don't want to parse as reactive, only want to MARK reactive for p
			//} else { // o/w assume base verb
			parseBaseVerbName(md);
			setDefaultActionAndThemes(md);		
			//}

			md.setConstructedSWUM(true);
			
			return RuleIndicator.SPECIAL;
			
		} else { return RuleIndicator.UNKOWN; }
	}


}
