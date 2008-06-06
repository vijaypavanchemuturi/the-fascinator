package au.edu.usq.solr.portal;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import au.edu.usq.solr.util.XmlUtils;

public class PortalManager {

    private static final String SUN_OF_FEDORA_HOME_DIR = ".sun-of-fedora";

    private static final String PORTALS_XML = "portals.xml";

    private Logger log = Logger.getLogger(PortalManager.class);

    private Portal defaultPortal;

    private Map<String, Portal> portals;

    private File portalFile;

    private Document portalDoc;

    private long lastModified;

    public PortalManager() {
        portals = new HashMap<String, Portal>();
        String sunOfFedoraHome = System.getenv("SUN_OF_FEDORA_HOME");
        if (sunOfFedoraHome == null) {
            sunOfFedoraHome = System.getProperty("user.home") + File.separator
                + SUN_OF_FEDORA_HOME_DIR;
        }
        portalFile = new File(sunOfFedoraHome, PORTALS_XML);
        log.debug("Loading portals from " + portalFile);

        try {
            load();
        } catch (Exception e) {
            log.error("Failed to load portal settings");
        }
    }

    public Portal getDefaultPortal() {
        return defaultPortal;
    }

    public Portal getPortal(String name) {
        Portal portal = null;
        if (getPortals().containsKey(name)) {
            portal = getPortals().get(name);
        }
        return portal;
    }

    public Map<String, Portal> getPortals() {
        if (lastModified != portalFile.lastModified()) {
            try {
                load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return portals;
    }

    public void addPortal(Portal portal) {
        if (portal.getFacetFieldList().isEmpty()) {
            portal.setFacetFields(new HashMap<String, String>(
                defaultPortal.getFacetFields()));
        }
        portals.put(portal.getName(), portal);
    }

    private void load() throws Exception {
        lastModified = portalFile.lastModified();
        portals = new HashMap<String, Portal>();

        portalDoc = XmlUtils.parseDocument(portalFile);
        loadDefaults();
        NodeList nodes = portalDoc.getDocumentElement().getElementsByTagName(
            "portal");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            String name = elem.getAttribute("name");
            String desc = XmlUtils.getText(elem, "description");
            String query = XmlUtils.getText(elem, "query");

            int recordsPerPage = XmlUtils.getInteger(elem, "records-per-page",
                defaultPortal.getRecordsPerPage());
            int facetCount = XmlUtils.getInteger(elem, "facetCount",
                defaultPortal.getFacetCount());
            Map<String, String> facetFields = new HashMap<String, String>(
                defaultPortal.getFacetFields());
            loadFacetFields(elem, facetFields);

            Portal portal = new Portal(name, desc, query);
            portal.setRecordsPerPage(recordsPerPage);
            portal.setFacetCount(facetCount);
            portal.setFacetFields(facetFields);

            addPortal(portal);
        }
    }

    private void loadFacetFields(Element parent, Map<String, String> facetFields)
        throws XPathExpressionException {

        Element facetFieldsElem = XmlUtils.getElement("facet-fields", parent);
        if (facetFieldsElem != null) {
            NodeList fields = facetFieldsElem.getElementsByTagName("field");
            for (int i = 0; i < fields.getLength(); i++) {
                Element field = (Element) fields.item(i);
                String name = field.getAttribute("name");
                String displayName = field.getTextContent().trim();
                facetFields.put(name, displayName);
            }
        }
    }

    private void loadDefaults() throws XPathExpressionException {
        Element defaults = XmlUtils.getElement("//defaults", portalDoc);

        int recordsPerPage = XmlUtils.getInteger(defaults, "records-per-page");
        int facetCount = XmlUtils.getInteger(defaults, "facet-count");
        Map<String, String> facetFields = new HashMap<String, String>();
        loadFacetFields(defaults, facetFields);

        defaultPortal = new Portal("all", "Everything", "");
        defaultPortal.setRecordsPerPage(recordsPerPage);
        defaultPortal.setFacetCount(facetCount);
        defaultPortal.setFacetFields(facetFields);

        addPortal(defaultPortal);
    }

    public void save() {
        try {
            int recordsPerPage = defaultPortal.getRecordsPerPage();
            int facetCount = defaultPortal.getFacetCount();
            saveDefaults();
            Element portalsElem = portalDoc.getDocumentElement();
            for (Portal p : portals.values()) {
                if (!p.equals(defaultPortal)) {
                    Element portalElem = XmlUtils.getElement("//portal[@name='"
                        + p.getName() + "']", portalsElem);
                    if (portalElem == null) {
                        portalElem = portalDoc.createElement("portal");
                        portalsElem.appendChild(portalElem);
                    }
                    if (recordsPerPage != p.getRecordsPerPage()) {
                        XmlUtils.setInteger(portalElem, "records-per-page",
                            p.getRecordsPerPage());
                    }
                    if (facetCount != p.getFacetCount()) {
                        XmlUtils.setInteger(portalElem, "facet-count",
                            p.getFacetCount());
                    }
                    portalElem.setAttribute("name", p.getName());
                    XmlUtils.setText(portalElem, "description",
                        p.getDescription());
                    XmlUtils.setText(portalElem, "query", p.getQuery());
                    saveFacetFields(portalElem, p.getFacetFields(), false);
                }
            }
            FileOutputStream out = new FileOutputStream(portalFile);
            XmlUtils.serializeDoc(portalDoc, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFacetFields(Element parent,
        Map<String, String> facetFields, boolean isDefault)
        throws XPathExpressionException {

        if (!isDefault) {
            // remove any default facets
            Map<String, String> defaults = defaultPortal.getFacetFields();
            Map<String, String> copy = new HashMap<String, String>(facetFields);
            for (String key : facetFields.keySet()) {
                String value = facetFields.get(key);
                if (defaults.containsKey(key)
                    && defaults.get(key).equals(value)) {
                    copy.remove(key);
                }
            }
            facetFields = copy;
        }

        Element facetFieldsElem = XmlUtils.getElement("facet-fields", parent);
        if (facetFieldsElem != null) {
            parent.removeChild(facetFieldsElem);
        }

        if (!facetFields.isEmpty()) {
            facetFieldsElem = portalDoc.createElement("facet-fields");
            parent.appendChild(facetFieldsElem);
            for (String key : facetFields.keySet()) {
                Element fieldElem = portalDoc.createElement("field");
                fieldElem.setAttribute("name", key);
                fieldElem.setTextContent(facetFields.get(key));
                facetFieldsElem.appendChild(fieldElem);
            }
        }
    }

    private void saveDefaults() throws XPathExpressionException {
        Element defaults = XmlUtils.getElement("//defaults", portalDoc);

        XmlUtils.setInteger(defaults, "records-per-page",
            defaultPortal.getRecordsPerPage());
        XmlUtils.setInteger(defaults, "facet-count",
            defaultPortal.getFacetCount());

        saveFacetFields(defaults, defaultPortal.getFacetFields(), true);
    }
}
