package chameleon.editor.presentation.outline;


import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import chameleon.core.element.Element;
import chameleon.editor.connector.ChameleonEditorPosition;

/**
 * 
 * @author Manuel Van Wesemael
 * 
 * content provider for ChameleonOutlineTree viewer
 *
 */
public class ChameleonOutlineTreeContentProvider implements ITreeContentProvider,IChameleonOutlineTreeListener  {


	private TreeViewer viewer;

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {

	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		System.out.println("ChameleonTreeContents inputChanged");
		
		this.viewer = (TreeViewer)viewer;
	    if(oldInput != null) {
	        removeListenerFrom((ChameleonOutlineTree)oldInput);
	    }
	    if(newInput != null) {
	       addListenerTo((ChameleonOutlineTree)newInput);
	    }
		
	}
	
	/** Because the domain model does not have a richer
	 * listener model, recursively remove this listener
	 * from each child box of the given box. */
	protected void removeListenerFrom(ChameleonOutlineTree tree) {
		tree.removeListener(this);
		for (Iterator iterator = tree.getChildren().iterator(); iterator.hasNext();) {
			ChameleonOutlineTree childTree = (ChameleonOutlineTree) iterator.next();
			removeListenerFrom(childTree);
		}
	}
	
	/** Because the domain model does not have a richer
	 * listener model, recursively add this listener
	 * to each child box of the given box. */
	protected void addListenerTo(ChameleonOutlineTree box) {
		box.addListener(this);
		for (Iterator iterator = box.getChildren().iterator(); iterator.hasNext();) {
			ChameleonOutlineTree childTree = (ChameleonOutlineTree) iterator.next();
			addListenerTo(childTree);
		}
	}

	
	/**
	 * @return the children for the parentelement
	 */
	public Object[] getChildren(Object parentElement) {
		//System.out.println("ChameleonTreeContents getChildren: ");
		Vector<ChameleonOutlineTree> kinderen = ((ChameleonOutlineTree) parentElement).getChildren();
		return kinderen.toArray();
	}

	public Object getParent(Object element) {
		//System.out.println("ChameleonTreeContents getparent: " );
		try{
			ChameleonEditorPosition dec = (ChameleonEditorPosition) element;
			System.out.println("CAST SUCCEEDED!");
			return dec.getParentDecorator();
		}catch(ClassCastException e){
			e.printStackTrace();
			//System.out.println("classcastException");
			return null;
		}catch(Exception e){ 
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * checks whether the given element has children
	 */
	public boolean hasChildren(Object element) {
		//System.out.println("ChameleonTreeContents hasChildren: ");
        if (((ChameleonOutlineTree) element).getChildren().size()==0)
            return false;
        else
            return true;
	}

	/**
	 * @see getChildren(object inputelement)
	 */
	public Object[] getElements(Object inputElement) {
		//System.out.println("ChameleonTreeContents getElements");
		return getChildren(inputElement);
	}

	public void add(ChameleonOutlineTreeEvent event) {
		//System.out.println("ChameleonOutlineTreeContentProvider.Add");
		Element elem = event.getInvolved();
		viewer.refresh(elem,true);
	}
		

	public void remove(ChameleonOutlineTreeEvent event) {
		//System.out.println("ChameleonOutlineTreeContentProvider.Remove");
		add(event);
		
	}

	public void fireChanged() {
		viewer.refresh();
	}
	


}

	



	


