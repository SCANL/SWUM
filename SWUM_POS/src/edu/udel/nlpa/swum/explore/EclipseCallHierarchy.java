package edu.udel.nlpa.swum.explore;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchyCore;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

@SuppressWarnings("restriction")
public class EclipseCallHierarchy implements CallingRelationship {
	private static final CallHierarchyCore callHierarchy = CallHierarchyCore.getDefault();
	
	public HashMap<IMethod, int[]> getCallees(IMember m) {
		// Get Callees
		MethodWrapper[] methodWrapper = callHierarchy.getCalleeRoots(new IMember[]{m});
		return getIMethods(methodWrapper[0]);
	}

	public HashMap<IMethod, int[]> getCallers(IMember m) {
		// Get Callers
		MethodWrapper[] methodWrapper = callHierarchy.getCallerRoots(new IMember[]{m});
		return getIMethods(methodWrapper[0]);
	}
	
	// Get all the methods from the Root wrapper
	public HashMap<IMethod, int[]> getIMethods(MethodWrapper methodWrapper) {
		HashMap<IMethod, int[]> c = new HashMap<IMethod, int[]>();
		if (methodWrapper == null) {
			System.err.println("Wrapper error");
			return c;
		}
		try {
			MethodWrapper[] wrappers = methodWrapper.getCalls(new NullProgressMonitor());

			int i = 0;
			for (MethodWrapper m : wrappers) {
				try {
					if (m.getMember() instanceof IMethod) {
						IMethod im = (IMethod)m.getMember();
						//if (im.getElementType() == IJavaElement.METHOD) {
						//System.out.println("Adding " + im.getElementName());
						//c.add((IMethod)m.getMember());
						if (c.containsKey(im))
							c.get(im)[0] = i;
						else
							c.put(im, new int[]{i});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				i++;
			}
		} catch (Exception e) {
//			System.err.println(methodWrapper.getName());
//			e.printStackTrace();
		}
		return c;
	}

}