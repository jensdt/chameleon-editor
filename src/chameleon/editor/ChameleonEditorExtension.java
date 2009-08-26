package chameleon.editor;

import chameleon.tool.ConnectorImpl;
import chameleon.core.element.Element;

/**
 * Created for experimentation
 * User: koenvdk
 * Date: 16-okt-2006
 * Time: 14:53:44
 */
public abstract class ChameleonEditorExtension extends ConnectorImpl {

    public abstract String getLabel(Element element);

}
