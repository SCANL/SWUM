package edu.udel.nlpa.swum.utils.idf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;

import edu.udel.nlpa.swum.utils.visitors.IDFVisitor;

/**
 * Used for idf calculations
 * @author gibson
 *
 */
public class DocumentFreqList {
	private int numDocs = 0;
	private long modification = IResource.NULL_STAMP;
	private HashMap<String, int[]> docFreq = new HashMap<String, int[]>();

	public int getNumDocs(){
		return numDocs;
	}
	
	public int getDF(String word) {
		if (docFreq.containsKey(word))
			return docFreq.get(word)[0];
		else
			return 0;
	}

	public void loadFreqTerms(IDFVisitor tfl) {
		numDocs = tfl.getNumDocs();
		docFreq = tfl.getDocFreq();
	}

	/** Prints to file */
	public void printSortedIDF(String fname) {
		FileOutputStream out = null;
		PrintStream p = null;
		try {		
			out=new FileOutputStream(fname);
			p=new PrintStream( out );

			DecimalFormat f = new DecimalFormat("0.0000");

			p.println("# " + numDocs + "\t" + modification);
			p.println("#[word] df\tidf=N/df log10(idf) loge(idf)");

			Vector<String> v = new Vector<String>(docFreq.keySet());
			Collections.sort(v);

			for (String s : v) {
				int df = docFreq.get(s)[0];
				/*double idf = 0;
				if (df != 0) idf = Math.log(numDocs/((double)df));*/
				//p.println(s + " " + df + " " + idf);

				p.println("[" + s + "]\t" + 
						df /*+ "\t" + 
							f.format(idf) + "\t" +
							f.format( Math.log10( idf ) ) + "\t" +
							f.format( Math.log( idf ) )*/);
			}

			p.flush();
			p.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void writeObject(String fname) {
		printSortedIDF(fname);
	}

	public void readObject(String fname) {
		BufferedReader input = null;

		try {
			input = new BufferedReader( new FileReader(fname) );
			String line = null; //not declared within while loop

			Pattern pat = Pattern.compile("^\\[(\\w+)\\]\\s+(\\d+).*");
			Pattern num = Pattern.compile("^#\\s*(\\d+)\\s+(\\d+)");
			Matcher m;

			while (( line = input.readLine()) != null){
				// Do something with the line

				m = pat.matcher(line);
				if (m.matches()) {
					addTerm(m.group(1), Integer.parseInt(m.group(2)));
				} else {
					m = num.matcher(line);
					if (m.matches()) {
						numDocs = Integer.parseInt(m.group(1));
						modification = Long.parseLong(m.group(2));
					}
				}
			}
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex){
			ex.printStackTrace();
		}
		finally {
			try {
				if (input!= null) {
					//flush and close both "input" and its underlying FileReader
					input.close();
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void addTerm(String t, int i) {
		if (docFreq.containsKey(t))
			docFreq.get(t)[0]+= i;
		else
			docFreq.put(t, new int[]{i});			
	}

	public long getModification() {
		return modification;
	}

	public void setModification(long modification) {
		this.modification = modification;
	}


}

