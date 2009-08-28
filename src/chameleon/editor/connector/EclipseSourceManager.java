package chameleon.editor.connector;

import org.eclipse.jface.text.BadLocationException;

import chameleon.core.element.ChameleonProgrammerException;
import chameleon.core.element.Element;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.project.ChameleonProjectNature;
import chameleon.input.SourceManager;
import chameleon.tool.Connector;
import chameleon.tool.ConnectorImpl;

public class EclipseSourceManager extends ConnectorImpl implements SourceManager {

	public EclipseSourceManager(ChameleonProjectNature nature) {
		setProjectNature(nature);
	}


	public ChameleonProjectNature projectNature() {
		return _projectNature;
	}

	public void setProjectNature(ChameleonProjectNature nature) {
		_projectNature = nature;
	}

	private ChameleonProjectNature _projectNature;

	@Override
	public Connector clone() {
		return new EclipseSourceManager(null);
	}

	public String text(Element element) {
		ChameleonEditorPosition decorator = (ChameleonEditorPosition) element.tag(ChameleonEditorPosition.ALL_DECORATOR);
		ChameleonDocument document = projectNature().document(element);
		try {
			return document.get(decorator.getOffset(), decorator.getLength());
		} catch (BadLocationException e) {
			throw new ChameleonProgrammerException("Bad location",e);
		}
	}

}
