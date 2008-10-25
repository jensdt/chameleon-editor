package chameleon.editor.editors.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import chameleon.editor.ChameleonEditorPlugin;

/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * The Root for the preference pages of the editor. No options are defined yet.
 */
public class ChameleonEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ChameleonEditorPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		
		// Set the preference store for the preference page.
		IPreferenceStore store = ChameleonEditorPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	@Override
	protected void createFieldEditors() {

		
	}

	public void init(IWorkbench workbench) {}
	
	public boolean performOk(){
		boolean prev = super.performOk();
		//performApply();
		return prev;
	}
	
	public void performApply(){
		super.performApply();
		
	}




}
