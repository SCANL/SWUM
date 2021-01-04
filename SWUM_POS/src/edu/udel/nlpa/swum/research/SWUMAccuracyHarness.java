/**
 *  SWUM - Copyright (C) 2009 Emily Hill (emily.m.hill@gmail.com)
 *  All rights reserved.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.udel.nlpa.swum.research;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.builders.UnigramBuilder;
import edu.udel.nlpa.swum.nodes.base.*;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.context.*;


public class SWUMAccuracyHarness implements AccuracyTestHarness {
	
	public void testAccuracy(String testFile) {
		
	}
	//Type: 1 - Method, 2 - Field or Decl
	public void annotateIdentifier(String line, int type) {
		
		//ISWUMBuilder swum = new BaseVerbBuilder();
		ISWUMBuilder swum = new UnigramBuilder();

		
		// To do what about array types?!? PreferencesSet[]
		Pattern p = 
			Pattern.compile("^(\\S+)?\\s(static\\s)?(\\S+)\\s([A-Za-z0-9_]+)(\\(.*\\))?$");
		if (type == 1) { // it's a method
			Matcher m = p.matcher(line);
			if (m.matches()) {
				String cl = m.group(1);
				if ( cl == null ) { cl = ""; }
				else {
					// parse of leading package
					cl = cl.replaceAll("\\s+\\.", "");
				}
				boolean stat = m.group(2) == null ? false : true;
				String returnType = m.group(3);
				String name = m.group(4);
				String args = m.group(5);
				MethodContext mc = new MethodContext(returnType);
				if (returnType.matches(name)) mc.setConstructor(true);
				mc.setStatic(stat);
				mc.setDeclaringClass(cl);
				//mc.setSig(line);

				args = args.replace("(", " ");
				args = args.replace(")", " ");
				args = args.trim();
				ArrayList<String> formals = new ArrayList<String>();

				if (!args.matches("^\\s*$")) {
					String[] f = args.split("\\s*,\\s*");
					for (String x : f)
						formals.add(x);
				}
				mc.setFormals(formals);

				MethodDecl md = new MethodDecl(name, mc);
				RuleIndicator ri = swum.applyRules(md);
				String md_string_raw = md.toString();
				String md_string = "";
				boolean seenParen = false;
				for(int i = 0; i<md_string_raw.toString().length(); ++i) {
					if(md_string_raw.charAt(i) == '[') {
						seenParen = true;
					}
					if(md_string_raw.charAt(i) == '|') {
						seenParen = false;
					}
					if(seenParen == false) {
						md_string += md_string_raw.charAt(i);
					}
				}
				String line_raw = line.trim();
				String line_processed = "";
				for(int i = 0; i<line_raw.length(); ++i) {
					if(line.charAt(i) == '[') {
						break;
					}else {
						line_processed += line_raw.charAt(i);
					}
				}
				if (md.isConstructedSWUM())
					System.out.println("FUNCTION#" + line_processed + 
						"@" + md_string);
				else
					System.out.println(RuleIndicator.UNKOWN + ":OOPS:" + line.trim() + ":");
			}else {
				System.out.println(RuleIndicator.UNKOWN + ": :" + line.trim() + ":");
			}
		} else if (type == 2){
			String[] splitLine = line.split(" ");
			FieldContext fc = new FieldContext(splitLine[0], splitLine[1]);
			FieldDecl fd = new FieldDecl(splitLine[1], fc);
			RuleIndicator ri = swum.applyRules(fd);
			if(fd.isConstructedSWUM()) {
				System.out.println("FIELD#" + fd);
			}
		}
	}
	//Type: 1 - Method, 2 - Field or Decl
	public void verifyOutput(BufferedReader in, int type) {
		try {
			//BufferedReader in = new BufferedReader(new FileReader(testFile));
			String line = "";
			while ((line = in.readLine()) != null) {
				annotateIdentifier(line, type);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void verifyOutput(String data, int type, boolean input_type) {
		if(input_type == false) { //file
			try {
				verifyOutput(new BufferedReader(new FileReader(data)), type);
			} catch (FileNotFoundException e) { e.printStackTrace(); }
		} else {
			annotateIdentifier(data, type);
		}
	}
	
	public void processSignatures(String data, int identifier_type, boolean input_type) {
		//verifyOutput("data/swum/testing.txt");
		//verifyOutput("data/swum/SWUM_POS_List.txt");
		//verifyOutput("data/swum/random10Kmethods-noArrays.txt");
		verifyOutput(data, identifier_type, input_type);
		//verifyOutput("data/swum/vdo.txt");
		
		//verifyOutput("data/swum/ctor-test.txt");
		
		//verifyOutput("data/swum/random-simpleType.methods"); //txt
		//verifyOutput("data/swum/ireport-simpleType.txt");
		//verifyOutput("data/swum/jajuk-simpleType.txt");
	}
	
	public void runAllSignatures() {
		String allSigs = "out/sf.elem.gz";
		allSigs = "out/t.gz"; // t1M
		
		try {
			verifyOutput(
					new BufferedReader(
						new InputStreamReader(
							new GZIPInputStream(
								new FileInputStream(allSigs)
			))), 1);
		} catch (FileNotFoundException e) { e.printStackTrace(); } 
		  catch (IOException e) { e.printStackTrace(); }
	}
	
	public void testAccuracy() {
		//this.testAccuracy("data/swum/tests/base-verb_void.txt");
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SWUMAccuracyHarness test = new SWUMAccuracyHarness();
		test.processSignatures(args[0], Integer.valueOf(args[1]), Boolean.valueOf(args[2]));
		//test.runAllSignatures();
		//test.testAccuracy();
	}

}
