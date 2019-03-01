package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.utils.constants.MethodRole;
import edu.udel.nlpa.swum.utils.constants.ProgramElementType;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

public class CheckerRule extends UnigramMethodRule {


	@Override
	protected boolean makeClassification(IMethodDecl md) {
		return isChecker(md.getParse().beginsWith());
	}
	
	@Override
	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IMethodDecl) { 
			IMethodDecl md = (IMethodDecl)in;
			
			// Now fill in SWUM
			md.assignStructuralInformation(idSplitter, tagger);	
			md.setRole(MethodRole.CHECKER);
			
			// TODO set agent/condition?
			parseBaseVerbName(md);
			setDefaultActionAndThemes(md);
			md.setConstructedSWUM(true);
			
			String id = md.getParse().beginsWith();
			
			//if (super.isPrepositionalPhrase(md.getParse())) System.out.print("XXXPREPXXX");
			//if (md.getContext().getType().matches("boolean")) System.out.print("XXXBOOLXXX");
			
			if (pos.is3rdPersonSingularVerb(id)) {
				return RuleIndicator.V_3PS;
			} else if (pos.is3rdPersonIrregularVerb(id)) {
				return RuleIndicator.V_3P_IRR;
			} else if (pos.isModalVerb(id)) {
				return RuleIndicator.V_MODAL;
			}

			return RuleIndicator.SPECIAL;
			
		} else { return RuleIndicator.UNKOWN; }
	}



}
