package chameleon.editor.editors;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import chameleon.editor.LanguageMgt;
import chameleon.editor.connector.ChameleonEditorPosition;
import chameleon.editor.presentation.PresentationManager;
import chameleon.editor.presentation.annotation.ChameleonAnnotation;
import chameleon.editor.presentation.outline.ChameleonContentOutlinePage;
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
	private ProjectionViewer projViewer;
	
	//manages the various aspects of the presentation
	private PresentationManager presentationManager;
	
	
	//The current Annotations used in the chameleon editor.  
	private Annotation[] oldAnnotations;
	
	//The chameleonAnnotations for this editor
	private Vector<ChameleonAnnotation> chameleonAnnotations;
	
	//The outline page with its content for this editor
	private ChameleonContentOutlinePage fOutlinePage;
	
	//The document that this editor uses.
	private ChameleonDocument document;
	
//	private Vector<Decorator> positions;


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
	    projViewer =(ProjectionViewer)getSourceViewer();
	    projectionSupport = new ProjectionSupport(projViewer,getAnnotationAccess(),getSharedColors());
	    projectionSupport.addSummarizableAnnotationType( "org.eclipse.ui.workbench.texteditor.error");
	    projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning");
	    projectionSupport.setHoverControlCreator(new IInformationControlCreator() {
	    	public IInformationControl createInformationControl(Shell parent) {
	    		return new DefaultInformationControl(parent);
	    	}
	    });
	    projectionSupport.install();
	    //turn projection mode on
	    projViewer.doOperation(ProjectionViewer.TOGGLE);
	    projViewer.setAnnotationHover(getSourceViewerConfiguration().getAnnotationHover(getSourceViewer()));
	    projViewer.enableProjection();
	    if (document != null)
	    	projViewer.addTextListener(new ChameleonTextListener(document, projViewer));			
	    

	    
	    annotationModel = projViewer.getProjectionAnnotationModel();
	   
	}
	

	
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
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
		
	    
		Vector<Position> positions = getDocument().getFoldablePositions();
		chameleonAnnotations.clear();
		
		Annotation[] annotations = new Annotation[positions.size()];
		//ChameleonAnnotation[] annotations = new ChameleonAnnotation[positions.size()];
		HashMap<Annotation,Position> newAnnotations = new HashMap<Annotation,Position>();
	   //HashMap<ChameleonAnnotation,Position> newAnnotations = new HashMap<ChameleonAnnotation,Position>();
	   
	   for(int i = 0; i < positions.size();i++)
	   {
		   //ChameleonAnnotation annotation = new ChameleonAnnotation( positions.get(i),false,false);
		   ProjectionAnnotation annotation = new ProjectionAnnotation();
		   int offset =positions.get(i).offset;
		   int length = positions.get(i).length;
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
	public void fold(Vector<ChameleonEditorPosition> positions){
		for(ChameleonAnnotation chamAnnot : chameleonAnnotations){
			for (int i = 0; i < positions.size(); i++) {
				ChameleonEditorPosition dec = positions.get(i);
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
	public void unfold(Vector<ChameleonEditorPosition> positions){
		for(ChameleonAnnotation chamAnnot : chameleonAnnotations){
			for (int i = 0; i < positions.size(); i++) {
				ChameleonEditorPosition dec = positions.get(i);
					if(chamAnnot.getPosition().getOffset()==dec.getOffset() &&
					   chamAnnot.getPosition().getLength()==dec.getLength()	){
					annotationModel.expand(chamAnnot.getAnnotation());
					((ChameleonSourceViewerConfiguration)getSourceViewerConfiguration()).getChameleonPresentationReconciler().doPresentation();
					break;
					
				}
			}
		}
	}
	
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	/**
	 * @return the adapter object for the given Class.
	 * if the class is an IContentOutlinePage, and then the outline Page is not effective,
	 * a new chameleon outline is created & set.
	 * 
	 */
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage= new ChameleonContentOutlinePage(getDocument().language(),this,this.getPresentationManager().getOutlineElements(),this.getPresentationManager().getDefaultOutlineElements());
				getDocument().getProjectNature().setOutlinePage(fOutlinePage);
				if (getEditorInput() != null)
					fOutlinePage.setInput(getEditorInput());
				
			}
			return fOutlinePage;
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
	protected PresentationManager getPresentationManager() {
		return presentationManager;
	}

	/**
	 * 
	 * @return the document from this editor
	 */
	public ChameleonDocument getDocument(){
//		try {
//		return (ChameleonDocument) ((ChameleonConfiguration)getSourceViewerConfiguration()).getChameleonReconciler().getDocument();
//		} catch (ClassCastException e){
//			return null;}
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
		
		if (document.getProject()==null){
			MessageBox box =  new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			box.setText("Decreased Functionality");
			box.setMessage("This document is not part of a Chameleon Project.  The editor will work in restricted mode. Thank you for reading this.");
			box.open();
		} else if (presentationManager==null && document!=null) {
			presentationManager = new PresentationManager(document, LanguageMgt.getInstance().getPresentationModel(document.language().name()));
		    if (projViewer != null)
		    	projViewer.addTextListener(new ChameleonTextListener(document/*, presentationManager*/, projViewer));			
		}
		
		document.setParseActionListener(this);
		
	}

	/**
	 * Is to be called whenever an update is needed for the markers
	 * It removes all problem markers, and then adds the ones needed.
	 */
	public void actionPerformed(ActionEvent arg0)  {
		
		ChameleonDocument doc = getDocument();
		IFile file = doc.getProject().getFile(getDocument().getRelativePathName());
		removeMarkers(file ,IMarker.PROBLEM);
		addProblemMarkers(file);
		
		
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







	

}
