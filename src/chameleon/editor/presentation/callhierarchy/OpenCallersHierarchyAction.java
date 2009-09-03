/**
 * Created on 22-jun-07
 * @author Tim Vermeiren
 */
package chameleon.editor.presentation.callhierarchy;

import org.eclipse.jface.viewers.IContentProvider;

import chameleon.editor.ChameleonEditorPlugin;
import chameleon.editor.project.ChameleonProjectNature;

/**
 * This action will open the hierarchy of the callers (the methods that call this method)
 * 
 * @author Tim Vermeiren
 */
public class OpenCallersHierarchyAction extends OpenCallHierarchyAction {

	public OpenCallersHierarchyAction(CallHierarchyView view) {
		super(view);
		setText("Open Callers Hierarchy");
		setImageDescriptor(ChameleonEditorPlugin.getImageDescriptor("call_hierarchy_callers.gif"));
	}

	@Override
	protected IContentProvider getContentProvider(ChameleonProjectNature projectNature) {
		return new CallersContentProvider(projectNature);
	}
	
	
	
}
