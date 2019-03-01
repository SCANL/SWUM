package edu.udel.nlpa.swum.stem;


public class Paice extends FileStemmer {

	private static String stemfile = "paice.txt";
	
	static {
		loader.readFileIntoMap(stemfile, stems);
	}
}
