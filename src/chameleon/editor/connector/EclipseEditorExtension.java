package chameleon.editor.connector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import chameleon.core.declaration.Declaration;
import chameleon.core.element.Element;
import chameleon.core.method.Method;
import chameleon.core.modifier.Modifier;
import chameleon.editor.presentation.DeclarationCategorizer;
import chameleon.tool.ConnectorImpl;

/**
 * @author Marko van Dooren
 */
public abstract class EclipseEditorExtension extends ConnectorImpl {

	public EclipseEditorExtension() {
		initializeRegistry();
	}
	
	private ImageRegistry _imageRegistry = new ImageRegistry();
	
	public ImageRegistry imageRegistry() {
		return _imageRegistry;
	}
	
	/**
	 * Add the image descriptors to the registry.
	 * @throws IOException 
	 */
	protected void initializeRegistry() {
		
	}
	
	/**
	 * Return a text label for the given element. This is used for example in the outline.
	 */
  public abstract String getLabel(Element element);
  
  /**
   * Return an icon to represent the given element.
   * @throws IOException 
   */
  public abstract Image getIcon(Element element) throws IOException;

  /**
   * Returns the string for the template for the given method.
   * The parameters will be editable regions, if used as template patternstring.
   * So the parameters are replaced by "${parametername}".
   * E.g. In the java syntax this could be: methodname(${param1}, ${param2})
   * Used for auto-completion of methods.
   * 
   * @param method the method to be converted to a (pattern) string
   * @return pattern string for a template
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

	/**
	 * Register an icon in the image registry. The icon must be in the icons/ directory
	 * of this language module editor plugin. The name of the file must be name+".png". The
	 * icon is registered under the given name.
	 */
	public void register(String name) throws MalformedURLException {
		Image image = image(name+".png");
		imageRegistry().put(name, image);
	}

	/**
	 * Create an image from the file in the icons directory of this language module editor plugin
	 * that has the same name as the given name. 
	 */
	public Image image(String fileName) throws MalformedURLException {
		URL url = icon(fileName);
		Image image = ImageDescriptor.createFromURL(url).createImage();
		return image;
	}

	/**
	 * Return the URL for the icons directory of this language module editor plugin.
	 */
	public URL icon(String name) throws MalformedURLException {
		URL root = Platform.getBundle(pluginID()).getEntry("/");
		URL icons = new URL(root,"icons/");
		URL url = new URL(icons, name);
		return url;
	}
	
	public abstract String pluginID();

//	public boolean isOutlineElement(Declaration elementChild) {
//		
//	}
  	
//  	public abstract ICompletionProposal completionProposal(Element element, ChameleonDocument document, int offset);

}
