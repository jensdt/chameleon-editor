package chameleon.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;



/**
 * @author Jef Geerinckx
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * The main plugin class to be used in the desktop.
 */
public class ChameleonEditorPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ChameleonEditorPlugin plugin;

	/**
	 * 
	 * @uml.property name="resourceBundle"
	 */
	//Resource bundle.
	private ResourceBundle resourceBundle;

	
	/**
	 * The constructor.
	 */
	public ChameleonEditorPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		try {
		super.start(context);
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			throw e;
			
			
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ChameleonEditorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ChameleonEditorPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}
	

	/**
	 * Returns the plugin's resource bundle,
	 * 
	 * @uml.property name="resourceBundle"
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle
					.getBundle("chameleonEditor.ChameleonEditorPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}
	
	/**
	 * 
	 * @param name
	 * 		name of the file to be searched
	 * @param language
	 * 		the currently used language in the chameleonEditor
	 * @return
	 * 		A search is made in the icons/[language] folder for the given filename
	 * 		A proper descriptor is returned if the name exists
	 * 		else a Descriptor for missing images is returned 
	 */
	public static ImageDescriptor getImageDescriptor(String name, String language) {
		 
			String iconPath = "icons/";
			iconPath = iconPath.concat(language.toLowerCase()).concat("/");
			//System.out.println("---- searching label : " +name);
		   try {
		       URL installURL = getDefault().getDescriptor().getInstallURL();
		       URL url = new URL(installURL, iconPath + name);
		      
		       return ImageDescriptor.createFromURL(url);
		   } catch (MalformedURLException e) {
		       return ImageDescriptor.getMissingImageDescriptor();
		   }
		}
	

}
