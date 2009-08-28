package chameleon.editor.connector;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;

import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.element.ChameleonProgrammerException;
import chameleon.core.element.Element;
import chameleon.core.reference.CrossReference;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.project.ChameleonProjectNature;
import chameleon.input.InputProcessor;
import chameleon.input.ParseException;
import chameleon.input.Position2D;
import chameleon.tool.Processor;
import chameleon.tool.ProcessorImpl;

public class EclipseEditorInputProcessor extends ProcessorImpl implements InputProcessor {

	public EclipseEditorInputProcessor(ChameleonProjectNature nature) {
		setProjectNature(nature);
	}
	
	private ChameleonProjectNature _projectNature;
	
	public ChameleonProjectNature projectNature() {
		return _projectNature;
	}
	
	public void setProjectNature(ChameleonProjectNature nature) {
		_projectNature = nature;
	}
	
	@Override
	public Processor clone() {
		return new EclipseEditorInputProcessor(null);
	}

	public void reportParseError(ParseException exc) {
		ChameleonDocument doc = document(exc.compilationUnit());
		if (doc!=null) {
			doc.handleParseError(exc);
		}
	}

//	public void setLocation(Element element, Position2D start, Position2D end, CompilationUnit compilationUnit) {
//		//System.out.println("dec at "+offset+" to "+length+" type "+dectype+" on "+el);
//		if(element == null) {
//			throw new ChameleonProgrammerException("Trying to set decorator to a null element.");
//		}
//		ChameleonDocument doc = document(compilationUnit);
//		String dectype = ChameleonEditorPosition.ALL_DECORATOR;
//		try {
//			int offset = doc.getLineOffset(start.lineNumber()) + start.offset();
//			int endOffSet = doc.getLineOffset(end.lineNumber()) + end.offset();
//			int length = endOffSet - offset;
//			ChameleonEditorPosition dec = new ChameleonEditorPosition(offset,length,element,dectype);
//			doc.addPosition(ChameleonEditorPosition.CHAMELEON_CATEGORY,dec);
//			element.setTag(dec,dectype);
//		} catch (BadLocationException e) {
//			System.err.println("Couldn't set decorator ["+dectype+"] for "+element);
//		} catch (BadPositionCategoryException e) {
//			System.err.println("Couldn't set decorator ["+dectype+"] for "+element);
//		}
//	}
	
	public ChameleonDocument document(Element element) {
		return projectNature().document(element);
	}

	public void setLocation(Element element, int offset, int length, CompilationUnit compilationUnit) {
		if(element == null) {
			throw new ChameleonProgrammerException("Trying to set decorator to a null element.");
		}
		String dectype = ChameleonEditorPosition.ALL_DECORATOR;
		setSingleLocation(element, offset, length, compilationUnit, dectype);
		if(element instanceof CrossReference) {
			dectype = ChameleonEditorPosition.CROSSREFERENCE_DECORATOR;
			setSingleLocation(element, offset, length, compilationUnit, dectype);
		}
	}

	private void setSingleLocation(Element element, int offset, int length, CompilationUnit compilationUnit, String dectype) {
		ChameleonDocument doc = document(compilationUnit);
		try {
			if(doc.getLength() == 0) {
				System.out.println("Empty document.");
			}
			ChameleonEditorPosition dec = new ChameleonEditorPosition(offset,length,element,dectype);
			doc.addPosition(ChameleonEditorPosition.CHAMELEON_CATEGORY,dec);
//			element.setTag(dec,dectype);
		} catch (BadLocationException e) {
			System.err.println("Couldn't set decorator ["+dectype+"] at offset "+offset+" with length " + length+ " for "+element);
			System.err.println("Document length: "+doc.getLength());
			System.err.println("Trying to show text starting from offset:");
			try {
				for(int i=0;i<length;i++) {
					System.err.println(doc.getChar(offset+i));
				}
			} catch(BadLocationException exc) {
				
			}
			e.printStackTrace();
		} catch (BadPositionCategoryException e) {
			System.err.println("Couldn't set decorator ["+dectype+"] at offset "+offset+" with length " + length+ " for "+element);
			e.printStackTrace();
		}
	}

}
