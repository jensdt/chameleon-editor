/**
 * Created on 7-mei-07
 * @author Tim Vermeiren
 */
package chameleon.editor.developertools.tagview;

import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.rejuse.predicate.Predicate;

import chameleon.editor.connector.EclipseEditorTag;
import chameleon.editor.editors.ChameleonDocument;

/**
 * Calculates the elements for the Editor Tag List View.
 * 
 * @author Tim Vermeiren
 */
public class EditorTagContentProvider implements IStructuredContentProvider {
	
	EditorTagListView view;
	Predicate<EclipseEditorTag> filterPredicate;
	
	public EditorTagContentProvider(EditorTagListView view, Predicate<EclipseEditorTag> filterPredicate) {
		this.view = view;
		this.filterPredicate = filterPredicate;
	}

	public Object[] getElements(Object inputObject) {
		if(inputObject instanceof ChameleonDocument){
			ChameleonDocument doc = (ChameleonDocument)inputObject;
			Collection<EclipseEditorTag> tags = new TreeSet<EclipseEditorTag>(EclipseEditorTag.beginoffsetComparator);
			doc.getEditorTagsWithPredicate(filterPredicate, tags);
			// set label of view:
			view.label.setText(tags.size() + " editor tags found for document "+doc.getFile());
			return tags.toArray();
		}
		return null;
	}

	public void dispose() {
		// NOP
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// NOP
	}

}
