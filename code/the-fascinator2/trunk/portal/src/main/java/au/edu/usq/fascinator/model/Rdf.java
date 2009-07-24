package au.edu.usq.fascinator.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rdf {
    private Logger log = LoggerFactory.getLogger(Rdf.class);

    private String RDFns = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private String DCns = "http://purl.org/dc/elements/1.1/";
    private String J0ns = "http://www.semanticdesktop.org/ontologies/2007/01/19/nie#";
    private String J1ns = "http://www.semanticdesktop.org/ontologies/2007/03/22/nco#";
    private String J2ns = "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#";
    private String FOAFns = "http://xmlns.com/foaf/0.1/";

    private Document document;
    private SAXReader saxReader;

    private String title;
    private String mimeType;
    private String filePath;
    private String dcDate;
    private String pageCount;
    private String generator;
    private String initialCreator;

    private Map<String, String> namespaces;

    public Rdf(InputStream is) {
        try {
            settingUpNamespace();
            saxReader = new SAXReader();
            document = saxReader.read(new InputStreamReader(is, "UTF-8"));
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
        if (title == "") {
            title = getNodeValue("./rdf:RDF/rdf:Description/*[local-name()='title']");
        }
        return title;
    }

    public String getMimeType() {
        return getNodeValue("./rdf:RDF/rdf:Description/*[local-name()='mimeType']");
    }

    public String getFilePath() {
        Node filePath = document
                .selectSingleNode("./rdf:RDF/rdf:Description[not(starts-with(@rdf:about, 'urn:'))]");
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
        if (pageCount != null) {
            return pageCount.getText();
        }
        return "";
    }

    private void settingUpNamespace() {
        namespaces = new HashMap<String, String>();
        registerNamespace("rdf", RDFns);
        registerNamespace("dc", DCns);
        registerNamespace("j.0", J0ns);
        registerNamespace("j.1", J1ns);
        registerNamespace("j.2", J2ns);
        registerNamespace("foaf", FOAFns);
        DocumentFactory.getInstance().setXPathNamespaceURIs(namespaces);
    }

    public void registerNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
    }

    public void unregisterNamespace(String prefix) {
        namespaces.remove(prefix);
    }
}
