/**
 * Created on 21-jun-07
 * @author Tim Vermeiren
 */
package chameleon.editor.presentation.callhierarchy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.rejuse.java.collections.Visitor;

import chameleon.core.expression.Invocation;
import chameleon.core.method.Method;
import chameleon.exception.ModelException;

/**
 * Calculates all the methods that are called by a given method
 * 
 * @author Tim Vermeiren
 *
 */
public class CalleesContentProvider implements ITreeContentProvider {

	/**
	 * Calculates all the methods that are called by a given method
	 */
	public Object[] getChildren(Object inputObject) {
		if (inputObject instanceof Method) {
			Method<?,?,?,?> method = (Method) inputObject;
			// get all the invocations of the given method:
			List<Invocation> invocations = method.descendants(Invocation.class);
			// get all the methods of these invocations:
			final List<Method> calledMethods = new ArrayList<Method>();
			new Visitor<Invocation>(){
				@Override
				public void visit(Invocation invocation) {
					try {
						calledMethods.add(invocation.getMethod());
					} catch (ModelException e) {
						e.printStackTrace();
					}
				}
			}.applyTo(invocations);
			// return result:
			return calledMethods.toArray();
		} else if(inputObject instanceof RootMethod){
			Method method = ((RootMethod)inputObject).getMethod();
			return new Object[]{method};
		}
		return null;
	}

	public Object getParent(Object inputObject) {
		return null;
	}

	public boolean hasChildren(Object inputObject) {
		return true;
	}

	public Object[] getElements(Object inputObject) {
		return getChildren(inputObject);
	}

	public void dispose() {
		// NOP
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// NOP
	}

}
