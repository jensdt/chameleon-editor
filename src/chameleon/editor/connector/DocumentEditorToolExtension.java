package chameleon.editor.connector;
//package chameleon.editor.linkage;
//
//import org.eclipse.jface.text.BadLocationException;
//import org.eclipse.jface.text.BadPositionCategoryException;
//import org.eclipse.jface.text.IDocument;
//
//import chameleon.core.compilationunit.CompilationUnit;
//import chameleon.core.element.ChameleonProgrammerException;
//import chameleon.core.element.Element;
//import chameleon.editor.editors.ChameleonDocument;
//
///**
// * @author Manuel Van Wesemael 
// * @author Joeri Hendrickx 
// * 
// * A linkage implementation for setting decorators
// */
//public class DocumentEditorToolExtension {
//
//	private IDocument _document;
//
//	public DocumentEditorToolExtension(IDocument document) {
//		_document = document;
//	}
//
////	public IParseErrorHandler getParseErrorHandler() {
////		return new ParseErrorHandler(_document);
////	}
//
//	public String getSource() {
//		return _document.get();
//	}
//
//	public void decoratePosition(int offset, int length, String dectype, Element el) {
//		//System.out.println("dec at "+offset+" to "+length+" type "+dectype+" on "+el);
//		if(el == null) {
//			throw new ChameleonProgrammerException("Trying to set decorator to a null element.");
//		}
//		Decorator dec = new Decorator(offset,length,el,dectype);
//		try {
//			_document.addPosition(Decorator.CHAMELEON_CATEGORY,dec);
//		} catch (BadLocationException e) {
//			System.err.println("Couldn't set decorator ["+dectype+"] for "+el);
//		} catch (BadPositionCategoryException e) {
//			System.err.println("Couldn't set decorator ["+dectype+"] for "+el);
//		}
//		el.setTag(dec,dectype);
//	}
//
//	public int getLineOffset(int i) {
//		try {
//			return _document.getLineOffset(i);
//		} catch (BadLocationException e) {
//			return 0;
//		}
//	}
//
////	public void addCompilationUnit(CompilationUnit cu) {
////		try {
////			ChameleonDocument cd = (ChameleonDocument) _document;
////			cd.setCompilationUnit(cu);
////		} catch (ClassCastException e){
////			System.err.println("Couldnt set Compilation Unit : not a Chameleon Document");
////		}
////		
////	}
//
//}
