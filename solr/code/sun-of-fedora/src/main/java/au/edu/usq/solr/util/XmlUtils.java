package au.edu.usq.solr.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

public class XmlUtils {

    private static DocumentBuilderFactory dbf;

    private static DocumentBuilder db;

    private static XPathFactory xpf;

    private static XPath xp;

    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        if (dbf == null) {
            dbf = DocumentBuilderFactory.newInstance();
        }
        return dbf;
    }

    public static DocumentBuilder getDocumentBuilder()
        throws ParserConfigurationException {
        if (db == null) {
            db = getDocumentBuilderFactory().newDocumentBuilder();
        }
        return db;
    }

    public static XPathFactory getXPathFactory() {
        if (xpf == null) {
            xpf = XPathFactory.newInstance();
        }
        return xpf;
    }

    public static XPath getXPath() {
        if (xp == null) {
            xp = getXPathFactory().newXPath();
        }
        return xp;
    }

    public static Document parseDocument(File xmlFile) throws SAXException,
        IOException, ParserConfigurationException {
        return getDocumentBuilder().parse(xmlFile);
    }

    public static String getText(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() > 0) {
            return ((Element) nodes.item(0)).getTextContent().trim();
        }
        return null;
    }

    public static int getInteger(Element parent, String tag) {
        return Integer.parseInt(getText(parent, tag));
    }

    public static int getInteger(Element parent, String tag, int defaultValue) {
        int value = defaultValue;
        try {
            value = XmlUtils.getInteger(parent, tag);
        } catch (NumberFormatException nfe) {
        }
        return value;
    }

    public static void setText(Element parent, String tag, String text) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() > 0) {
            ((Element) nodes.item(0)).setTextContent(text);
        } else {
            Element elem = parent.getOwnerDocument().createElement(tag);
            parent.appendChild(elem);
            elem.setTextContent(text);
        }
    }

    public static void setInteger(Element parent, String tag, int value) {
        setText(parent, tag, Integer.toString(value));
    }

    public static Node getNode(String expr, Node source)
        throws XPathExpressionException {
        return (Node) getXPath().evaluate(expr, source, XPathConstants.NODE);
    }

    public static Element getElement(String expr, Node source)
        throws XPathExpressionException {
        Node node = getNode(expr, source);
        if (node != null && Node.ELEMENT_NODE == node.getNodeType()) {
            return (Element) node;
        }
        return null;
    }

    public static boolean hasChild(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0;
    }

    public static void serializeDoc(Document doc, OutputStream out)
        throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("indent", "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(out));
    }

    public static void serializeDoc3(Document doc, OutputStream out)
        throws Exception {
        DOMImplementation impl = DOMImplementationRegistry.newInstance()
            .getDOMImplementation("XML 3.0");
        DOMImplementationLS feature = (DOMImplementationLS) impl.getFeature(
            "LS", "3.0");
        LSSerializer serializer = feature.createLSSerializer();
        LSOutput output = feature.createLSOutput();
        output.setByteStream(out);
        serializer.write(doc, output);
    }
}
