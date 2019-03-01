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

package edu.udel.nlpa.swum.utils.context;

import java.util.ArrayList;

/**
 * Any contextual information of use in identifier splitting, such as
 * whether the identifier is a class, interface, static, final,
 * method, field, variable, etc.
 * 
 * @author hill
 *
 */
public class MethodContext extends IdContext {
	private boolean ctor = false; // constructor
	private boolean stat = false; // is static
	
	//private String sig = "";


	private ArrayList<String> formals = new ArrayList<String>();


	public MethodContext() { }
	
	public MethodContext(boolean c, boolean s, String t) {
		ctor = c;
		stat = s;
		type = t;
	}
	
	public MethodContext(String t) {
		type = t;
	}


	public boolean isConstructor() {
		return ctor;
	}
	public void setConstructor(boolean ctor) {
		this.ctor = ctor;
	}
	public boolean isStatic() {
		return stat;
	}
	public void setStatic(boolean stat) {
		this.stat = stat;
	}


	public ArrayList<String> getFormals() {
		return formals;
	}

	public void setFormals(ArrayList<String> formals) {
		this.formals = formals;
	}

	
	
	/*public String getSig() {
		return sig;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}*/
}
