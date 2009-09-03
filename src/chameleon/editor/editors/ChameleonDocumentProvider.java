package chameleon.editor.editors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import chameleon.editor.project.ChameleonProjectNature;

/**
 * @author Jef Geerinckx
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * Shared document provider specialized for ChameleonDocuments 
 *
 */
public class ChameleonDocumentProvider extends FileDocumentProvider {

	//the editor where this documentprovider is used
	private ChameleonEditor _editor;
	
	public void setChameleonEditor(ChameleonEditor chamEditor) {
		this._editor = chamEditor;
	}

	public ChameleonDocumentProvider(ChameleonEditor editor){
		setChameleonEditor(editor);
		
	}
	
	/*
	 * Creates a new chameleon document according to the element that is given.
	 * 
	 * 
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(java.lang.Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {
		IProject project = null;
		ChameleonProjectNature nature = null;
		IPath path=null;
		if (element instanceof IFileEditorInput){
			try {
				IFileEditorInput input = (IFileEditorInput)element;
				project = input.getFile().getProject();
				path=input.getFile().getFullPath();
				nature = (ChameleonProjectNature) project.getNature(ChameleonProjectNature.NATURE);
			}
			catch(ClassCastException a){
				project=null;
			}
			if (nature==null) {
				project=null;
			}
		}
		ChameleonDocument document = null;
		boolean newDocument = false;
		if(nature != null) {
			document = nature.documentOfPath(path);
		}
		if(document == null) {
			document = createEmptyChameleonDocument(project, path);
			newDocument = true;
		} else {
			System.out.println("Document exists");
		}
		if (setDocumentContent(document, (IEditorInput) element, getEncoding(element))) {
			setupDocument(element, document);
			_editor.documentChanged(document);
		}
		if(newDocument) {
		  nature.addToModel(document);
		} else {
			nature.updateModel(document);
		}
		
		return document;
	}
	
//	/*
//	 * Creates a new Chameleon document according to the element that is given.
//	 * The object also receives a meta model factor & content.
//	 * @param element
//	 * 		The element that is used to create the ChameleonDocument
//	 */
//	protected ChameleonDocument createChameleonDocument(Object element) throws CoreException {
//
//	}
	
	/*
	 * creates a new Chameleon document that is empty in the sense that there is no text in it yet
	 */
	protected ChameleonDocument createEmptyChameleonDocument(IProject project, IPath path) {
		return new ChameleonDocument(project, null, path);
	}
	

}