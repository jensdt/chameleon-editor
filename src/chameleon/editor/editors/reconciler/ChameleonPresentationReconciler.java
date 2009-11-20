package chameleon.editor.editors.reconciler;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.ui.texteditor.MarkerUtilities;

import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.validation.BasicProblem;
import chameleon.core.validation.Invalid;
import chameleon.core.validation.VerificationResult;
import chameleon.editor.connector.EclipseEditorTag;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.editors.ChameleonEditor;

/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * A class that handles the representation of the ChameleonDocument. It can process 
 * the dirty regions and is responsible for example syntax highlighting
 */
public class ChameleonPresentationReconciler extends AbstractChameleonReconciler implements IPresentationReconciler {
	//The strategy for this presentation reconciler
	private ChameleonReconcilingStrategy _strategy;
	//	The editor where this presentation reconciler is used
	//private ChameleonEditor editor;
	private ChameleonEditor _editor;
	
	/**
	 * Creation of a new PresentationReconciler for the given document with given strategy
	 * @param chamEditor
	 * 	The editor where this PresentationReconciler is used
	 * @param chameleonReconcilingStrategy
	 * 	The strategy that is being used
	 * @param incremental
	 * 	Denotes whether this  PresentationReconciler is incremental
	 * @param delay
	 * 	Denotes the delay for this PresentationReconciler
	 */
	public ChameleonPresentationReconciler(ChameleonEditor chamEditor,ChameleonReconcilingStrategy chameleonReconcilingStrategy) {
	       _strategy = chameleonReconcilingStrategy;
	       _editor = chamEditor;
	} 

	/**
	 * @return null;
	 */
	public IPresentationDamager getDamager(String contentType) {
		return damager;
	}

	/**
	 * @return null;
	 */
	public IPresentationRepairer getRepairer(String contentType) {
		return repairer;
	}

	
	private boolean presenting =false;
	private IPresentationDamager damager;
	private IPresentationRepairer repairer;
	//private HashMap<String, IPresentationDamager> fDamagers;
	//private HashMap<String, IPresentationRepairer> fRepairers;
	
	/**
	 * Colors the document where this presentationReconciler is used
	 * Folding is also done.
	 */
	public void doPresentation(){
		if (presenting) return;
		presenting = true;
		
		doFolding();
		doColoring();
    doVerificationErrors();
		
		presenting = false;
	}
	
	private void doVerificationErrors() {
		CompilationUnit cu = getDocument().compilationUnit();
		VerificationResult result = cu.verify();
		if(result instanceof Invalid) {
		  for(BasicProblem problem: ((Invalid)result).problems()) {
			  markError(problem);
		  }
		}
	}
	
	@Override
	public ChameleonDocument getDocument() {
		return (ChameleonDocument)super.getDocument();
	}
	
	public void markError(BasicProblem problem) {
		String message = problem.message();
		HashMap<String, Object> attributes = createProblemMarkerMap(message);
		EclipseEditorTag positionTag = (EclipseEditorTag) problem.element().tag(EclipseEditorTag.ALL_TAG);
		ChameleonDocument document = getDocument();
		if(positionTag != null) {
			int offset = positionTag.getOffset();
			int length = positionTag.getLength();
			setProblemMarkerPosition(attributes, offset, length,document);
		} else {
			attributes.put(IMarker.LINE_NUMBER, 1);
			System.out.println("ERROR: element of type "+problem.element().getClass().getName()+" is invalid, but there is no position attached.");
		}
		addProblemMarker(attributes, document);
	}

	public static void addProblemMarker(Map<String, Object> attributes, ChameleonDocument document) {
		try {
			MarkerUtilities.createMarker(document.getFile(),attributes,IMarker.PROBLEM);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static void setProblemMarkerPosition(Map<String, Object> attributes, int offset, int length, ChameleonDocument document) {
		int lineNumber;
		try {
			lineNumber = document.getLineOfOffset(offset);
			lineNumber++;
		} catch (BadLocationException e) {
			lineNumber = 0;
		}
    attributes.put(IMarker.CHAR_START, offset);
		attributes.put(IMarker.CHAR_END, offset + length);
	  attributes.put(IMarker.LINE_NUMBER, lineNumber);
	}

	public static HashMap<String, Object> createProblemMarkerMap(String message) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(IMarker.SEVERITY,IMarker.SEVERITY_ERROR);
		attributes.put(IMarker.MESSAGE, message);
		return attributes;
	}

	//Do the coloring
	private void doColoring(){
		try {
			((ChameleonDocument)this.fDocument).doPresentation(fViewer);
		} catch (ClassCastException e){
			e.printStackTrace();
		}
		 catch (NullPointerException npe){
			 npe.printStackTrace();
			System.out.println("assuming closure");
		 }
	}
	
	//Do the folding
	private void doFolding(){
		_editor.updateFoldingStructure();
		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see chameleonEditor.editors.reconciler.AbstractChameleonReconciler#reconcilerDocumentChanged(org.eclipse.jface.text.IDocument)
	 */
	protected void reconcilerDocumentChanged(ChameleonDocument newDocument) {
	//	System.out.println("ChameleonPresentationReconciler.reconcilerDocumentChanged is opgeroepen");
		_strategy.setDocument(newDocument);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconciler#getReconcilingStrategy(java.lang.String)
	 */
	public IReconcilingStrategy getReconcilingStrategy(String contentType) {
		return (IReconcilingStrategy) _strategy;
	}

	/**
	 * @return the editor for this PresentationReconciler
	 */
	public ChameleonEditor getEditor() {
		return _strategy.getConfiguration().getChameleonEditor();
	}

	/*
	 *  (non-Javadoc)
	 * @see chameleonEditor.editors.reconciler.AbstractChameleonReconciler#initialProcess()
	 */
	protected void initialProcess(){

		doPresentation();

		//FIXME als er elementen gefold zijn, en er wordt tekst bijgetypt, dan worden alle foldings ongedaan gemaakt
		getEditor().fold(((ChameleonDocument)fDocument).getFoldedElementsFromModel());

		

	}

	/**
	 * recoloring is done for the document this presenatationreconciler is working for
	 *
	 */
	public void reColor() {
		try {
			((ChameleonDocument)this.fDocument).doPresentation(fViewer);
		} catch (ClassCastException e){}
	}
	
	/**
	 * returns the text viewer
	 */
	public ITextViewer getTextViewer(){
		return fViewer;
	}

	@Override
	/**
	 * does nothing at all
	 */
	protected void process(ChameleonDirtyRegion dirtyRegion) {
			
	}









	


}
