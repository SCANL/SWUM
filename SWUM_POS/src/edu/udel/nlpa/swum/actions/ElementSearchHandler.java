package edu.udel.nlpa.swum.actions;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import edu.udel.nlpa.swum.utils.LibFileLoader;
import edu.udel.nlpa.swum.utils.idf.DocumentFreqList;
import edu.udel.nlpa.swum.utils.idf.PersistentProjectInformation;
import edu.udel.nlpa.swum.utils.idf.StoredProjectInformation;
import edu.udel.nlpa.swum.utils.visitors.JavaElementVisitor;
import edu.udel.nlpa.swum.utils.visitors.SWUMElementVisitor;
import edu.udel.nlpa.swum.utils.visitors.SWUMSearchVisitor;

public class ElementSearchHandler implements IObjectActionDelegate {
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
		//JavaElementVisitor jv = new SWUMElementVisitor();
		//jv.startVisiting(m);
		String q = getQuery();
		if (!q.equals("")) {
			PrintStream p = getOutputFile(q);	
			SWUMSearchVisitor swum = new SWUMSearchVisitor(q, getProject(m), p);
			swum.startVisiting(m);
			
			p.close();
		} else {
			error("Invalid query");
		}
	}


	private PrintStream getOutputFile(String q) {
		String fname = q.replaceAll("\\s+", "_");

		PrintStream p = null;
		try {		
			FileOutputStream out = new FileOutputStream(
					LibFileLoader.getInstance().getLead(
							"out/" + fname + ".out"));
			p = new PrintStream( out );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}
	
	
	private IJavaProject getProject(IJavaElement m) {
		return m.getJavaProject();
	}


	private String getQuery() {
		InputDialog id = new InputDialog(
				shell, 
				"Enter Query", 
				"Please enter a query:", 
				"", 
				null);
		id.open();
		String q = "";
		if (id.getValue() != null && !id.getValue().equals(""))
			q = id.getValue().trim();
		return q;
	}

}
