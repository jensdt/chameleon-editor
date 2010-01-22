package chameleon.editor.connector;

import java.util.Comparator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Position;
import org.rejuse.predicate.SafePredicate;

import chameleon.core.element.Element;
import chameleon.core.tag.Tag;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.exception.ChameleonProgrammerException;

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
public class EclipseEditorTag extends Position implements Tag {

	// FIXME: We should keep a reference to the ChameleonDocument.
	//        We need this to switch documents if this tag is moved to
	//        another element.
	
	/**
	 * Initialize a new Chameleon editor position with the given offset and length, and
	 * with the given name.
	 * 
	 * @param offset
	 * @param length
	 * @param element
	 * @param name
	 */
	public EclipseEditorTag(ChameleonDocument document, int offset, int length, Element element, String name){
		super(Math.max(0,offset),Math.max(0,length));
  	if(element == null) {
  		throw new ChameleonProgrammerException("Initializing decorator with null element");
  	}
  	_document = document;
  	try {
			_document.addPosition(EclipseEditorTag.CHAMELEON_CATEGORY,this);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (BadPositionCategoryException e) {
			e.printStackTrace();
		}
		setElement(element,name);
		setName(name);
	}
	
	private ChameleonDocument _document;
	
	public ChameleonDocument getDocument() {
		return _document;
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
		Element old = _element;
  	if(element != _element) {
  		// Set new pointer, backup old for removal.
  		_element = element;
  		// Remove from current element.
  		if((_element != null) && (_element.tag(name) == this)){
  			_element.removeTag(name);
  		}
  		// Add to new element.
  		if((_element != null) && (_element.tag(name) != this)) {
  		  _element.setTag(this, name);
  		}
  	}
  	ChameleonDocument chameleonDocument = getDocument();
		ChameleonDocument newDocument = chameleonDocument.getProjectNature().document(element);
		_document = newDocument;
		if(newDocument != chameleonDocument) {
  		try {
  			if(chameleonDocument != null) {
  			  chameleonDocument.removePosition(EclipseEditorTag.CHAMELEON_CATEGORY,this);
  			}
  			if(newDocument != null) {
				  newDocument.addPosition(EclipseEditorTag.CHAMELEON_CATEGORY,this);
  			}
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch(NullPointerException e) {
				e.printStackTrace();
			} catch (BadPositionCategoryException e) {
				e.printStackTrace();
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

	
//	public EclipseEditorTag getParentDecorator(){
//		Element parentElem = getElement().parent();
//		EclipseEditorTag parentDeco = (EclipseEditorTag)parentElem.tag(ALL_TAG);
//		return parentDeco;
//	}
	
	/** Decorator spanning an entire element **/
	public final static String ALL_TAG= "__ALL";
	public static final String NAME_TAG = "__NAME";
	public static final String CROSSREFERENCE_TAG = "__CROSS_REFERENCE";
	public static final String KEYWORD_TAG = "__KEYWORD";
	public static final String MODIFIER_TAG = "__MODIFIER";
	
	public String toString(){
		return "Offset : "+getOffset()+"\tLength : "+getLength()+"\tElement : "+getElement();
	}

//	public EclipseEditorTag clonePosition() {
//		return new EclipseEditorTag(getOffset(),getLength(),getElement(),_name);
//	}


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
	public static Comparator<EclipseEditorTag> lengthComparator = new Comparator<EclipseEditorTag>() {
		public int compare(EclipseEditorTag t1, EclipseEditorTag t2) {
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
	public static Comparator<EclipseEditorTag> beginoffsetComparator = new Comparator<EclipseEditorTag>() {
		public int compare(EclipseEditorTag t1, EclipseEditorTag t2) {
			// 1) compare by offset:
			int compare = new Integer(t1.getOffset()).compareTo(t2.getOffset());
			// if no difference found
			if(compare==0) {
				// 2) compare by length:
				compare = new Integer(t1.getLength()).compareTo(t2.getLength());
			}
			// if still the same
			if(compare==0) {
				// 3) compare by tag name:
				compare = t1.getName().compareTo(t2.getName());
			}
			// if still the same
			if(compare==0) {
				// 4) order does not matter. MUST NOT RETURN 0, that would remove the tag in the treeset.
				compare = -1;
			}
			return compare;
		}
	};

	
	
	/**
	 * Predicate that checks wheter an EditorTag has a given tagname
	 */
	public static class NamePredicate extends SafePredicate<EclipseEditorTag>{
		private String _tagName;
		/**
		 * @param 	tagName
		 * 			Must be a constant of EditorTagTypes
		 */
		public NamePredicate(String tagName) {
			this._tagName = tagName;
		}
		@Override
		public boolean eval(EclipseEditorTag tag) {
			return tag.getName().equals(_tagName);
		}
	}



	/**
	 * Predicate that checks wheter an EditorTag has a given tagname
	 */
	public static class SurroundsOffsetPredicate extends SafePredicate<EclipseEditorTag>{
		private int offset;
		public SurroundsOffsetPredicate(int offset) {
			this.offset = offset;
		}
		@Override
		public boolean eval(EclipseEditorTag editorTag) {
			return editorTag.includes(offset);
		}
	}
	

}
