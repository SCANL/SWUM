package edu.udel.nlpa.swum.rules.unigram;

import edu.udel.nlpa.swum.nodes.IFieldDecl;
import edu.udel.nlpa.swum.nodes.IProgramElementNode;
import edu.udel.nlpa.swum.nodes.base.FieldDecl;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;

public class FieldRule extends UnigramRule {

	public RuleIndicator constructSWUM(IProgramElementNode in) {
		if (in instanceof IFieldDecl) { 
			IFieldDecl fd = (IFieldDecl)in;
			fd.parse(idSplitter);
			tagger.tagNounPhrase(fd.getParse());
			fd.assignStructuralInformation(idSplitter, tagger);
			fd.getType().setPrimitive(super.isPrimitive(fd.getType().getName()));
			fd.setConstructedSWUM(true);
			return RuleIndicator.FIELD;
		} else { return RuleIndicator.UNKOWN; }
	}

	public boolean inClass(IProgramElementNode in) {
		if (in instanceof IFieldDecl) {
			//FieldDecl fd = (FieldDecl)in;
			
			// parse separate from creation so splitter can live in rule
			//fd.parse(idSplitter);
			
			//tagger.preTag(in);
			
			return true;
		}
		return false;
	}

}
