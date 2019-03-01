package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IMethodDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

/**
 * Assumes base verb is last, default rule -- so no check
 * @author hill
 *
 */
public class DefaultBaseVerbRule extends BaseVerbRule {
	public boolean inClass(IProgramElementNode in) {
		if (in instanceof IMethodDecl) { return true; }
		return false;
	}

	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IMethodDecl) {
			((IMethodDecl)in).parse(idSplitter);
			tagger.preTag(in);
			
			//if (isPrepositionalPhrase(((MethodDeclaration)in).getParse())) System.out.print("XXXPREPXXX");
			
			return super.constructSWUM(in);
		}
		return RuleIndicator.UNKOWN;
	}

}
