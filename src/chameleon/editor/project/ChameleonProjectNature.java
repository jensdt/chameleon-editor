package chameleon.editor.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
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

import chameleon.core.MetamodelException;
import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.element.Element;
import chameleon.core.language.Language;
import chameleon.core.namespace.Namespace;
import chameleon.editor.LanguageMgt;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.editors.reconciler.ChameleonReconcilingStrategy;
import chameleon.editor.linkage.DocumentEditorToolExtension;
import chameleon.editor.presentation.outline.ChameleonContentOutlinePage;
import chameleon.input.ModelFactory;
import chameleon.input.ParseException;

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
	private IProject project;
	
	//the language of this nature
	private Language _language;
	
	//The elements in the model of this nature
	private ArrayList<ChameleonDocument> modelElements;
	
	public static final String NATURE = "ChameleonEditor.ChameleonNature";

	public void init(Language language){
//		setMetaModelFactory(LanguageMgt.getInstance().getModelFactory(language));
		this._language = language;
		createModel();
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
		return project;
	}

	/**
	 * A new project for this nature is set.
	 * All documents (if any) are loaded and the according model is built.
	 */
	public void setProject(IProject project) {
		this.project = project;
		
		try {
			BufferedReader f = new BufferedReader(new FileReader(new File(project.getLocation()+"/.CHAMPROJECT")));
			String lang = f.readLine();
			init(LanguageMgt.getInstance().findLanguage(lang));
			f.close();
		} catch (IOException e) {
			System.out.println("No initfile...");
		}
		
		loadDocuments();
		createModel();
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
	public void createModel(){

		if (modelFactory()!=null) {
		
		for (ChameleonDocument element:modelElements) {
			try {
				element.getFile().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
			  //getDocument().removeParseErrors();
			} catch (CoreException e) {
			   // something went wrong
				e.printStackTrace();
			}
			element.dumpPositions();
		}
		
		try{
			IDocument[]  docs = (IDocument[]) modelElements.toArray(new IDocument[0]);
			ModelFactory fact = modelFactory();
			Namespace model = fact.getMetaModel(new SourceSupplier(docs));
			ChameleonReconcilingStrategy.showSize(model);
//			setModel(model);
			
			for (Iterator<ChameleonDocument> iter = modelElements.iterator(); iter.hasNext();) {
				ChameleonDocument element =  iter.next();
				System.out.println(element);
			}
			
			
		}
		catch (Exception exc) {
			exc.printStackTrace();
		   
		}
		}
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
			addToModel(new ChameleonDocument(project,(IFile)resource,resource.getFullPath()));
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
	 * adds an extra chameleonDocument to this nature
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
		createModel();
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
			modelFactory().addSource(new DocumentEditorToolExtension(document),document.get(), parent);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MetamodelException e) {
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
