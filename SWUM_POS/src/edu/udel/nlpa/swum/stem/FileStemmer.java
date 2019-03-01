package edu.udel.nlpa.swum.stem;

import java.util.HashMap;

import edu.udel.nlpa.swum.utils.LibFileLoader;

public abstract class FileStemmer implements Stemmer {

	protected static LibFileLoader loader = LibFileLoader.getInstance();
	
	protected static HashMap<String,String> stems = new HashMap<String,String>();
	//private static String stemfile = "mstem.txt";
	
//	static {
//		loader.readFileIntoMap(stemfile, stems);
//	}
	
	public String stem(String s) {
		if (stems.containsKey(s))
			return stems.get(s);
		return s;
	}
}
