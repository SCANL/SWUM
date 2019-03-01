/**
 *  SWUM - Copyright (C) 2009 Emily Hill (emily.m.hill@gmail.com)
 *  All rights reserved.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.udel.nlpa.swum.utils.visitors;


import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;


public abstract class JavaFilesVisitor  {

	public void visit(IJavaElement start) {
		processElement(start);
	}

	/**
	 * Recursively process java elements until imethod, ifield,
	 * and itype visitors can be called.
	 */
	private void processElement(IJavaElement ij) {
		if(ij instanceof IMethod){
			if( ! ((IMethod)ij).isBinary())
				visit((IMethod) ij);
		}
		if(ij instanceof IField){
			if( ! ((IField)ij).isBinary())
				visit((IField) ij);			
		}
		if(ij instanceof IType){
			if( ! ((IType)ij).isBinary())
				visit((IType) ij);			
		}
		if(ij instanceof ICompilationUnit){
			visit((ICompilationUnit) ij);
		}
		
		// Otherwise, drill down to tree leaves we're interested in
		if(ij instanceof IParent){
			try {
				for (IJavaElement element : ((IParent) ij).getChildren()) {
					// skip jars
					if (! (element instanceof JarPackageFragmentRoot)) 
						processElement(element);
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		// packages and imports
		//else System.out.println("this is a problem " + ij.toString());
	}

	protected abstract void visit(IMethod imeth) ;
	protected abstract void visit(IField field) ;
	protected abstract void visit(ICompilationUnit icu) ;
	protected abstract void visit(IType type) ;

}

