package chameleon.editor.connector;

import java.util.Comparator;

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
		ChameleonEditorPosition parentDeco = (ChameleonEditorPosition)parentElem.tag(ALL_TAG);
		return parentDeco;
	}
	
	/** Decorator spanning an entire element **/
	public final static String ALL_TAG= "__ALL";
	public static final String NAME_TAG = "__NAME";
	public static final String CROSSREFERENCE_TAG = "__CROSS_REFERENCE";
	public static final String KEYWORD_TAG = "__KEYWORD";
	public static final String MODIFIER_TAG = "__MODIFIER";
	
	public String toString(){
		return "Offset : "+getOffset()+"\tLength : "+getLength()+"\tElement : "+getElement();
	}

	public ChameleonEditorPosition clonePosition() {
		return new ChameleonEditorPosition(getOffset(),getLength(),getElement(),_name);
	}


	// FIXME Tim wrote a hashCode() method, but no equals. Do we need the hashCode method?
	
	/**
	 * Position overrides the hashCode method and does not take
	 * the name and the element into account.
	 * @post	The hashcode will be different for editortags with a different position
	 * or a different name or a different element.
	 */
//	@Override
//	public int hashCode() {
//		return super.hashCode() ^ _name.hashCode() ^ _element.hashCode();
//	}

	
	/**
	 * This comparator will compare editorTags and they will be sorted (ascending) by:
	 * - length
	 * - offset
	 * - hashCode
	 * 
	 * So the shortest EditorTags will come first. if the length is the same, 
	 * the EditorTag with the smallest offset comes first and if they are still the same
	 * the EditorTag with the smallest hashCode comes first.
	 * 
	 * @author Tim Vermeiren
	 */
	public static Comparator<ChameleonEditorPosition> lengthComparator = new Comparator<ChameleonEditorPosition>() {
		public int compare(ChameleonEditorPosition t1, ChameleonEditorPosition t2) {
			// compare by length:
			int compare = new Integer(t1.getLength()).compareTo(t2.getLength());
			// if no difference found
			if(compare!=0)
				return compare;
			// compare by offset:
			compare = new Integer(t1.getOffset()).compareTo(t2.getOffset());
			// if still the same
			if(compare!=0)
				return compare;
			// compare by hashCode:
			return new Integer(t1.hashCode()).compareTo(t2.hashCode());
		}
	};
	
	/**
	 * This comparator will compare editorTags and they will be sorted (ascending) by:
	 * - offset
	 * - length
	 * - hashCode
	 * 
	 * So the EditorTags with the lowest begin-offset will come first. if this is the same, 
	 * the smallest EditorTag comes first and if they are still the same
	 * the EditorTag with the smallest hashCode comes first.
	 * 
	 * @author Tim Vermeiren
	 */
	public static Comparator<ChameleonEditorPosition> beginoffsetComparator = new Comparator<ChameleonEditorPosition>() {
		public int compare(ChameleonEditorPosition t1, ChameleonEditorPosition t2) {
			// compare by offset:
			int compare = new Integer(t1.getOffset()).compareTo(t2.getOffset());
			// if no difference found
			if(compare!=0)
				return compare;
			// compare by length:
			compare = new Integer(t1.getLength()).compareTo(t2.getLength());
			// if still the same
			if(compare!=0)
				return compare;
			// compare by hashCode:
			return new Integer(t1.hashCode()).compareTo(t2.hashCode());
		}
	};

}
