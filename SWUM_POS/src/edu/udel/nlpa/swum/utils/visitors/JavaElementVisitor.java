package edu.udel.nlpa.swum.utils.visitors;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

public class JavaElementVisitor extends AbstractJavaElementVisitor {

	@Override
	protected boolean visit(IMethod node) {
		return true;
	}

	@Override
	protected boolean visit(IField node) {
		return true;
	}

	@Override
	protected boolean visit(IInitializer node) {
		return true;
	}

	@Override
	protected boolean visit(IType node) {
		return true;
	}

	@Override
	protected boolean visit(IPackageDeclaration node) {
		return true;
	}

	@Override
	protected boolean visit(IImportDeclaration node) {
		return true;
	}

	@Override
	protected boolean visit(ICompilationUnit node) {
		return true;
	}

	@Override
	protected boolean visit(IClassFile node) {
		return true;
	}

	@Override
	protected boolean visit(IPackageFragment node) {
		return true;
	}
	
	@Override
	protected boolean visit(IPackageFragmentRoot node) {
		return true;
	}

	@Override
	protected boolean visit(IJavaProject node) {
		return true;
	}

}
