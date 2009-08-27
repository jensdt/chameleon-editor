package chameleon.editor.presentation.outline;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import chameleon.core.element.Element;
import chameleon.core.language.Language;
import chameleon.core.modifier.ElementWithModifiers;
import chameleon.core.modifier.Modifier;
import chameleon.editor.ChameleonEditorExtension;
import chameleon.editor.ChameleonEditorPlugin;
import chameleon.editor.editors.ChameleonEditor;

/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx
 *  
 * Gets the image and text for the tree elements in the outline view
 */
public class ChameleonOutlineLabelProvider implements ILabelProvider {
	//a cache for the images, used for faster lookup
	private HashMap<ImageDescriptor,Image> imageCache = new HashMap<ImageDescriptor,Image>(11);

	//the current language used => so the path can be set correct
	// TODO why do they use a string here? Seem primitive to me (Marko)
	private Language language;
	
	/**
	 * Creates a new labelProvider with for the given language and the paths for the icons
	 * @param hashIcons
	 * 	A hashmap with the name of the elements and the corresponding paths
	 * @param language
	 * 	the language used
	 * 
	 * both parameters <bold>must</bold> be effecive
	 */
	public ChameleonOutlineLabelProvider(/*HashMap<String, String> hashIcons,*/Language language,ChameleonEditor editor){
		//imageNames = hashIcons;
		this.language = language;
		_editor = editor;
	}
	
	private ChameleonEditor _editor;
	
	public ChameleonEditor getEditor() {
		return _editor;
	}
	


	/**
	 * @return the image corresponding to the given element.
	 * 			No image is returned if the element does not have image available
	 */
	public Image getImage(Object element) {
		ImageDescriptor descriptor = getDescriptor(element);

		   //obtain the cached image corresponding to the descriptor
		   Image image = (Image)imageCache.get(descriptor);
		   if (image == null) {
		       image = descriptor.createImage();
		       if(descriptor == ImageDescriptor.getMissingImageDescriptor() || image == null) { //this occurs when no icon is found from the description
		    	   return null;
		       }
		       imageCache.put(descriptor, image);
		   }
		   return image;
	}

	/**
	 * 
	 * @param _element
	 * 	the element the descriptor is looked-up for
	 * 
	 * @return the descriptor for the image of the given element.
	 */
	private ImageDescriptor getDescriptor(Object imageElement) {
		ImageDescriptor descriptor=null;
		ChameleonOutlineTree elem = (ChameleonOutlineTree) imageElement;
		Element element = elem.getElement();
		//String name = element.getShortDescription();
		String name = element.getClass().getSimpleName();
		if(element instanceof ElementWithModifiers) {

				List modifiers = ((ElementWithModifiers)element).modifiers();
				for (Iterator iter = modifiers.iterator(); iter.hasNext();) {
					Modifier modifier = (Modifier) iter.next();
					name = name+ ((modifier.toString()).toLowerCase());
			    }
			name =name.concat(".png");
			descriptor = ChameleonEditorPlugin.getImageDescriptor(name,language);
		}
		else {
			name =name.concat(".png");
			descriptor = ChameleonEditorPlugin.getImageDescriptor(name,language);
		}
		
		if(descriptor==null) {
			descriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		if(descriptor.getImageData()==null) {
			descriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		return descriptor;
	}









	/**
	 * @return the text for the given element.
	 * 
	 */
	public String getText(Object element) {
		if(element instanceof ChameleonOutlineTree) {
			Element e = ((ChameleonOutlineTree)element).getElement();
			ChameleonEditorExtension ext = getEditor().getDocument().getProjectNature().getModel().language().connector(ChameleonEditorExtension.class);
			return ext.getLabel(e);
		} else if (element != null) {
			return element.toString();
		} else {
			return "unknown element";
		}
//		ChameleonOutlineTree tree=null;
//		try{
//			 tree = (ChameleonOutlineTree) element;
//			 Element modelElement = tree.getElement();
//			 
//			 String name = modelElement.getClass().getSimpleName();
//			 return name;
//		}catch(ClassCastException e){
//			e.printStackTrace(); //should not happen
//			throw e;
//		}

	}



	/**
	 * clears the image cache, to prevent leaks
	 */
	public void dispose() {
		   for (Iterator i = imageCache.values().iterator(); i.hasNext();) {
		       ((Image) i.next()).dispose();
		   }
		   imageCache.clear();
		}

	/**
	 * 
	 * @return false
	 */

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * UNUSED AT THE MOMENT
	 */
	public void addListener(ILabelProviderListener listener) {
		
		
	}


	/**
	 * UNUSED AT THE MOMENT
	 */
	public void removeListener(ILabelProviderListener listener) {
		
		
	}





}
