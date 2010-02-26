package chameleon.editor.connector;

import java.io.IOException;

import chameleon.core.compilationunit.CompilationUnit;
import chameleon.exception.ModelException;

public abstract class Builder {

	public abstract void build(CompilationUnit compilationUnit) throws ModelException, IOException;
	
}
