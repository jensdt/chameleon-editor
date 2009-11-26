package chameleon.editor.connector;

import java.util.List;

import org.eclipse.swt.graphics.Image;

import chameleon.core.element.Element;
import chameleon.core.method.Method;
import chameleon.core.modifier.Modifier;
import chameleon.editor.presentation.DeclarationCategorizer;
import chameleon.tool.ConnectorImpl;

/**
 * @author Marko van Dooren
 */
public abstract class EclipseEditorExtension extends ConnectorImpl {

	/**
	 * Return a text label for the given element. This is used for example in the outline.
	 */
  public abstract String getLabel(Element element);
  
  /**
   * Return an icon to represent the given element.
   */
  public abstract Image getIcon(Element modelObject);

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
  
  /**
   * Return a declaration categorizer. A declaration categorizer is used to group declarations
   * into categories. When e.g. an outline is sorted, elements of the same category are grouped
   * together, and sorted within their own group. The categorizer also imposes an order on the
   * groups.
   */
  public abstract DeclarationCategorizer declarationCategorizer();
  	
//  	public abstract ICompletionProposal completionProposal(Element element, ChameleonDocument document, int offset);

}
