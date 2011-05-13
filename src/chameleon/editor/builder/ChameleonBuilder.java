package chameleon.editor.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.language.Language;
import chameleon.core.validation.Valid;
import chameleon.core.validation.VerificationResult;
import chameleon.editor.ChameleonEditorPlugin;
import chameleon.editor.LanguageMgt;
import chameleon.editor.editors.ChameleonDocument;
import chameleon.editor.editors.reconciler.ChameleonReconcilingStrategy;
import chameleon.editor.project.ChameleonProjectNature;
import chameleon.editor.project.ChameleonResourceDeltaFileVisitor;
import chameleon.exception.ModelException;
import chameleon.plugin.build.Builder;

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
			_nature = nature;
			Language language = nature.language();
			IFile projectFile = getProject().getFile(".project");
			IPath location = projectFile.getLocation();
			File file = null;
			if (location != null) {
			   file = location.toFile().getParentFile();
			}
			//FIXME ACTUAL BUILDER MUST BE A CONNECTOR 
			_builder = LanguageMgt.getInstance().createBuilder(language,file);
		}
	}
	
	private ChameleonProjectNature _nature;
	
	public ChameleonProjectNature nature() {
		return _nature;
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

	protected IProject[] fullBuild(Map arguments, IProgressMonitor monitor) throws CoreException {
		System.out.println("RUNNING FULL BUILD!");
		build(chameleonNature().compilationUnits(), monitor);
		return new IProject[0];
	}

	public ChameleonProjectNature chameleonNature() throws CoreException {
		return ((ChameleonProjectNature)getProject().getNature(ChameleonProjectNature.NATURE));
	}

	protected IProject[] incrementalBuild(Map arguments, IProgressMonitor monitor) throws CoreException {
		IResourceDelta delta = getDelta(getProject());
		incrementalBuild(delta, monitor);
		return new IProject[0];
	}

	public void incrementalBuild(IResourceDelta delta, final IProgressMonitor monitor) throws CoreException {
		System.out.println("RUNNING INCREMENTAL BUILD!");
		
		// First collect all the compilation units. If we pass them in one call to
		// the build method, the progress monitor works without writing additional code.
		final List<CompilationUnit> cus = new ArrayList<CompilationUnit>();
		delta.accept(new ChameleonResourceDeltaFileVisitor(chameleonNature()){
		
			@Override
			public void handleRemoved(IResourceDelta delta) throws CoreException {
				System.out.println("build: removed "+delta.getProjectRelativePath());
			}
		
			@Override
			public void handleChanged(IResourceDelta delta) throws CoreException {
				ChameleonDocument doc = documentOf(delta);
				System.out.println("build: changed "+delta.getProjectRelativePath());
				if(doc != null) {
					CompilationUnit cu = doc.compilationUnit();
					cus.add(cu);
				}
			}

			@Override
			public void handleAdded(IResourceDelta delta) throws CoreException {
				System.out.println("build: added "+delta.getProjectRelativePath());
			}
		});

		build(cus,monitor);
	}

	private void checkForCancellation(IProgressMonitor monitor) throws CoreException {
		if(monitor.isCanceled()) {
			forgetLastBuiltState();
			getProject().deleteMarkers(IMarker.PROBLEM,true,IResource.DEPTH_INFINITE);
			throw new OperationCanceledException("The build was cancelled by the user.");
		}
	}
	
	private String buildName() {
		return "Building project";
	}
	
	public void build(List<CompilationUnit> compilationUnits, IProgressMonitor monitor) throws CoreException {
		boolean released = true;
		try {
			int totalWork = compilationUnits.size();
			monitor.setTaskName(buildName());
			monitor.beginTask(buildName(), totalWork);
			chameleonNature().acquire();
			released = false;
			chameleonNature().flushProjectCache();
			int i = 0;
			for(CompilationUnit cu: compilationUnits) {
				checkForCancellation(monitor);
				build(cu);
				i++;
				monitor.worked(1);
			}
		} catch(InterruptedException exc) {
		} finally {
			if(! released) {
				chameleonNature().release();
			}
			monitor.done();
		}
	}
	
	private void build(CompilationUnit cu) throws CoreException {
		Builder builder = builder();
		List<CompilationUnit> compilationUnits = nature().compilationUnits();
		if(builder != null) {
			try {
				ChameleonDocument doc = chameleonNature().document(cu);
				VerificationResult ver = ChameleonReconcilingStrategy.checkVerificationErrors(doc);
				if(ver == null) {
					System.out.println("debug");
				}
				if(ver.equals(Valid.create())) {
					builder.build(cu,compilationUnits);
				}
			} catch (ModelException e) {
				//TODO report error using a MARKER
				e.printStackTrace();
			} catch (IOException e) {
				//TODO report error using a MARKER
				e.printStackTrace();
			}
		}
	}

}
