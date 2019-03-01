package edu.udel.nlpa.swum.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

import edu.udel.nlpa.swum.actions.PluginActivator;

public class LibFileLoader {
	protected static String head = "lib/";
	private static LibFileLoader ref;
	
	protected String getLead() {
		return getLead(head);
	}
	
	public String getLead(String h) {
		if (PluginActivator.getDefault() == null)
			return h;
		return PluginActivator.getDefault().getBundle().getLocation().replaceAll(".*:", "")
			+ h;
	}
	
	public void readFileIntoHash(String fname, HashSet<String> list) {
		try {
			String verb = "";
			BufferedReader in = new BufferedReader(new FileReader(getLead() + fname));
			while ((verb = in.readLine()) != null) {
				if (!verb.startsWith("#"))
					list.add(verb.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Ignore commented (#) lines & skip any preceding frequency, adding just
	 * words to hash
	 * @param fname
	 * @param list
	 */
	public void readFrequencyFileIntoHash(String fname, HashSet<String> list) {
		try {
			String line = "";
			BufferedReader in = new BufferedReader(new FileReader(getLead() + fname));
			while ((line = in.readLine()) != null) {
				if (!line.startsWith("#")) {
					line = line.replaceAll("^\\s*", "");
					String[] words = line.split("\\s+");
					if (words.length == 2)
					list.add(words[1].trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Ignore commented (#) lines & add words & frequency to hash
	 * @param fname
	 * @param list
	 */
	public void readFrequencyFileIntoMap(String fname, HashMap<String, int[]> list) {
		try {
			String line = "";
			BufferedReader in = new BufferedReader(new FileReader(getLead() + fname));
			while ((line = in.readLine()) != null) {
				if (!line.startsWith("#")) {
					line = line.replaceAll("^\\s*", "");
					String[] words = line.split("\\s+");
					if (words.length == 2)
						list.put(words[1].trim(), new int[] { Integer.parseInt(words[0]) });
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readFileIntoNestedMap(String fname, HashMap<String,HashSet<String>> map) {
		try {
			String line = "";
			BufferedReader in = new BufferedReader(new FileReader(getLead() + fname));
			while ((line = in.readLine()) != null) {
				if (!line.startsWith("#")) {
					String[] words = line.split("\\s");
					if (words.length == 2) {
						if (!map.containsKey(words[1])) {
							HashSet<String> p = new HashSet<String>();
							map.put(words[1], p);
						} // o/w we know it contains key
						map.get(words[1]).add(words[0]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readFileIntoMap(String fname, HashMap<String,String> map) {
		try {
			String line = "";
			BufferedReader in = new BufferedReader(new FileReader(getLead() + fname));
			while ((line = in.readLine()) != null) {
				if (!line.startsWith("#")) {
					String[] words = line.split("\\s");
					if (words.length == 2) {
						// word stem
						map.put(words[0], words[1]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Ignore commented (#) lines & add words & frequency to hash
	 * @param fname
	 * @param list
	 */
	public PositionalFrequencies readPositionalFrequencyFile(String fname) {
		PositionalFrequencies pf = PositionalFrequencies.getInstance();
		try {
			String line = "";
			BufferedReader in = new BufferedReader(new FileReader(getLead() + fname));
			while ((line = in.readLine()) != null) {
				if (!line.startsWith("#")) {
					line = line.replaceAll("^\\s*", "");
					String[] words = line.split("\\s+");
					if (words.length == 5) {
						//list.put(words[1].trim(), new int[] { Integer.parseInt(words[0]) });
						pf.addFrequency(words[0].trim(), Integer.parseInt(words[1]), 
								Integer.parseInt(words[2]), Integer.parseInt(words[3]), 
								Integer.parseInt(words[4]));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pf;
	}
	
	private LibFileLoader() { }
	
	public static LibFileLoader getInstance() {
		if (ref == null)
			ref = new LibFileLoader();		
		return ref;
	}

	public Object clone() throws CloneNotSupportedException { 
		throw new CloneNotSupportedException(); 
	}

}
