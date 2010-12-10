package chameleon.editor.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

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
		build(chameleonNature().compilationUnits());
		return new IProject[0];
	}

	public ChameleonProjectNature chameleonNature() throws CoreException {
		return ((ChameleonProjectNature)getProject().getNature(ChameleonProjectNature.NATURE));
	}

	protected IProject[] incrementalBuild(Map arguments, IProgressMonitor monitor) throws CoreException {
		IResourceDelta delta = getDelta(getProject());
		incrementalBuild(delta);
		return new IProject[0];
	}

	public void incrementalBuild(IResourceDelta delta) throws CoreException {
		System.out.println("RUNNING INCREMENTAL BUILD!");
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
					List<CompilationUnit> cus = new ArrayList<CompilationUnit>();
					cus.add(cu);
					build(cus);
				}
			}

			@Override
			public void handleAdded(IResourceDelta delta) throws CoreException {
				System.out.println("build: added "+delta.getProjectRelativePath());
			}
		});
	}

	public void build(List<CompilationUnit> compilationUnits) throws CoreException {
		chameleonNature().flushProjectCache();
		for(CompilationUnit cu: compilationUnits) {
			build(cu);
		}
	}
	
	public void build(CompilationUnit cu) throws CoreException {
		Builder builder = builder();
		if(builder != null) {
			try {
				ChameleonDocument doc = chameleonNature().document(cu);
				VerificationResult ver = ChameleonReconcilingStrategy.checkVerificationErrors(doc);
				if(ver == null) {
					System.out.println("debug");
				}
				if(ver.equals(Valid.create())) {
					builder.build(cu);
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
