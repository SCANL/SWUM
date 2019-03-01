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

package edu.udel.nlpa.swum.actions;

import java.io.FileOutputStream;
import java.io.PrintStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import edu.udel.nlpa.swum.utils.LibFileLoader;
import edu.udel.nlpa.swum.utils.visitors.CallGraphVisitor;
import edu.udel.nlpa.swum.utils.visitors.CommentVisitor;
import edu.udel.nlpa.swum.utils.visitors.JavaElementVisitor;
import edu.udel.nlpa.swum.utils.visitors.SWUMElementVisitor;
import edu.udel.nlpa.swum.utils.visitors.SWUMSearchVisitor;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class BatchSWUMHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public BatchSWUMHandler() {
	}
	
	IWorkbenchWindow window;

	/**
	 * the command has been executed, so extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (p.isOpen()) {
				IJavaProject jp = JavaCore.create(p);
				if (jp != null) {
					visit(jp);
				}
			}
		}
		return null;
	}

	private void visit(IJavaProject jp) {
		
		/*MessageDialog.openInformation(
				window.getShell(),
				"SWUM",
				"Visiting " + jp.getElementName());*/
		
		System.err.print("\tVisiting " + jp.getElementName());
		long start = System.currentTimeMillis();
		//JavaElementVisitor jv = new SWUMElementVisitor();
		PrintStream p = getOutputFile("out", jp.getElementName(), "cg");
		JavaElementVisitor jv = //new SWUMElementVisitor(p);
				//new CommentVisitor(System.out);
				new CallGraphVisitor(p);
		jv.startVisiting(jp);
		System.err.println(": " + (System.currentTimeMillis()-start)/1000d + " s");

	}
	
	private PrintStream getOutputFile(String lead, String q, String ext) {
		String fname = q.replaceAll("\\s+", "_");

		PrintStream p = null;
		try {		
			FileOutputStream out = new FileOutputStream(
					LibFileLoader.getInstance().getLead(
							lead + "/" + fname + "." + ext));
			p = new PrintStream( out );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}
}
