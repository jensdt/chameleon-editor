package chameleon.editor.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;

import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.element.Element;
import chameleon.core.language.Language;
import chameleon.core.namespace.Namespace;
import chameleon.editor.ChameleonEditorPlugin;
import chameleon.editor.LanguageMgt;
import chameleon.editor.builder.ChameleonBuilder;
import chameleon.editor.connector.EclipseEditorInputProcessor;
import chameleon.editor.connector.EclipseSourceManager;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.editors.ChameleonEditor;
import chameleon.editor.presentation.PresentationModel;
import chameleon.exception.ChameleonProgrammerException;
import chameleon.input.InputProcessor;
import chameleon.input.ModelFactory;
import chameleon.input.ParseException;
import chameleon.input.SourceManager;

/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx
 * @author Tim Vermeiren
 * @author Marko van Dooren
 * 
 * Defines the nature for ChameleonProjects, it contains the elements in the model 
 * of the nature, the language of the project, and knows the project it is created for.
 */
public class ChameleonProjectNature implements IProjectNature{
	public ChameleonProjectNature() {
		_documents=new ArrayList<ChameleonDocument>();
	}
	
	//the project this natures resides
	private IProject _project;
	
	//the language of this nature
	private Language _language;
	
	public static final String CHAMELEON_PROJECT_FILE_EXTENSION = "CHAMPROJECT";

	//The elements in the model of this nature
	private ArrayList<ChameleonDocument> _documents;
	
	public static final String NATURE = ChameleonEditorPlugin.PLUGIN_ID+".ChameleonNature";
	
	private PresentationModel _presentationModel;
	
	public PresentationModel presentationModel() {
		return LanguageMgt.getInstance().getPresentationModel(_language.name());
//		PresentationModel result = _presentationModel;
//		if(result == null) {
//      String filename = "/xml/presentation.xml";
//      InputStream stream = language().getClass().getClassLoader().getResourceAsStream(filename);
//      result = new PresentationModel(language().name(), stream);
//		}
//		return result;
	}

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
		System.out.println("Configuring Chameleon project nature.");
		//later om de builders in te steken (compiler?)
		addBuilder(getProject(), ChameleonBuilder.BUILDER_ID);
		new Job("Chameleon Project Build"){
		
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					getProject().build(ChameleonBuilder.FULL_BUILD, ChameleonBuilder.BUILDER_ID, null, monitor);
					return Status.OK_STATUS;
				}catch(CoreException exc) {
					return Status.CANCEL_STATUS;
				}
			}
		}.schedule();
	}
	
  private void addBuilder(IProject project, String id) throws CoreException {
    IProjectDescription desc = project.getDescription();
    ICommand[] commands = desc.getBuildSpec();
    for (int i = 0; i < commands.length; ++i) {
       if (commands[i].getBuilderName().equals(id)) {
          return;
       }
    }
    //add builder to project
    ICommand command = desc.newCommand();
    command.setBuilderName(id);
    ICommand[] nc = new ICommand[commands.length + 1];
    // Add it before other builders.
    System.arraycopy(commands, 0, nc, 1, commands.length);
    nc[0] = command;
    desc.setBuildSpec(nc);
    project.setDescription(desc, null);
 }


	/**
	 * Deconfigures this nature. To be called before object destruction.
	 */
	public void deconfigure() throws CoreException {
		getProject().getWorkspace().removeResourceChangeListener(_projectListener);
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
			IProject old = _project;
			this._project = project;
			if(project != null) {
			try {
				BufferedReader f = new BufferedReader(new FileReader(new File(project.getLocation()+"/."+CHAMELEON_PROJECT_FILE_EXTENSION)));
				String lang = f.readLine();
				f.close();
				Language language = LanguageMgt.getInstance().createLanguage(lang);
				init(language);
				loadDocuments();
				_projectListener = new ProjectChangeListener();
				getProject().getWorkspace().addResourceChangeListener(_projectListener, IResourceChangeEvent.POST_CHANGE);
				//			updateAllModels();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("No initfile...");
			}
			} else {
				old.getWorkspace().removeResourceChangeListener(_projectListener);
			}
		}
	}
	
	private IResourceChangeListener _projectListener;
	
	public class ProjectChangeListener implements IResourceChangeListener {

		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			try {
				delta.accept( new ResourceDeltaVisitor() {

					@Override
					public void handleAdded(IResourceDelta delta) throws CoreException {
					}

					@Override
					public void handleChanged(IResourceDelta delta) throws CoreException {
					}

					@Override
					public void handleRemoved(IResourceDelta delta) throws CoreException {
					}
					
				}
				);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
				for (ChameleonDocument doc : _documents) {
					updateModel(doc);
				}
//				if (Config.DEBUG) {
//					// DEBUG: This prints the size of the entire model so memory problems
//					// can be detected. If there is a problem,
//					// the number will keep getting bigger while the size of the source
//					// files remains constant.
//					ChameleonReconcilingStrategy.showSize(language().defaultNamespace());
//					for (Iterator<ChameleonDocument> iter = _documents.iterator(); iter.hasNext();) {
//						ChameleonDocument element = iter.next();
//						System.out.println(element);
//					}
//				}
			} catch (Exception exc) {
				exc.printStackTrace();

			}
		
	}

	/**
	 * Removes the old file markers and document positions, and asks the model factory
	 * to replace the contents of the compilation unit associated with the document.
	 * @param doc
	 */
	public void updateModel(ChameleonDocument doc) {
		try {
			doc.getFile().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
			// getDocument().removeParseErrors();
		} catch (CoreException e) {
			// something went wrong
			e.printStackTrace();
		}
		doc.dumpPositions();
		try {
			modelFactory().addToModel(doc.get(), doc.compilationUnit());
		} catch (ParseException e) {
			// FIXME Can we ignore this exception? Normally, the parse error markers should have been set.
			e.printStackTrace();
		}

	}

	/**
	 * Loads all the project documents.
	 *
	 */
	public void loadDocuments(){
		_documents.clear();
		IResource[] resources;
		try {
			resources = getProject().members();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				addResourceToModel(resource);
//				if((resource instanceof IFile)){
//					if(!(isEclipseProjectFile(resource) || (isChameleonProjectFile(resource)))) {
//						IPath fullPath = resource.getFullPath();
//						String fileExtension = fullPath.getFileExtension();
//						if(extensions.contains(fileExtension)) {
//						  addResourceToModel(resource);
//						}
//					}
//				}
//				else //tis ne folder
//					if (resource instanceof IFolder) {
//						addResourceToModel(resource);
//					}
					
			}
		
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
	}

	private boolean isChameleonProjectFile(IResource resource) {
		String ext = extension(resource);
		return ext != null && ext.equals(CHAMELEON_PROJECT_FILE_EXTENSION);
	}

	private boolean isEclipseProjectFile(IResource resource) {
		String ext = extension(resource);
		return ext != null && ext.equals("project");
	}

	private String extension(IResource resource) {
		return resource.getFullPath().getFileExtension();
	}

	/**
	 * Adds another resource to the model. This can be either a file or a folder
	 * @param resource
	 */
	public void addResourceToModel(IResource resource) {
		List<String> extensions = LanguageMgt.getInstance().extensions(language());
		if (resource instanceof IFile)  {
			if(!(isEclipseProjectFile(resource) || (isChameleonProjectFile(resource)))) {
				IPath fullPath = resource.getFullPath();
				String fileExtension = fullPath.getFileExtension();
				if(extensions.contains(fileExtension)) {
					System.out.println("ADDING :: "+resource.getName());
					addToModel(new ChameleonDocument(this,(IFile)resource,resource.getFullPath()));
				}
			}
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


	


	/* (non-Javadoc)
	 * @see chameleonEditor.editors.IChameleonDocument#getModel()
	 */
	public Namespace<?> getModel(){
		return language().defaultNamespace();
	}

	/**
	 * Adds an extra chameleonDocument to this nature
	 * @param document
	 */
	public void addToModel(ChameleonDocument document) {
		//FIXME: why do we remove the 'same' document and add the new one if
		//       the document was already in the model?
		ChameleonDocument same = null;
		for (ChameleonDocument element: _documents) {
			if (element.isSameDocument(document)) {
				same = element;
			}
		}
		if (same!=null) {
			_documents.remove(same);
		}
		_documents.add(document);
		updateModel(document);
	}

	public List<ChameleonDocument> documents(){
		return new ArrayList<ChameleonDocument>(_documents);
	}
	
	/**
	 * Return the Chameleon/Eclipse document for the compilation unit of the
	 * given element.
	 * 
	 * @param element
	 * @return
	 */
	public ChameleonDocument document(Element<?,?> element) {
		if(element != null) {
			CompilationUnit cu = element.nearestElement(CompilationUnit.class);
			for(ChameleonDocument doc : _documents) {
				if(doc.compilationUnit().equals(cu)) {
					return doc;
				}
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
		_documents.remove(document);
		
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
		_documents.add(document);
		try {
			modelFactory().addToModel(document.get(), document.compilationUnit());
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	}
	
	public ChameleonDocument documentOfPath(IPath path) {
		ChameleonDocument result = null;
		for(ChameleonDocument doc:_documents) {
			if(doc.path().equals(path)) {
				result = doc;
				break;
			}
		}
		return result;
	}

	/**
	 * Returns the document with the given file
	 * @param file must be effective
	 * @return returns null if no appropriate document found.
	 */
	public ChameleonDocument documentOfFile(IFile file){
		if(file == null)
			return null;
		for(ChameleonDocument doc : _documents){
			if(file.equals(doc.getFile())){
				return doc;
			}
		}
		return null;
	}

	/**
	 * Static method to retrieve the language of the current editor. This
	 * is required because the views are global, and not connected to a particular
	 * project.
	 * @return
	 */
	public static Language getCurrentLanguage(){
		ChameleonEditor editor = ChameleonEditor.getCurrentActiveEditor();
		if(editor!=null){
			ChameleonDocument doc = editor.getDocument();
			if(doc!= null)
				return doc.language();
		}
		return null;
	}

	public List<CompilationUnit> compilationUnits() {
		ArrayList<CompilationUnit> result = new ArrayList<CompilationUnit>();
		for(ChameleonDocument document: documents()) {
			result.add(document.compilationUnit());
		}
		return result;
	}

	public void flushProjectCache() {
		for(CompilationUnit compilationUnit: compilationUnits()) {
			compilationUnit.flushCache();
		}
	}

}
