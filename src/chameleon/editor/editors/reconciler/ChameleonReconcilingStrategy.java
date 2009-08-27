package chameleon.editor.editors.reconciler;


import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

import chameleon.core.Config;
import chameleon.core.element.ChameleonProgrammerException;
import chameleon.core.element.Element;
import chameleon.core.namespace.Namespace;
import chameleon.core.namespacepart.NamespacePart;
import chameleon.core.type.Type;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.editors.ChameleonSourceViewerConfiguration;
import chameleon.editor.linkage.Decorator;
import chameleon.editor.linkage.DocumentEditorToolExtension;
import chameleon.input.ModelFactory;


/**
 * @author Jef Geerinckx
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * This class does the actual reconciling. it starts & initiates the reconciling 
 * 
 *
 */
public class ChameleonReconcilingStrategy implements IChameleonReconcilingStrategy{
	
	public static boolean DEBUG;
	static {
		if(Config.DEBUG) {
			// change the value of this assignment to true or false
			// to enable or disable output to the console
			DEBUG = true;
		} else {
			DEBUG = false;
		}
	}
	
	//the configuration where this ChameleonReconcilingStrategy is needed
	private ChameleonSourceViewerConfiguration _configuration;

	/**
	 * Constructs a new ChameleonReconcilingStrategy.
	 * @param configuration 
	 * 	the configuration where this ChameleonReconcilingStrategy is needed
	 *
	 */
	public ChameleonReconcilingStrategy(ChameleonSourceViewerConfiguration configuration){
		_alreadyInit = false;
		_firstDR = true;
		this._configuration = configuration;
	}
	
	/**
	 * sets the document for this ChameleonReconcilingStrategy to the given document
	 * @param document
	 * 		The new document
	 */
	public void setDocument(ChameleonDocument document){	
		_document = document;
	}
	
	/**
	 * @return the document for this ChameleonReconcilingStrategy
	 */
	public ChameleonDocument getDocument(){
		return _document;
	}
	
	// checks if there are dirty regions ?
	private boolean _firstDR;
	
	//check whether reconciling is initiated
	private boolean _alreadyInit;
	//the document to which this ReconcilingStrategy applies
	private ChameleonDocument _document;
	//Vector containing clones of positions
	private ArrayList<ClonedDecorator> clonedPositions = new ArrayList<ClonedDecorator>();
	// Vector containing all the dirtyPositions in this document
	private ArrayList<ClonedDecorator> dirtyPositions = new ArrayList<ClonedDecorator>();
	//states whether the whole document is dirty
	private boolean _wholeDocumentDirty = false;

	public boolean isWholeDocumentDirty() {
		return _wholeDocumentDirty;
	}
	
	private void setWholeDocumentDirty(boolean dirty) {
		if(DEBUG) {
			System.out.println("Setting _wholeDocumentDirty to "+dirty);
		}
		_wholeDocumentDirty = dirty;
	}
	
	private ArrayList<ActionListener> modelListeners = new ArrayList<ActionListener>();
	
	// initialize reconciling
	// positions are cloned
	/**
	 * initializes reconciling, during which the positions are saved
	 */
	public void initReconciling(){
		if(_alreadyInit == false){
			clonePositions();
			_alreadyInit = true;
		}
	}
	
	/*
	 * clones all the positions
	 */
	private void clonePositions(){
		Position[] positions = null;
		try {
			positions = _document.getPositions(Decorator.CHAMELEON_CATEGORY);
		} catch (BadPositionCategoryException e) {
			e.printStackTrace();
		}
				
		Decorator eP = null;
		for(int i=0; i<positions.length; i++){
			eP = (Decorator)positions[i];
			clonedPositions.add(cloneDecorator(eP));
		}
	}
	
	private ClonedDecorator cloneDecorator(Decorator dec) {
		return new ClonedDecorator(dec.getOffset(),dec.getLength(),dec.getElement(),dec.getName());
	}
	
	// start reconciling
	/**
	 * This processes the document. 
	 * If the whole document is dirty, it tries to process the whole document at once
	 * else it tries to process the smallest altered positions
	 */
	public void startReconciling(){
		if(DEBUG) {
		  System.out.println("starting reconciling,in chameleonReconcilingStrategy");
		}
		clonedPositions.clear();
		if(DEBUG) {
		  try {
				System.out.println("Number of positions in document: "+getDocument().getPositions(Decorator.CHAMELEON_CATEGORY).length);
			} catch (BadPositionCategoryException e) {
				e.printStackTrace();
			}
		}
		
		if(isWholeDocumentDirty()){
			if(DEBUG) {
			  System.out.println("\n 2. verwerken hele document");
			}
			try{
				parseWholeDocument(_document);
				if(DEBUG) {
				  System.out.println("    verwerken hele document geslaagd!");
				}
			}catch(Exception err){
				err.printStackTrace();
				if(DEBUG) {
				  System.out.println("    verwerken hele document NIET geslaagd! ");
				}
			}
		}
		else{
      if(DEBUG) {
			  System.out.println("\n 2. Verwerken kleinst gewijzigde posities");
      }
			removeCoveredPositions();
			boolean[] status = new boolean[dirtyPositions.size()];		
			for(int i=0; i<dirtyPositions.size(); i++){
				status[i] = true;
			}
			Position[] positions = null;
			for(int i=0; i<dirtyPositions.size(); i++){
				if(status[i] == true){
					ClonedDecorator position = dirtyPositions.get(i);
					//System.out.println("  verwerken positie --> offset: "+position2.getOffset()+" - lengte: "+position2.getLength()+" - element: "+position.getElement().getClass().getName());
					try{
						
						// A. Verwijderen decorators van element
						
						getDocument().removePosition(Decorator.CHAMELEON_CATEGORY,position);
						removeEmbeddedPos(position, positions, status);						
		
						Element element = position.getElement();
						
						// B. reparsen element
						
						ModelFactory factory = getDocument().compilationUnit().language().connector(ModelFactory.class);
						factory.reParse(element);
//						element.reParse(new DocumentEditorToolExtension(getDocument()),getDocument().modelFactory());
						
						
						//System.out.println("  verwerken positie geslaagd\n");
					}catch(Exception e){
						reparseEntireDocument(status, positions, i);
					}
					status[i] = false;
				}
			}
			
		}
		
		// voor test huidige posities even printen
		//System.out.println("--------------------");
		System.out.println("Einde synchronisatie");
		//System.out.println("--------------------");
		//System.out.println(" ==> Posities na synchronisatie");
		//getDocument().printPositions(Decorator.CHAMELEON_CATEGORY);
		dirtyPositions.clear();
		clonePositions();
		this._firstDR = true;
		
		fireModelUpdated();
		
	}

	private void reparseEntireDocument(boolean[] status, Position[] positions, int i) {
		try{
			if(DEBUG) {
			  System.out.println("Members verwerken mislukt. Proberen hele document verwerken");
			}
			parseWholeDocument(_document);
			//System.out.println("  verwerken hele document geslaagd");
			for(int n=0; n<dirtyPositions.size(); n++){
				status[n] = false;
			}
		}catch(Exception err){
			//System.out.println("  verwerken positie niet geslaagd");
			//System.out.println("   => positie(s) en element verwijderd");
			ClonedDecorator pos = ((ClonedDecorator) dirtyPositions.get(i));
			pos.getElement().disconnect();
			try{
				getDocument().removePosition(Decorator.CHAMELEON_CATEGORY,pos);				
				removeEmbeddedPos(pos, positions, status);
			}catch(Exception error){
				error.printStackTrace();
			}	
			
		}
	}
	
	public void addUpdateListener(ActionListener newListener){
		modelListeners.add(newListener);
	}
	
	private void fireModelUpdated() {
		for (Iterator<ActionListener> iter = modelListeners.iterator(); iter.hasNext();) {
			ActionListener listener = iter.next();
			listener.actionPerformed(null);
		}
		
	}

	/*
	 * 
	 */
	private void removeEmbeddedPos(ClonedDecorator pos, Position[] positions, boolean[] status){
		try{
			ClonedDecorator posB = null;
			for(int i=0; i<positions.length; i++){
				posB = (ClonedDecorator)positions[i];
				if(posB.getOffset()>pos.getOffset() && 
				   (posB.getOffset()+posB.getLength())<(pos.getOffset()+pos.getLength())){
					getDocument().removePosition(Decorator.CHAMELEON_CATEGORY,posB);				
					for(int n=0; n<status.length; n++){
						if((dirtyPositions.get(n)) == posB){
							status[n] = false;
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * 
	 */
	private void removeCoveredPositions(){
		ClonedDecorator posA, posB;
		for(int i=0; i<dirtyPositions.size(); i++){
			posA = dirtyPositions.get(i); 
			for(int t=0; t<dirtyPositions.size(); t++){
				posB = dirtyPositions.get(t);
				if(posB.getOffset()>posA.getOffset() && 
				   (posB.getOffset()+posB.getLength())<(posA.getOffset()+posA.getLength())){
					dirtyPositions.remove(posB);
				}
			}
		}
	}
	
	/*
	 * parses the whole document 
	 */
	private void parseWholeDocument(IDocument document) throws Exception{
		ChameleonDocument doc = this.getDocument();
		doc.reParse();
		
	}

	
	/**
	 * Search in the list of cloned positions for the smallest decorator covering the dirty region
	 * 
	 * @param dR
	 * @return
	 */
	private ClonedDecorator getSmallestCoveringPos(ChameleonDirtyRegion dR){
		ClonedDecorator covPos = null;
		for(ClonedDecorator pos : clonedPositions) {
			// if dirty region is completely in position ...
			if(dR.getOffset()>pos.offset && dR.getOffset()<=(pos.getOffset()+pos.getLength()-1) && 
					(dR.getOffset()+dR.getLength()-1)<(pos.getOffset()+pos.getLength()-1)){
				if(covPos == null || pos.getLength()<covPos.getLength()) {
					covPos = pos;
				}
			}
		}
		
		return covPos;
	}
	
	// reconstruct positions (via dirty region)
	private void adaptClonedPositions(ChameleonDirtyRegion dR){
		// dirty region of type _INSERT
		ClonedDecorator eP;
		if(dR.getType()==ChameleonDirtyRegion.INSERT){
			for(int j=0; j<clonedPositions.size(); j++){
				eP = clonedPositions.get(j);
				// offset dirty region before position (adjust offset position)
				if(dR.getOffset()<=eP.offset){
					eP.setOffset(eP.getOffset()+dR.getLength());
				}
				// offset dirty region after position (do nothing)
				else if(dR.getOffset()>(eP.getOffset()+eP.getLength()-1)){
					// do nothing
				}
				// offset dirty region in position (adjust length position)
				else{
					eP.setLength(eP.getLength()+dR.getLength());
				}
			}				
		}
		// dirty region of type _REMOVE
		else{
			int beginOffsetDR = dR.getOffset();
			int endOffsetDR = dR.getOffset()+dR.getLength()-1;
			int beginOffsetPos;
			int endOffsetPos;
			for(int j=0; j<clonedPositions.size(); j++){
				eP = clonedPositions.get(j);
				beginOffsetPos = eP.getOffset();
				endOffsetPos = eP.getOffset()+eP.getLength()-1;
				// dirty region completely in position (adjust length position)
				if(beginOffsetDR >= beginOffsetPos && endOffsetDR <= endOffsetPos){
					eP.setLength(eP.getLength()-dR.getLength());
				}
				// dirty region completely before position (adjust offset position)
				else if(endOffsetDR < beginOffsetPos){
					eP.setOffset(eP.getOffset()-dR.getLength());
				}
				// dirty region completely covers position (set length position zero)
				else if(beginOffsetDR <= beginOffsetPos && endOffsetDR >= endOffsetPos){
					eP.setLength(0);
				}
				// dirty region paritally covers position, only first part position (adjust offset and length position)
				else if(beginOffsetDR < beginOffsetPos && endOffsetDR < endOffsetPos &&  endOffsetDR >= beginOffsetPos){
					eP.setOffset(dR.getOffset());
					eP.setLength(endOffsetPos-endOffsetDR);
				}
				// dirty region partially covers position, only last part position (adjust length position)
				else if(beginOffsetDR > beginOffsetPos && endOffsetDR > endOffsetPos &&  beginOffsetDR <= endOffsetPos){
					eP.setLength(beginOffsetDR - beginOffsetPos);
				}
			}
		}
	}
	
	
	
	
	/**
	 * Activates incremental reconciling of the specified dirty region.
	 * As a dirty region might span multiple content types, the segment of the
	 * dirty region which should be investigated is also provided to this 
	 * reconciling strategy. The given regions refer to the document passed into
	 * the most recent call of {@link #setDocument(IDocument)}.
	 *
	 * @param dirtyRegion the document region which has been changed
	 * @param subRegion the sub region in the dirty region which should be reconciled 
	 */
	public static void showSize(Namespace ns) {
		int size = ns.getNamespaceParts().size();
		System.out.println(ns.getFullyQualifiedName()+" defined by "+size+" parts");
		for(Namespace nested: ns.getSubNamespaces()) {
			showSize(nested);
		}
	}
	
	public static void showTypeSize(Namespace root) {
		for(Type type:root.descendants(Type.class)) {
		  int size = type.descendants(Element.class).size();
		  System.out.println(type.getFullyQualifiedName()+" contains "+size+" elements");
		}
	}
	public void reconcile(ChameleonDirtyRegion dirtyRegion, IRegion subRegion){
		Namespace root = getDocument().compilationUnit().language().defaultNamespace();
		if(DEBUG) {
			System.out.println("Number of elements in model: "+root.descendants(Element.class).size());
			System.out.println("Number of namespaces in model: "+root.descendants(Namespace.class).size());
			System.out.println("Number of namespacesparts in model: "+root.descendants(NamespacePart.class).size());
			System.out.println("Number of types in model: "+root.descendants(Type.class).size());
			showSize(root);
			showTypeSize(root);
		}
		System.out.println("reconciling dirtyregion & subregion,in chameleonReconcilingStrategy");
		//if(_firstDR == false){
		if(_firstDR == true){
			System.out.println("Start synchronisatie");
			System.out.println(" 1. Verwerken vervuilde tekstgebieden");
		}

			setWholeDocumentDirty(false);
			_firstDR = false;
			//System.out.println("   VERVUILD TEKSTGEBIED - offset: "+dirtyRegion.getOffset()+" - lengte: "+dirtyRegion.getLength()+ " - type: "+dirtyRegion.getType()/*+" tekst: "+dirtyRegion.getText()*/);
			
			if(dirtyRegion.getType() == ChameleonDirtyRegion.INSERT){
				
				adaptClonedPositions(dirtyRegion);

			}
			
			ClonedDecorator coveringPos = getSmallestCoveringPos(dirtyRegion);
			
			if(coveringPos != null){
				//System.out.println("     => KLEINST GEWIJZIGDE POSITIE - offset: "+coveringPos.getOffset()+" - lengte: "+coveringPos.getLength()+" - element: "+coveringPos.getElement().getClass().getName());
				addListDirtyPositions(coveringPos);
			}
			else{
				System.out.println("     => HELE DOCUMENT VERVUILD");
				setWholeDocumentDirty(true);
			}

			if(dirtyRegion.getType() == ChameleonDirtyRegion.REMOVE){
				adaptClonedPositions(dirtyRegion);
			}
			
			//System.out.println("Klaar met reconcilen");

			

			
			
			
		/*}
		else{
			_firstDR = false;
		}*/
	}
	
//	// 	Geeft de minimale (kleinste) positie met lengte groter dan 'length' terug die de volledige dirtyRegion omvat en null indien geen 
//	// 	positie kan gevonden worden.
//	private Decorator getCoveringPosition(ChameleonDirtyRegion dirtyRegion, int length){
//		Decorator pos = null;
//		int minLength = Integer.MAX_VALUE;
//		int offset = dirtyRegion.getOffset();
//		int len = dirtyRegion.getLength();
//		
//		Position[] positions = null;
//		try{
//			positions = _document.getPositions(Decorator.CHAMELEON_CATEGORY);
//		} catch (BadPositionCategoryException e){}
//		
//		for(int t=0;t<positions.length;t++){
//			//System.out.println(" "+positions[t].getOffset()+" - "+positions[t].getLength()+" / "+offset+" - "+len);
//			if(	positions[t].getOffset() <= offset &
//				(offset+len) <= (positions[t].getOffset()+positions[t].getLength()) &
//				positions[t].getLength() < minLength &
//				positions[t].getLength() > length){
//					minLength = positions[t].getLength();
//					pos = (Decorator) positions[t];
//			}
//		}
//		
//		return pos;
//	}
	
//	//	Geeft de minimale (kleinste) positie waarbij offset binnen positie valt of null indien geen 
//	// 	positie kan gevonden worden.
//	private Decorator getCoveringPosition(int offset){
//		Decorator pos = null;
//		int minLength=Integer.MAX_VALUE;
//		
//		Position[] positions = null;
//		try{
//			positions = _document.getPositions(Decorator.CHAMELEON_CATEGORY);
//		} catch (BadPositionCategoryException e){}
//		
//		for(int t=0;t<positions.length;t++){
//			//System.out.println(" "+positions[t].getOffset()+" - "+positions[t].getLength()+" / "+offset);
//			if(	positions[t].getOffset() <= offset &
//				offset < (positions[t].getOffset()+positions[t].getLength()) &
//				positions[t].getLength() < minLength){
//					minLength = positions[t].getLength();
//					pos = (Decorator) positions[t];
//			}
//		}
//		
//		return pos;
//	}
//	
	private void addListDirtyPositions(ClonedDecorator position){
		if(!dirtyPositions.contains(position)) {
			dirtyPositions.add(position);
		}
	}

	/**
	 * Activates non-incremental reconciling. The reconciling strategy is just told
	 * that there are changes and that it should reconcile the given partition of the
	 * document most recently passed into {@link #setDocument(IDocument)}.
	 *
	 * @param partition the document partition to be reconciled
	 */
	public void reconcile(IRegion partition){
		
	}

//private ChameleonPresentationReconciler _presrec;

	
//	public void setPresentationReconciler(ChameleonPresentationReconciler presrec) {
//		_presrec = presrec;
//	}

	/**
	 * 
	 * @return the configuration where this strategy is used
	 */
	public ChameleonSourceViewerConfiguration getConfiguration() {
		return _configuration;
	}
	
	public static class ClonedDecorator extends Position {

		public ClonedDecorator(int offset, int length, Element element, String name){
			super(Math.max(0,offset),Math.max(0,length));
	  	if(element == null) {
	  		throw new ChameleonProgrammerException("Initializing decorator with null element");
	  	}
			setElement(element);
			setName(name);
		}

		private Element _element;
		
		public Element getElement() {
			return _element;
		}
		
		public void setElement(Element element) {
		  _element = element;
		}
		
		private String _name;

		private void setName(String name) {
			_name = name;
		}
		public String getName() {
			return _name;
		}

	}
}
