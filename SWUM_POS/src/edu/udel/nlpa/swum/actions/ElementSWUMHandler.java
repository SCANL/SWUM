package edu.udel.nlpa.swum.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import edu.udel.nlpa.swum.utils.visitors.JavaElementVisitor;
import edu.udel.nlpa.swum.utils.visitors.SWUMElementVisitor;
import edu.udel.nlpa.swum.utils.visitors.SWUMSearchVisitor;
import edu.udel.nlpa.swum.utils.visitors.WordIDFElementVisitor;

public class ElementSWUMHandler implements IObjectActionDelegate {
	private ISelection selection;
	private Shell shell;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}


	/**
	 * Selection will be an IMember or IResource
	 */
	public void run(IAction action) {
		//System.out.println(this.getClass().toString().replaceAll(".+\\.", "") + ": " 
		//		+ selection.getClass());
		//IStructuredSelection s = (IStructuredSelection)selection;
		//System.out.println("\t" + s.getFirstElement().getClass());
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection)selection;
			if (iss.getFirstElement() instanceof IJavaElement) {
				visit((IJavaElement)iss.getFirstElement());
				//System.out.println("\t" + ((IJavaElement)iss.getFirstElement()).getElementName());
			} else if (iss.getFirstElement() instanceof IFile) {
				// Get compilation unit
				IFile file = (IFile) iss.getFirstElement();
				ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
				
				// Get offset
				ITextSelection it = (ITextSelection) PlatformUI.getWorkbench().
					getActiveWorkbenchWindow().getActivePage().getActiveEditor().
					getEditorSite().getSelectionProvider().getSelection();
				int offset = it.getOffset();
				
				IJavaElement m = null;
				try {
					m = unit.getElementAt(offset);
				} catch (JavaModelException e) { e.printStackTrace(); }
				if (m == null)
					m = unit;
				visit(m);
				//System.out.println("\t" + offset + " " + m.getElementName());
			} else {
				error("Invalid selection");
			}
		}
	}

	private void error(String message) {
		MessageDialog.openInformation(
				shell,
				"SWUM",
				message);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	private void visit(IJavaElement m) {
		//System.out.println("\tVisiting " + m.getElementName());
		
		//System.err.print("\tVisiting " + m.getElementName());
		long start = System.currentTimeMillis();
		JavaElementVisitor jv = 
				//new SWUMElementVisitor();
				new WordIDFElementVisitor("temp", getProject(m), System.out);
		System.out.flush();
		System.out.println("*************************");
		jv.startVisiting(m);
		System.err.println("\tVisiting " + m.getElementName() + ": " + 
				(System.currentTimeMillis()-start)/1000d + " s");
		System.out.flush();
		System.out.println("*************************");
	}
	
	private IJavaProject getProject(IJavaElement m) {
		return m.getJavaProject();
	}
}
