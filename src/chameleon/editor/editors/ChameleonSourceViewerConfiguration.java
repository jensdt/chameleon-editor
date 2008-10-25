package chameleon.editor.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import chameleon.editor.editors.reconciler.ChameleonPresentationReconciler;
import chameleon.editor.editors.reconciler.ChameleonReconciler;
import chameleon.editor.editors.reconciler.ChameleonReconcilingStrategy;
import chameleon.editor.presentation.hyperlink.ChameleonHyperlinkDetector;
import chameleon.editor.presentation.annotation.ChameleonAnnotationHover;


/**
 * @author Jef Geerinckx
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * This class bundles the configuration space of a chameleon source viewer.
 * Each method in this class get as argument the source viewer for which it should 
 * provide a particular configuration setting such as a presentation reconciler. 
 * Based on its specific knowledge about the returned object, the configuration might share such objects or compute them according to some rules.
 *
 */
public class ChameleonSourceViewerConfiguration extends SourceViewerConfiguration {

	//The presentationReconciler from this ChameleonConfiguration
	//private ChameleonPresentationReconciler presrec;
	
	
	/*
	 * The strategy that is being used for this particular configuration
	 */
	private ChameleonReconcilingStrategy chameleonReconcilingStrategy;
	
	
	//The editor for which this configuration applies;
	private ChameleonEditor ChamEditor;

	//The reconciler for this configuration
	private IReconciler reconciler;

	//The presentation reconciler for this configuration
	private ChameleonPresentationReconciler presentationReconciler;
	
	/**
	 * Creates a new configuration for the given editor.
	 * A new reconciling strategy is made. The reconciler & the reconciler for the presentation
	 * are added to the strategy.
	 * A listener is attached, to detect updates
	 * @param editor
	 * 		The editor for this configuration. May not be null;
	 */
	public ChameleonSourceViewerConfiguration(ChameleonEditor editor){
		//Assert.isNotNull(editor);
		ChamEditor = editor;
		chameleonReconcilingStrategy=new ChameleonReconcilingStrategy(this);
		//vorige waarden waren 1000 & 3000
		presentationReconciler = new ChameleonPresentationReconciler(ChamEditor,chameleonReconcilingStrategy,true,100);
		//chameleonReconcilingStrategy.setPresentationReconciler((ChameleonPresentationReconciler) presentationReconciler);
		reconciler = new ChameleonReconciler(/*ChamEditor,*/chameleonReconcilingStrategy,true,100);
		chameleonReconcilingStrategy.addUpdateListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				if(presentationReconciler.getDocument()!=null){ //anders is het document gesloten
					presentationReconciler.doPresentation();
				}}});
		
		chameleonReconcilingStrategy.addUpdateListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ChamEditor.getDocument().getProjectNature().updateOutline();
					}
				});	
				
			}});
	}
	
	/** 
	 * Returns the reconciler ready to be used with the given source viewer
	 * The reconciler already has a strategy.
	 * @param sourceViewer
	 * 		The sourceViewer used. may <u>not</u> be null
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer){
		chameleonReconcilingStrategy.setDocument((ChameleonDocument) sourceViewer.getDocument());	
		return reconciler;
	}
	
	
	
	
	/**
	 * A presentation Reconciler is returned, with a strategy already set.
	 * @param sourceViewer
	 * 		The sourceViewer used. may <u>not</u> be null
	 */
//	 De getPresentationReconciler wordt het eerst opgeroepen bij het opstarten van eclipse
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		chameleonReconcilingStrategy.setDocument((ChameleonDocument) sourceViewer.getDocument());
		return presentationReconciler;
	}
	
	
	/**
	 * Returns the annotation hover which will provide the information to be shown in a 
	 * hover popup window when requested for the given source viewer
	 * Can only be <code>null</code> when nothing is available for the hover
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new ChameleonAnnotationHover();
		
	}
	
	/**
	 * 
	 * @return the ChameleonPresentationReconciler from this configuration
	 */
	public ChameleonPresentationReconciler getChameleonPresentationReconciler(){
		return (ChameleonPresentationReconciler) presentationReconciler;
	}
	
	/**
	 * 
	 * @return the ChameleonReconciler from this configuration
	 */
	public ChameleonReconciler getChameleonReconciler(){
		return (ChameleonReconciler) reconciler;
	}

	/**
	 * 
	 * @return the editor this configuration is for
	 */
	public ChameleonEditor getChameleonEditor() {
		return ChamEditor;
	}
	
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer){
		// TODO: supercall??
		IHyperlinkDetector newDetector = new ChameleonHyperlinkDetector();
		// IHyperlinkDetector[] result = (IHyperlinkDetector[]) hyperlinkDetectors.toArray(new IHyperlinkDetector[hyperlinkDetectors.size()]);
		IHyperlinkDetector[] detectors = new IHyperlinkDetector[]{newDetector};
		return detectors;
	}
	
	@Override
	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer){
		//TODO: supercall?
		return new DefaultHyperlinkPresenter(new Color(null, 0, 0, 255));
	}

	

}
