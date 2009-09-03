package chameleon.editor.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import chameleon.editor.editors.reconciler.ChameleonPresentationReconciler;
import chameleon.editor.editors.reconciler.ChameleonReconciler;
import chameleon.editor.editors.reconciler.ChameleonReconcilingStrategy;
import chameleon.editor.presentation.annotation.ChameleonAnnotationHover;
import chameleon.editor.presentation.autocompletion.ChameleonContentAssistProcessor;
import chameleon.editor.presentation.formatting.ChameleonAutoEditStrategy;
import chameleon.editor.presentation.formatting.ChameleonFormatterStrategy;
import chameleon.editor.presentation.hyperlink.ChameleonHyperlinkDetector;


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
	private ChameleonReconciler reconciler;

	//The presentation reconciler for this configuration
	private ChameleonPresentationReconciler presentationReconciler;
	
	/**
	 * Wheter the hyperlinks should be colored according to their accessibility
	 */
	private final boolean USE_COLORED_HYPERLINKS = true;

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
		presentationReconciler = new ChameleonPresentationReconciler(ChamEditor,chameleonReconcilingStrategy);
		reconciler = new ChameleonReconciler(chameleonReconcilingStrategy,true,100);
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
		return presentationReconciler;
	}
	
	/**
	 * 
	 * @return the ChameleonReconciler from this configuration
	 */
	public ChameleonReconciler getChameleonReconciler(){
		return reconciler;
	}

	/**
	 * 
	 * @return the editor this configuration is for
	 */
	public ChameleonEditor getChameleonEditor() {
		return ChamEditor;
	}
	
	/**
	 * Returns the detectors for hyperlinks. These are used to decide if a word
	 * is presented as a hyperlink.
	 * 
	 * @author Tim Vermeiren
	 */
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer){
		// Add HyperlinkDetectors of superclass to Vector (URLHyperlinkDetector)
		Vector<IHyperlinkDetector> result = new Vector<IHyperlinkDetector>(Arrays.asList(super.getHyperlinkDetectors(sourceViewer)));
		// add ChameleonHyperlinkDetector to vector:
		result.add(new ChameleonHyperlinkDetector());
		// return vector as array:
		return result.toArray(new IHyperlinkDetector[result.size()]);
	}

	/**
	 * Decides how hyperlinks are presented on the screen.
	 * Default is blue underlined.
	 * 
	 * @author Tim Vermeiren
	 */
	@Override
	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer){
		if(USE_COLORED_HYPERLINKS){
			return new ChameleonHyperlinkPresenter(new Color(null, 0, 0, 255), new Color(null, 0, 200, 0), new Color(null, 255, 0, 0), new Color(null, 255, 128, 0));
		} else {
			return super.getHyperlinkPresenter(sourceViewer);
		}
	}

	// Nesessary for Quick fixes (of code errors and warnings):
//	@Override
//	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
//	return new QuickAssistAssistant();
//	}

	/**
	 * Auto-completion assistant
	 * 
	 * @author Tim Vermeiren
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant= new ContentAssistant();
		assistant.setContentAssistProcessor(new ChameleonContentAssistProcessor(),  IDocument.DEFAULT_CONTENT_TYPE);
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(100);
		assistant.enableAutoInsert(false);
		assistant.setEmptyMessage("No auto-completion proposals found.");
		
		IInformationControlCreator informationControlCreator= new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell shell) {
				IInformationControl result = new DefaultInformationControl(shell);
				result.setInformation("Information control information");
				return result;
			}
		};
		
		assistant.setInformationControlCreator(informationControlCreator);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		return assistant;
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		ArrayList<IAutoEditStrategy> result = new ArrayList<IAutoEditStrategy>(Arrays.asList(super.getAutoEditStrategies(sourceViewer, contentType)));
		result.add(new ChameleonAutoEditStrategy());
		return result.toArray(new IAutoEditStrategy[result.size()]);
	}

	@Override
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		ContentFormatter result = new ContentFormatter();
		result.enablePartitionAwareFormatting(false);
		result.setFormattingStrategy(new ChameleonFormatterStrategy(getChameleonEditor()), IDocument.DEFAULT_CONTENT_TYPE);
		return result;
	}

//	@Override
//	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer){
//		// TODO: supercall??
//		IHyperlinkDetector newDetector = new ChameleonHyperlinkDetector();
//		// IHyperlinkDetector[] result = (IHyperlinkDetector[]) hyperlinkDetectors.toArray(new IHyperlinkDetector[hyperlinkDetectors.size()]);
//		IHyperlinkDetector[] detectors = new IHyperlinkDetector[]{newDetector};
//		return detectors;
//	}
//	
//	@Override
//	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer){
//		//TODO: supercall?
//		return new DefaultHyperlinkPresenter(new Color(null, 0, 0, 255));
//	}

	

}
