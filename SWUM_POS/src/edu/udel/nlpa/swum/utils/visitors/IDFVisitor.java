package edu.udel.nlpa.swum.utils.visitors;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import edu.udel.nlpa.swum.stem.Stemmer;
import edu.udel.nlpa.swum.utils.context.ContextBuilder;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public class IDFVisitor extends JavaElementVisitor {

	//int[] array only every has size 1;
	private HashMap<String, int[]> docFreq = new HashMap<String, int[]>();
	private HashSet<String> docs =  new HashSet<String>();
	private int numDocs = 0;
	private IdentifierSplitter splitter;
	private Stemmer stemmer;

	public IDFVisitor(IdentifierSplitter idSplitter, Stemmer stemmer) {
		this.splitter = idSplitter;
		this.stemmer = stemmer;
	}


	@Override
	protected boolean visit(IMethod imeth) {
		numDocs++;
		docs.add(ContextBuilder.getSignature(imeth));
		try {
			for (String t : getSet(imeth.getSource())) {
				if (docFreq.containsKey(t))
					docFreq.get(t)[0]++;
				else
					docFreq.put(t, new int[]{1});
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return false;
	}


	public HashMap<String, int[]> getDocFreq() {
		return docFreq;
	}

	public int getNumDocs() {
		return numDocs;
	}

	public HashSet<String> getDocs() {
		return docs;
	}

	public HashSet<String> getSet(String source) {
		String[] str = splitter.splitId(source);
		HashSet<String> terms = new HashSet<String>();
		for (String w : str) {
			w = stemmer.stem(w.toLowerCase());
			if (!w.matches("^\\s*$")) {
				terms.add(w);	
			}
		}

		return terms;
	}

}
