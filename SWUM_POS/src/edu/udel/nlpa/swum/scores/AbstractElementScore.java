package edu.udel.nlpa.swum.scores;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import edu.udel.nlpa.swum.actions.BatchSearchHandler;
import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.nodes.Node;
import edu.udel.nlpa.swum.nodes.searchable.SearchableNode;
import edu.udel.nlpa.swum.nodes.searchable.SearchableWord;
import edu.udel.nlpa.swum.rules.unigram.pos.PCKimmoPOSData;
import edu.udel.nlpa.swum.rules.unigram.pos.UnigramPOSData;
import edu.udel.nlpa.swum.stem.*;
import edu.udel.nlpa.swum.utils.IgnorableWords;
import edu.udel.nlpa.swum.utils.PrintfFormat;
import edu.udel.nlpa.swum.utils.identifiers.ConservativeCamelCaseSplitter;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;
import edu.udel.nlpa.swum.utils.idf.DocumentFreqList;
import edu.udel.nlpa.swum.utils.idf.PersistentProjectInformation;
import edu.udel.nlpa.swum.utils.idf.StoredProjectInformation;

public abstract class AbstractElementScore implements IElementScore, IRunnableWithProgress {
	public static DocumentFreqList dfl = null;
	public static StoredProjectInformation spi;
	public static IdentifierSplitter idSplitter = new ConservativeCamelCaseSplitter();
	public static Stemmer stemmer = //BatchSearchHandler.lead_stemmer;
		//new MStem();
		//new KStem();
		new Porter();
		//new Snowball();
		//new NoStemmer();
		//new Paice();
	protected static PrintfFormat pf = new PrintfFormat("%.2f");
	protected static UnigramPOSData pos = new PCKimmoPOSData();
	
	public static UnigramPOSData getPos() {
		return pos;
	}

	protected static boolean debug_score = true;
	protected String score_formula = "";
	
	private String query;
	protected HashMap<String,SearchableWord> queryWords;
	
	protected ISWUMBuilder swum;
	protected IJavaProject _proj;
	protected HashMap<String, double[]> contains = new HashMap<String, double[]>();
	protected boolean isReactive = false;
	protected boolean isCtor = false;
	
	public boolean isQueryWord(String s) {
		return queryWords.containsKey(s);
	}
	
	public AbstractElementScore(String q) {		
		query = idSplitter.splitIdIntoSpaces(q).trim();
		
		queryWords = new HashMap<String,SearchableWord>();
		for (String w : query.split("\\s+")) {
			SearchableWord qw = new SearchableWord(w,stemmer);
			queryWords.put(qw.getStemmedWord(), qw);
			contains.put(qw.getStemmedWord(), new double[]{0d});
		}
	}

	public AbstractElementScore(String q, ISWUMBuilder s) {
		this(q);
		swum = s;
	}
	
	public AbstractElementScore(String q, IJavaProject jp, ISWUMBuilder s) {
		this(q, s);
		getIDFInfo(jp);
		_proj = jp;
	}
	
	public double score(IMember node) {
		if (node instanceof IMethod)
			return score((IMethod) node);
		else if (node instanceof IField)
			return score((IField) node);
		return 0d;
	}
	
	public abstract double score(IMethod node);
	public abstract double score(IField node);
	
	private void getIDFInfo(IJavaElement elem) {
		//IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(elem.getJavaProject());
		IJavaProject jp = elem.getJavaProject();//JavaCore.create();
		if (jp == null) return;
		spi = new PersistentProjectInformation(jp);
		if (spi.recomputeNecessary()) {
			ProgressMonitorDialog lProgressDialog = new ProgressMonitorDialog( PlatformUI.getWorkbench().
					getActiveWorkbenchWindow().getShell() );
			try {
				lProgressDialog.run(false, true, this);
			} catch (InvocationTargetException e) { e.printStackTrace();
			} catch (InterruptedException e) { e.printStackTrace(); }
		} else { dfl = spi.getProjectDocumentFrequency(); }
	}
	

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		monitor.beginTask("Calculating IDF (may take a few minutes)", 
				IProgressMonitor.UNKNOWN);
		try { 
			dfl = spi.computeProjectDocumentFrequency(idSplitter, stemmer);
		} finally { monitor.done(); }
	}

	protected double getIDF(String t) {
		return getLinearIDF(t);
	}
	
	protected double getNoIDF(String t) {
		return 1d;
	}
	
	public static double getLinearIDF(String qw) {
		double idf = 1d;
		
		if (dfl == null)
			return idf;

		int df = dfl.getDF(qw);
		if (df > 0)  // possible -- df doesn't count fields & types right now
			idf = 1 - ((double)df) / dfl.getNumDocs();
			//idf = Math.log(dfl.getNumDocs()/((double)df));
		
		return idf;
	}
	
	public static double getLogIDF(String qw) {
		double idf = 1d;
		
		if (dfl == null)
			return idf;

		int df = dfl.getDF(qw);
		if (df > 0)  // possible -- df doesn't count fields & types right now
			idf = Math.log(dfl.getNumDocs()/((double)df));
		
		return idf;
	}
	
	public static double getLogScaledIDF(String qw) {
		double idf = 1d;
		
		if (dfl == null)
			return idf;

		int df = dfl.getDF(qw);
		if (df > 0)  // possible -- df doesn't count fields & types right now
			idf = Math.log(dfl.getNumDocs()/((double)df)) / Math.log(dfl.getNumDocs());
		
		return idf;
	}
	
	protected int getApproxStmts(String src) {
		int lines = 0;
		
		if (src != null && !src.equals("")) {
			for (char c : src.toCharArray()) {
				if (c == ';' || c == '}' || c == '{')
					lines++;
			}
		}
		
		return lines;
	}
	
	protected double lntf(String qw, String name, String type, int n) {
		double lntf = 0d;
		
		int tf = 0;
		String search = " " + qw + " ";
		while (name.contains(search)) {
			name = name.replaceFirst(search, "  ");
			tf++;
		}
		
		if (tf > 0) {
			
			lntf = 1d;
			// TODO multiword abbrev ok if first word? - head kind of takes care of
			if (tf < n) {
				int ntf = n;
				name = name.trim();
				for (String leftover : name.split("\\s+")) 
					if (leftover != null && !leftover.equals("") && !leftover.matches("^\\s*$") &&
							(queryWords.containsKey(leftover) ||
									leftover.matches("^.$") || 
									pos.isDeterminer(leftover) ||
									pos.isPronoun(leftover) ||
									isAcronymAbbreviation(leftover, type)
									)
						)
						ntf--;
				lntf = 1 + Math.log10((1d * tf) / (1d * ntf));
			} // else tf == n
			
		}
			
			//lntf = 1d * tf / n;
			//lntf = 1d * tf / (Math.log(1d * n) + 1d);
		
		return lntf;
	}
	
	protected boolean isAcronymAbbreviation(String sf, String lf) {
		String s = sf.toLowerCase();
		String l = " " + lf.toLowerCase() + " ";
		
		//if (l.matches(s))
		//	return true;

		String match = ".*\\s+";
		for ( char c : s.toCharArray())
			match += c + "[^ ]*?\\s+";

		if (l.matches(match +".*"))
			return true;
		
		return false;
	}
	
	public static boolean isMultiWordAbbreviation(String sf, String lf) {
		String s = sf.toLowerCase();
		String l = " " + lf.toLowerCase() + " ";
		
		//if (l.matches(s))
		//	return true;

		String match = ".* ";
		for ( char c : s.toCharArray())
			match += c + "\\S*\\s*";

		if (l.matches(match + ".*"))
			return true;
		
		return false;
	}
	
	// Finds max head score
	protected double getHeadScore(String qw, ArrayList<Node> args) {
		int minx = -1;
		for (Node n : args) {
			if (n instanceof SearchableNode) {
				int x = ((SearchableNode)n).getStemmedHeadPosition(qw);
				if (x > -1 &&
						(minx == -1 || x < minx))
					minx = x;
			} else
				System.err.println("OOPS!!! " + n);
		}
		if (minx > -1)
			return 1d / (1d + minx);
		return 0d;
	}
	
	protected double getHeadScore(String qw, Node n) {
		double score = 0d;
		if (n instanceof SearchableNode) {
			int x = ((SearchableNode)n).getStemmedHeadPosition(qw);
			if (x > -1)
				score = 1d / (1d + x);
		} else
			System.err.println("OOPS!!! " + n);
		return score;
	}

	protected int getHeadPositionOfQuery(SearchableNode n) {
		int head = -1;
		for (SearchableWord q : queryWords.values()) {
			int t = n.getStemmedHeadPosition(q.getStemmedWord());
			if (t > -1 && head > -1)
				head = Math.min(head, t);
			else if (t > -1 && head == -1)
				head = t;
		}
		return head;
	}
		
	protected int containsQuery(String s) {
		int count = 0;
		for (SearchableWord q : queryWords.values())
			if (s.contains(q.getStemmedWord()))
				count++;
		return count;
	}
	
	protected boolean containsQueryBool(String s) {
		for (SearchableWord q : queryWords.values())
			if (s.contains(q.getStemmedWord()) || s.contains(q.getWord()))
				return true;
		return false;
	}
	
	protected boolean containsQueryBool(SearchableWord q, String s) {
		if (s.contains(q.getStemmedWord()) || s.contains(q.getWord()))
			return true;
		return false;
	}
	
	protected boolean containsQueryBool(String w, String s) {
		if (s.contains(w))
			return true;
		return false;
	}
	
	protected int containsQuery(String w, String s) {
		int tf = 0;
		while (s.contains(w)) {
			s = s.replaceFirst(w, "");
			tf++;
		}
		return tf;
	}
	
	protected int containsQueryFreq(String s) {
		int qf = 0;
		for (SearchableWord q : queryWords.values()) {
			if (s.contains(q.getStemmedWord()) /*|| s.contains(q.getWord())*/)
				qf++;
		}
		return qf;
	}
	
	protected String stem(String splitId) {
		String s = " ";
		for (String i : splitId.split("\\s+"))
			s += stemmer.stem(i) + " ";
		return s;
	}
	
	public String getFormula() {
		return score_formula;
	}
	
	public HashMap<String, double[]> getContains() {
		return contains;
	}
	
	public boolean isReactive() {
		return isReactive;
	}
	
	public boolean isConstructor() {
		return isCtor;
	}
	
	protected void resetContains() {
		for (String qw : queryWords.keySet())
			contains.get(qw)[0] = 0d;
		isReactive = false;
		isCtor = false;
	}

}
