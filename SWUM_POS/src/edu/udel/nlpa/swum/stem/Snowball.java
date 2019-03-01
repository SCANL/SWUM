package edu.udel.nlpa.swum.stem;


public class Snowball extends FileStemmer {

	private static String stemfile = "Snwbl.txt";
	
	static {
		loader.readFileIntoMap(stemfile, stems);
	}
}
