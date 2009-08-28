package chameleon.editor.connector;

import chameleon.core.element.Element;
import chameleon.editor.project.ChameleonProjectNature;
import chameleon.tool.ConnectorImpl;

/**
 * @author Marko van Dooren
 */
public abstract class ChameleonEditorExtension extends ConnectorImpl {

//	  public ChameleonEditorExtension(ChameleonProjectNature nature) {
//	  	_projectNature = nature;
//	  }
//	
    public abstract String getLabel(Element element);
    
//    public ChameleonProjectNature projectNature() {
//    	return _projectNature;
//    }
//    
//    private ChameleonProjectNature _projectNature;

}
