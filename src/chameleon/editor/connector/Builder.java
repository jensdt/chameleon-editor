package chameleon.editor.connector;

import chameleon.core.compilationunit.CompilationUnit;
import chameleon.exception.ModelException;

public abstract class Builder {

	public abstract void build(CompilationUnit compilationUnit) throws ModelException;

}
