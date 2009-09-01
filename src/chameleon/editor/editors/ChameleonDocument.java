package chameleon.editor.editors;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.widgets.Display;

import chameleon.core.Config;
import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.language.Language;
import chameleon.core.namespace.Namespace;
import chameleon.editor.LanguageMgt;
import chameleon.editor.connector.ChameleonEditorPosition;
import chameleon.editor.presentation.PresentationManager;
import chameleon.editor.project.ChameleonProjectNature;
import chameleon.input.ModelFactory;
import chameleon.input.ParseException;

/**
 * @author Jef Geerinckx
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * A document for the chameleon framework. The ChameleonDocument contains positions 
 * and a positionUpdater.  
 * After initialisation, the ChameleonDocument contains a meta-model, which depends on 
 * the language the Document is used for.
 */

public class ChameleonDocument extends Document {
	
	//The compilation unit of the document
	private CompilationUnit _cu;
	//The project where this document is found
	private IProject _project;
	//manages the presentation
	private PresentationManager presentationManager;
	//the path to the file of which this document is made
	private IPath path;
	//the last known presenation of the document
	private TextPresentation lastpresentation;
	
	//check whether presentation is going on by some reconciler
	private boolean presenting;
	//when the document contains language errors, they are stored here.
	private List<ParseException> parseErrors;	
	//A listener for the time when the errors change
	private ActionListener parseErrorActionListener;
	//The name of the document.
	private String name;
	//The file of which this document is made
	private IFile file;
	private String relativePathName;

	/**
	 * creates a new ChameleonDocument & intializes it.
	 * Initially there are no parse errors 
	 * The document receives its name and is parsed when effective
	 * Support for updating the positions in the document is set
	 * @param file 
	 *
	 */
	public ChameleonDocument(IProject project, IFile file, IPath path){
		
		super();
		
		setCompilationUnit(new CompilationUnit());
		
		parseErrors = new ArrayList<ParseException>();
		
		_project = project;
		initialize();
		
		this.path=path;
		
		if (file!=null){ 		
			try {
				parseFile(file);
				relativePathName = path.removeFirstSegments(1).toString();
				name = file.getName();
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else{
		   IPath projPath = path.removeFirstSegments(1);	 
		   file = project.getFile(projPath);
		   name = file.getName();
		   relativePathName = projPath.toString();
		   
		}
		this.file = file;
			}
	
	/**
	 * 
	 * @return
	 * The presentation managager if it is present; else a new presentation manager is made and returned
	 */
	public PresentationManager getPresentationManager(){
		PresentationManager result = presentationManager;
		if(result == null) {
			result = new PresentationManager(this, getProjectNature().presentationModel());
			presentationManager = result;
		}
		return result;
	}
	
	/**
	 * Checks whether the paths of the documents are the same
	 * @param cd
	 * 	the other chameleonDocument
	 * @return
	 * 	true if the paths are identical.
	 */
	public boolean isSameDocument(ChameleonDocument cd){
		return path.toOSString().equals(cd.path.toOSString());
	}
	
	//parses the givenfile
	private void parseFile(IFile file) throws CoreException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
		String nxt = "";
		String tot = "";
		while (nxt!=null) {
			nxt = reader.readLine();
			if (nxt!=null) tot+="\n"+nxt;
		}
		// QUESTION: does this trigger the reconcilers?
		set(tot);
		
	}


	/*
	 * The initialisation of the document. 
	 * PositionCategories are set together with a position updater.
	 * The meta model is also set here.
	 */
	private void initialize(){
		addPositionCategory(ChameleonEditorPosition.CHAMELEON_CATEGORY);
		addPositionUpdater(new DefaultPositionUpdater(ChameleonEditorPosition.CHAMELEON_CATEGORY));
	}

	/**
	 * prints debugging stuff
	 * @param chameleon_category
	 */
	public void printPositionsAndParents(String chameleon_category){
		Position[] pos;
		try {
				pos = getPositions(ChameleonEditorPosition.CHAMELEON_CATEGORY);
				System.out.println("***********************\n" +
								   "***********************\n" +
								   "* elements with __ALL decorator\n " +
								   "***********************");
				for(int i=0; i<pos.length; i++){
					String elementname = ((ChameleonEditorPosition)pos[i]).getElement().getClass().getName();
					String decoratorName =((ChameleonEditorPosition)pos[i]).getName();
					if (decoratorName.equals("__ALL")){
						System.out.println(elementname);
					}
				}
		
		
		
				System.out.println("***********************\n" +
						   "***********************\n" +
						   "* elements with __ALL decorator and its parent\n " +
						   "***********************");

			for(int i=0; i<pos.length; i++){
				String elementname = ((ChameleonEditorPosition)pos[i]).getElement().getClass().getName();
				String decoratorName =((ChameleonEditorPosition)pos[i]).getName();
				if (decoratorName.equals("__ALL")
						&&
					(elementname.equals("org.jnome.mm.compilationunit.CompilationUnit")||
					elementname.equals("org.jnome.mm.type.JavaClass")||
					elementname.equals("chameleon.core.variable.MemberVariable")||
					elementname.equals("org.jnome.mm.method.Constructor")||
					elementname.equals("chameleon.core.method.RegularMethod")||
					elementname.equals("org.jnome.mm.type.JavaInnerClass"))
					
					
				){
					String parentDecorator;

					try {
						parentDecorator= ((ChameleonEditorPosition)pos[i]).getParentDecorator().getElement().getClass().getName();
						
					} catch (Exception e) {
						parentDecorator = "parentDecorator not found" ;
					}
					
					System.out.println("element: "+elementname+ "\t" + "ParentDecorator: "
										+  parentDecorator
										);
				}
			}
		} catch (BadPositionCategoryException e1) {
			
			e1.printStackTrace();
		}
		
	}
	/*
	 *  (non-Javadoc)
	 * @see chameleonEditor.editors.IChameleonDocument#printPositions(java.lang.String)
	 */
	public void printPositions(String cat){
//		try{
//			Position[] pos = getPositions(ChameleonPosition.CHAMELEON_CATEGORY);
//			for(int i=0; i<pos.length; i++){
//				int currentLine = this.getLineOfOffset(pos[i].getOffset());
//				int lineoffset = this.getLineOffset(currentLine);
//				currentLine = currentLine + 1; //eclipse begint pas te tellen vanaf rij 1
//				int currentColumn = pos[i].getOffset() - lineoffset;
//				System.out.println("  POSITIE - offset: "+pos[i].getOffset()+" - lengte: "+pos[i].getLength()+ " - line: "+ currentLine + " - column: " + currentColumn +" - element: "+((Decorator)pos[i]).getElement().getClass().getName());
//			}
//			//pos = getPositions(RangeDecorator.NAME_CATEGORY);
//			//for(int i=0; i<pos.length; i++){
//			//	System.out.println("  POSITIE - offset: "+pos[i].getOffset()+" - lengte: "+pos[i].getLength()+" - element: "+((ChameleonPosition)pos[i]).getDecorator().getElement().getClass().getName());
//			//}
//		}catch(Exception e){}
	}


	/**
	 * Sets the compilation unit for this document
	 * physically nothing changes !
	 * @param cu
	 * @pre cu must be effective
	 */
	public void setCompilationUnit(CompilationUnit cu) {
		_cu = cu;
		//_cu.setDocument(this);
	}
	
	

	/**
	 * 
	 * @return the project where this compilation unit is in
	 */
	public IProject getProject() {
		return _project;
	}
	
	/**
	 * Returns the project nature of the project of this 
	 * @return
	 */
	public ChameleonProjectNature getProjectNature(){
		try {
			IProjectNature nature = _project.getNature(ChameleonProjectNature.NATURE);
			return (ChameleonProjectNature) nature;
		}
		//TODO : Marko : why are all these exceptions caught here?
		catch (NullPointerException e) {
			return null;
		} 
		catch (CoreException e) {
			return null;
		}
		catch (ClassCastException e) {
			return null;
		}
	}
	
	public Object getModel(){
		throw new Error("Apparently this method is used by eclipse.");
//		return getProjectNature().getModel();
	}
	
	public ModelFactory modelFactory(){
		return getProjectNature().modelFactory();
	}


	/**
	 * 
	 * @return the compilation unit
	 */
	public CompilationUnit compilationUnit() {
		return _cu;
	}

	/** 
	 * Empty out the chameleonpositions, none of the decorators are left.
	 *
	 */
	public void dumpPositions() {
		try {
			this.removePositionCategory(ChameleonEditorPosition.CHAMELEON_CATEGORY);
			this.addPositionCategory(ChameleonEditorPosition.CHAMELEON_CATEGORY);
		} catch (BadPositionCategoryException e) {}
		
	}


	@Override
	public void addPosition(Position position) throws BadLocationException {
		super.addPosition(position);
	}


	@Override
	public void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException {
		super.addPosition(category, position);
	}

//	/**
//	 * 
//	 * @return the language of the project where this document is in.
//	 * if no project is defined, an empty string is returned
//	 */
//	public String getLanguage() {
//		if (getProject()==null) {
//			return "";
//		}
//		return getProjectNature().getLanguage();
//	}
	
	public Language language() {
		try {
			return ((ChameleonProjectNature)_project.getNature(ChameleonProjectNature.NATURE)).language();
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * The textrepresentation for the viewer is changed to the presentation we get from 
	 * our presenation manager.
	 * that representation is now the last known one.
	 * @param viewer
	 */
	public void doPresentation(final ITextViewer viewer) {
		

		
		try{
			lastpresentation = getPresentationManager().createTextPresentation();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.changeTextPresentation(lastpresentation, false);
					viewer.changeTextPresentation(lastpresentation, true);
				}
			});	
		}catch (NullPointerException npe){
			npe.printStackTrace();
		}
	}

	/**
	 * 
	 * @return 
	 *   the foldable positions
	 */
	public Vector<Position> getFoldablePositions() {
		return getPresentationManager().getFoldablePositions();
	}

	/**
	 * 
	 * @return the elements which are folded
	 */
	public Vector<ChameleonEditorPosition> getFoldedElementsFromModel() {
		return getPresentationManager().getFoldedElementsFromModel();
	}

	private int nbNamespaceParts(Namespace ns) {
		int result = ns.getNamespaceParts().size();
		for(Namespace namespace:ns.getSubNamespaces()) {
			result += nbNamespaceParts(namespace);
		}
		return result;
	}
	

	/**
	 * Reparse this document.
	 * 
	 * The document is basically removed from the project and then added again.
	 */
	public void reParse() {

		System.out.println("Starting reparse");

		// to reparse an entire document

		// A. remove all document positions & problem markers
		System.out.println("DUMPING POSITIONS");
		dumpPositions();
		try {
			getFile().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
			parseErrors.clear();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		// if this is not set, the document is new and has never been parsed before.
		if (_cu == null) {
			getProjectNature().addToModel(this);
		} else {

			// B. remove compilation unit from model tree
			if(Config.DEBUG) {
			  System.out.println("removing compilation unit from tree");
			}
			_cu.disconnect();

			// C. remove Document from project
			if(Config.DEBUG) {
			  System.out.println("Remove document from project");
			}
			getProjectNature().removeModelElement(this);

			// D. Re-add Document to the project (wich will cause it to be parsed)
			if(Config.DEBUG) {
			  System.out.println("Re-add document to project");
			}
			Namespace root = getProjectNature().getModel();
			getProjectNature().addModelElement(this, root);
		}

		System.out.println("Einde reparse");
	}

	/**
	 * The textrepresentation for the viewer is changed to the presentation we get
	 * from our presenation manager. the presentation is done for the speciefied
	 * offset & length that representation is now the last known one.
	 * 
	 * @param viewer
	 * @param offset
	 * @param length
	 */
	public void doPresentation(final ITextViewer viewer, final int offset, final int length) {
//		System.out.println("ChameleonDocument.dopresentation is opgeroepen");
		
		try{

			lastpresentation = getPresentationManager().createTextPresentation();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.changeTextPresentation(lastpresentation, true);
				}
			});	
		}catch (NullPointerException npe){}
		
	}

	/**
	 * A parse error has occured while making the model for the document;
	 * that error is handled here. It is added to the other existing errors.
	 * @param exc
	 */
	public void handleParseError(ParseException exc) {
//		if(!parseErrors.contains(exc))
//				parseErrors.add(exc);
		boolean alreadyPresent = false;
		for (ParseException exception : parseErrors) {
			if (exception.toString().equals(exc.toString()))
				 alreadyPresent = true;
		}
		if(!alreadyPresent)
			parseErrors.add(exc);
		if (parseErrorActionListener!=null) 
			parseErrorActionListener.actionPerformed(null);
		
	}

	public void setParseActionListener(ActionListener listener){
		this.parseErrorActionListener = listener;
	}

	/**
	 * 
	 * @return all the parse errors for this document
	 */
	public List<ParseException> getParseErrors() {
		return new ArrayList<ParseException>(parseErrors);
	}

	/**
	 * 
	 * @return the relative path of this document, starting from the project.
	 */
	public IPath getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public IFile getFile() {
		return file;
	}

	public void removeParseErrors() {
		parseErrors.clear();
		
	}

	public String getRelativePathName() {
		
		return relativePathName;
	}

	
	public ChameleonEditorPosition getReferenceDecoratorAtRegion(IRegion region){
		try {
			Position[] positions = getPositions(ChameleonEditorPosition.CHAMELEON_CATEGORY);
			// Find smallest decorater including the specified region:
			int minLength = Integer.MAX_VALUE;
			ChameleonEditorPosition result = null;
			for (Position position : positions) {
				if(position instanceof ChameleonEditorPosition ){
					ChameleonEditorPosition decorator = (ChameleonEditorPosition) position;
					if(decorator.getName().equals(ChameleonEditorPosition.CROSSREFERENCE_DECORATOR)){
						if(decorator.includes(region.getOffset()) && decorator.getLength()<minLength){
							result = decorator;
						}
					}
				}
					
			}
			if(Config.DEBUG) {
				if(result == null) {
					System.out.println("No cross reference decoration found.");
				}
			}
			return result;
		} catch (BadPositionCategoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}


}


