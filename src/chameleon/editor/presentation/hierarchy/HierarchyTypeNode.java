/**
 * Created on 23-mei-07
 * @author Tim Vermeiren
 */
package chameleon.editor.presentation.hierarchy;

import chameleon.core.lookup.LookupException;
import chameleon.editor.project.ChameleonProjectNature;
import chameleon.oo.language.ObjectOrientedLanguage;
import chameleon.oo.type.Type;
import chameleon.oo.type.TypeReference;

public class HierarchyTypeNode implements HierarchyTreeNode {
	
	private String fullyQualifiedName;
	
	private ChameleonProjectNature projectNature;
	
	private HierarchyTreeNode parent;
	
	public HierarchyTypeNode(Type type, ChameleonProjectNature projectNature, HierarchyTreeNode parent){
		fullyQualifiedName = type.getFullyQualifiedName();
		this.projectNature = projectNature;
		this.parent = parent;
	}
	
	public Type getType() throws LookupException {
		Type result = null; 
		try {
			TypeReference tref= projectNature.getModel().language(ObjectOrientedLanguage.class).createTypeReference(fullyQualifiedName);
			tref.setUniParent(projectNature.getModel());
			result = tref.getType();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public HierarchyTreeNode getParent(){
		return parent;
	}
	
	public ChameleonProjectNature getProjectNature(){
		return projectNature;
	}
	
	/**
	 * STATIC MEMBERS
	 */
	
	public static HierarchyTypeNode[] encapsulateInHierarchyTreeNodes(Type[] types, ChameleonProjectNature projectNature, HierarchyTreeNode parent){
		HierarchyTypeNode[] nodes = new HierarchyTypeNode[types.length];
		for(int i=0; i<types.length; i++){
			Type type = types[i];
			HierarchyTypeNode node = new HierarchyTypeNode(type, projectNature, parent);
			nodes[i] = node;
		}
		return nodes;
	}
	
}
