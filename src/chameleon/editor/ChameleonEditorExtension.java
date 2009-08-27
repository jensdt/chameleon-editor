package chameleon.editor;

import chameleon.tool.ConnectorImpl;
import chameleon.core.element.Element;

/**
 * @author Marko van Dooren
 */
public abstract class ChameleonEditorExtension extends ConnectorImpl {

    public abstract String getLabel(Element element);

}
