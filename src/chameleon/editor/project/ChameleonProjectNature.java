package chameleon.editor.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.rejuse.predicate.TypePredicate;

import chameleon.core.Config;
import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.element.ChameleonProgrammerException;
import chameleon.core.element.Element;
import chameleon.core.language.Language;
import chameleon.core.namespace.Namespace;
import chameleon.editor.LanguageMgt;
import chameleon.editor.connector.EclipseEditorInputProcessor;
import chameleon.editor.connector.EclipseSourceManager;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.editors.reconciler.ChameleonReconcilingStrategy;
import chameleon.editor.presentation.outline.ChameleonContentOutlinePage;
import chameleon.input.InputProcessor;
import chameleon.input.ModelFactory;
import chameleon.input.ParseException;
import chameleon.input.SourceManager;

/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * Defines the nature for ChameleonProjects, it contains the elements in the model 
 * of the nature, the language of the project, and knows the project it is created for.
 */
public class ChameleonProjectNature implements IProjectNature{

	public ChameleonProjectNature() {
		modelElements=new ArrayList<ChameleonDocument>();
	}
	
	//the project this natures resides
	private IProject _project;
	
	//the language of this nature
	private Language _language;
	
	//The elements in the model of this nature
	private ArrayList<ChameleonDocument> modelElements;
	
	public static final String NATURE = "ChameleonEditor.ChameleonNature";

	public void init(Language language){
//		setMetaModelFactory(LanguageMgt.getInstance().getModelFactory(language));
		if(language == null) {
			throw new ChameleonProgrammerException("Cannot set the language of a Chameleon project nature to null.");
		}
		this._language = language;
		language.setConnector(SourceManager.class, new EclipseSourceManager(this));
		language.addProcessor(InputProcessor.class, new EclipseEditorInputProcessor(this));
	}
	
	/**
	 * Configures this nature. to be called after initialisation.
	 */
	public void configure() throws CoreException {
		
		//later om de builders in te steken (compiler?)
		
	}

	/**
	 * Deconfigures this nature. To be called before object destruction.
	 */
	public void deconfigure() throws CoreException {
		
//		later om de builders in te steken (compiler?)
	}

	/**
	 * returns the project where this nature is linked with
	 */
	public IProject getProject() {
		return _project;
	}

	/**
	 * A new project for this nature is set.
	 * All documents (if any) are loaded and the according model is built.
	 */
	public void setProject(IProject project) {
		if(project != _project) {
		this._project = project;
		
		try {
			BufferedReader f = new BufferedReader(new FileReader(new File(project.getLocation()+"/.CHAMPROJECT")));
			String lang = f.readLine();
			f.close();
			Language language = LanguageMgt.getInstance().createLanguage(lang);
			init(language);
			loadDocuments();
			updateAllModels();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("No initfile...");
		}
		}
	}
	
//	private void setMetaModelFactory(IMetaModelFactory mMF) {
//		_metaModelFactory = mMF;
//	}

	/*
	 *  (non-Javadoc)
	 * @see chameleonEditor.editors.IChameleonDocument#getMetaModelFactory()
	 */
	public ModelFactory modelFactory() {
		return language().connector(ModelFactory.class);
	}


	
	/**
	 * Creates a new model for this document. If another model was set, anything of it is removed
	 * and is replaced by the new model.
	 */
	public void updateAllModels() {

			try {
				for (ChameleonDocument doc : modelElements) {
					updateModel(doc);
				}
				if (Config.DEBUG) {
					// DEBUG: This prints the size of the entire model so memory problems
					// can be detected. If there is a problem,
					// the number will keep getting bigger while the size of the source
					// files remains constant.
					ChameleonReconcilingStrategy.showSize(language().defaultNamespace());
					for (Iterator<ChameleonDocument> iter = modelElements.iterator(); iter.hasNext();) {
						ChameleonDocument element = iter.next();
						System.out.println(element);
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();

			}
		
	}

	private void updateModel(ChameleonDocument doc) throws ParseException  {
		try {
			doc.getFile().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
			// getDocument().removeParseErrors();
		} catch (CoreException e) {
			// something went wrong
			e.printStackTrace();
		}
		doc.dumpPositions();
		modelFactory().addToModel(doc.get());
	}

	/**
	 * Loads all the project documents.
	 *
	 */
	public void loadDocuments(){
		modelElements.clear();
		IResource[] resources;
		try {
			resources = getProject().members();

			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if((resource instanceof IFile)){
					if(!((resource.getFullPath().getFileExtension().equals("project"))
						||
						((resource.getFullPath().getFileExtension().equals("CHAMPROJECT"))
						)))
						addResourceToModel(resource);
				}
				else //tis ne folder
					if (resource instanceof IFolder) {
						addResourceToModel(resource);
					}
					
			}
		
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Adds another resource to the model. This can be either a file or a folder
	 * @param resource
	 */
	private void addResourceToModel(IResource resource) {
		System.out.println("ADDING :: "+resource.getName());
		
		if (resource instanceof IFile)  {
			addToModel(new ChameleonDocument(_project,(IFile)resource,resource.getFullPath()));
		}
		if (resource instanceof IFolder) {
			IFolder folder = (IFolder) resource ;
			IResource[] resources = null;
			try {
				resources = folder.members();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < resources.length; i++) {
				IResource folderResource = resources[i];
				addResourceToModel(folderResource);
			}
			
		}
	}


	

	//The meta model factory that this document can use
//	private IMetaModelFactory _metaModelFactory;
	
	//The meta model for this factory
//	private Namespace _metaModel;
	
	private ChameleonContentOutlinePage _outlinePage;
	

//	/**
//	 * Sets model as the new model for this ChameleonDocument
//	 * @param model
//	 * 		The new model.
//	 */
//	private void setModel(Namespace model){
//		_metaModel = model;
//	}
	

	/* (non-Javadoc)
	 * @see chameleonEditor.editors.IChameleonDocument#getModel()
	 */
	public Namespace getModel(){
		return language().defaultNamespace();
	}

	/**
	 * Adds an extra chameleonDocument to this nature
	 * @param document
	 */
	public void addToModel(ChameleonDocument document) {
		ChameleonDocument same = null;
		for (ChameleonDocument element: modelElements) {
			if (element.isSameDocument(document)) {
				same = element;
			}
		}
		if (same!=null) {
			modelElements.remove(same);
		}
		modelElements.add(document);
		// SPEED: does this really reparse all documents?
		try {
			updateModel(document);
		} catch (ParseException e) {
			// FIXME Can we ignore this exception? Normally, the parse error markers should have been set.
			e.printStackTrace();
		}
	}

	public List<ChameleonDocument> documents(){
		return new ArrayList<ChameleonDocument>(modelElements);
	}
	
	public ChameleonDocument document(Element<?,?> element) {
		CompilationUnit cu = element.nearestAncestor(CompilationUnit.class);
		for(ChameleonDocument doc : modelElements) {
			if(doc.compilationUnit() == cu) {
				return doc;
			}
		}
		return null;
	}
	
/*	public IMetaModel getMetamodel(IDocument document){
		if (modelElements.contains(document))
			try {
				return  getMetaModelFactory().getMetaModel(new IDocument[]{document});
			} catch (Exception e) {
				return null;
			}
		else
			return null;
	}

	
	public IMetaModel getMetamodel(IDocument[] document){
		if (modelElements.contains(document))
			try {
				return  getMetaModelFactory().getMetaModel(document);
			} catch (Exception e) {
				return null;
			}
		else
			return null;
	}
	*/
	public void removeModelElement(IDocument document){
		modelElements.remove(document);
		
	}

//	/**
//	 * 
//	 * @return the language of the project
//	 */
//	public String getLanguage() {
//		return _language;
//	}
	
	public Language language() {
		return _language;
	}

	public void addModelElement(ChameleonDocument document, Element parent) {
		modelElements.add(document);
		try {
			modelFactory().addToModel(document.get());
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	}
	
	public void updateOutline() {
		if (_outlinePage!=null) {
		  _outlinePage.updateOutline();
		}
	}

	public void setOutlinePage(ChameleonContentOutlinePage outlinePage) {
		this._outlinePage = outlinePage;
		
	}


}
