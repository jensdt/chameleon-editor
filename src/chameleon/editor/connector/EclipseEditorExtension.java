package chameleon.editor.connector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import chameleon.core.element.Element;
import chameleon.core.method.Method;
import chameleon.core.modifier.Modifier;
import chameleon.editor.ChameleonEditorPlugin;
import chameleon.editor.presentation.outline.ChameleonOutlineSelector;
import chameleon.editor.presentation.treeview.DeclarationCategorizer;
import chameleon.editor.presentation.treeview.IconProvider;
import chameleon.exception.ModelException;
import chameleon.plugin.PluginImpl;

/**
 * @author Marko van Dooren
 */
public abstract class EclipseEditorExtension extends PluginImpl {

	public EclipseEditorExtension() {
		_imageRegistry = ChameleonEditorPlugin.getDefault().getImageRegistry();
		initializeRegistry();
		_outlineSelector = createOutlineSelector();
	}
	
	private ImageRegistry _imageRegistry;
	
	public ImageRegistry imageRegistry() {
		return _imageRegistry;
	}
	
	/**
	 * Add the image descriptors to the registry.
	 * @throws IOException 
	 */
	protected void initializeRegistry() {}
	
	/**
	 * Return a text label for the given element. This is used for example in the outline.
	 */
  public abstract String getLabel(Element element);
  
  /**
   * Return an icon to represent the given element.
   * @throws IOException 
   * @throws ModelException 
   */
	public Image getIcon(Element<?> element) throws ModelException {
		return image(iconProvider().iconName(element));
	}

	public abstract IconProvider createIconProvider();

	public IconProvider iconProvider() {
		if(_iconProvider == null) {
			_iconProvider = createIconProvider();
		}
		return _iconProvider;
	}
	
	private IconProvider _iconProvider;
	
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
	 * of this language module editor plugin..
	 */
	public void register(String fileName, String iconName, String pluginID) throws MalformedURLException {
		Image image = createImage(fileName, pluginID);
		imageRegistry().put(iconName, image);
	}

	/**
	 * Create an image from the file in the icons directory of this language module editor plugin
	 * that has the same name as the given name. 
	 */
	public Image createImage(String fileName, String pluginID) throws MalformedURLException {
		URL url = icon(fileName, pluginID);
		Image image = ImageDescriptor.createFromURL(url).createImage();
		return image;
	}
	
	public Image image(String iconName) {
		return imageRegistry().get(iconName);
	}

	/**
	 * Return the URL for the icons directory of this language module editor plugin.
	 */
	public URL icon(String name, String pluginID) throws MalformedURLException {
		URL root = Platform.getBundle(pluginID).getEntry("/");
		URL icons = new URL(root,"icons/");
		URL url = new URL(icons, name);
		return url;
	}
	
	/**
	 * Return the ID of this plugin.
	 */
	public abstract String pluginID();

	
	public ChameleonOutlineSelector createOutlineSelector() {
		return new ChameleonOutlineSelector();
	}
	
	private ChameleonOutlineSelector _outlineSelector;
	
	public ChameleonOutlineSelector outlineSelector() {
		return _outlineSelector;
	}
	
//  	public abstract ICompletionProposal completionProposal(Element element, ChameleonDocument document, int offset);

}
