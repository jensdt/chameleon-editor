package chameleon.editor.connector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chameleon.core.language.Language;
import chameleon.input.ParseException;
import chameleon.output.Syntax;

/**
 * @author Joeri Hendrickx
 * @author Marko van Dooren
 * 
 * A Language Identification Interface to be implemented by all Language Models
 * The implementationclass must be called "LanguageModelID" and must be sat on the root
 * of the language-model-package
 */
public abstract class EclipseBootstrapper {

	public EclipseBootstrapper() {
		_extensions = new ArrayList<String>();
		registerFileExtensions();
	}
	
	/**
	 * @return Informal name of the supported language
	 */
	public abstract String getLanguageName();
	
	/**
	 * @return Version information of the supported language
	 */
	public abstract String getLanguageVersion();
	
	public List<String> fileExtensions() {
		return new ArrayList<String>(_extensions);
	}
	
	protected void addExtension(String extension) {
		_extensions.add(extension);
	}
	
	protected void removeExtension(String extension) {
		_extensions.remove(extension);
	}
	
	private List<String> _extensions;
	
	public abstract void registerFileExtensions();

	
//	/**
//	 * @return Version information of the Language Model
//	 */
//	String getVersion();
	
	/**
	 * @return General description of this language model
	 */
	public abstract String getDescription();
	
	/**
	 * @return Licensing information for the language model
	 */
	public abstract String getLicense();
	
	/**
	 * Create the language object, and attach at least the following extensions:
	 * <ul>
	 *   <li>chameleon.output.Syntax</li>
	 *   <li>chameleon.editor.connector.EclipseEditorExtension</li>
	 * </ul>
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public abstract Language createLanguage() throws IOException, ParseException;
	
	/**
	 * @return the CodeWriter for this language
	 */
	public abstract Syntax getCodeWriter();
	
	// FIXME: must be a language/project extension (or move to editor extension)
	public Builder createBuilder(Language source, File projectDirectory) {
		return null;
	}

}
