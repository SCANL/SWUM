package edu.udel.nlpa.swum.explore;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;

public interface CallingRelationship {
	public HashMap<IMethod, int[]> getCallees(IMember m);
	public HashMap<IMethod, int[]> getCallers(IMember m);
}