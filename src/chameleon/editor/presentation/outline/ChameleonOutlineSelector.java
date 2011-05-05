package chameleon.editor.presentation.outline;

import java.util.List;

import chameleon.core.declaration.Declaration;
import chameleon.core.element.Element;
import chameleon.exception.ModelException;

public class ChameleonOutlineSelector {

	public List<Element> outlineChildren(Element<?> element) throws ModelException {
		return (List<Element>) element.children();
	}
	
	/**
	 * Check whether the given element is allowed to be in the outline tree.
	 * 
	 * By default only declarations are allowed.
	 */
	public boolean isAllowed(Element<?> element) throws ModelException {
		return element instanceof Declaration;
	}
}
