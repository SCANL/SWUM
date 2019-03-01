package edu.udel.nlpa.swum.utils.visitors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class BatchCUASTVisitor extends JavaFilesVisitor {
	private ASTVisitor astv = null;
	private boolean bindings = true;
	
	public BatchCUASTVisitor(ASTVisitor a) {
		super();
		astv = a;
	}
	
	public BatchCUASTVisitor(ASTVisitor a, boolean b) {
		this(a);
		bindings = b;
	}

	@Override
	protected void visit(IMethod imeth) { }

	@Override
	protected void visit(IField field) { }

	@Override
	protected void visit(ICompilationUnit icu) {
		// Process AST
		ASTParser lParser = ASTParser.newParser(AST.JLS3); 
		lParser.setKind(ASTParser.K_COMPILATION_UNIT);
		lParser.setSource(icu); // set source
		lParser.setResolveBindings(bindings); // set bindings
		
		CompilationUnit cu = (CompilationUnit) lParser.createAST(null);
		cu.accept(astv);
	}

	@Override
	protected void visit(IType type) { }

}
