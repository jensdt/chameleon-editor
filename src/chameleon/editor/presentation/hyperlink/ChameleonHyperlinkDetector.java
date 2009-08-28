/**
 * Created on 16-okt-06
 * @author Tim Vermeiren
 */
package chameleon.editor.presentation.hyperlink;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import chameleon.core.Config;
import chameleon.core.element.Element;
import chameleon.core.reference.CrossReference;
import chameleon.editor.connector.ChameleonEditorPosition;
import chameleon.editor.editors.ChameleonDocument;

  

/**
 * This class will check for Hyperlinks in the ChameleonEditor.
 * @author Tim Vermeiren
 *
 */
public class ChameleonHyperlinkDetector implements IHyperlinkDetector {

  public static boolean DEBUG;
  static {
  	if(Config.DEBUG) {
  		//set to true or false to enable or disable debug info in this class
  		DEBUG=true;
  	} else {
  		DEBUG=false;
  	}
  }

	/**
	 * returns the ChameleonHyperlinks detected in the specified region.
	 * Returns null if no hyperlinks found.
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		int offset = region.getOffset();
		ChameleonDocument document;
		try{
			document = (ChameleonDocument)textViewer.getDocument();
		} catch(ClassCastException e){
			e.printStackTrace();
			return null;
		}
//		 get text -------------------------------
		IRegion wordRegion = findWord(document, offset);
		String word="ILLEGAL_LOCATION";
		try {
			word = document.get(wordRegion.getOffset(), wordRegion.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		// ----------------------------------------
		ChameleonEditorPosition decorator = document.getReferenceDecoratorAtRegion(region);
		// Tag decorator = document.getDecoratorAtRegionOfType(region, refclass);
		IHyperlink[] result = null;
		if (decorator != null) {
			try {
				CrossReference element = (CrossReference) decorator.getElement();
				
				if(DEBUG) {
				  System.out.println("Highlighted word: " + word);
				  System.out.println("Element: " + element);
				  System.out.println("Decoratorname: " + decorator.getName());
				}
				
				ChameleonHyperlink hyperlink = new ChameleonHyperlink(element, new Region(decorator.getOffset(), decorator.getLength()),((ChameleonDocument)textViewer.getDocument()));
				result = new IHyperlink[] { hyperlink };
			} catch (ClassCastException exc) {
				exc.printStackTrace();
			}
		} 
		return result;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.ui.text.JavaWordFinder
	 */
	public static IRegion findWord(IDocument document, int offset) {

		int start= -2;
		int end= -1;
		
		try {
			int pos= offset;
			char c;

			while (pos >= 0) {
				c= document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}
			start= pos;

			pos= offset;
			int length= document.getLength();

			while (pos < length) {
				c= document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}
			end= pos;

		} catch (BadLocationException x) {
		}

		if (start >= -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}
	
}
