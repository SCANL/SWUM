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

import edu.udel.nlpa.swum.builders.BaseVerbBuilder;
import edu.udel.nlpa.swum.builders.ISWUMBuilder;
import edu.udel.nlpa.swum.builders.UnigramBuilder;
import edu.udel.nlpa.swum.nodes.base.MethodDecl;
import edu.udel.nlpa.swum.utils.constants.RuleIndicator;
import edu.udel.nlpa.swum.utils.context.MethodContext;
import edu.udel.nlpa.swum.utils.identifiers.ConservativeCamelCaseSplitter;
import edu.udel.nlpa.swum.utils.identifiers.IdentifierSplitter;

public class SWUMAccuracyHarness implements AccuracyTestHarness {
	
	public void testAccuracy(String testFile) {
		
	}
	
	
	public void verifyOutput(BufferedReader in) {
		try {
			//BufferedReader in = new BufferedReader(new FileReader(testFile));
			String line = "";
			
			//ISWUMBuilder swum = new BaseVerbBuilder();
			ISWUMBuilder swum = new UnigramBuilder();

			
			// To do what about array types?!? PreferencesSet[]
			Pattern p = 
				Pattern.compile("^(\\S+)?\\s(static\\s)?(\\S+)\\s([A-Za-z0-9_]+)(\\(.*\\))?$");
			while ((line = in.readLine()) != null) {
				Matcher m = p.matcher(line);
				if (m.matches()) {
					String cl = m.group(1);
					if ( cl == null ) { cl = "_ANONYMOUS_"; }
					else {
						// parse of leading package
						cl = cl.replaceAll("\\s+\\.", "");
					}
					boolean stat = m.group(2) == null ? false : true;
					String returnType = m.group(3);
					String name = m.group(4);
					String args = m.group(5);
					
					if (args != null) { // it's a method

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
						
						if (md.isConstructedSWUM())
							System.out.println(ri + ":" + md.getThemeLocation() + ":" + line.trim() + 
									": " + md);
						else
							System.out.println(RuleIndicator.UNKOWN + ":OOPS:" + line.trim() + ":");
					} 
				} else {
					System.out.println(RuleIndicator.UNKOWN + ": :" + line.trim() + ":");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void verifyOutput(String testFile) {
		try {
			verifyOutput(new BufferedReader(new FileReader(testFile)));
		} catch (FileNotFoundException e) { e.printStackTrace(); }
	}
	
	public void processSignatures() {
		//verifyOutput("data/swum/testing.txt");
		//verifyOutput("data/swum/SWUM_POS_List.txt");
		//verifyOutput("data/swum/random10Kmethods-noArrays.txt");
		verifyOutput("data/swum/eval.txt");
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
			))));
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
		test.processSignatures();
		//test.runAllSignatures();
		//test.testAccuracy();
	}

}