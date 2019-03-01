package edu.udel.nlpa.swum.stem;


public class Porter extends FileStemmer {

	private static String stemfile = "Portr.txt";
	
	static {
		loader.readFileIntoMap(stemfile, stems);
	}
}
