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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.udel.nlpa.swum.stem.*;
import edu.udel.nlpa.swum.utils.LibFileLoader;
import edu.udel.nlpa.swum.utils.identifiers.ConservativeCamelCaseSplitter;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;
import edu.udel.nlpa.swum.utils.visitors.SWUMElementVisitor;
import edu.udel.nlpa.swum.utils.visitors.SWUMSearchVisitor;
import edu.udel.nlpa.swum.utils.visitors.SWUMTopDoraVisitor;
import edu.udel.nlpa.swum.utils.visitors.TFIDFSearchVisitor;
import edu.udel.nlpa.swum.utils.visitors.WordCountElementVisitor;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class BatchSearchHandler extends AbstractHandler {
	
	HashMap<String,String> queries = new HashMap<String,String>();
	HashMap<String,double[]> maxes = new HashMap<String,double[]>();
	HashMap<String,Set<String>> seeds = new HashMap<String, Set<String>>();
	
	/**
	 * The constructor.
	 */
	public BatchSearchHandler() {
		//trainingConcerns();
		
		//searchEvalConcerns();
		//evalQueries();
		
		//origDoraEvalConcerns();
		
		//searchAndExploreStudy();
	}
	
	private void searchAndExploreStudy() {
		/*queries.put("add auction", "jbidwatcher10pre6");

		queries.put("save auction", "jbidwatcher10pre6");
		queries.put("save auctions", "jbidwatcher10pre6");
		queries.put("save", "jbidwatcher10pre6");
		
		queries.put("add snipe", "jbidwatcher10pre6");
		queries.put("do you wish to snipe", "jbidwatcher10pre6");
		queries.put("set snipe", "jbidwatcher10pre6");
		queries.put("snipe", "jbidwatcher10pre6");
		queries.put("sniping", "jbidwatcher10pre6");
		
		queries.put("IReportCompiler", "iReport");
		queries.put("compile", "iReport");
		queries.put("compile report", "iReport");
		
		queries.put("textfieldReportElement", "iReport");
		queries.put("add text", "iReport");
		queries.put("create textfield", "iReport");
		queries.put("text field tool", "iReport");
		queries.put("textfield", "iReport");
		
		queries.put("play", "jajuk");*/
		queries.put("play file", "jajuk");
		/*queries.put("play track", "jajuk");
		queries.put("open file", "jajuk");
		queries.put("start", "jajuk");
		
		queries.put("search", "jajuk");
		queries.put("search file", "jajuk");

		queries.put("find", "javaHMO");
		queries.put("get theater", "javaHMO");
		queries.put("movie", "javaHMO");
		queries.put("theater", "javaHMO");
		queries.put("view listing", "javaHMO");*/
	}

	private void trainingConcerns() {
		queries.put("add auction", "jbidwatcher10pre6");
		queries.put("update auction", "jbidwatcher10pre6");
		queries.put("toggle folded", "freemind");
		queries.put("undo edit manager", "ganttproject");
		//queries.put("undo edit", "ganttproject");
		//queries.put("compare sort track style", "jajuk");
		queries.put("compare sort style", "jajuk");
		
										// sig 		body 	combo   fcombo
		loadMaxes("add auction", 		1.72619, 1.72619, 1.83842, 1.59628);
		loadMaxes("compare sort style", 1.93701, 1.61670, 2.01420, 1.17616);
		loadMaxes("toggle folded", 		1.48370, 1.73216, 1.66313, 1.73216);
		loadMaxes("undo edit manager", 	1.98257, 1.73778, 2.08334, 1.73588);
		loadMaxes("update auction", 	1.81696, 1.81696, 1.96996, 1.56860);

	}

	private void doraEvalConcerns() {
		queries.put("add auction", "jbidwatcher10pre6");
		queries.put("set snipe bid price", "jbidwatcher10pre6");
		queries.put("save auction files backup dir print", "jbidwatcher10pre6");
		queries.put("compile report file", "iReport");
		queries.put("drop text field", "iReport");
		queries.put("play file", "jajuk");
		queries.put("search file result", "jajuk");
		queries.put("gather find mp3 files", "javaHMO");
		queries.put("find load movie show", "javaHMO");
	}
	
	private void origDoraEvalConcerns() {
		queries.put("download thumbnail image", "jbidwatcher10pre6");
		queries.put("execute auction bid", "jbidwatcher10pre6");
		queries.put("delete auction", "jbidwatcher10pre6");
		queries.put("zoom in out", "freemind");
		queries.put("auto save file", "freemind");
		queries.put("task progress complete", "ganttproject");
		
		// only need sig score max
		
										// sig 		body 	combo   fcombo
		loadMaxes("download thumbnail image", 	2.06006, 1, 1, 1);
		loadMaxes("execute auction bid", 		2.12675, 1, 1, 1);
		loadMaxes("delete auction", 			1.92012, 1, 1, 1);
		loadMaxes("zoom in out", 				2.04896, 1, 1, 1);
		loadMaxes("auto save file", 			2.03025, 1, 1, 1);
		loadMaxes("task progress complete", 	1.51206, 1, 1, 1);
	}
	
	private void searchEvalConcerns() {
//		queries.put("add auction", "jbidwatcher10pre6");
//		queries.put("add auction entry", "jbidwatcher10pre6");
		queries.put("prepare snipe", "jbidwatcher10pre6");
		//queries.put("snipe", "jbidwatcher10pre6");
		queries.put("save auctions", "jbidwatcher10pre6");
		//queries.put("save", "jbidwatcher10pre6");
		queries.put("report compiler", "iReport");
		queries.put("text field", "iReport");
		//queries.put("constructs text report element", "iReport");
		queries.put("play file", "jajuk");
		queries.put("search result", "jajuk");
		//queries.put("search", "jajuk");
		//queries.put("search s criteria", "jajuk");
		queries.put("gather directory", "javaHMO");
		queries.put("find shows", "javaHMO");
	}
	
	// Is it a problem if I pick the queries?
	private void evalQueries() {
		//queries.put("add auction", "jbidwatcher10pre6");
		queries.put("set snipe bid price", "jbidwatcher10pre6");
		queries.put("save auction files", "jbidwatcher10pre6");
		queries.put("compile report", "iReport");
		queries.put("drop text field", "iReport");
		queries.put("play file", "jajuk");
		queries.put("search file result", "jajuk");
		queries.put("gather find mp3 files", "javaHMO");
		queries.put("find load movie show", "javaHMO");
	}
	
	private void loadMaxes(String q, double sig, double body, double combo, double fcombo) {
		double[] m = {sig, body, combo, fcombo};
		maxes.put(q, m);
	}
	
	
	IWorkbenchWindow window;

	/**
	 * the command has been executed, so extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		//searchAllProjects();
		//searchProjectsForPredefinedQueries();
		searchProjectsForRhinoQueries();
		//searchProjectsForDiffRhinoQueries();
		//exploreProjectsForPredefinedQueries();
		return null;
	}

	private void searchAllProjects() {
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (p.isOpen()) {
				IJavaProject jp = JavaCore.create(p);
				if (jp != null) {
					visitProject(jp);
				}
			}
		}
	}
	
	private void searchProjectsForPredefinedQueries() {
		for (String q : queries.keySet()) {
			String proj = queries.get(q);
			IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(proj);
			if (p.isOpen()) {
				IJavaProject jp = JavaCore.create(p);
				if (jp != null) {
					System.out.println("Searching " + jp.getElementName() + " for " + q);
					visit(jp, q);
				}
			}
		}
	}
	
	private void searchProjectsForDiffRhinoQueries() {
		IdentifierSplitter idSplitter = new ConservativeCamelCaseSplitter();
		String q = "15.11.6.4_-_SyntaxError error syntaxError parsing";
				//"15.9.5.36_-_setDate date prototype setDate";
				//"11.5.1_-_Applying_the_MULTIPLY_Operator apply multiplication operator zero";
				//"15.4.4.2_-_toString  toString convert Array to String join elements";

		String proj = "Rhino";
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(proj);
		if (p.isOpen()) {
			IJavaProject jp = JavaCore.create(p);
			if (jp != null) {
				String line = q;

				String[] words = line.split("\\s+");
				if (words.length > 1) {
					String concern = words[0]; // concern
					String query = buildQuery(idSplitter, words);
					String lead = "out/rhino/temp_"; 
					String ofile = "";
					System.out.println("--Searching " + jp.getElementName() + " for " + concern);

					// for each stemmer
					
					// MStem
					lead_stemmer = new MStem();
					ofile = lead + "mstem" + ".out";
					//System.out.println("Output: " + ofile + " Q: " + query);
					System.out.println("\tMStem ");
					runRhinoStemmer(query, ofile, jp);
					
					
					//new KStem();
					lead_stemmer = new KStem();
					ofile = lead + "kstem" + ".out";
					System.out.println("\tKStem ");
					runRhinoStemmer(query, ofile, jp);
					
					//new Porter();
					lead_stemmer = new Porter();
					ofile = lead + "porter" + ".out";
					System.out.println("\tPorter ");
					runRhinoStemmer(query, ofile, jp);
					
					//new Snowball();
					lead_stemmer = new Snowball();
					ofile = lead + "snowball" + ".out";
					System.out.println("\tSnowball ");
					runRhinoStemmer(query, ofile, jp);
					
					//new Paice();
					lead_stemmer = new Paice();
					ofile = lead + "paice" + ".out";
					System.out.println("\tPaice ");
					runRhinoStemmer(query, ofile, jp);
					
					//new NoStemmer();
					lead_stemmer = new NoStemmer();
					ofile = lead + "nostem" + ".out";
					System.out.println("\tNoStem ");
					runRhinoStemmer(query, ofile, jp);

				} else {
					System.out.println("EEK! no query: " + line);
				}
			}
		}
	}

	
	
	private void searchProjectsForRhinoQueries() {
		IdentifierSplitter idSplitter = new ConservativeCamelCaseSplitter();
		ArrayList<String> qfiles = new ArrayList<String>();
		String lead = "out/rhino/queries/";
//		qfiles.add("out/rhino/queries/test.queries");
		qfiles.add("out/rhino/queries/subject1.queries");
		qfiles.add("out/rhino/queries/subject2.queries");
		qfiles.add("out/rhino/queries/subject3.queries");
		qfiles.add("out/rhino/queries/subject4.queries");
		qfiles.add("out/rhino/queries/subject5.queries");
		qfiles.add("out/rhino/queries/subject6.queries");
		qfiles.add("out/rhino/queries/subject7.queries");
		qfiles.add("out/rhino/queries/subject8.queries");
		
		String proj = "Rhino";
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(proj);
		if (p.isOpen()) {
			IJavaProject jp = JavaCore.create(p);
			if (jp != null) {

				for (String fname : qfiles) {

					BufferedReader in = getBufferedReader(fname);
					String subject = fname.replaceFirst(lead, "").replaceFirst("\\.queries", "");

					System.out.println(subject + " " + (new Date()));

					try {
						String line = "";
						while ((line = in.readLine()) != null) {
							String[] words = line.split("\\s+");
							if (words.length > 1) {
								String concern = words[0]; // concern
								String query = buildQuery(idSplitter, words);
								int nqw = getNumQueryWords(query);
								String ofile = ""; // get output file: out/rhino/stemmer/subj.concern.nqw.out

								System.out.println("--Searching " + jp.getElementName() + " for " + concern);

								// for each stemmer
								
//								// MStem
								lead_stemmer = new MStem();
								ofile = "out/rhino/mstem/" + subject + "." + concern + "." + nqw + ".out";
								//System.out.println("Output: " + ofile + " Q: " + query);
								System.out.println("\tMStem ");
								runRhinoStemmer(query, ofile, jp);
//								
//								
//								//new KStem();
//								lead_stemmer = new KStem();
//								ofile = "out/rhino/kstem/" + subject + "." + concern + "." + nqw + ".out";
//								System.out.println("\tKStem ");
//								runRhinoStemmer(query, ofile, jp);
//								
//								//new Porter();
//								lead_stemmer = new Porter();
//								ofile = "out/rhino/porter/" + subject + "." + concern + "." + nqw + ".out";
//								System.out.println("\tPorter ");
//								runRhinoStemmer(query, ofile, jp);
//								
//								//new Snowball();
//								lead_stemmer = new Snowball();
//								ofile = "out/rhino/snowball/" + subject + "." + concern + "." + nqw + ".out";
//								System.out.println("\tSnowball ");
//								runRhinoStemmer(query, ofile, jp);
//								
//								//new Paice();
//								lead_stemmer = new Paice();
//								ofile = "out/rhino/paice/" + subject + "." + concern + "." + nqw + ".out";
//								System.out.println("\tPaice ");
//								runRhinoStemmer(query, ofile, jp);
								
								//new NoStemmer();
//								lead_stemmer = new NoStemmer();
//								ofile = "out/rhino/nostem/" + subject + "." + concern + "." + nqw + ".out";
//								System.out.println("\tNoStem ");
//								runRhinoStemmer(query, ofile, jp);
								
							} else {
								System.out.println("EEK! no query: " + line);
							}
						}
					} catch (Exception e) {e.printStackTrace();}
					System.out.println(new Date());
				}
			}
		}
	}

	private String buildQuery(IdentifierSplitter idSplitter, String[] words) {
		String q = ""; // get query
		
		for (int i = 1; i < words.length; i++)
			q += words[i] + " ";
		
		return idSplitter.splitIdIntoSpaces(q).trim();
	}

	private int getNumQueryWords(String line) {
		String[] words = line.split("\\s+");
		HashSet<String> qw = new HashSet<String>();
		for (String w : words)
			qw.add(w);
		return qw.size();
	}
	
	public static Stemmer lead_stemmer;
	
	
	
	
	private void runRhinoStemmer(String q, String ofile, IJavaProject jp) {
		PrintStream ps = getOutputStream(ofile);
		SWUMElementVisitor swum = new TFIDFSearchVisitor(q, jp, ps);
//		new SWUMSearchVisitor(q,jp, ps);
		// TODO setup TFIDF to use right formula + output results
		swum.startVisiting(jp);
	}
	
	private void exploreProjectsForPredefinedQueries() {
		for (String q : queries.keySet()) {
			String proj = queries.get(q);
			loadSeedsForRDoraEval(q);
			//loadSeeds(q);
			//HashMap<String,String> s = loadSeedsForDoraEval(q);
			//HashMap<String,String> s = loadGoldSet(q);
			if (seeds.get(q) != null && seeds.get(q).size() > 0){
				IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(proj);
				if (p.isOpen()) {
					IJavaProject jp = JavaCore.create(p);
					if (jp != null) {
						System.out.println("\n# Exploring " + jp.getElementName() + " for \"" + q + "\"");
						if (!q.equals("")) {
							//PrintStream p2 = getOutputFile(q);
							//WordElementVisitor swum = new WordElementVisitor(q,jp, p2, System.out, 
							//SWUMDoraVisitor swum = new SWUMDoraVisitor(q,jp, System.out, 
							//SigBodyDoraVisitor swum = new SigBodyDoraVisitor(q,jp, System.out,
							//LogLinearSigBodyDoraVisitor swum = new LogLinearSigBodyDoraVisitor(q,jp, System.out, 
							SWUMTopDoraVisitor swum = new SWUMTopDoraVisitor(q,jp, System.out,
							//SWUMBodyDoraVisitor swum = new SWUMBodyDoraVisitor(q,jp, System.out, 
							//GoldSetVisitor swum = new GoldSetVisitor(q,jp, System.out, s);
							//SWUMEval1DoraVisitor swum = new SWUMEval1DoraVisitor(q,jp, System.out, s, maxes.get(q));
									seeds.get(q), maxes.get(q));
							swum.startVisiting(jp);
							System.out.println("");
							swum.exploreSeedSet();
							//swum.calculate();
						} else {
							error("Invalid query");
						}
					} else {
						error("Invalid project");
					}
				} else {
					error("Project closed");
				}
			} else {
				error("No seeds");
			}
		}
	}
	
	private void visit(IJavaProject jp) {
		visit(jp, getQuery());
	}
	
	private void visitProject(IJavaProject jp) {
		PrintStream p = getOutputFile("out/wusage", jp.getElementName(), "wusage");
		WordCountElementVisitor swum = new WordCountElementVisitor(p);
		swum.startVisiting(jp);
	}

	private void visit(IJavaProject jp, String q) {
		if (!q.equals("")) {
			PrintStream p = getOutputFile("out", /*jp.getElementName() + " " +*/ q, "out");
			SWUMElementVisitor swum = // new TFIDFSearchVisitor(q,jp, p);
				new SWUMSearchVisitor(q,jp, p);
			swum.startVisiting(jp);
		} else {
			error("Invalid query");
		}
	}
	
	private String getQuery() {
		InputDialog id = new InputDialog(
				window.getShell(), 
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
	
	private void error(String message) {
		MessageDialog.openInformation(
				window.getShell(),
				"SWUM",
				message);
	}
	
	private PrintStream getOutputFile(String lead, String q, String ext) {
		String fname = q.replaceAll("\\s+", "_");
		return getOutputStream(lead + "/" + fname + "." + ext);
	}
	
	private PrintStream getOutputStream(String fname) {
		PrintStream p = null;
		try {		
			FileOutputStream out = new FileOutputStream(
					LibFileLoader.getInstance().getLead(fname));
			p = new PrintStream( out );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}
	
	private BufferedReader getInputFile(String q, String ext) {
		String fname = q.replaceAll("\\s+", "_");

		return getBufferedReader("out/" + fname + "." + ext);
	}
	
	private BufferedReader getBufferedReader(String fname) {
		BufferedReader in = null;
		try {		
			in = new BufferedReader(new FileReader(
					LibFileLoader.getInstance().getLead(fname)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return in;
	}
	
	private void loadSeeds(String q) {
		BufferedReader in = getInputFile(q, "txt"); // training
		//BufferedReader in = getInputFile(q, "seeds");
		//BufferedReader in = getInputFile("experiment/concerns/" + q, "concern"); // eval
		HashSet<String> s = new HashSet<String>();
		String line = "";
		
		try {
			while ((line = in.readLine()) != null) {
				if (line.matches("^[m] .*")) {
					line = line.replaceFirst("[m] ", "");
					s.add(line);
				}
				//s.add(line);
			}
		} catch (IOException e) {e.printStackTrace();}
		
		seeds.put(q, s);
	}
	
	private void loadSeedsForRDoraEval(String q) {
		BufferedReader in = getInputFile("dora_results/Eval/concerns/" + q, "rank"); // eval
		HashSet<String> s = new HashSet<String>();
		String line = "";
		
		try {
			while ((line = in.readLine()) != null) {
				if (line.matches("^[^ ]* YY")) {
					line = line.replaceFirst(" YY", "");
					s.add(line);
				}
				//s.add(line);
			}
		} catch (IOException e) {e.printStackTrace();}
		
		seeds.put(q, s);
	}
	
	private HashMap<String, String> loadSeedsForDoraEval(String q) {
		BufferedReader in = getInputFile("dora_results/Eval/concerns/" + q, "rank"); // eval		
		HashMap<String,String> s = new HashMap<String,String>();
		String line = "";
		
		try {
			while ((line = in.readLine()) != null) {
				if (line.matches("^[^ ]* [PY][YN]")) {
					String l1 = line.replaceFirst(" [PY][YN]", "");
					String l2 = line.substring(line.length() - 2,line.length());
					s.put(l1, l2);
					//System.out.println(l2 + "-" + l1);
				}
			}
		} catch (IOException e) {e.printStackTrace();}
		
		seeds.put(q, s.keySet());
		return s;
	}
	
	private HashMap<String, String> loadGoldSet(String q) {
		BufferedReader in = getInputFile(q, "txt"); // training
		HashMap<String,String> s = new HashMap<String,String>();
		String line = "";
		
		try {
			while ((line = in.readLine()) != null) {
				if (line.matches("^[a-z] .*")) {
					//line = line.replaceFirst("[a-z] ", "");
					s.put(line.replaceFirst("[a-z] ", ""), line.substring(0,1));
				}
			}
		} catch (IOException e) {e.printStackTrace();}
		
		seeds.put(q, s.keySet());
		return s;
	}
}
