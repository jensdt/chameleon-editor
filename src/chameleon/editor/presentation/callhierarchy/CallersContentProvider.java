/**
 * Created on 13-jun-07
 * @author Tim Vermeiren
 */
package chameleon.editor.presentation.callhierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.rejuse.predicate.SafePredicate;
import org.rejuse.predicate.TypePredicate;

import chameleon.core.MetamodelException;
import chameleon.core.element.Element;
import chameleon.core.expression.Invocation;
import chameleon.core.method.Method;
import chameleon.editor.project.ChameleonProjectNature;

/**
 * Calculates all the methods that call a given method.
 * 
 * @author Tim Vermeiren
 */
public class CallersContentProvider implements ITreeContentProvider {
	
	private ChameleonProjectNature projectNature;
	
	public CallersContentProvider(ChameleonProjectNature projectNature) {
		this.projectNature = projectNature;
	}
	
	/**
	 * Returns all the methods that call the given method 
	 * (if inputObject is a Method)
	 */
	public Object[] getChildren(Object inputObject) {
		if(inputObject instanceof Method){
			final Method method = (Method)inputObject;
			// get all invocations in this project:
			Collection<Invocation> invocations = getInvocations();
			// System.out.println("found "+ invocations.size()+" invocations");
			// build a predicate to search for matching invocations:
			SafePredicate<Invocation> predicate = new SafePredicate<Invocation>(){
				@Override
				public boolean eval(Invocation invocation) {
					try {
						return method.equals(invocation.getMethod());
					} catch (MetamodelException e) {
						e.printStackTrace();
						return false;
					}
				}
			};
			// filter invocations
			predicate.filter(invocations);
			// get the methods containing the invocations:
			Collection<Method> result = new ArrayList<Method>();
			for(Invocation invocation : invocations){
				Method callerMethod = (Method) invocation.nearestAncestor(Method.class);
				result.add(callerMethod);
			}
			return result.toArray();
		} else if(inputObject instanceof RootMethod){
			Method method = ((RootMethod)inputObject).getMethod();
			return new Object[]{method};
		}
		return null;
	}
	
	/**
	 * Cashing of the method invocations of this project (for performance reasons)
	 */
	private Collection<Invocation> cachedInvocations;
	
	/**
	 * Returns all the method invocations in the current project.
	 * 
	 */
	private Collection<Invocation> getInvocations() {
		if(cachedInvocations == null){
			cachedInvocations = new HashSet<Invocation>(projectNature.getModel().descendants(Invocation.class));
		}
		return cachedInvocations;
	}

	public Object getParent(Object inputObject) {
		return null;
	}

	public boolean hasChildren(Object inputObject) {
		return true;
	}

	public Object[] getElements(Object inputObject) {
		return getChildren(inputObject);
		// return new Object[]{inputObject}; 
	}
	
	
	public void dispose() {
		// NOP
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// NOP
	}

	
}