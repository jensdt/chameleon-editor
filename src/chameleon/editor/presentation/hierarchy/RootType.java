/**
 * Created on 24-apr-07
 * @author Tim Vermeiren
 */
package chameleon.editor.presentation.hierarchy;

import org.eclipse.core.resources.IFile;

import chameleon.core.MetamodelException;
import chameleon.core.type.Type;
import chameleon.core.type.TypeReference;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.editors.ChameleonEditor;
import chameleon.editor.project.ChameleonProjectNature;

/**
 * This is just an ObjectWrapper to wrap round the root object
 * of the HierarchyTree, because otherwise the root object isn't shown.
 * 
 * @author Tim Vermeiren
 */
public class RootType implements HierarchyTreeNode {
	
	private Type _typeCache;
	private IFile docFile;
	private String rootTypeFqn;
	private ChameleonProjectNature projectNature;
	
	public RootType(Type rootType, ChameleonEditor editor) {
		this.rootTypeFqn = rootType.getFullyQualifiedName();
		projectNature = editor.getDocument().getProjectNature();
		ChameleonDocument elementDoc = projectNature.document(rootType);
		if(elementDoc != null){
			this.docFile = elementDoc.getFile();
		}
	}
	
	public Object[] getChildren(){
		// try to lookup type with fqn (so the newest rootTypeElement in the model is found)
		try {
			TypeReference tref = new TypeReference(rootTypeFqn);
			tref.setUniParent(projectNature.getModel());
			Type type = tref.getElement();
			if(type!=null){
				_typeCache = type;
				return new Object[]{new HierarchyTypeNode(type, projectNature, this)};
			}
		} catch (MetamodelException e) {
			e.printStackTrace();
		}
		// if not succeeded, try to get the element of the docFile
		if(docFile!=null){
			Type type = projectNature.documentOfFile(docFile).compilationUnit().descendants(Type.class).iterator().next();
			if(type != null){
				_typeCache = type;
				return new Object[]{new HierarchyTypeNode(type, projectNature, this)};
			}
		}
		// CHANGE show nothing if no type is found
		return new Object[]{new HierarchyTypeNode(_typeCache, projectNature, this)};
	}
	
	public HierarchyTreeNode getParent(){
		// the root item has no parent:
		return null;
	}
	
	
	
}
