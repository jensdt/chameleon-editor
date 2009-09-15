package chameleon.editor.connector;

import java.util.List;

import chameleon.core.element.Element;
import chameleon.core.method.Method;
import chameleon.core.modifier.Modifier;
import chameleon.editor.presentation.DeclarationCategorizer;
import chameleon.tool.ConnectorImpl;

/**
 * @author Marko van Dooren
 */
public abstract class EclipseEditorExtension extends ConnectorImpl {

    public abstract String getLabel(Element element);

    /**
  	 * Returns the string for the template for the given method.
  	 * The parameters will be editable regions, if used as template patternstring.
  	 * So the parameters are replaced by "${parametername}".
  	 * E.g. In the java syntax this could be: methodname(${param1}, ${param2})
  	 * Used for auto-completion of methods.
  	 * 
  	 * @param method the method to be converted to a (pattern) string
  	 * @return patternstring for a template
  	 */
  	public abstract String getMethodTemplatePattern(Method method);
      
  	/**
  	 * Returns all modifiers for which we want to define a filter
  	 * (to filter the type hierarchy or the outline)
  	 */
  	//FIXME: filter based on properties!!!!
  	public abstract List<Modifier> getFilterModifiers();
  	
  	public abstract DeclarationCategorizer declarationCategorizer();
  	
//  	public abstract ICompletionProposal completionProposal(Element element, ChameleonDocument document, int offset);

}
