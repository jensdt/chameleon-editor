/**
 * Created on 20-mrt-07
 * @author Tim Vermeiren
 */
package chameleon.editor.presentation.formatting;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

import chameleon.editor.ChameleonEditorPlugin;
import chameleon.editor.editors.preferences.ChameleonEditorPreferencePage;

/**
 * This class makes it possible to edit a command before it is executed.
 * So you can do whatever you want when a letter is typed for instance.
 * 
 */
public class ChameleonAutoEditStrategy implements IAutoEditStrategy {

	public static boolean ENABLE_AUTO_FORMATTING = ChameleonEditorPlugin.getDefault().getPreferenceStore().getBoolean(ChameleonEditorPreferencePage.ENABLE_AUTO_FORMATTING);

	/**
	 * @see IAutoEditStrategy
	 * 
	 * If an opening brace { is typed the ending brace } will be automaticaly
	 * be typed en indented.
	 */
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		if(ENABLE_AUTO_FORMATTING){
			try {
				// braces
				if(command.text.equals("{")){
					DefaultIndentLineAutoEditStrategy indent = new DefaultIndentLineAutoEditStrategy();
					String eol = document.getLegalLineDelimiters()[0];
					command.text = eol;
					// calculate the indentation of the current line:
					indent.customizeDocumentCommand(document, command);
					String newlineIndent = command.text;
					command.text = "{" + newlineIndent + "\t";
					int cursorOffset = command.text.length();
					command.text += newlineIndent + "}";
					//command.text = "{\r\n  \r\n";*/
					command.caretOffset = command.offset+cursorOffset;
					command.shiftsCaret = false;
				} // quotation marks:
				else if(command.text.equals("\"")){
					char nextChar = document.get(command.offset, 1).charAt(0);
					if(nextChar == '"'){
						command.text = "";
					} else {
						command.text += "\""; // add ending quotion mark "
					}
					command.caretOffset = command.offset+1;
					command.shiftsCaret = false; // move cursor with added selection 
				} else if(command.text.equals("'")){
					char nextChar = document.get(command.offset, 1).charAt(0);
					if(nextChar == '\''){
						command.text = "";
					} else {
						command.text += "'"; // add ending quotion mark '
					}
					command.caretOffset = command.offset+1;
					command.shiftsCaret = false; // move cursor with added selection 
				} // brackets
				else if(command.text.equals("(")){
					command.text += ")"; // add ending )
					command.caretOffset = command.offset+1;
					command.shiftsCaret = false; // move cursor with added selection 
				} else if(command.text.equals(")")){
					char nextChar = document.get(command.offset, 1).charAt(0);
					if(nextChar == ')'){
						command.text = "";
						command.caretOffset = command.offset+1;
						command.shiftsCaret = false; // move cursor with added selection 
					}
				}  else if(command.text.equals("[")){
					command.text += "]"; // add ending )
					command.caretOffset = command.offset+1;
					command.shiftsCaret = false; // move cursor with added selection 
				} else if(command.text.equals("]")){
					char nextChar = document.get(command.offset, 1).charAt(0);
					if(nextChar == ']'){
						command.text = "";
						command.caretOffset = command.offset+1;
						command.shiftsCaret = false; // move cursor with added selection 
					}
				} 
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

}
