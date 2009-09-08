/**
 * Created on 25-apr-07
 * @author Tim Vermeiren
 */
package chameleon.editor.presentation.hierarchy;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import chameleon.core.type.Type;

/**
 * This class calculates the members of a given type. 
 * Needed to populate the member viewer of the hierarchy view.
 * 
 * @author Tim Vermeiren
 */
public class MemberContentProvider implements IStructuredContentProvider {
	
	/**
	 * If inputElement is a Type, the direct members are returned
	 */
	public Object[] getElements(Object inputElement) {
//		try {
//			throw new Exception("MEMBER CONTENT PROVIDER GET ELEMENTS");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if(inputElement instanceof Type){
			Type type = (Type)inputElement;
			return type.directlyDeclaredMembers().toArray();
		}
		return null;
	}

	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		System.out.println("INPUT CHANGED!!!!:   old: " + oldInput +"new: "+newInput);
	}

}
