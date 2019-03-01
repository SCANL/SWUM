package edu.udel.nlpa.swum.utils.idf;

import edu.udel.nlpa.swum.stem.Stemmer;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public interface StoredProjectInformation {
	public DocumentFreqList getProjectDocumentFrequency();
	public DocumentFreqList computeProjectDocumentFrequency(IdentifierSplitter idSplitter, Stemmer stemmer);
	public boolean recomputeNecessary();
}
