package chameleon.editor.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import chameleon.core.language.Language;
import chameleon.editor.ChameleonEditorPlugin;
import chameleon.editor.LanguageMgt;
import chameleon.editor.connector.Builder;
import chameleon.editor.connector.EclipseBootstrapper;
import chameleon.editor.project.ChameleonProjectNature;
import chameleon.editor.project.ResourceDeltaFileVisitor;

public class ChameleonBuilder extends IncrementalProjectBuilder {

	public ChameleonBuilder() {
		super();
	}
	
	public Builder builder() {
		return _builder;
	}
	
	public void setupBuilder() throws CoreException {
		if(_builder == null) {
			ChameleonProjectNature nature = (ChameleonProjectNature)getProject().getNature(ChameleonProjectNature.NATURE);
			Language language = nature.language();
			//FIXME ACTUAL BUILDER MUST BE A CONNECTOR 
			_builder = LanguageMgt.getInstance().createBuilder(language);
		}
	}
	
	private Builder _builder;
	
	public static final String BUILDER_ID = ChameleonEditorPlugin.PLUGIN_ID+".ChameleonBuilder";
	
	@Override
	protected IProject[] build(int kind, Map arguments, IProgressMonitor monitor) throws CoreException {
		setupBuilder();
		if(kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
			return incrementalBuild(arguments, monitor);
		} else {
		  return fullBuild(arguments, monitor);
		}
	}

	protected IProject[] fullBuild(Map arguments, IProgressMonitor monitor) {
		System.out.println("RUNNING FULL BUILD!");
		return new IProject[0];
	}

	protected IProject[] incrementalBuild(Map arguments, IProgressMonitor monitor) throws CoreException {
		IResourceDelta delta = getDelta(getProject());
		System.out.println("RUNNING INCREMENTAL BUILD!");
		delta.accept(new ResourceDeltaFileVisitor(){
		
			@Override
			public void handleRemoved(IResourceDelta delta) throws CoreException {
				System.out.println("build: removed "+delta.getProjectRelativePath());
			}
		
			@Override
			public void handleChanged(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				System.out.println("build: changed "+delta.getProjectRelativePath());
			}
		
			@Override
			public void handleAdded(IResourceDelta delta) throws CoreException {
				System.out.println("build: added "+delta.getProjectRelativePath());
			}
		});
		return new IProject[0];
	}

}
