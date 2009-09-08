package chameleon.editor.presentation;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import chameleon.core.declaration.Declaration;
import chameleon.core.element.Element;
import chameleon.core.language.Language;
import chameleon.editor.connector.EclipseEditorExtension;

public class OutlineComparator extends ViewerComparator {
	
	public OutlineComparator() {
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return super.compare(viewer, e1, e2);
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
