package edu.udel.nlpa.swum.research.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

public class IdProcessingVisitor extends ASTVisitor {
	public boolean visit(SimpleName node) {
		System.out.println(node.getIdentifier());
		return true;
	}
}
