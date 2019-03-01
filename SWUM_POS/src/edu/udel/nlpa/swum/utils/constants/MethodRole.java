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

package edu.udel.nlpa.swum.utils.constants;

public enum MethodRole {
	FUNCTION {
		public String toString() { return "FU"; }
	}, 
	GETTER {
		public String toString() { return "GT"; }
	}, 
	CHECKER {
		public String toString() { return "CH"; }
	}, 
	ACTION {
		public String toString() { return "AC"; }
	}, 
	SETTER{
		public String toString() { return "ST"; }
	}, 
	CALLBACK {
		public String toString() { return "CB"; }
	}, 
	EVENT_HANDLER {
		public String toString() { return "EH"; }
	}, 
	VISITOR {
		public String toString() { return "VS"; }
	},
	UNKOWN {
		public String toString() { return "UN"; }
	}
}
