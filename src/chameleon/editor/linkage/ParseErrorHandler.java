//package chameleon.editor.linkage;
//
//import org.eclipse.jface.text.IDocument;
//
//import chameleon.editor.editors.ChameleonDocument;
//import chameleon.input.ParseException;
//import chameleon.linkage.IParseErrorHandler;
//
///**
// * @author Manuel Van Wesemael 
// * @author Joeri Hendrickx 
// * 
// * A parse-error-handler for giving error notices to the editor
// */
//public class ParseErrorHandler implements IParseErrorHandler {
//
//	private ChameleonDocument _document;
//
//	public ParseErrorHandler(IDocument doc) {
//		try {
//		_document = (ChameleonDocument) doc;
//		} catch (ClassCastException e){
//			System.err.println("Attempted to create ParseErrorHandler for a non-chameleon-document.  Errorhandling disabled.");
//			
//		}
//	}
//
//	public void reportError(ParseException exc) {
//		if (_document!=null) _document.handleParseError(exc);
//	}
//
//}
