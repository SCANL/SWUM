package edu.udel.nlpa.swum.stem;


public class KStem extends FileStemmer {

	private static String stemfile = "KStem.txt";
	
	static {
		loader.readFileIntoMap(stemfile, stems);
	}
}
