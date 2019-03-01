package edu.udel.nlpa.swum.nodes.base;

import edu.udel.nlpa.swum.nodes.IArgumentNode;
import edu.udel.nlpa.swum.nodes.IPhraseNode;
import edu.udel.nlpa.swum.nodes.IWordNode;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.utils.constants.Location;

/**
 * Node wrapper for secondary args
 * @author hill
 *
 */
public class ArgumentNode extends IArgumentNode {

	public ArgumentNode(Node a, IWordNode p) {
		super(a, p);
	}

}
