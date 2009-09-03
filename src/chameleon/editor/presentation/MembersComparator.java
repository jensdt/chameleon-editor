/**
 * Created on 25-apr-07
 * @author Tim Vermeiren
 */
package chameleon.editor.presentation;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import chameleon.core.element.Element;
import chameleon.core.member.Member;
import chameleon.core.method.Method;
import chameleon.core.method.RegularMethod;
import chameleon.core.type.Type;
import chameleon.core.variable.MemberVariable;
import chameleon.support.member.simplename.operator.Operator;

public class MembersComparator extends ViewerComparator {
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return super.compare(viewer, e1, e2);
	}
	
	@Override
	public int category(Object object) {
		Element element = ChameleonLabelProvider.getElement(object);
		if(element instanceof Member){
			if(element instanceof Method){
				if(element instanceof RegularMethod)
					return 3;
				if(element instanceof Operator)
					return 4;
				else
					return 10;
			} 
			if(element instanceof Type)
				return 5;
			if(element instanceof MemberVariable)
				return 1;
			else
				return 20;
		}
		return 30;
	}
}
