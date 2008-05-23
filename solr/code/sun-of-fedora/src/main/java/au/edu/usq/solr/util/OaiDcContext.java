package au.edu.usq.solr.util;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class OaiDcContext implements NamespaceContext {

    public static final String DC_PREFIX = "dc";

    public static final String DC_NS = "http://purl.org/dc/elements/1.1/";

    public static final String OAI_DC_PREFIX = "oai_dc";

    public static final String OAI_DC_NS = "http://www.openarchives.org/OAI/2.0/oai_dc/";

    public String getNamespaceURI(String prefix) {
        if (DC_PREFIX.equals(prefix)) {
            return DC_NS;
        } else if (OAI_DC_PREFIX.equals(prefix)) {
            return OAI_DC_NS;
        }
        return null;
    }

    public String getPrefix(String ns) {
        if (DC_NS.equals(ns)) {
            return DC_PREFIX;
        } else if (OAI_DC_NS.equals(ns)) {
            return OAI_DC_PREFIX;
        }
        return null;
    }

    public Iterator getPrefixes(String ns) {
        return null;
    }
}
