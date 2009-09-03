package chameleon.editor.presentation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;

import chameleon.editor.connector.ChameleonEditorPosition;
import chameleon.editor.editors.ChameleonDocument;


/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 *
 *	The presentation manager is responsible for creating and updating
 *	a syntax coloring and folding for a single document.  This can be
 *	done by updating ranges of presentation
 *
 */
public class PresentationManager {

	/*
	 * The document where this presentationManager is used 
	 */
	private ChameleonDocument document;
	
	/*
	 * the presentation model used 
	 */
	private PresentationModel _presentationModel;

	//private ChameleonEditor editor;

	/**
	 * Creates a new presentation manager with a given document & model
	 * @param doc
	 * 	the document where this manager is to be used
	 * @param model
	 *   the supporting model for this presentationmanager
	 * @throws IllegalArgumentException
	 * 	 when model or document are not effective
	 */
	public PresentationManager(ChameleonDocument doc, PresentationModel model/*, ChameleonEditor editor*/) throws IllegalArgumentException{
		if (model == null)
			throw new IllegalArgumentException("model must be effective");
		if (doc==null)
			throw new IllegalArgumentException("document must be effective");
		
		this.document=doc;
		this._presentationModel = model;
		//this.editor = editor;
	}
	
	/**
	 * @return the textpresentation for the given document. 
	 * All styles for each of the elements are made
	 * @throws NullPointerException
	 * 		When the editor was shutdown during or preceding this method excecution
	 * 
	 */
	public TextPresentation createTextPresentation() throws NullPointerException{
		final TextPresentation pres = new TextPresentation();
		try {
			Position[] poss = document.getPositions(ChameleonEditorPosition.CHAMELEON_CATEGORY);
			
			for (int i = 0; i < poss.length; i++) {
				ChameleonEditorPosition dec = (ChameleonEditorPosition) poss[i];
				try {
					// FIXME
					StyleRange sr = _presentationModel.map(dec.getOffset(), dec.getLength(), dec.getElement().getClass().getName().toLowerCase(), dec
							.getName());

					if (sr != null) {
						pres.mergeStyleRange(sr);
					}
				} catch (NullPointerException exc) {
					throw exc;
				}
				
			}
		} catch (BadPositionCategoryException e) {
			e.printStackTrace();
			
		} catch (ClassCastException e) {
			e.printStackTrace();
			
		}
		return pres;
	}	

	/**
	 * checks whether the given element is foldable
	 * @param decorator 
	 * 		The decorator to be checked
	 * 		
	 */
	private boolean isFoldable(ChameleonEditorPosition decorator) {
		PresentationStyle presStyle = _presentationModel.getRule(decorator.getElement().getClass().getName().toLowerCase(), decorator.getName());
		if(presStyle != null && presStyle.isfoldable())
			return true;
		else
			return false;
	}
	
	/**
	 * 
	 * @return all the positions which are foldable, by definition of the XML file
	 * 		   if no foldable positions are found, then an empty Vector is returned
	 */
	public Vector<Position> getFoldablePositions(){
		try {
			if(document == null)
				throw new NullPointerException();
			Position[] poss = document.getPositions(ChameleonEditorPosition.CHAMELEON_CATEGORY);
			Vector<Position> foldablePos= new Vector<Position>(0);
 			for (int i = 0; i < poss.length; i++) {
				if(isFoldable(((ChameleonEditorPosition) poss[i]))){
					foldablePos.add( poss[i]);
				}
			}
 			
 			return foldablePos;
		} catch (BadPositionCategoryException e) {
		} catch (NullPointerException npe){
		}
		//in case there are nog foldablePositions
		return new Vector<Position>(0);
		
	}
	
	/**
	 * returns all the elements that are marked to be folded from the model
	 */
	public Vector<ChameleonEditorPosition> getFoldedElementsFromModel() {
		try {
			Position[] poss = document.getPositions(ChameleonEditorPosition.CHAMELEON_CATEGORY);
			Vector<ChameleonEditorPosition> foldPos= new Vector<ChameleonEditorPosition>(0);
 			for (int i = 0; i < poss.length; i++) {
				if(isFolded(((ChameleonEditorPosition) poss[i]))){
					foldPos.add((ChameleonEditorPosition) poss[i]);
				}
			}
 			
 			return foldPos;
		} catch (BadPositionCategoryException e) {
			//When this happens, somethings wrong with the positions in the file
			e.printStackTrace();
		}
		//in case there are no foldablePositions
		return new Vector<ChameleonEditorPosition>(0);
	}	
	
	/**
	 * Checks whether the decorator is folded. 
	 * @param decorator
	 * 	The decorator that is being checked
	 * @return True when the decorator is folded in the editor. False otherwise
	 */
	private boolean isFolded(ChameleonEditorPosition decorator) {
		//FIXME
		PresentationStyle presStyle = _presentationModel.getRule(decorator.getElement().getClass().getName().toLowerCase(), decorator.getName());
		if(presStyle != null && presStyle.isFolded())
			return true;
		else
			return false;
		
		
	}

	public TextPresentation createTextPresentation(TextPresentation pres, int offset, int length) {
		try {
			System.out.println("doing presentation on " + document);
			Position[] poss = document.getPositions(ChameleonEditorPosition.CHAMELEON_CATEGORY);

			for (int i = 0; i < poss.length; i++) {
				if (poss[i].getOffset()+poss[i].getLength() > offset && poss[i].getOffset() < offset+length){
					ChameleonEditorPosition dec = (ChameleonEditorPosition) poss[i];
					StyleRange sr = _presentationModel.map(dec.getOffset(),dec.getLength(),dec.getElement().getClass().getName().toLowerCase(), dec.getName());
					if (sr!=null) pres.mergeStyleRange(sr);
				}
			}
		} catch (BadPositionCategoryException e)
		{
			System.out.println(e);
			
		} catch (ClassCastException e)
		{
			System.out.println(e);
			
		}
		return pres;
	}

//	public List<String> getOutlineElements(){
//		
//		List<String[]> pm = getPresentationModel().getOutlineElements();
//		
//		List<String> v = new ArrayList<String>();
//		for (Iterator<String[]> iter = pm.iterator(); iter.hasNext();) {
//			String[] element = iter.next();
//			v.add(element[0]);
//			
//		}		
//		return v;
//	}

	public PresentationModel getPresentationModel() {
		return _presentationModel;
	}

	public List<String> getDefaultOutlineElements() {
		return getPresentationModel().getDefaultOutlineElements();
	}

	
	
}
