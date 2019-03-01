package edu.udel.nlpa.swum.stem;


public class MStem extends FileStemmer {

	private static String stemfile = "mstem.txt";
	
	static {
		loader.readFileIntoMap(stemfile, stems);
	}
}
