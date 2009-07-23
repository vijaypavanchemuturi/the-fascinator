package au.edu.usq.fascinator.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rdf {
    private Logger log = LoggerFactory.getLogger(Rdf.class);

    private String DC = "http://purl.org/dc/elements/1.1/";
    private String J0 = "http://www.semanticdesktop.org/ontologies/2007/01/19/nie#";
    private String J1 = "http://www.semanticdesktop.org/ontologies/2007/03/22/nco#";
    private String J2 = "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#";

    private Document document;

    private String title;
    private String mimeType;
    private String filePath;
    private String dcDate;
    private String pageCount;
    private String generator;
    private String initialCreator;

    Namespace namespace1 = new Namespace("j.0",
        "http://www.semanticdesktop.org/ontologies/2007/01/19/nie#");

    public Rdf(InputStream is) {
        try {
            document = new SAXReader().read(new InputStreamReader(is, "UTF-8"));
            settingUpNamespace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getTitle() {
        String title = getNodeValue("./rdf:RDF/rdf:Description/dc:title");
        if (title == "")
            title = getNodeValue("./rdf:RDF/rdf:Description/*[local-name()='title']");
        return title;
    }

    public String getMimeType() {
        log.info("j.0: " + document.getRootElement().additionalNamespaces());
        log.info("------ I have it?: "
            + getNodeValue("./rdf:RDF/rdf:Description/*[local-name()='mimeType']"));
        return getNodeValue("./rdf:RDF/rdf:Description/*[local-name()='mimeType']");
    }

    public String getFilePath() {
        Node filePath = document.selectSingleNode("./rdf:RDF/rdf:Description[not(starts-with(@rdf:about, 'urn:'))]");
        if (filePath != null) {
            Element filePathElem = (Element) filePath;
            return filePathElem.attributeValue("about");
        }
        return "";
    }

    public String getDcDate() {
        return getNodeValue("./rdf:RDF/rdf:Description/dc:date");
    }

    public String getPageCount() {
        return getNodeValue("./rdf:RDF/rdf:Description/*[local-name()='page-count']");
    }

    public String getGenerator() {
        return getNodeValue("./rdf:RDF/rdf:Description/*[local-name()='generator']");
    }

    public String getInitialCreator() {
        return getNodeValue("./rdf:RDF/rdf:Description/*[local-name()='initial-creator']");
    }

    private String getNodeValue(String xPath) {
        Node pageCount = document.selectSingleNode(xPath);
        if (pageCount != null)
            return pageCount.getText();
        return "";
    }

    private void settingUpNamespace() {
        // DocumentFactory --> to register, check SolrIndexer
        Element rootElement = document.getRootElement();
        if (rootElement.getNamespaceForPrefix("dc") == null)
            rootElement.addNamespace("dc", DC);
        if (rootElement.getNamespaceForPrefix("j.0") == null)
            rootElement.addNamespace("j.0", J0);
        if (rootElement.getNamespaceForPrefix("j.1") == null)
            rootElement.addNamespace("j.1", J1);
        if (rootElement.getNamespaceForPrefix("j.2") == null)
            rootElement.addNamespace("j.2", J2);
    }
}
