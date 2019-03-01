package edu.udel.nlpa.swum.utils.idf;

import java.io.File;

import org.eclipse.jdt.core.IJavaProject;

import edu.udel.nlpa.swum.actions.BatchSearchHandler;
import edu.udel.nlpa.swum.actions.PluginActivator;
import edu.udel.nlpa.swum.stem.*;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;
import edu.udel.nlpa.swum.utils.visitors.IDFVisitor;

public class PersistentProjectInformation implements StoredProjectInformation {
	private DocumentFreqList docFreqList;
	private IJavaProject _proj;
	private String filename = "";
	
	public PersistentProjectInformation(IJavaProject p) {
		_proj = p;
	}
	
	public boolean recomputeNecessary() {
		return tryProjectDocumentFrequency();
	}
	
	public DocumentFreqList getProjectDocumentFrequency() {
		return docFreqList;
	}	
	
	public DocumentFreqList computeProjectDocumentFrequency(IdentifierSplitter idSplitter, Stemmer stemmer) {
		// Otherwise, calculate the information anew
		docFreqList = getProjectDocumentFrequency(
				getAllDocumentsForProject(_proj, idSplitter, stemmer),
				filename, 
				_proj.getProject().getModificationStamp());
		return docFreqList;
	}
	
	private DocumentFreqList getProjectDocumentFrequency(IDFVisitor tv, String filename, long mod) {

		DocumentFreqList docFreqList = new DocumentFreqList();
		docFreqList.setModification(mod);
		docFreqList.loadFreqTerms(tv);

		//System.out.println("Writing to "+filename);
		docFreqList.writeObject(filename);

		return docFreqList;
	}
	
	public boolean tryProjectDocumentFrequency() {
		
		docFreqList = null;
		String tempDirString = PluginActivator.getDefault().getStateLocation().addTrailingSeparator().toString();
		
		//Preferences.getPreference(Preferences.STEM)
		String pref = "nostem";
		if (BatchSearchHandler.lead_stemmer instanceof MStem)
			pref = "mstem";
		if (BatchSearchHandler.lead_stemmer instanceof KStem)
			pref = "kstem";
		if (BatchSearchHandler.lead_stemmer instanceof Paice)
			pref = "paice";
		if (BatchSearchHandler.lead_stemmer instanceof Porter)
			pref = "porter";
		if (BatchSearchHandler.lead_stemmer instanceof Snowball)
			pref = "snowbl";
			
		//boolean stem = true;
		//String pref = stem ? "_stem" : "";
		filename = tempDirString + _proj.getElementName() + "_" + pref + ".dfl";
		
		File file = new File(filename);
		
		// Try to read from file
		if (file.exists()) {
			// Retrieve
			//System.out.println("Reading from "+filename);
			docFreqList = new DocumentFreqList();
			docFreqList.readObject(filename);
			
			/*System.out.println("Old mod: " +  
					docFreqList.getModification() + "\tNew mod: " +
					_proj.getProject().getModificationStamp());*/
			
			// if the project hasn't been modified
			if (_proj.getProject().getModificationStamp() == docFreqList.getModification())
				return false; // no need to recompute
			
		} 
		
		return true; // need to recompute
	}
	
	private IDFVisitor getAllDocumentsForProject(IJavaProject proj, IdentifierSplitter idSplitter, Stemmer stemmer) {
		
		/* Analyze whole program to calculate IDF for all
		   terms in docFreqList */		
		IDFVisitor tv = new IDFVisitor(idSplitter, stemmer);
		tv.startVisiting(proj);

		return tv;
	}

}
