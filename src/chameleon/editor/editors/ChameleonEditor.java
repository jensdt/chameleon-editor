package chameleon.editor.editors;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.rejuse.java.collections.Visitor;
import org.rejuse.predicate.SafePredicate;

import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.element.Element;
import chameleon.core.language.Language;
import chameleon.core.tag.Tag;
import chameleon.editor.ChameleonEditorPlugin;
import chameleon.editor.connector.EclipseEditorTag;
import chameleon.editor.editors.actions.IChameleonEditorActionDefinitionIds;
import chameleon.editor.presentation.PresentationManager;
import chameleon.editor.presentation.annotation.ChameleonAnnotation;
import chameleon.editor.presentation.hierarchy.HierarchyView;
import chameleon.editor.presentation.outline.ChameleonOutlinePage;
import chameleon.exception.ChameleonProgrammerException;
import chameleon.input.ParseException;

/**
 * @author Jef Geerinckx
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * An Editor for the chameleon framework
 * It supports features like : highlighting , code folding, ...
 * It updates the folding structure if asked. It also creates the outline view
 */
public class ChameleonEditor extends TextEditor implements ActionListener {

	//The support for folding in the editor. It arranges the projection of the foldable elements
	private ProjectionSupport projectionSupport;
	//the model for that handles the annotations throughout the editor
	private ProjectionAnnotationModel annotationModel;
	
	//the projectionViewer for this editor
	private ProjectionViewer _projViewer;
	
	//The current Annotations used in the chameleon editor.  
	private Annotation[] oldAnnotations;
	
	//The chameleonAnnotations for this editor
	private Vector<ChameleonAnnotation> chameleonAnnotations;
	
	
	//The document that this editor uses.
	private ChameleonDocument document;
	
  //FIXME: make this scalable wrt the number of views	
	
	//The outline page with its content for this editor
	private ChameleonOutlinePage _fOutlinePage;

	public ChameleonOutlinePage outlinePage() {
		return _fOutlinePage;
	}
	
	private HierarchyView _hierarchyView;

	/**
	 * Creates a new Editor that is properly configured
	 * The correct document provider is set and the configuration is made
	 */
	public ChameleonEditor() {
		super();
		
		/*
		 * Dit gaat een nieuw chameleon document provider geven
		 * A document provider has the following responsibilities:
		 *  #create an annotation model of a domain model element
		 *  #create and manage a textual representation, i.e., a document, of a domain model element
		 *  #create and save the content of domain model elements based on given documents
		 *  #update the documents this document provider manages for domain model elements to changes directly applied to those domain model elements
		 *  # notify all element state listeners about changes directly applied to domain model elements this document provider manages a document for, i.e. the document provider must know which changes of a domain model element are to be interpreted as element moves, deletes, etc.
		 *  
		 */
		setDocumentProvider(new ChameleonDocumentProvider(this));

		/*
		 * this can be used to retrieve the reconcilers
		 */
		ChameleonSourceViewerConfiguration configuration = new ChameleonSourceViewerConfiguration(this);
	    
		setSourceViewerConfiguration(configuration);
		initEditor();
	}

	public ChameleonEditor(ChameleonDocumentProvider documentProvider, ChameleonSourceViewerConfiguration configuration){
		setDocumentProvider(documentProvider);
		documentProvider.setChameleonEditor(this);
		setSourceViewerConfiguration(configuration);

		initEditor();
	}

	/**
	 * Common statements for both constructors
	 *
	 */
	private void initEditor(){

		chameleonAnnotations = new Vector<ChameleonAnnotation>(0);
		
	}
		
	/**
	 * 
	 * @return the configuration for this chameleonEditor.
	 * May return null before the editor's part has been created and after disposal. 
	 */
		public ChameleonSourceViewerConfiguration getChameleonConfiguration(){
			return (ChameleonSourceViewerConfiguration) getSourceViewerConfiguration();
		}
	
	
//	   private IResource extractSelection(ISelection sel) {
//		      if (!(sel instanceof IStructuredSelection))
//		         return null;
//		      IStructuredSelection ss = (IStructuredSelection)sel;
//		      Object element = ss.getFirstElement();
//		      if (element instanceof IResource)
//		         return (IResource) element;
//		      if (!(element instanceof IAdaptable))
//		         return null;
//		      IAdaptable adaptable = (IAdaptable)element;
//		      Object adapter = adaptable.getAdapter(IResource.class);
//		      return (IResource) adapter;
//		   }

		protected void createActions() {
			createLocalActions();
			super.createActions();
		}

		private void createLocalActions() {
			ResourceBundle bundle = ChameleonEditorPlugin.getDefault().getResourceBundle();
			if(bundle != null){
				// Content assistant (auto-completion)
				//System.out.println(bundle.getString("resource_bundle_loaded"));
				IAction action = new ContentAssistAction(bundle, "ContentAssistProposal.", this);
				String actionId = IChameleonEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
				action.setActionDefinitionId(actionId);
				setAction(actionId, action);
				markAsStateDependentAction(actionId, true);
				// Content Information
				action= new TextOperationAction(bundle, "ContentAssistContextInformation.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);	//$NON-NLS-1$
				actionId = IChameleonEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION;
				action.setActionDefinitionId(actionId);
				setAction(actionId, action);
				markAsStateDependentAction(actionId, true);
				// Formatting
				action = new TextOperationAction(bundle, "ContentFormatProposal.", this, ISourceViewer.FORMAT);
				actionId = IChameleonEditorActionDefinitionIds.FORMAT;
				//actionId = "ContentFormatProposal";
				action.setActionDefinitionId(actionId);
				setAction(actionId, action);
				markAsStateDependentAction(actionId, true);
			} else {
				String errorMsg = "\nThe source-folder of the ChameleonEditor must contain an internationalisation-file";
				errorMsg += "named \'" + ChameleonEditorPlugin.CHAMELEON_RESOURCEBUNDLE_BASENAME + ".properties\'\n";
				errorMsg += "otherwise the actions cannot be added to the ChameleonEditor.\n";
				System.err.println(errorMsg);
				new Exception().printStackTrace();
				throw new ChameleonProgrammerException(errorMsg);
			}
		}

		@Override
		protected void editorContextMenuAboutToShow(IMenuManager menu) {
			super.editorContextMenuAboutToShow(menu);
			addAction(menu, ITextEditorActionConstants.GROUP_REST,  IChameleonEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS); 
			addAction(menu, ITextEditorActionConstants.GROUP_REST,  IChameleonEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION); 
			addAction(menu, ITextEditorActionConstants.GROUP_REST,  IChameleonEditorActionDefinitionIds.FORMAT);
			
		}
	/**
	 * This creates the controls & models necessary to handle the projection annotations .
	 * Projection Support is also made here and made active.
	 * Hover control is created here
	 * A Chameleon Text listener is attached to the projection viewer
	 * 
	 * @param parent.
	 * 		The element used to create the parts
	 */
	public void createPartControl(Composite parent)
	{
			
			
		super.createPartControl(parent);
	    
	    /*
	     * alles wat te maken heeft met projection en projection document
	     */
	    _projViewer =(ProjectionViewer)getSourceViewer();
	    projectionSupport = new ProjectionSupport(_projViewer,getAnnotationAccess(),getSharedColors());
	    projectionSupport.addSummarizableAnnotationType( "org.eclipse.ui.workbench.texteditor.error");
	    projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning");
	    projectionSupport.setHoverControlCreator(new IInformationControlCreator() {
	    	public IInformationControl createInformationControl(Shell parent) {
	    		return new DefaultInformationControl(parent);
	    	}
	    });
	    projectionSupport.install();
	    //turn projection mode on
	    _projViewer.doOperation(ProjectionViewer.TOGGLE);
	    _projViewer.setAnnotationHover(getSourceViewerConfiguration().getAnnotationHover(getSourceViewer()));
	    _projViewer.enableProjection();
	    updateTextListener(document);
	    

	    
	    annotationModel = _projViewer.getProjectionAnnotationModel();
	   
	}
	

	private ChameleonTextListener _listener;
	
	/**
	 * The source viewer manages the annotation on the vertical ruler bar.
	 */
	protected ISourceViewer createSourceViewer(Composite parent,IVerticalRuler ruler, int styles){
		ISourceViewer viewer = new ProjectionViewer(parent, ruler,getOverviewRuler(), isOverviewRulerVisible(), styles);

	   // ensure decoration support has been created and configured.
	   getSourceViewerDecorationSupport(viewer);
	
	   return viewer;
	}
	

	
	/**
	 * Updates the folding structure on the basis of new decorator positions
	 * @param positions
	 * 		The new annotation positions
	 */
	public void updateFoldingStructure()
	{
		
	    
		List<Position> positions = getDocument().getFoldablePositions();
		System.out.println("Found "+positions.size()+" foldable positions.");
		chameleonAnnotations.clear();
		
		Annotation[] annotations = new Annotation[positions.size()];
		//ChameleonAnnotation[] annotations = new ChameleonAnnotation[positions.size()];
		HashMap<Annotation,Position> newAnnotations = new HashMap<Annotation,Position>();
	   //HashMap<ChameleonAnnotation,Position> newAnnotations = new HashMap<ChameleonAnnotation,Position>();
	   
	   for(int i = 0; i < positions.size();i++)
	   {
		   //ChameleonAnnotation annotation = new ChameleonAnnotation( positions.get(i),false,false);
		   ProjectionAnnotation annotation = new ProjectionAnnotation();
		   Position position = positions.get(i);
			 int offset =position.offset;
		   int length = position.length;
		   Position pos =new Position(offset,length);
	     newAnnotations.put(annotation,pos );
	     ChameleonAnnotation chamAnnotation = new ChameleonAnnotation(annotation,pos,false);
	     chameleonAnnotations.add(chamAnnotation);
	     annotations[i] = annotation;
	   }
	   
	   boolean go=false;
	   do {
		   
		   try {
			   annotationModel.modifyAnnotations(oldAnnotations, newAnnotations,null);
			   go=false;
		   } catch(NullPointerException e){
			   go=true;
		   }
	   } while (go);
		   oldAnnotations = annotations;

	}
	
	/**
	 * Folds the given positions in the Editor
	 * @param positions
	 * 	The positions to be folded
	 */
	public void fold(List<EclipseEditorTag> positions){
		for(ChameleonAnnotation chamAnnot : chameleonAnnotations){
			for (EclipseEditorTag dec : positions) {
					if(chamAnnot.getPosition().getOffset()==dec.getOffset() &&
					   chamAnnot.getPosition().getLength()==dec.getLength()	){
						annotationModel.collapse(chamAnnot.getAnnotation());
						break;
					}
				}
			}
		}
		
	
	/**
	 * Unfolds the given positions in the Editor
	 * @param positions
	 * 	The positions to be unfolded
	 */
	public void unfold(Vector<EclipseEditorTag> positions){
		for(ChameleonAnnotation chamAnnot : chameleonAnnotations){
			for (int i = 0; i < positions.size(); i++) {
				EclipseEditorTag dec = positions.get(i);
					if(chamAnnot.getPosition().getOffset()==dec.getOffset() &&
					   chamAnnot.getPosition().getLength()==dec.getLength()	){
					annotationModel.expand(chamAnnot.getAnnotation());
					((ChameleonSourceViewerConfiguration)getSourceViewerConfiguration()).getChameleonPresentationReconciler().doPresentation();
					break;
					
				}
			}
		}
	}
	
	
	/**
	 * This method is invoked by Eclipse to obtain e.g. the outline page for an editor.
	 */
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (_fOutlinePage == null) {
				Language language = getDocument().language();
				List<String> defaultAllowedOutlineElements = getPresentationManager().getDefaultOutlineElements();
				List<String> allowedElements = getPresentationManager().getPresentationModel().getOutlineElementsSimple();
				_fOutlinePage= new ChameleonOutlinePage(language, this, allowedElements, defaultAllowedOutlineElements);
			}
			return _fOutlinePage;
		}
		return super.getAdapter(required);
	}







//	/**
//	 * @return the decorator positions for this editor
//	 */
//	public Vector<Decorator> getPositions() {
//		return positions;
//	}


//	/**
//	 * Sets the new positions for the editor
//	 * @param positions2
//	 * 	the new positions. If positions2 is not effective, nothing happen
//	 */
//	public void setPositions(Position[] positions2) {
//		if (positions2!=null ){
//			Vector<Decorator> vect = new Vector<Decorator>(0);
//			for (int i = 0; i < positions2.length; i++) {
//				vect.add((Decorator)positions2[i]);
//			}
//			positions = vect;
//		} // 
//		
//	}
	
	/**
	 * Returns the presentation Manager for the editor.
	 */
	public PresentationManager getPresentationManager() {
		return getDocument().getPresentationManager();
	}

	/**
	 * 
	 * @return the document from this editor
	 */
	public ChameleonDocument getDocument(){
		return document;
	}

	/**
	 * Is called when the document has changed. 
	 * When the document is not part of a chameleon project, it will function with decreased functionality
	 * e.g. not syntax coloring. 
	 * Else when nothing manages the presentation, a new manager is made , together with a listener
	 * @param document
	 */
	public void documentChanged(ChameleonDocument document) {
		
		this.document = document;
		
		if (document.getProjectNature()==null){
			ChameleonEditorPlugin.showMessageBox("Decreased Functionality", "This document is not part of a Chameleon Project. " +
					"The editor will work in restricted mode. Thank you for reading this.", SWT.ICON_ERROR);		
		} 
		updateTextListener(document);
		
		document.setParseActionListener(this);
		
	}

	private void updateTextListener(ChameleonDocument document) {
		if(_projViewer != null && _listener != null) {
			_projViewer.removeTextListener(_listener);
		}
    if (document != null && _projViewer != null) {
			_listener = new ChameleonTextListener(document, _projViewer);
			_projViewer.addTextListener(_listener);
		}			
	}

	/**
	 * Is to be called whenever an update is needed for the markers
	 * It removes all problem markers, and then adds the ones needed.
	 */
	public void actionPerformed(ActionEvent arg0)  {
		System.out.println("XXXXXXXXXXXXXXXXXX EVENT RECEIVED");

		IFile file = getFile();
		removeMarkers(file ,IMarker.PROBLEM);
		addProblemMarkers(file);
	}

	public IFile getFile() {
		return getDocument().getFile();
	}
	
	//adds problem markers for the given file
	private void addProblemMarkers(IFile file) {
		List<ParseException> exceptions = getDocument().getParseErrors();
		for(ParseException exception : exceptions){
			String exceptionMessage = exception.toString();
			int lineNumber = getLineNumber(exceptionMessage);
			String message = getUsermessage(exceptionMessage);
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			attributes.put(IMarker.SEVERITY,IMarker.SEVERITY_ERROR);
			attributes.put(IMarker.MESSAGE, message);
			attributes.put(IMarker.LINE_NUMBER, lineNumber);
			
			try {
				MarkerUtilities.createMarker(file,attributes,IMarker.PROBLEM);

			} catch (CoreException e) {
				
				e.printStackTrace();
			}
		}
	}

	//removes markers from the given type out of the given file
	private void removeMarkers(IFile file, String type) {
		int depth = IResource.DEPTH_ZERO;
		try {
		  file.deleteMarkers(type, true, depth);
		  //getDocument().removeParseErrors();
		} catch (CoreException e) {
		   // something went wrong
		}
	}
	
	//parses the message from te exception message that was generated by ANTLR
	private String getUsermessage(String exceptionMessage) {
		
		exceptionMessage = exceptionMessage.replaceFirst(":","");
		exceptionMessage = exceptionMessage.replaceFirst(":","");
		String substring2 = exceptionMessage.substring(exceptionMessage.indexOf(":")+1);
		
		return substring2;
	}

	//parses the message fomr the message from ANTLR and returns the line number.
	private int getLineNumber(String message) {
		int result = Integer.parseInt(message.split(":")[1]);
		return result;
	}



	/**
	 * The line of the given elementen will be highlighted and visible
	 * 
	 * @param 	element a chameleon element
	 * @pre		The given element must be valid and must be an element opened in this editor
	 * @see		ChameleonEditor#showInEditor(Element, boolean, ChameleonEditor, String)
	 * 			if you don't have a reference to the correct editor, or no editor reference at all
	 * 			use the showInEditor method
	 * @see		#highLightElement(Element, boolean, String)
	 * 			for extra options
	 */
	public void highLightElement(Element element){
		highLightElement(element, true, EclipseEditorTag.NAME_TAG);
	}

	/**
	 * Highlight the given Chameleon element
	 * 
	 * @see		#highLightElement(Element)
	 * 
	 * @param 	element
	 * @pre		The given element must be valid and must be an element opened in this editor
	 * @param 	selectElement wheter to select the element, or just highlight the line
	 * @param	The name of the editorTag that has to be selected (if he exists). 
	 * 			Must be a value of EditorTagTypes
	 */
	public void highLightElement(Element element, boolean selectElement, String editorTagName){
		if(element==null){
			resetHighlight(selectElement);
			return;
		}
		int start = 0;
		int length = 0;
		if(element.tags().size()<1){
			System.err.println("Element "+element.toString()+" has no Editor tags");
			resetHighlight(selectElement);
			return;
		} else if(element.hasTag(editorTagName)){
			EclipseEditorTag tag = (EclipseEditorTag)element.tag(editorTagName);
			start = tag.getOffset();
			length = tag.getLength();
		} else if(element.hasTag(EclipseEditorTag.ALL_TAG)){
			EclipseEditorTag tag = (EclipseEditorTag)element.tag(EclipseEditorTag.ALL_TAG);
			start = tag.getOffset();
			length = tag.getLength();
		} else { // gebruik een willekeurige editorTag
			Collection<Tag> tags = element.tags();
			EclipseEditorTag firstTag = (EclipseEditorTag) tags.iterator().next();
			start = firstTag.getOffset();
			length = firstTag.getLength();
		}
		setHighlightOrSelection(selectElement, start, length);
	}
	
	public void highlight(EclipseEditorTag tag, boolean selectElement) {
		if(tag != null) {
			int start = tag.getOffset();
			int length = tag.getLength();
			setHighlightOrSelection(selectElement, start, length);
		}
	}

	private void setHighlightOrSelection(boolean selectElement, int start, int length) {
		if(selectElement){
			ISelectionProvider selProv = getEditorSite().getSelectionProvider();
			if(selProv != null){
				selProv.setSelection(new TextSelection(getDocument(), start, length));
			}
		} else {
			try {
				setHighlightRange(start, length, true);
			} catch (IllegalArgumentException x) {
				resetHighlight(selectElement);
			} 
		}
	}

	/**
	 * Resets the highlight of this editor
	 * 
	 * @param 	resetSelection
	 * 			if true, the selection will be removed
	 */
	public void resetHighlight(boolean resetSelection){
		if(resetSelection){
			ISelectionProvider selProv = getEditorSite().getSelectionProvider();
			if(selProv != null){
				ISelection sel = selProv.getSelection(); 
				int offset = 0;
				if(sel instanceof TextSelection){
					offset = ((TextSelection)sel).getOffset();
				}
				selProv.setSelection(new TextSelection(getDocument(), offset, 0));
			}
		}
		resetHighlightRange();
	}

	/**
	 * Show the given element in a Chameleon editor
	 * 
	 * @param 	element must be effective
	 * @param 	openNewEditor
	 * 			Whether to open a new editor if the element is part of an editor that isn't opened yet
	 * @param 	editor 
	 * 			a reference to a (arbitrary) Chameleon Editor, might be null
	 * 			if no Chameleon editor is opened, this reference is necessary to open a new Chameleon editor
	 * @result	true if the element is shown, false when failed or (element is part of closed editor and openNewEditor == false)
	 */
	public static boolean showInEditor(Element element, boolean openNewEditor, ChameleonEditor chamEditor){
		return showInEditor(element, openNewEditor, chamEditor, true, EclipseEditorTag.NAME_TAG);
	}

	/**
	 * Show the given element in a Chameleon editor
	 * 
	 * @param 	element must be effective
	 * 			only NamespacePartElement's can be shown (so no modifiers)
	 * @param 	openNewEditor
	 * 			Whether to open a new editor if the element is part of an editor that isn't opened yet
	 * @param 	editor 
	 * 			a reference to a (arbitrary) Chameleon Editor, might be null
	 * 			if no Chameleon editor is opened, this reference is necessary to open a new Chameleon editor
	 * @param	selectElement
	 * 			whether to select the element (if true), or just highlight the line of the element (if false) 
	 * @param 	editorTagToHighlight
	 * 			the type of editortag that has to be highlighted (must be a constant of EditorTagTypes)
	 * @result	true if the element is shown, false when failed or (element is part of closed editor and openNewEditor == false)
	 */
	public static boolean showInEditor(Element<?,?> element, boolean openNewEditor, ChameleonEditor chamEditor, boolean selectElement, String editorTagToHighlight){
		if(element==null){
			chamEditor.resetHighlight(selectElement);
			return false;
		}
		// if no editor given, get an active Chameleon editor:
		if(chamEditor==null){
			chamEditor = getAnActiveChameleonEditor();
		}
		// if ChameleonEditor found:
		if(chamEditor != null){
			try {
				// check wheter the compilationunit of the element is opened in the active editor
				final CompilationUnit elementCU = element.nearestAncestor(CompilationUnit.class);
				if(elementCU==null){
					chamEditor.resetHighlight(selectElement);
					return false;
				}
				// if already opened in the active editor, highlight element
				IEditorPart activeEditor = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if(activeEditor instanceof ChameleonEditor && elementCU.equals(((ChameleonEditor)activeEditor).getDocument().compilationUnit())){
					((ChameleonEditor)activeEditor).highLightElement(element, selectElement, editorTagToHighlight);
					return true;
				} else {
					ChameleonDocument doc = chamEditor.getDocument().getProjectNature().document(elementCU);
					if(doc!=null){
						IFile file = doc.getFile();
						// chamEditor is not the editor of element
						// search an opened editor with the same compilationUnit
						ChameleonEditor openedEditor = getOpenedEditorOfCompilationUnit(elementCU);
						if(openedEditor!=null){
							// do the IShowEditorInput notification before showing the editor
							// to reduce flicker
							if (openedEditor instanceof IShowEditorInput) {
								((IShowEditorInput) openedEditor).showEditorInput(new FileEditorInput(file));
							}
							IWorkbenchPage page = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage();
							page.bringToTop(openedEditor);
							openedEditor.highLightElement(element, selectElement, editorTagToHighlight);
							return true;
						}
						// if desired open in new ChameleonEditor:
						else if(openNewEditor){
							// @invar chamEditor!=null && openedEditor==null
							IWorkbenchPage page = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage();
							openedEditor = (ChameleonEditor)IDE.openEditor(page, file, ChameleonEditorPlugin.CHAMELEON_EDITOR_ID);
							openedEditor.highLightElement(element, selectElement, editorTagToHighlight);
							return true;
						} else {
							chamEditor.resetHighlight(selectElement);
							return false; // element not opened yet in an editor, and openNewEditor == false
						}
					} else {
						System.err.println("No chameleon document found for element "+ element.toString());
						chamEditor.resetHighlight(selectElement);
						return false;
					}
				}
				
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			chamEditor.resetHighlight(selectElement);
		}
		return false;
	}

	private static ChameleonEditor getAnActiveChameleonEditor(){
		IEditorPart editor = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		// check wheter the active editor is an ChameleonEditor:
		if (editor instanceof ChameleonEditor) {
			return (ChameleonEditor) editor;
		}
		// if active editor is no Chameleon editor, search an active chameleonEditor
		Collection<ChameleonEditor> activeEditors = getActiveChameleonEditors();
		if(activeEditors.size()>0)
			return activeEditors.iterator().next();
		return null;
	}

	private static Collection<ChameleonEditor> getActiveChameleonEditors(){
		final List<ChameleonEditor> editors = new ArrayList<ChameleonEditor>();
		IEditorReference[] references = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		new Visitor<IEditorReference>(){
			@Override
			public void visit(IEditorReference element) {
				IEditorPart editor = element.getEditor(true);
				if(editor instanceof ChameleonEditor)
					editors.add((ChameleonEditor)editor);
			}
		}.applyTo(references);
		return editors;
	}

	/**
	 * If an Chameleon-editor is opened with the given CompilationUnit, this chameleon editor is returned
	 */
	private static ChameleonEditor getOpenedEditorOfCompilationUnit(final CompilationUnit cu){
		// add all editors to a List:
		Collection<ChameleonEditor> editors = getActiveChameleonEditors();
		// filter out the one that has the same compilationunit as the given one
		new SafePredicate<ChameleonEditor>(){
			@Override
			public boolean eval(ChameleonEditor editor) {
				return(
						editor.getDocument().compilationUnit().equals(cu)  
				) ;
			}
		}.filter(editors);
		if(!editors.isEmpty()){
			// type check is done in the predicate eval method
			return editors.iterator().next();
		}
		return null;
	}

	/**
	 * Returns the chameleonEditor (if any) of the currently active page
	 * can return null!
	 */
	public static ChameleonEditor getCurrentActiveEditor(){
		IWorkbenchWindow activeWorkbenchWindow = Workbench.getInstance().getActiveWorkbenchWindow();
		if(activeWorkbenchWindow==null)
			return null;
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if(activePage == null)
			return null;
		IEditorPart editor = activePage.getActiveEditor();
		if(editor instanceof ChameleonEditor){
			return ((ChameleonEditor)editor);
		}
		return null;
	}




	

}
