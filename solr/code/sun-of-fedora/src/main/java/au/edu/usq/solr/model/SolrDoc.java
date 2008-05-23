package au.edu.usq.solr.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SolrDoc {

    private Logger log = Logger.getLogger(SolrDoc.class);

    private String id;

    private String title;

    private List<String> identifiers;

    private String description;

    public SolrDoc(Element elem) {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        try {
            id = xpath.evaluate("./*[@name='id']", elem);
            title = xpath.evaluate("./*[@name='title']", elem);
            identifiers = new ArrayList<String>();
            NodeList identifierNodes = (NodeList) xpath
                    .evaluate("./*[@name='identifier']/str", elem,
                            XPathConstants.NODESET);
            for (int i = 0; i < identifierNodes.getLength(); i++) {
                Element idenifierElem = (Element) identifierNodes.item(i);
                identifiers.add(idenifierElem.getTextContent());
            }
            description = xpath.evaluate("./*[@name='description']", elem);
        } catch (XPathExpressionException e) {
            log.error("Invalid XPath expression", e);
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public String getDescription() {
        return description;
    }
}
