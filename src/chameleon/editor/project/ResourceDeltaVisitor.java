/**
 * 
 */
package chameleon.editor.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

public abstract class ResourceDeltaVisitor implements IResourceDeltaVisitor {
	public boolean visit(IResourceDelta delta) throws CoreException {
			switch (delta.getKind()) {
			case IResourceDelta.ADDED :
				handleAdded(delta);
				break;
			case IResourceDelta.REMOVED :
				handleRemoved(delta);
				break;
			case IResourceDelta.CHANGED :
				handleChanged(delta);
				break;
			}
		return true;
	}
	
	public abstract void handleAdded(IResourceDelta delta) throws CoreException;

	public abstract void handleRemoved(IResourceDelta delta) throws CoreException;

	public abstract void handleChanged(IResourceDelta delta) throws CoreException;

}