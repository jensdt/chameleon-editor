/**
 * Created on 25-apr-07
 * @author Tim Vermeiren
 */
package chameleon.editor.presentation.hierarchy;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import chameleon.core.MetamodelException;
import chameleon.core.type.Type;

/**
 * returns all members (also inherited) of a type
 * 
 * @author Tim Vermeiren
 *
 */
public class AllMemberContentProvider implements IStructuredContentProvider {
	
	/**
	 * If the inputElement is a type, all the (direct and inherited) member
	 * are returned.
	 */
	public Object[] getElements(Object inputElement) {
		try {
			if(inputElement instanceof Type){
				Type type = (Type)inputElement;
				return type.members().toArray();
			}
		} catch (MetamodelException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

}
