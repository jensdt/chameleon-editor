/**
 * Created on 3-apr-07
 * @author Tim Vermeiren
 */
package chameleon.editor.editors.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import chameleon.editor.ChameleonEditorPlugin;

/**
 * This class can add items to the menus
 * At the moment it is used to add Content Assist, Content Format and Assist Context Information menu items
 * 
 * @author Tim Vermeiren
 */
public class ChameleonEditorActionContributor extends BasicTextEditorActionContributor {
	
	
	protected RetargetTextEditorAction contentAssistProposal;
	protected RetargetTextEditorAction contentAssistContextInformation;
	protected RetargetTextEditorAction formatProposal;

	/**
	 * Constructor for ChameleonEditorActionContributor. Creates a new contributor in the
	 * form of adding Content Assist, Content Format and Assist Context Information menu items
	 */
	public ChameleonEditorActionContributor()
	{
		super();
		ResourceBundle bundle = ChameleonEditorPlugin.getDefault().getResourceBundle();

		contentAssistProposal = new RetargetTextEditorAction(bundle, "ContentAssistProposal.", IChameleonEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		formatProposal = new RetargetTextEditorAction(bundle, "ContentFormatProposal.", IChameleonEditorActionDefinitionIds.FORMAT);
		contentAssistContextInformation = new RetargetTextEditorAction(bundle, "ContentAssistContextInformation.", IChameleonEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);

	}
	
	/**
	 * Sets the active editor to this contributor. This updates the actions to
	 * reflect the editor.
	 * 
	 * @see EditorActionBarContributor#editorChanged
	 */
	public void setActiveEditor(IEditorPart part)
	{

		super.setActiveEditor(part);

		ITextEditor editor = null;
		if (part instanceof ITextEditor)
			editor = (ITextEditor) part;
		
		contentAssistProposal.setAction(getAction(editor, IChameleonEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS));
		formatProposal.setAction(getAction(editor, IChameleonEditorActionDefinitionIds.FORMAT));
		contentAssistContextInformation.setAction(getAction(editor, IChameleonEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION));

	}
	
	/**
	 * Adds the three items to the edit-menu
	 */
	public void contributeToMenu(IMenuManager mm)
	{
		super.contributeToMenu(mm);
		IMenuManager editMenu = mm.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null)
		{
			editMenu.add(new Separator());
			editMenu.add(contentAssistProposal);
			editMenu.add(formatProposal);
			editMenu.add(contentAssistContextInformation);
		}
	}
	
}
