/**
 * Created on 23-apr-07
 * @author Tim Vermeiren
 */
package chameleon.editor.developertools.modelview;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import chameleon.core.element.Element;

/**
 * Calculates the children for a given element in the Metamodel View 
 * 
 * @author Tim Vermeiren
 */
public class ModelContentProvider implements ITreeContentProvider {

	public ModelContentProvider() {
		
	}

	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof Element){
			Element elem = (Element)parentElement;
			return elem.children().toArray();
		}
		return null;
	}

	public Object getParent(Object element) {
		if(element instanceof Element){
			Element elem = (Element)element;
			return elem.parent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if(element instanceof Element){
			Element elem = (Element)element;
			return ! elem.children().isEmpty();
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

}
