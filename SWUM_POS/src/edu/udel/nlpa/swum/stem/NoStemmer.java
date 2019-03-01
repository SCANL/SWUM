package edu.udel.nlpa.swum.stem;

public class NoStemmer implements Stemmer {

	public String stem(String s) {
		return s;
	}

}
