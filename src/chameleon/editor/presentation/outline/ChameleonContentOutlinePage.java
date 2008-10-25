/**
 * 
 */
package chameleon.editor.presentation.outline;


import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import chameleon.core.element.Element;
import chameleon.core.tag.Tag;
import chameleon.editor.ChameleonEditorPlugin;
import chameleon.editor.LanguageMgt;
import chameleon.editor.editors.ChameleonEditor;
import chameleon.editor.linkage.Decorator;
import chameleon.editor.presentation.PresentationModel;

/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * A class to create the outline view for the chameleonFramework. 
 * The outline is generated language independently through use of XML files: language elements, icons, etc are loaded here.
 * A standard treeviewer is used to view the tree structure that was created.
 * The contents & labels come from the ChameleonTreeContentsProvider & ChameleonLabelProvider 
 */
public class ChameleonContentOutlinePage extends ContentOutlinePage {

	//contains all trees created during the life of the workbench (for 1 session)
	private static Vector<ChameleonContentOutlinePage> allTrees;

	//the used languages
	private static Vector<String> initedLanguages = new Vector<String>();

	//the current language used in the editor
	private String currentLanguage;

	//The treeviewer that is used to store and view our tree
	private TreeViewer treeViewer;

	//the editor where this ChameleonContentOutlinePage is used
	private ChameleonEditor editor;

	//The chameleonTree used by this content outline
	private ChameleonOutlineTree chameleonTree;

	//private Vector<String> allowedTreeElements;

	/**
	 * The creation of a ChameleonContentOutlinePage with given language & editor
	 * 
	 * @param language
	 * 		The language that is currently used. 
	 * 		If the language is not available for the outline, no outline is created 
	 * @param allowedElements
	 * 		The editor tho which this ChameleonContentOutlinePage is linked.
	 * 		If the editor is not effective, no outline is created
	 * @param defaultAllowedElements
	 * 		the default allowed elements
	 */
	public ChameleonContentOutlinePage(String language,ChameleonEditor editor, Vector<String> allowedElements, Vector<String> defaultAllowedElements) {
		super();

 
		try {
			if(editor == null)
				throw new NullPointerException("editor is not available, ouline is not created");
			this.editor = editor;
			//this.allowedTreeElements = allowedElements;
			changeLanguage(language);
		} catch (NullPointerException npe){
			System.err.println(npe.getMessage());
		}

		initByDefaults(language, allowedElements, defaultAllowedElements);
		if (allTrees == null) allTrees = new Vector<ChameleonContentOutlinePage>();
		allTrees.add(this);		
	}

	/**
	 * initializes a given languages' outline
	 * @param lang
	 * 	The language that is initialised
	 */
	public static void initByDefaults(String lang){
		
		PresentationModel pm = LanguageMgt.getInstance().getPresentationModel(lang);
		
		Vector<String> v = new Vector<String>();
		for (Iterator<String[]> iter = pm.getOutlineElements().iterator(); iter.hasNext();) {
			String[] element = iter.next();
			v.add(element[0]);
			
		}
		
		initByDefaults(lang,v,pm.getDefaultOutlineElements());
		
	}
	
	
	/**
	 * Initialises this language with the given defaults if no defaults are found in the 
	 * preference store
	 */
	private static void initByDefaults(String language, Vector<String> allowedElements, Vector<String> defaultAllowedElements) {
		if (!initedLanguages.contains(language)){
			
			IPreferenceStore store = ChameleonEditorPlugin.getDefault().getPreferenceStore();
			
			Vector<String> allowed = new Vector<String>();
			if (store.getBoolean("Chameleon_prefs_inited")) {
			
				for (Iterator<String> iter = allowedElements.iterator(); iter.hasNext();) {
					String element = iter.next();
					
					String fieldNaam = "outlineElement_"+language+"_"+element;
					if (store.getBoolean(fieldNaam)) allowed.add(element);
				}
			}
			else {
				allowed = defaultAllowedElements;
				
				for (Iterator<String> iter = allowedElements.iterator(); iter.hasNext();) {
					String element = iter.next();
					String fieldNaam = "outlineElement_"+language+"_"+element;
					boolean b = (allowed.contains(element));
					store.setValue(fieldNaam, b);
				}		
				
				store.setValue("Chameleon_prefs_inited", true);
				
			}

			ChameleonOutlineTree.setAllowedElements(language,allowed);
			initedLanguages.add(language);
		}

		
	}


	/**
	 * Changes the currently used language to the given one, if the new one is supported
	 * Then the language elements for the new language are loaded
	 * @param language
	 * 	The language to be switched at
	 * 	 */
	public void changeLanguage(String language) {
		//System.out.println("ChameleonContentOutlinePage ChangeLanguage");
			currentLanguage = language;


	}


	/**
	 * 
	 * @return the current language used
	 */
	public String getCurrentLanguage() {
		//System.out.println("ChameleonContentOutlinePage getCurrentLanguage");
		return currentLanguage;
	}


	@Override
	public void createControl(Composite parent) {
		//System.out.println("ChameleonContentOutlinePage createControl");
		
		super.createControl(parent);
		//if (!(languageElements==null)){
			//creates the treeViewer
			 treeViewer = getTreeViewer();
			
			// Add drag/drop support for the tree viewer
			//------------------------------------------
	//		int ops = DND.DROP_MOVE;
	//
	//		Transfer[] transfers = new Transfer[] { ScriptUIEditorIntegerTransfer.getInstance()};
	//
	//		treeViewer.addDragSupport(ops, transfers, new ScriptUIEditorTreeViewerDragAdapter(treeViewer));
	//		treeViewer.addDropSupport(ops, transfers, new ScriptUIEditorTreeViewerDropAdapter(treeViewer, this));
	
			// Add tree viewer listeners
			//--------------------------
			treeViewer.addSelectionChangedListener(this);
	
			// Create content providers for the tree viewer
			//---------------------------------------------
			treeViewer.setContentProvider(new ChameleonOutlineTreeContentProvider());
			treeViewer.setLabelProvider(new ChameleonOutlineLabelProvider(/*hashIcons,*/getCurrentLanguage(),editor)); 
	
			// Set initial expansion level
			//----------------------------
			treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

	 		buildTree();

		
	}


	/**
	 * This responds to a SelectionChangedEvent.
	 * When another element in the ChameleonTree is selected, 
	 * a highlight of the element is made.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		int start = 0;
		int length = 0;
		super.selectionChanged(event);

		ISelection selection= event.getSelection();
		if (selection.isEmpty())
			editor.resetHighlightRange();
		else {
			ChameleonOutlineTree segment= (ChameleonOutlineTree) ((IStructuredSelection) selection).getFirstElement();
			Element element = ((Element)segment.getElement());
			if(element.hasDecorator(Decorator.NAME_DECORATOR)){
				 start= ((Decorator)element.getDecorator(Decorator.NAME_DECORATOR)).getOffset();
				 length =((Decorator)element.getDecorator(Decorator.NAME_DECORATOR)).getLength();
			}
			else{ //het ding heeft geen naam, dus pak een decorator
				Collection<Tag> decs = element.getDecorators();
				Decorator firstDec = (Decorator) decs.iterator().next();
			 start = firstDec.getOffset();
 			 length = firstDec.getLength();

			}
			try {
				editor.setHighlightRange(start, length, true);
			} catch (IllegalArgumentException x) {
				editor.resetHighlightRange();
			} 
			
		}
	}
		
		
	

	/*
	 * first all decorators which are 'All_DECORATOR' & language element are put in a vector
	 * with this vector , a chameleontree is made.
	 * The rootelement of this tree is used as input for the treeviewer
	 * 
	 */
	private void buildTree() {
		
		try{
			//System.out.println("ChameleonContentOutlinePage buildTree");
			chameleonTree  = new ChameleonOutlineTree();
			chameleonTree.composeTree(currentLanguage,getTreeRootElement());
			try {
				treeViewer.setInput(chameleonTree);
				treeViewer.refresh();
			} catch (RuntimeException e) {} // voor bij het afsluiten
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}



	/*
	 * returns the root element for the tree
	 */
	private Element getTreeRootElement() {
		return getEditor().getDocument().getCompilationUnit();

	}


	/**
	 * Does nothing
	 * @param editorInput
	 */
	public void setInput(IEditorInput editorInput) {
		//System.out.println("ChameleonContentOutlinePage setInput");
		
		
	}
	


	/**
	 * 
	 * @return the editor where this is used
	 */
	public ChameleonEditor getEditor() {
		return editor;
	}

	/**
	 * updates the outline by adding the elements added to the model, and by removing
	 * the elements removed at the model
	 * @param added
	 * 	the elements added to the model
	 * @param removed
	 * 	the elements removed at the model
	 */
	public void updateOutline() {
		System.out.println("ChameleonContentOutlinePage.updateOutlineDecorators");

		buildTree();
	
	}

	/**
	 * updates all outlines to display the newly used elements
	 *
	 */
	public static void updateAll() {
		for (Iterator<ChameleonContentOutlinePage> iter = allTrees.iterator(); iter.hasNext();) {
			ChameleonContentOutlinePage tree = iter.next();
			tree.updateOutline();
		}
		
	}


	@Override
	public void dispose() {
		allTrees.remove(this);
		super.dispose();
	}


	
	





}
