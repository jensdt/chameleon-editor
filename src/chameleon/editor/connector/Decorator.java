package chameleon.editor.connector;

import org.eclipse.jface.text.Position;

import chameleon.core.element.ChameleonProgrammerException;
import chameleon.core.element.Element;
import chameleon.core.tag.Tag;

/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * A decorator intended for linking a position to a meta-element.
 */
public class Decorator extends Position implements Tag {

	
	public Decorator(int offset, int length, Element element, String name){
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
	
	
	public final static String CHAMELEON_CATEGORY= "__ChameleonDecorator";

	private String _name;

	private void setName(String name) {
		_name = name;
	}
	public String getName() {
		return _name;
	}

	
	public Decorator getParentDecorator(){
		Element parentElem = getElement().parent();
		Decorator parentDeco = (Decorator)parentElem.tag(ALL_DECORATOR);
		return parentDeco;
	}
	
	/** Decorator spanning an entire element **/
	public final static String ALL_DECORATOR= "__ALL";
	public static final String NAME_DECORATOR = "__NAME";
	public static final String REFERENCE_DECORATOR = "__Reference";

	
	
	public String toString(){
		return "Offset : "+getOffset()+"\tLength : "+getLength()+"\tElement : "+getElement();
	}

	public Decorator clonePosition() {
		return new Decorator(getOffset(),getLength(),getElement(),_name);
	}


}
