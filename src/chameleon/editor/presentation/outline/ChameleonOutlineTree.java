package chameleon.editor.presentation.outline;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import chameleon.core.element.Element;
import chameleon.core.language.Language;

/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * Creates a new tree of chameleon Decorators
 */
public class ChameleonOutlineTree {
	//the current element
	private Element node;
	//the current element's children
	private Vector<ChameleonOutlineTree> children;
	//A listener for this tree
	private IChameleonOutlineTreeListener listener= NullChameleonOutlineTreeListener.getSoleInstance();
	
	//contains string representation of all the elements that may be contained in this tree
	private static HashMap<Language, List<String>> allowedTreeElements = new HashMap<Language, List<String>>(0);
	
	/**
	 * Creates a new empty tree with empty current node, and no children
	 */
	public ChameleonOutlineTree() {
		// this.hashElements = hashElements;
		//  this.decorators = decorators_All;
		this.node = null;
		//this.editor = editor;
		this.children = new Vector<ChameleonOutlineTree>(0);  
	}
	
	/*
	 * Creates a new  tree with a given node and no children
	 */
	private ChameleonOutlineTree(Element node) {
		//  this.hashElements = hashElements;
		//  this.decorators = decorators_All;
		this.node = node;
		//this.editor = editor;
		this.children = new Vector<ChameleonOutlineTree>(0);
	}
	
	/**
	 * adds a child to the current tree
	 * @param child
	 * 	the child to be added
	 * @return
	 * 	a new Chameleontree with child as node
	 */
	public ChameleonOutlineTree addChild(Element child) {
		return addTreeNodeChild( new ChameleonOutlineTree( child) );
	}
	
	
	/*
	 * if there are no children, a new vector is created. either way , 
	 * the child tree is added 
	 */
	private ChameleonOutlineTree addTreeNodeChild(ChameleonOutlineTree childTNode) {
		if (children == null)
			children = new  Vector<ChameleonOutlineTree>(0);
		children.add(childTNode);
		return childTNode;
	}
	
	/**
	 * 
	 * @param parentElement
	 * 	the element from which the children are returned
	 * @return
	 * 	The Decorator children of the given parentelement
	 */
	public List<Element> getChildren(Language lang, Element parentElement) {
		try{
			List<Element> children = new ArrayList<Element>(0);
			Element elem = (Element) parentElement;
			List elementChildren = elem.children();
			//Vector<String> possibleChildren = hashElements.get(elem.getElement().getClass().getName());
			for(int i = 0; i < elementChildren.size(); i++){
				Element elementChild = ((Element)elementChildren.get(i));
				if (isAllowedInTree(lang, elementChild)){ //Dit fungeert als een filter voor de element die moeten getoond worden	
					children.add((Element) elementChild);
				}
			}
			if (children.size()>0)
				return children;
			else
				return null;
			
			
		}catch(ClassCastException e){
			return null;
		}
	}
	
	/**
	 * Checks whether this element can be be stated in the chameleonTree 
	 * 
	 * @param elementChild 
	 * @return true if the element is a proper tree element and has decorators
	 */
	public static boolean isAllowedInTree(Language language, Element elementChild) {
		 return ((elementChild.hasTags()) && (ChameleonOutlineTree.isAllowedDescription(language, elementChild.getClass().getSimpleName())));
	}
	
	
	/*
	 * check whether the given description matches one of the allowed ones
	 */
	private static boolean isAllowedDescription(Language language, String shortDescription) {
		return allowedTreeElements.get(language).contains(shortDescription);
	}
	
	/**
	 * @return the children of this tree
	 */
	public Vector<ChameleonOutlineTree> getChildren() {
		return children;
	}
	
	/**
	 * 
	 * @return the current element (= current node in the tree)
	 */
	public Element getElement() {
		return node;
	}
	
	
	/**
	 * Recursively adds the children to this tree,  from the given elements children
	 * if the treeElement is not effective or doesn't has children, nothing happens
	 * @param treeRootElement
	 * 	The beginElement.
	 */
	public void composeTree(Language lang, Element treeElement) {
		node = treeElement;
		if(treeElement !=null){
			List<Element> treeElementChildren = getChildren(lang, treeElement);
			if(!(treeElementChildren==null||treeElementChildren.size()==0)){ //Als er dus kinderen zijn
				for (Iterator iter = treeElementChildren.iterator(); iter.hasNext();) {
					Element element = (Element) iter.next();
					if (!isAllowedInTree(lang, element))
						treeElementChildren.remove(element);
					
				}
				//adds the children
				
				for (Iterator iter = treeElementChildren.iterator(); iter.hasNext();) {
					Element element = (Element) iter.next();
					children.add(new ChameleonOutlineTree(element));	
				}
				
				//compose the tree for each of the children
				
				for(int i = 0; i < children.size(); i++){
					children.get(i).composeTree(lang, children.get(i).getElement());
				}
			}
		}
	}
	
	/**
	 * String representation of this tree
	 */
	public String toString(){
		
		String woorden = null;
		//try {
		//int length = editor.getDocument().getLineInformationOfOffset(node.offset).getLength();
		//woorden = editor.getDocument().get(node.offset,length );
		woorden = node.toString();
		System.out.println("chameleontree woorden: " + woorden );
		//} catch (BadLocationException e) {
		//  Auto-generated catch block
		//.printStackTrace();
		//}
		//woorden = removeUnneededText(woorden);
		return woorden;
	}
	
	/**
	 * Removes or add the given element properly in the tree
	 * Then triggers the necessary event(s)
	 * @param element
	 * 
	 * @param bool
	 * 	True = addition; False = removal
	 */
	public void update(Element element, boolean bool) {
		System.out.println("ChameleonOutlineTree.update");
		if (bool){ //addition
			if(element.parent().equals(node)){
				//tis een van dees kinderen.
				addChild(element);
				fireAdd(element);
			}
			else{
				for (int i=0; i < children.size(); i++){
					children.get(i).update(element,bool);
				}
			}
		}
		else{ //removal
			if (element == node){
				node = null;
				children = new Vector<ChameleonOutlineTree>(0);
				fireRemove(element);
			}
			else{
				for (int i=0; i < children.size(); i++){
					children.get(i).update(element,bool);
				}
			}
		}
		
	}
	
	/**
	 * adds a listener to this tree
	 * @param listener
	 * 	The given listener to be added
	 */
	public void addListener(IChameleonOutlineTreeListener listener) {
		this.listener = listener;
	}
	
	
	/**
	 * Removes a listener from this tree
	 * @param listener
	 */
	public void removeListener(IChameleonOutlineTreeListener listener) {
		if(this.listener.equals(listener)) {
			this.listener = NullChameleonOutlineTreeListener.getSoleInstance();
		}
	}
	
	/**
	 * Notifies any listeners that an element has been added to this tree
	 * @param added
	 * 		the added element
	 */
	protected void fireAdd(Element added) {
		System.out.println("ChameleonOutlineTree.fireAdd");
		listener.add(new ChameleonOutlineTreeEvent(added));
	}
	
	/**
	 * Notifies any listeners that an element has been removed from this tree
	 * @param removed
	 * 	The element removed from the tree
	 */
	protected void fireRemove(Element removed) {
		System.out.println("ChameleonOutlineTree.fireRemove");
		listener.remove(new ChameleonOutlineTreeEvent(removed));
	}
	
	/**
	 * This notifies any listeners that this tree has changed.
	 * 
	 */
	public void fireChanged() {
		listener.fireChanged();
		
	}
	
	/**
	 * Checks whether this tree has <code>element<code> as element. 
	 * <code>element<code> is an element of the tree when 
	 *  - <code>element<code> is effective
	 *  - the current node equals <code>element<code> or one of the child(ren) 
	 *     has <code>element<code> as element
	 *  
	 * 
	 * @param element
	 * 		the element to be checked
	 * 
	 */
	public boolean hasAsElement(Element element) {
		try{
			if (element == null){
				return false;
			}
			else if(((Element)element).equals(node)){
				return true;
			}
			else{
				for (Iterator iter = children.iterator(); iter.hasNext();) {
					ChameleonOutlineTree childTree = (ChameleonOutlineTree) iter.next();
					if (childTree.hasAsElement(element))
						return true;
				}
				return false;
				
			}
		}catch(ClassCastException cce){
			return false; // element is not even a tree element !
		}
	}
	
	/**
	 * @param language 
	 * @param allowedTreeElements 
	 *  
	 * Sets the elements which are to be shown in the outline tree.
	 * This does not update the tree !
	 */
	public static void setAllowedElements(Language lang, List<String> alloweds) {

		try {
		for (Iterator<String> iter = alloweds.iterator(); iter.hasNext();) {
			String element = iter.next();
		}} catch (NullPointerException e){}
		
		allowedTreeElements.put(lang,alloweds);		

		for (Iterator<String> iter = alloweds.iterator(); iter.hasNext();) {
			String element = iter.next();
		}
					
	}

	
	
	
	
	
}
