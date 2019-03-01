package edu.udel.nlpa.swum.utils.context;

import java.util.ArrayList;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import edu.udel.nlpa.swum.utils.SingletonObject;


public class ContextBuilder {
	
	public static FieldContext buildFieldContext(IField node) {
		String cl = node.getDeclaringType().getElementName();
		if (cl == null) 
			cl = "_ANONYMOUS_";
		
		String type = "";
		try {
			type = getType(node.getTypeSignature());
		} catch (JavaModelException e) { e.printStackTrace(); }
		
		return new FieldContext(type, cl);
	}
	
	public static MethodContext buildMethodContext(IMethod node) {
		String returnType = "";
		
		// set return type
		try {
			returnType = getType(node.getReturnType());
		} catch (JavaModelException e) { e.printStackTrace(); }
		
		// set if constructor
		MethodContext mc = new MethodContext(returnType);
		try {
			if (node.isConstructor())
				mc.setConstructor(true);
		} catch (JavaModelException e) { e.printStackTrace(); }
		
		// set if static
		try {
			if (Flags.isStatic(node.getFlags()))
				mc.setStatic(true);
		} catch (JavaModelException e) { e.printStackTrace(); }
		
		// set declaring class
		String cl = node.getDeclaringType().getElementName();
		if (cl == null) 
			cl = "_ANONYMOUS_";
		mc.setDeclaringClass(cl);
		
		// set formals
		String names[] = null;
		try {
			names = node.getParameterNames();
		} catch (JavaModelException e) { e.printStackTrace(); }
		
		if (names != null) {
			String types[] = node.getParameterTypes();
			ArrayList<String> formals = new ArrayList<String>();
			for (int i = 0; i < names.length; i++)
				formals.add(getType(types[i]) + " " + names[i]);
			mc.setFormals(formals);
		}
		
		return mc;
	}
	
	public static String getSignature(IField field) {
		return getSignature(field, false);
	}
	
	public static String getSignature(IMember m) {
		return getSignature(m, false);
	}
	
	public static String getSignature(IMember m, boolean pkg) {
		if (m instanceof IMethod)
			return getSignature((IMethod)m, pkg);
		else if (m instanceof IField)
			return getSignature((IField)m, pkg);
		return m.getElementName();
	}
	
	public static String getEvalSignature(IMember m, boolean pkg) {
		if (m instanceof IMethod)
			return getEvalSignature((IMethod)m, pkg);
		else if (m instanceof IField)
			return getEvalSignature((IField)m, pkg);
		return m.getElementName();
	}
	
	public static String getSignature(IField field, boolean pkg) {
		String sig = "";
		String type = "";
		if (pkg) type = field.getDeclaringType().getFullyQualifiedName();
		else type = field.getDeclaringType().getElementName();
		try {
			sig += type + " ";
			
			if (Flags.isStatic(field.getFlags()))
				sig += "static ";
			if (Flags.isFinal(field.getFlags()))
				sig += "final ";
			
			sig +=	getType(field.getTypeSignature()) + " " + 
					field.getElementName();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return sig;
	}
	
	public static String getSignature(IMethod imeth) { 
		return getSignature(imeth, false);
	}
	
	public static String getFileSignature(IField field) {
		boolean pkg = true;
		String sig = "";
		String type = "";
		if (pkg) type = field.getDeclaringType().getFullyQualifiedName();
		else type = field.getDeclaringType().getElementName();
		try {
			sig += type + "_";
			
			if (Flags.isStatic(field.getFlags()))
				sig += "static_";
			if (Flags.isFinal(field.getFlags()))
				sig += "final_";
			
			sig +=	getType(field.getTypeSignature()) + "_" + 
					field.getElementName();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return sig;
	}
	
	public static String getFileSignature(IMethod imeth) {
		boolean pkg = true;
		String sig = "";
		String type = "";
		type = imeth.getDeclaringType().getFullyQualifiedName();
		if (!pkg) type = type.replaceAll("[A-Za-z0-9_]+\\.", "");
//		if (pkg) type = imeth.getDeclaringType().getFullyQualifiedName();
//		else type = imeth.getDeclaringType().getElementName();
		
		//sig = "$" + type + "$ ";
		try {
			type = type.replaceAll(" ", "_");
			sig = type + "_";
			String rt = getType(imeth.getReturnType()).replaceAll(" ", "_");;

			if(imeth.isConstructor())
				sig += imeth.getElementName();
			else if (Flags.isStatic(imeth.getFlags()))
				sig += "static_" + rt;
			else
				sig += rt;
		} catch (JavaModelException e) { /*e.printStackTrace();*/ }
		
			sig += "_" + imeth.getElementName() + "__";
			String[] types = imeth.getParameterTypes();

			for (int i = 0; i < types.length; i++) {
				String t = getType(types[i]).replaceAll(" ", "_");
				if (i == 0)
					sig += t;
				else
					sig += "-" + t;						
			}
			

			sig += "__";
		
		return sig;
	}

	
	public static String getSignature(IMethod imeth, boolean pkg) {
		String sig = "";
		String type = "";
		type = imeth.getDeclaringType().getFullyQualifiedName();
		if (!pkg) type = type.replaceAll("[A-Za-z0-9_]+\\.", "");
//		if (pkg) type = imeth.getDeclaringType().getFullyQualifiedName();
//		else type = imeth.getDeclaringType().getElementName();
		
		//sig = "$" + type + "$ ";
		try {
			sig = type + " ";

			if(imeth.isConstructor())
				sig += imeth.getElementName();
			else if (Flags.isStatic(imeth.getFlags()))
				sig += "static " + getType(imeth.getReturnType());
			else
				sig += getType(imeth.getReturnType());
		} catch (JavaModelException e) { /*e.printStackTrace();*/ }
		
			sig += " " + imeth.getElementName() + "(";
			String[] types = imeth.getParameterTypes();

			String[] names = null;
			try {
				names = imeth.getParameterNames();
			} catch (JavaModelException e) { /*e.printStackTrace();*/ }
			
			if (names != null) {
				for (int i = 0; i < names.length; i++) {
					if (i == 0)
						sig += getType(types[i]) + " " + names[i];
					else
						sig += ", " + getType(types[i]) + " " + names[i];						
				}
			} else {
				for (int i = 0; i < types.length; i++) {
					if (i == 0)
						sig += getType(types[i]);
					else
						sig += ", " + getType(types[i]);						
				}
			}

			sig += ")";
		
		return sig;
	}
	
	public static String getEvalSpaceSignature(IMethod imeth, boolean pkg, String space) {
		String sig = "";
		String type = "";
		type = imeth.getDeclaringType().getFullyQualifiedName();
		if (!pkg) type = type.replaceAll("[A-Za-z0-9_]+\\.", "");

		sig = type + ".";

		sig += imeth.getElementName() + "(";
		String[] types = imeth.getParameterTypes();

		for (int i = 0; i < types.length; i++) {
			if (i == 0)
				sig += getType(types[i]);
			else
				sig += "," + space + getType(types[i]);						
		}

		sig += ")";
		
		return sig;
	}
	
	public static String getEvalSignature(IMethod imeth, boolean pkg) {
		return getEvalSpaceSignature(imeth, pkg, " ");
	}
	
	public static String getEvalSignature(IField field, boolean pkg) {
		String sig = "";
		String type = "";
		if (pkg) type = field.getDeclaringType().getFullyQualifiedName();
		else type = field.getDeclaringType().getElementName();
		
		sig = type + "." + field.getElementName();

		return sig;
	}
	
	public static String getRhinoSignature(IMember i) {
		return i.getHandleIdentifier();
	}
	
	public static String getType(String type) {
		return getFieldType(type);
	}
	
	public static String getVariableType(String type) {
		return getFieldType(type.replaceAll("/", "."));
	}
	
	public static String getFieldType(String type) {
		String t = Signature.getSignatureSimpleName(type);
		t.replaceAll("[\\[\\]]", ""); // replace with array??
		return t;
	}

	public static String getEvalSignature(IMethod node) {
		return getEvalSignature(node, false);
	}
	
	public static String getEvalSignature(IField node) {
		return getEvalSignature(node, false);
	}
}
