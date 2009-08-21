package chameleon.editor.linkage;

import org.eclipse.jface.text.IDocument;

import chameleon.linkage.ILinkage;
import chameleon.linkage.ISourceSupplier;

/**
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * A source-supplier to create models from memory-retained documents
 */
public class SourceSupplier implements ISourceSupplier  {

	private int current = 0;
	private IDocument[] docs;
	
	public SourceSupplier(IDocument[] docs) {
		this.docs = docs;
	}

	public boolean hasNext() {
		return current<docs.length;
	}

	public void reset() {
		current=0;
		
	}

	public void next() {
		if (hasNext()) current++;
		
	}

	public String getSource() {
		if (current<docs.length) return docs[current].get();
		return null;
	}

	public ILinkage getLinkage() {
		if (current<docs.length) return new DocumentEditorToolExtension(docs[current]);
		return null;
	}

}
