package chameleon.editor;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import chameleon.core.element.Element;
import chameleon.editor.presentation.PresentationModel;
import chameleon.editor.toolextension.ILanguageModelID;
import chameleon.editor.toolextension.IMetaModelFactory;
import chameleon.output.Syntax;
import chameleon.tool.Connector;

/**
 * @author Manuel Van Wesemael
 * @author Joeri Hendrickx
 *
 * A class that manages the languages of the chameleonEditor. This class is
 * implemented with the singleton pattern
 */
public class LanguageMgt {

    /*
      * The instance of this language management
      */
    private static LanguageMgt instance;

    // keeps track of the presentation model for each of the languages
    private Map<String, PresentationModel> presentationModels;

    // the languages names
    private Map<String, ILanguageModelID> models;

    private Map<String, Connector> tools;

    private URLClassLoader languageloader;
    /**
     * creates a new mapping for the languages & presenation models These are
     * instantiated from the XML file
     *
     */
    private LanguageMgt() {
        presentationModels = new HashMap<String, PresentationModel>();
        models = new HashMap<String, ILanguageModelID>();
        tools = new HashMap<String,Connector>();
        try {
            loadJars();
        }
        catch (IOException e) {
            System.err.println("Couldn't load languages : "+e.getMessage());
        }
    }

    private void loadJars() throws IOException {
            URL url = Platform.asLocalURL(Platform.find(
                    Platform.getBundle("ChameleonEditor"),
                    new Path("languages/"))
            );
            FilenameFilter filter = new FilenameFilter(){
                public boolean accept(File dir, String name) {
                    return name.contains(".jar");
                }};
            File[] files = (new File(url.getFile())).listFiles(filter);
            URL[] urls = new URL[files.length];

            for (int i = 0; i < urls.length; i++) {
                try {
                    String s = "jar:"+files[i].toURL().toExternalForm()+"!/";
                    urls[i] = new URL(s);
                }
                catch (MalformedURLException e) {
                    System.err.println("Incorrect language ["+i+"] "+files[i].toString());
                }
            }

            languageloader = new URLClassLoader(urls, Element.class.getClassLoader());

            System.out.println("\nLoading Languages ===========");
            System.out.println("Filename   \tpackage      \tLanguage          \tModel");

            for (int i = 0; i < urls.length; i++) {
                try {
                String[] filenameParts = urls[i].getFile().split("/");
                String filename = filenameParts[filenameParts.length-1].replace("!","");
                String packagename = filename.substring(0, filename.length()-4);

                try {
                    ILanguageModelID id = (ILanguageModelID) languageloader.loadClass(packagename+".LanguageModelID").newInstance();
                    models.put(id.getLanguageName(), id);
                    ChameleonEditorExtension editorExt = (ChameleonEditorExtension)languageloader.loadClass(packagename+".tool."+id.getLanguageName()+"EditorExtension").newInstance();
                    tools.put(id.getLanguageName(),editorExt);
                    System.out.println(filename+"\t"+packagename+"\t"+id.getLanguageName()+" "+id.getLanguageVersion()+"\t"+id.getDescription());
                }
                catch (InstantiationException e) {
                    System.err.println("Error while loading language : "+packagename+"\n"+e.getMessage());
                }
                catch (ClassCastException e) {
                    System.err.println("Error while loading language : "+packagename+"\nLanguageModelID is not of the correct type.");
                }
                catch (IllegalAccessException e) {
                    System.err.println("Error while loading language : "+packagename+"\n"+e.getMessage());
                }
                catch (ClassNotFoundException e) {
                    System.err.println("Error while loading language : "+packagename+"\nNo LanguageModelID is supplied.");
                }
                } catch (Exception e){
                    System.err.println("Couldnt load language from "+urls[i]+" : "+e.getMessage());

                }

            }

            System.out.println("Done.\n");

    }

    /**
     * @return The one and only instance of this language management
     */
    public static LanguageMgt getInstance() {
        if (instance == null) instance = new LanguageMgt();
        return instance;
    }

    /**
     * @return an array containing string representations of all the supported
     *         languages of the chameleonEditor
     */
    public String[] getLanguageStrings() {
        return models.keySet().toArray(new String[0]);
    }

    /**
     *
     * @param languageString
     *            The string representation of the language to be used
     * @return gives the correct metaModel for the given language string. It is
     *         advised to call getLanguageStrings so the correct string
     *         representation can be used
     */
    public IMetaModelFactory getModelFactory(String languageString) {
        try {
            IMetaModelFactory fact = models.get(languageString).getMetaModelFactory();
            fact.addToolExtension(ChameleonEditorExtension.class,tools.get(languageString));
            return fact;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Syntax getCodeWriter(String language) {
        try {
            return models.get(language).getCodeWriter();

        } catch (NullPointerException e){ return null; }
    }

    /**
     * @return the xml document in the directory of the chameleonEditor
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Document getXMLDocument(String filename) throws IOException, ParserConfigurationException, SAXException {
        URL url = ChameleonEditorPlugin.getDefault().getBundle().getEntry(
                "xml/" + filename);
        String pathStr = Platform.asLocalURL(url).getPath();
        IPath path = new Path(pathStr);
        path.removeTrailingSeparator();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(path.toOSString());
        return document;
    }

    /**
     * creates a new presentation model and returns it for the given language.
     * The model is unique and is loaded when needed. when the language is not
     * supported, an empty model is loaded.
     */
    public PresentationModel getPresentationModel(String languageString) {
        PresentationModel r = presentationModels.get(languageString);
        if (r == null) {
            try {
                String filename = "xml/" + languageString.toLowerCase()
                        + "pres.xml";
                URL url = ChameleonEditorPlugin.getDefault().getBundle()
                        .getEntry(filename);
                String pathStr = Platform.asLocalURL(url).getPath();
                IPath path = new Path(pathStr);
                path.removeTrailingSeparator();
                r = new PresentationModel(languageString, path.toOSString());
                presentationModels.put(languageString, r);
            }
            catch (IOException ioe) {
                System.err.println("Unable to load presentation model for "
                        + languageString + ".\nEmpty model created instead.");
                System.err.println(ioe.getMessage());
                // ioe.printStackTrace();
                r = new PresentationModel(languageString);
            }
        }
        return r;
    }

}
