package chameleon.editor.connector;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;

import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.element.ChameleonProgrammerException;
import chameleon.core.element.Element;
import chameleon.core.modifier.Modifier;
import chameleon.core.reference.CrossReference;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.project.ChameleonProjectNature;
import chameleon.input.InputProcessor;
import chameleon.input.ParseException;
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
		// ECLIPSE NEEDS A +1 INCREMENT FOR THE LENGTH
		length++;
		setSingleLocation(element, offset, length, compilationUnit, EclipseEditorTag.ALL_TAG);
		if(element instanceof CrossReference) {
			setSingleLocation(element, offset, length, compilationUnit, EclipseEditorTag.CROSSREFERENCE_TAG);
		}
		if(element instanceof Modifier) {
			setSingleLocation(element, offset, length, compilationUnit, EclipseEditorTag.MODIFIER_TAG);
		}
 
	}
	
	public void setLocation(Element element, int offset, int length, CompilationUnit compilationUnit, String tagType) {
		if(element == null) {
			throw new ChameleonProgrammerException("Trying to set decorator to a null element.");
		}
		// ECLIPSE NEEDS A +1 INCREMENT FOR THE LENGTH
		length++;
		setSingleLocation(element, offset, length, compilationUnit, tagType);
	}


	private void setSingleLocation(Element element, int offset, int length, CompilationUnit compilationUnit, String tagType) {
		ChameleonDocument doc = document(compilationUnit);
//		try {
			if(doc.getLength() == 0) {
				System.out.println("Empty document.");
			}
			if(! element.hasTag(tagType)) {
				Element ancestor = element.furthestAncestor();
				boolean cleanup = false;
				if(element != compilationUnit) {
					cleanup = true;
					ancestor.setUniParent(compilationUnit);
				}
				
				EclipseEditorTag dec = new EclipseEditorTag(doc,offset,length,element,tagType);
//				doc.addPosition(EclipseEditorTag.CHAMELEON_CATEGORY,dec);
				
				if(cleanup) {
					ancestor.setUniParent(null);
				}
			}
//		} catch (BadLocationException e) {
//			System.err.println("Couldn't set decorator ["+tagType+"] at offset "+offset+" with length " + length+ " for "+element);
//			System.err.println("Document length: "+doc.getLength());
//			System.err.println("Trying to show text starting from offset:");
//			try {
//				for(int i=0;i<length;i++) {
//					System.err.println(doc.getChar(offset+i));
//				}
//			} catch(BadLocationException exc) {
//				
//			}
//			e.printStackTrace();
//		} catch (BadPositionCategoryException e) {
//			System.err.println("Couldn't set decorator ["+tagType+"] at offset "+offset+" with length " + length+ " for "+element);
//			e.printStackTrace();
//		}
	}

}
