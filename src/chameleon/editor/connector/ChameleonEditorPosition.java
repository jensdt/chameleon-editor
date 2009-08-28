package chameleon.editor.connector;

import org.eclipse.jface.text.Position;

import chameleon.core.element.ChameleonProgrammerException;
import chameleon.core.element.Element;
import chameleon.core.tag.Tag;

/**
 * A tag intended for linking a position to a model element.
 * 
 * Eclipse registers positions under certain categories. The Chameleon positions are registered
 * under the category ChameleonEditorPosition.CHAMELEON_CATEGORY. To request the Chameleon positions, 
 * use someDocument.getPositions(ChameleonEditorPosition.CHAMELEON_CATEGORY);
 *
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * @author Marko van Dooren
 */
public class ChameleonEditorPosition extends Position implements Tag {

	/**
	 * Initialize a new Chameleon editor position with the given offset and length, and
	 * with the given name.
	 * 
	 * @param offset
	 * @param length
	 * @param element
	 * @param name
	 */
	public ChameleonEditorPosition(int offset, int length, Element element, String name){
		super(Math.max(0,offset),Math.max(0,length));
  	if(element == null) {
  		throw new ChameleonProgrammerException("Initializing decorator with null element");
  	}
		setElement(element,name);
		setName(name);
	}

	/**
	 * COPIED FROM TAGIMPL BECAUSE WE INHERIT FROM POSITION
	 * 
	 * DO NOT MODIFY!!!
	 */
	
	private Element _element;
	
  public final Element getElement() {
  	return _element;
  }
  
  public void setElement(Element element, String name) {
  	//FIXME: remove this check and exception signalling
  	if(element == null) {
  		throw new ChameleonProgrammerException("Changing element of decorator with name "+name+" to null");
  	}
  	if(element != _element) {
  		// Remove from current element.
  		if((_element != null) && (_element.tag(name) == this)){
  			_element.removeTag(name);
  		}
  		_element = element;
  		if((_element != null) && (_element.tag(name) != this)) {
  		  _element.setTag(this, name);
  		}
  	}
  }

/**
 * END COPYING
 */
	
	/**
	 * Eclipse registers positions under certain categories. The Chameleon positions are registered
	 * under the category ChameleonEditorPosition.CHAMELEON_CATEGORY.
	 */
	public final static String CHAMELEON_CATEGORY= "__ChameleonDecorator";

	private String _name;

	private void setName(String name) {
		_name = name;
	}
	public String getName() {
		return _name;
	}

	
	public ChameleonEditorPosition getParentDecorator(){
		Element parentElem = getElement().parent();
		ChameleonEditorPosition parentDeco = (ChameleonEditorPosition)parentElem.tag(ALL_DECORATOR);
		return parentDeco;
	}
	
	/** Decorator spanning an entire element **/
	public final static String ALL_DECORATOR= "__ALL";
	public static final String NAME_DECORATOR = "__NAME";
	public static final String CROSSREFERENCE_DECORATOR = "__Reference";

	
	
	public String toString(){
		return "Offset : "+getOffset()+"\tLength : "+getLength()+"\tElement : "+getElement();
	}

	public ChameleonEditorPosition clonePosition() {
		return new ChameleonEditorPosition(getOffset(),getLength(),getElement(),_name);
	}


}
