package chameleon.editor.presentation.treeview;

import java.util.ArrayList;
import java.util.List;

import chameleon.core.element.Element;

public class DefaultIconProvider extends IconProvider {

	
	@Override
	public String iconName(Element element) {
		String result = null;
		if(selectedClass().isInstance(element)) {
			result = auxIconName(element);
		}
		return result;
	}
	
	protected String auxIconName(Element element) {
		
	}

	private List<NameBasedIconDecorator> _decorators = new ArrayList<NameBasedIconDecorator>();
	
	private String _undecoratedIconName;
	
	public Class<?> selectedClass() {
		return _selectedClass;
	}
	
	private Class<?> _selectedClass;
}
