package chameleon.editor.connector;

import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.rejuse.association.Association;
import org.rejuse.association.SingleAssociation;

import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.element.Element;
import chameleon.core.modifier.Modifier;
import chameleon.core.reference.CrossReference;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.editors.reconciler.ChameleonPresentationReconciler;
import chameleon.editor.project.ChameleonProjectNature;
import chameleon.exception.ChameleonProgrammerException;
import chameleon.input.InputProcessor;
import chameleon.input.ParseException;
import chameleon.plugin.Processor;
import chameleon.plugin.ProcessorImpl;

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

	public ChameleonDocument document(Element element) {
		return projectNature().document(element);
	}

	public void setLocation(Element element, int offset, int length, CompilationUnit compilationUnit) {
		if(element == null) {
			//throw new ChameleonProgrammerException("Trying to set decorator to a null element.");
		} else {
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
		if(doc.getName().endsWith("A.jlo") && element.getClass().getName().endsWith("JavaMethodInvocation")) {
				  System.out.println("debug");
		}
		Element parent = element.parent();
		SingleAssociation stub = new SingleAssociation(new Object());
			if(! element.hasTag(tagType)) {
				SingleAssociation elementParentLink = element.parentLink();
				Association parentLink = elementParentLink.getOtherRelation();
				boolean cleanup = false;
				if(element != compilationUnit) {
					cleanup = true;
					if(parentLink != null) {
					  parentLink.replace(elementParentLink, stub);
					}
					element.setUniParent(compilationUnit);
				}
				EclipseEditorTag dec = new EclipseEditorTag(doc,offset,length,element,tagType);
				if(cleanup) {
					element.setUniParent(null);
					if(parentLink != null) {
						// The setUniParent(null) call above create a new parentLink for the element, so we
						// must obtain a new reference.
						elementParentLink = element.parentLink();
						parentLink.replace(stub,elementParentLink);
					}
				}
			}
//		}
			if(element.parent() != parent) {
				throw new ChameleonProgrammerException();
			}
	}

	public void markParseError(int offset, int length, String message,Element element) {
		ChameleonDocument document = document(element);
		String header;
		int lineNumber;
		try {
			lineNumber = document.getLineOfOffset(offset);
			int offsetWithinLine = offset - document.getLineOffset(lineNumber);
			lineNumber++;
			offsetWithinLine++;
			header = "line "+lineNumber+":"+(offsetWithinLine);
		} catch (BadLocationException e) {
			header = "cannot determine position of syntax error:";
			lineNumber = 0;
		}
		//FIXME don't like that all this static code is in ChameleonPresentationReconciler.
		Map<String,Object> attributes = ChameleonPresentationReconciler.createProblemMarkerMap(header+" "+message);
		ChameleonPresentationReconciler.setProblemMarkerPosition(attributes, offset, length, document);
		ChameleonPresentationReconciler.addProblemMarker(attributes, document);
	}

}
