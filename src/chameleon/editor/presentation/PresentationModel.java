package chameleon.editor.presentation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import chameleon.core.element.ChameleonProgrammerException;
import chameleon.core.language.Language;
import chameleon.editor.ChameleonEditorPlugin;

/***
 * @author Manuel Van Wesemael 
 * @author Joeri Hendrickx 
 * 
 * A Presentation Model is part of the language model of a ChameleonEditor instance.
 * 
 * It contains definitions about what and how to colour, to fold, and other presentational
 * parameters.  This object is created from a presentational XML node conform to the
 * DTD as described in the ChameleonEditor documentation.
 */
public class PresentationModel {
	
	/*A vector containing the style rules */
	private List<StyleRule> rules;
	private List<String[]> outlineElements;
	private List<String> defaultOutlineElements;
	private Language language;
	
	/**
	 * Creates a model with no rules
	 *
	 */
	public PresentationModel(Language lang){
		language=lang;
		rules=new ArrayList<StyleRule>();
		outlineElements = new ArrayList<String[]>();	
		defaultOutlineElements = new ArrayList<String>();	
	}
	
	/**
	 * Creates a new presentation model with its rules from an XML document
	 * @param XMLFile
	 * 		The XML document being used
	 */
	public PresentationModel(Language lang, InputStream XMLFile){
		if(lang == null) {
			throw new ChameleonProgrammerException("The language for presentation model is null.");
		}
		if(XMLFile == null) {
			throw new ChameleonProgrammerException("The input stream for presentation model is null.");
		}
		language=lang;
		rules=new ArrayList<StyleRule>();
		outlineElements = new ArrayList<String[]>();
		defaultOutlineElements = new ArrayList<String>();	
		
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			   DocumentBuilder builder = factory.newDocumentBuilder();
			   Document document = builder.parse(XMLFile);
			   NodeList stylerules = document.getChildNodes().item(0).getChildNodes(); // stylerules
			   for (int i = 0; i < stylerules.getLength(); i++) {
				   Node currentrule = stylerules.item(i);
				   if (currentrule.getNodeName().equals("stylerule"))
					   addRule(currentrule);
				   if (currentrule.getNodeName().equals("outline"))
					   addOutlineElements(currentrule.getChildNodes());
				   
			}
		} catch (SAXParseException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * adds the outline elements to the list, according to the occurences in de xml file
	 */
	private void addOutlineElements(NodeList childNodes) {
		
		IPreferenceStore store = ChameleonEditorPlugin.getDefault().getPreferenceStore();
		
		for(int i= 0; i< childNodes.getLength(); i ++){
			Node currNode = childNodes.item(i);
			if (currNode.getNodeName().equals("element")){
				
				Node child = currNode.getFirstChild();
				String[] v = new String[2];
				v[0] = child.getNodeValue();
				v[1] = currNode.getAttributes().getNamedItem("description").getTextContent();
				
				outlineElements.add(v);
				if (currNode.getAttributes().getNamedItem("default").getTextContent().equals("visible")){
					defaultOutlineElements.add(child.getNodeValue());
					String fieldNaam = "outlineElement_"+language+"_"+child.getNodeValue();
					store.setDefault(fieldNaam, true);
				} else {
					String fieldNaam = "outlineElement_"+language+"_"+child.getNodeValue();
					store.setDefault(fieldNaam, true);
				}
			}
		}
		
	}

	/*
	 * adds a new rule to the rules
	 */
	private void addRule(Node currentrule) {
		String element = currentrule.getAttributes().getNamedItem("element").getTextContent();
		String dec = currentrule.getAttributes().getNamedItem("decorator").getTextContent();
		String desc = currentrule.getAttributes().getNamedItem("description").getTextContent();
		PresentationStyle style = new PresentationStyle(currentrule.getChildNodes());
		addRule(style, new Selector(element, dec, desc));
	}

	/*
	 * adds a rule to the rules. A new styleRule is made from the style and the selector
	 */
	private void addRule(PresentationStyle style, Selector selector)	{
		
		
		addDefaultToStore(style, selector);
		if (ruleSetInStore(selector)){
			rules.add(new StyleRule(getFromStore(selector),selector));
		}
		else {
			rules.add(new StyleRule(style, selector));
			addToStore(style, selector);
		}
	}
	
	/*
	 * returns the style for the given selector, it is retrieved from the store.
	 */
	private PresentationStyle getFromStore(Selector selector) {
		IPreferenceStore store = ChameleonEditorPlugin.getDefault().getPreferenceStore();
		String fieldname = "stylerule_"+language+"_"+selector.getElementType()+"_"+selector.getDecoratorType()+"_";
		return PresentationStyle.fromStore(store, fieldname);
	}


	private boolean ruleSetInStore(Selector selector) {
		IPreferenceStore store = ChameleonEditorPlugin.getDefault().getPreferenceStore();
		String fieldNaam = "stylerule_"+language+"_"+selector.getElementType()+"_"+selector.getDecoratorType()+"_ruleSet";
		return store.getBoolean(fieldNaam);
	}

	private void addDefaultToStore(PresentationStyle style, Selector selector){
		IPreferenceStore store = ChameleonEditorPlugin.getDefault().getPreferenceStore();
		String fieldNaam = "stylerule_"+language+"_"+selector.getElementType()+"_"+selector.getDecoratorType()+"_";
		store.setDefault(fieldNaam+"foreground_color",style.getForeground().getColorString());
		store.setDefault(fieldNaam+"background_color",style.getBackground().getColorString());
		store.setDefault(fieldNaam+"foreground_set",style.getForeground().isDefined());
		store.setDefault(fieldNaam+"background_set",style.getBackground().isDefined());
		store.setDefault(fieldNaam+"folded",style.isFolded());
		store.setDefault(fieldNaam+"foldable",style.isfoldable());
		store.setDefault(fieldNaam+"bold",style.isBold());
		store.setDefault(fieldNaam+"italic",style.isItalic());
		store.setDefault(fieldNaam+"underline",style.isUnderlined());
	}

	private void addToStore(PresentationStyle style, Selector selector){
		IPreferenceStore store = ChameleonEditorPlugin.getDefault().getPreferenceStore();
		String fieldNaam = "stylerule_"+language+"_"+selector.getElementType()+"_"+selector.getDecoratorType()+"_";
		store.setValue(fieldNaam+"foreground_color",style.getForeground().getColorString());
		store.setValue(fieldNaam+"background_color",style.getBackground().getColorString());
		store.setValue(fieldNaam+"foreground_set",style.getForeground().isDefined());
		store.setValue(fieldNaam+"background_set",style.getBackground().isDefined());
		store.setValue(fieldNaam+"folded",style.isFolded());
		store.setValue(fieldNaam+"foldable",style.isfoldable());
		store.setValue(fieldNaam+"bold",style.isBold());
		store.setValue(fieldNaam+"italic",style.isItalic());
		store.setValue(fieldNaam+"underline",style.isUnderlined());

		store.setValue(fieldNaam+"ruleSet",true);
		
	}

	/*
	 * creates a color from a string 
	 */
	private Color createColor(String coded) {
		int r = Integer.parseInt(coded.substring(1,3),16);
		int g = Integer.parseInt(coded.substring(3,5),16);
		int b = Integer.parseInt(coded.substring(5,7),16);
		return new Color(Display.getCurrent(),r,g,b);
	}	
	
	/**
	 * Finds a presentation style for a certain position
	 * @param element
	 * 	the element type of the given position.  May be expressed hierarchically using periods
	 * @param decorator
	 *  the decorator type of the given position
	 * @return the style if any, null otherwise
	 */
	public PresentationStyle getRule(String element, String decorator){
		Iterator it = rules.iterator();
		while (it.hasNext()){
			StyleRule sr = (StyleRule) it.next();
			if (sr.selector.map(element, decorator))
				return sr.style;
		}
		return null;
	}
	
	/**
	 * Represents a new style rule with a certain style & selector
	 *
	 */
	class StyleRule {
		public PresentationStyle style;
		public Selector selector;
		public StyleRule(PresentationStyle style, Selector selector){
			this.style=style;
			this.selector=selector;
		}
		
	}

	/**
	 * 
	 * @param offset
	 * 	the offset of the element
	 * @param length
	 * 	the length of the element
	 * @param element
	 * 	the element where a stylerange is to be made of
	 * @param decname
	 * 	the decorator namen for the element
	 * @return a new styleRange for a given element
	 */
	public StyleRange map(int offset, int length, String element, String decname) {
		//System.out.println("Mapping from "+this);

		Iterator<StyleRule> it = rules.iterator();
		while(it.hasNext())
		{
			StyleRule rule = it.next();
			
			if (rule.selector.map(element,decname))
				return rule.style.getStyleRange(offset,length);
		}
		return null;
	}

	public List<String[]> getOutlineElements() {
		return outlineElements;
	}

	public void setOutlineElements(Vector<String[]> outlineElements) {
		this.outlineElements = outlineElements;
	}

	public List<String> getDefaultOutlineElements() {
		return defaultOutlineElements;
	}

	public HashMap<Selector, PresentationStyle> getRules() {
		HashMap<Selector, PresentationStyle> result = new HashMap<Selector, PresentationStyle>();
		
		for (Iterator<StyleRule> iter = rules.iterator(); iter.hasNext();) {
			StyleRule element = iter.next();
			result.put(element.selector, element.style);
		}
		
		return result;
	}

	public void updateRule(Selector selector, PresentationStyle style) {
		addToStore(style, selector);
		Iterator<StyleRule> it = rules.iterator();
		while(it.hasNext())
		{
			StyleRule rule = it.next();
			
			if (rule.selector.map(selector.getElementType(),selector.getDecoratorType()))
				rule.style=style;
		}
	}
	
}
