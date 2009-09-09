package chameleon.editor.presentation;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import chameleon.core.declaration.Declaration;
import chameleon.core.element.Element;
import chameleon.core.language.Language;
import chameleon.editor.connector.EclipseEditorExtension;

/**
 * A class for comparing objects in order to sort them in an Eclipse view. The comparator
 * uses the Eclipse mechanism of assigning a category to different kinds of elements.
 * The super class uses alphabetical sorting of the lowercase value return by the label provider
 * to sort elements within a category.
 * 
 * If you want a completely different sorting, you must directly extend ViewerComparator and override the compare
 * method.
 * 
 * The class wrap a DeclarationCategorizer because otherwise, the connector classes for language modules depend on
 * Eclipse classes, resulting in classpath problems.
 * 
 * @author Marko van Dooren
 */
public class OutlineComparator extends ViewerComparator {
	
	public OutlineComparator() {
	}
	
	/**
	 * In this method you determine the order in which the elements are shown in the outline. Elements
	 * grouped per category.
	 */
	@Override
	public int category(Object object) {
	  Element element = ChameleonLabelProvider.getElement(object);
		if(element instanceof Declaration) {
			Language language = element.language();
			DeclarationCategorizer categorizer = language.connector(EclipseEditorExtension.class).declarationCategorizer();
			return categorizer.category((Declaration)element);
		} else {
			return 0;
		}
	}
	
}
