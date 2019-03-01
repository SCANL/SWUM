package edu.udel.nlpa.swum.utils.visitors;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import edu.udel.nlpa.swum.stem.Stemmer;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public class CommentVisitor extends JavaElementVisitor {

	//int[] array only every has size 1;
	/*private HashMap<String, int[]> docFreq = new HashMap<String, int[]>();
	private HashSet<String> docs =  new HashSet<String>();
	private int numDocs = 0;
	private IdentifierSplitter splitter;
	private Stemmer stemmer;*/

	public CommentVisitor(PrintStream o) {
		out = o;
	}

	private PrintStream out = System.out;

	@Override
	protected boolean visit(IMethod imeth) {
		String comment = getComments(imeth);
		
		if (!comment.equals("")) {
			//comment.replaceAll("\\n", " --n ");
			out.println(comment);
		}
		return false;
	}
	
	protected String getComments(IMember im) {
		String comment = "";
		String src = "";
		if (im == null)
			return comment;
		try {
			src = im.getSource();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//src = "  /* Adds something to x\n   */\n";
		if (src.contains("//")) {
			src = src.replaceAll("^\\s+", "");
			String[] lines = src.split("\\n");
			//comment = lines[0];
			for (int i = 0; i < lines.length && lines[i].startsWith("//"); i++) {
				comment += lines[i] + " ";
			}
		}

		if(src.contains("/*")) {
			String[] lines = src.split("\\s+");
			if(lines[0].contains("/*")) {
				boolean end = false;
				for (int i = 0; i < lines.length && !end; i++) {
					comment += lines[i] + " ";
					end = lines[i].contains("*/");
				}
			}
		}
		
		comment = comment.replace("@brief", " ");
		/*if (!comment.equals(""))
			System.out.println(comment);*/
		
		return comment;
	}

}
