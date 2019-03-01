package edu.udel.nlpa.swum.explore;

import java.util.HashSet;
import java.util.Stack;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.*;

public class LastCallVisitor extends ASTVisitor {
	private HashSet<IMethod> last = new HashSet<IMethod>();
	private IMethod imeth;
	private boolean in = false;
	private boolean keep_all = false;
	
	private HashSet<IMethod> lastSeen = new HashSet<IMethod>();

	public LastCallVisitor(IMethod m) {
		super();
		imeth = m;
	}

	public boolean visit(MethodDeclaration node) {
		if (node.resolveBinding() != null) {
			IMethod im = (IMethod) node.resolveBinding().getJavaElement();
			if (im.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
				in = true;
				//lastSeen.push(new HashSet<IMethod>());
			}
		}
		return in;
	}

	public void endVisit(MethodDeclaration node) {
		if (node.resolveBinding() != null) {
			IMethod im = (IMethod) node.resolveBinding().getJavaElement();
			if (im.getHandleIdentifier().equals(imeth.getHandleIdentifier())) {
				in = false;
				addLast();
			}
		}
	}

	private void addLast() {
		if (!lastSeen.isEmpty())
			last.addAll(lastSeen);
	}
	
	public boolean visit(IfStatement node) {
		if (in) {
			keep_all = true;
			node.getExpression().accept(this);
			keep_all = false;
			HashSet<IMethod> current = new HashSet<IMethod>();
			current.addAll(lastSeen);
			//lastSeen.clear(); // otherwise miss calls in conditions if body ONLY return stmt
			ASTNode then = node.getThenStatement();
			ASTNode els = node.getElseStatement();
			
			then.accept(this);	
			
			if (els == null) { // no else, only then
				// add prior seen call to if calls
				if (!current.equals(lastSeen))
					lastSeen.addAll(current);
				// o/w leave current on top
			} else { // then & else
				// if there was a call in the if
				if (lastSeen.size() > 0)  {
					// no need to save previous
					// current is the last seen before the if, last = if
					HashSet<IMethod> t = new HashSet<IMethod>();
					t.addAll(lastSeen);
					lastSeen.clear();
					els.accept(this);
					// if there was a call in the else, unify
					if (lastSeen.size() > 0) // ISSUE: if calls of else == calls of if, will include current
						lastSeen.addAll(t);
					// otherwise, join if + before (current)
					else //if (!current.equals(lastSeen))
						lastSeen.addAll(current);				
				} else { // no call in if -- might not be in else either
					els.accept(this);
					
					// save previous?
					if (!current.equals(lastSeen))
						lastSeen.addAll(current);
				}
			}
		}
		return false;
	}
	
	public boolean visit(WhileStatement node) {
		if (in) {
			HashSet<IMethod> current = new HashSet<IMethod>();
			current.addAll(lastSeen);
			lastSeen.clear();
			node.getBody().accept(this);
			if (!current.equals(lastSeen))
				lastSeen.addAll(current);
		}
		return false;
	}
	
	public boolean visit(ForStatement node) {
		if (in) {
			HashSet<IMethod> current = new HashSet<IMethod>();
			current.addAll(lastSeen);
			lastSeen.clear();
			node.getBody().accept(this);
			if (!current.equals(lastSeen))
				lastSeen.addAll(current);
		}
		return false;
	}
	
	public boolean visit(TryStatement node) {
		if (in) {
			node.getBody().accept(this);
		}
		return false;
	}
	
	public boolean visit(SwitchStatement node) {
		if (in) {
			keep_all = true;
			node.getExpression().accept(this);
			keep_all = false;
			HashSet<IMethod> current = new HashSet<IMethod>();
			current.addAll(lastSeen);
			lastSeen.clear();
			HashSet<IMethod> switches = new HashSet<IMethod>();
			
			for (Object o : node.statements()) {
				Statement s = (Statement) o;
				s.accept(this);
				switches.addAll(lastSeen);
				lastSeen.clear();
			}
			
			lastSeen.addAll(switches);
			lastSeen.addAll(current);
		}
		return false;
	}
	
	public boolean visit(MethodInvocation node) {
		return in;
	}
	
	public void endVisit(MethodInvocation node) {
		if (in) {
			IMethod l = (IMethod) node.resolveMethodBinding().getJavaElement();
			if (l != null) {
				// replace the top with current call
				if (!keep_all && !lastSeen.isEmpty())
					lastSeen.clear();

				lastSeen.add(l);
				
//				HashSet<IMethod> temp;
//				if (!lastSeen.isEmpty()) {
//					temp = lastSeen.pop();
//					temp.clear();
//				} else {
//					temp = new HashSet<IMethod>();
//				}
//				temp.add(l);
//				lastSeen.push(temp);
//				change = true;
			}
		}
	}
	
	public void endVisit(ReturnStatement node) {
		if (in)
			addLast();
	}

	public HashSet<IMethod> getLast() { return last; }

}
