/*
 * Created on 17-okt-06
 *
 */
package chameleon.editor.presentation.hyperlink;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import chameleon.core.MetamodelException;
import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.declaration.Declaration;
import chameleon.core.declaration.DeclarationContainer;
import chameleon.core.declaration.Signature;
import chameleon.core.reference.CrossReference;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.linkage.Decorator;

/**
 * The Hyperlink of a cross reference element in the editor. When clicked, it will open
 * the document of the element referenced by the cross reference.
 * 
 * @author Tim Vermeiren
 * @author Marko van Dooren
 */
public class ChameleonHyperlink implements IHyperlink {
	
	/**
	 * The cross reference in the model.
	 */
	CrossReference _element;
	
	IRegion _region;
	
	ChameleonDocument _document;
	
	public ChameleonHyperlink(CrossReference element, IRegion region,ChameleonDocument document){
		_element = element;
		_region = region;
		_document = document;
	}
	
	public String getHyperlinkText() {
		return getReference().toString();
	}
	
	public CrossReference getReference(){
		return this._element;
	}
	
	public Declaration getReferencedElement() throws MetamodelException {
		return getReference().getElement();
	}
	
	public String getTypeLabel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private ChameleonDocument findDocument(CompilationUnit cu) {
		List<ChameleonDocument> docs = _document.getProjectNature().getAllMetaModelElements();
		ChameleonDocument result = null;
		for(ChameleonDocument doc: docs) {
			if(doc.getCompilationUnit().equals(cu)) {
				result = doc;
			}
		}
		return result;
	}

	public void open() {
		try {
			Declaration<?,?,?,? > referencedElement = getReferencedElement();
			if (referencedElement != null) {
				System.out.println("De link wordt geopend...");
				CompilationUnit cu = referencedElement.nearestAncestor(CompilationUnit.class);
				ChameleonDocument doc = findDocument(cu);
				

				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				IWorkbenchPage page = win.getActivePage();
				IFile file = doc.getFile();
				
				//create Marker to jump immediately to the referenced element.
				Decorator dec = (Decorator) referencedElement.tag(Decorator.ALL_DECORATOR);
				IMarker marker = null;
				
				try {
					int offset = dec.getOffset();
					System.out.println("Offset: "+offset);
					int lineNumber = doc.getLineOfOffset(offset);
					 HashMap map = new HashMap();
					 map.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
					 marker = file.createMarker(IMarker.TEXT);
					 marker.setAttributes(map);
				} catch (BadLocationException exc) {
					System.out.println("Could not calculate line number of declaration of referenced element when opening a Chameleon hyperlink.");
					exc.printStackTrace();
				} catch (CoreException exc) {
					System.out.println("Could not create text marker for referenced element when opening a Chameleon hyperlink.");
					exc.printStackTrace();
				}
				// map.put(IWorkbenchPage.EDITOR_ID_ATTR,"org.eclipse.ui.DefaultTextEditor");
				// IMarker marker = file.createMarker(IMarker.TEXT);
				// marker.setAttributes(map);
				// page.openEditor(marker); //2.1 API
				try {
					if(marker == null) {
					  IDE.openEditor(page, file);
					} else {
						IDE.openEditor(page, marker);
					}
				} catch (PartInitException e) {
					e.printStackTrace();
				}
				// marker.delete();
			}
		} catch (MetamodelException exc) {
			exc.printStackTrace();
			System.out.println("Referenced element not found.");
		}
	}

	public IRegion getHyperlinkRegion() {
		return _region;
	}

}
