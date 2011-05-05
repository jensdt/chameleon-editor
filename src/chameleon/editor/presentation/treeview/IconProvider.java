package chameleon.editor.presentation.treeview;

import chameleon.core.element.Element;

public abstract class IconProvider {

	/**
	 * Return the name of the icon of the given element. If the icon is not
	 * appropriate for the given element, null is returned. 
	 * @param element
	 * @return
	 */
 /*@
   @ public behavior
   @
   @ pre element != null;
   @*/
	public abstract String iconName(Element element);
}
