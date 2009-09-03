/**
 * 
 */
package chameleon.editor.presentation.outline;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import chameleon.core.element.Element;
import chameleon.core.language.Language;
import chameleon.editor.ChameleonEditorPlugin;
import chameleon.editor.editors.ChameleonEditor;
import chameleon.editor.presentation.ChameleonLabelProvider;
import chameleon.editor.presentation.Filters;
import chameleon.editor.presentation.MembersComparator;
import chameleon.editor.presentation.PresentationModel;
import chameleon.editor.presentation.TreeViewerActions;

/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * A class to create the outline view for the chameleonFramework. 
 * The outline is generated language independently through use of XML files: language elements, icons, etc are loaded here.
 * A standard treeviewer is used to view the tree structure that was created.
 * The contents & labels come from the ChameleonTreeContentsProvider & ChameleonLabelProvider 
 */
public class ChameleonOutlinePage extends ContentOutlinePage {

	// might create memory-leak:
//	//contains all trees created during the life of the workbench (for 1 session)
//	private static Vector<ChameleonContentOutlinePage> allTrees;

	
	//the current language used in the editor
	private Language currentLanguage;

	//The treeviewer that is used to store and view our tree
	private TreeViewer treeViewer;

	//the editor where this ChameleonContentOutlinePage is used
	private ChameleonEditor editor;

	//The chameleonTree used by this content outline
	private ChameleonOutlineTree chameleonTree;

	// Wheter to show the root-element of the tree, or not
	public boolean showRoot = false;

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
	public ChameleonOutlinePage(Language language, ChameleonEditor editor, List<String> allowedElements, List<String> defaultAllowedElements) {
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
		PresentationModel.initAllowedOutlineElementsByDefaults(language.name(), allowedElements, defaultAllowedElements);
//		if (allTrees == null) allTrees = new Vector<ChameleonContentOutlinePage>();
//		allTrees.add(this);		
	}

	/**
	 * Changes the currently used language to the given one, if the new one is supported
	 * Then the language elements for the new language are loaded
	 * @param language
	 * 	The language to be switched at
	 * 	 */
	public void changeLanguage(Language language) {
		if(currentLanguage != null && ! currentLanguage.equals(language)){
			updateMenu();
		}
		currentLanguage = language;
	}

	/**
	 * 
	 * @return the current language used
	 */
	public Language getCurrentLanguage() {
		return currentLanguage;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
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
		treeViewer.setLabelProvider(new ChameleonLabelProvider(currentLanguage, true, false, false)); 

		// Set initial expansion level
		//----------------------------
		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

		// Add menu
		//----------------------------
		IMenuManager menuMgr = getSite().getActionBars().getMenuManager();
		//menuMgr.setRemoveAllWhenShown(true);
		createMenuActions(menuMgr);
		menuMgr.addMenuListener(new IMenuListener(){
			public void menuAboutToShow(IMenuManager mgr) {
				mgr.removeAll();
				createMenuActions(mgr);
			};
		});

		// Add Toolbar
		//----------------------------
		IToolBarManager ToolbarMgr = getSite().getActionBars().getToolBarManager();
		ToolbarMgr.add(new Separator("Sorting"));
		ToolbarMgr.add(new SortMembersAction(treeViewer));
		// mgr.add(new RefreshAction(this)); // show refresh-button in toolbar
		ToolbarMgr.add(new Separator(Filters.FILTER_GROUP_NAME));
		ToolbarMgr.add(new Separator());

		Filters.addGlobalFilters(treeViewer, ToolbarMgr);

		// Build tree
		//----------------------------
		buildTree();
	}

	/**
	 * The modifier filters-submenu must be updated if the language is changed
	 */
	protected void updateMenu(){
		IMenuManager menuMgr = getSite().getActionBars().getMenuManager();
		menuMgr.removeAll();
		createMenuActions(menuMgr);
	}
	protected void createMenuActions(IMenuManager mgr) {
		mgr.add(new Separator("Sorting"));
		mgr.add(new SortMembersAction(treeViewer));
		mgr.add(new Separator("TreeViewer actions"));
		mgr.add(new TreeViewerActions.ExpandAllAction(treeViewer));
		mgr.add(new TreeViewerActions.CollapseAllAction(treeViewer));
		mgr.add(new TreeViewerActions.RefreshAction(treeViewer));
		mgr.add(new Separator("Outline actions"));
		mgr.add(new ShowRootAction(this));
		mgr.add(new hideTypesAction(treeViewer));
		mgr.add(new Separator(Filters.FILTER_GROUP_NAME));
		// here the filters will be added
		mgr.add(new Separator());

		Filters.addGlobalFilters(treeViewer, mgr);
		Filters.addModifierFiltersSubmenu(treeViewer, mgr, currentLanguage);
	}

	public class ShowRootAction extends Action{
		private ChameleonOutlinePage outline;
		public ShowRootAction(ChameleonOutlinePage outline){
			super("Show the root element", AS_CHECK_BOX);
			this.outline = outline;
			if(outline.showRoot){
				setChecked(true);
			}
		}
		@Override
		public void run() {
			outline.showRoot = ! outline.showRoot;
			setChecked(showRoot);
			buildTree();
		}
	}

	public class hideTypesAction extends Action {
		private TreeViewer treeViewer;
		public hideTypesAction(TreeViewer treeViewer){
			super("Hide defining types", AS_CHECK_BOX);
			this.treeViewer = treeViewer;
			ChameleonLabelProvider labelProv = ((ChameleonLabelProvider)treeViewer.getLabelProvider());
			if(!labelProv.isShowingDefiningTypes()){
				setChecked(true);
			}
		}
		@Override
		public void run() {
			ChameleonLabelProvider labelProv = ((ChameleonLabelProvider)treeViewer.getLabelProvider());
			if(labelProv.isShowingDefiningTypes()){
				labelProv.setShowDefiningTypes(false);
				treeViewer.refresh();
			} else {
				labelProv.setShowDefiningTypes(true);
				treeViewer.refresh();
			}
		}
	}

	public class SortMembersAction extends Action {
		private TreeViewer treeViewer;
		public SortMembersAction(TreeViewer treeViewer){
			super("Sort elements by category and name", AS_CHECK_BOX);
			setImageDescriptor(ChameleonEditorPlugin.getImageDescriptor("alphabetical_sort.gif"));
			this.treeViewer = treeViewer;
			if(membersComparator.equals(treeViewer.getComparator())){
				setChecked(true);
			}
		}
		@Override
		public void run() {
			if(! membersComparator.equals(treeViewer.getComparator())){
				// if not yet sorted
				treeViewer.setComparator(membersComparator);
			} else {
				treeViewer.setComparator(null);
			}
		}
	}
	public static final MembersComparator membersComparator = new MembersComparator();

	/**
	 * This responds to a SelectionChangedEvent.
	 * When another element in the ChameleonTree is selected, 
	 * a highlight of the element is made.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);

		ISelection selection= event.getSelection();
		if (selection.isEmpty())
			editor.resetHighlightRange();
		else {
			ChameleonOutlineTree segment= (ChameleonOutlineTree) ((IStructuredSelection) selection).getFirstElement();
			Element element = segment.getElement();
			editor.highLightElement(element);
		}
	}

	private void buildTree() {
		try{
			chameleonTree = new ChameleonOutlineTree();
			chameleonTree.composeTree(currentLanguage, getTreeRootElement());
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
		Element<?,?> result = getEditor().getDocument().compilationUnit();
		// if we don't want to show the root, and the root has one valid child, get this as root element
		if(!showRoot && result!=null && result.children()!=null && result.children().size()==1)
			result = result.children().iterator().next();
		return result;
	}

	/**
	 * 
	 * @return the editor where this is used
	 */
	private ChameleonEditor getEditor() {
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
		buildTree();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}