package au.edu.usq.solr.portal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Portal implements Comparable<Portal> {

    private static Portal defaultPortal = null;

    private String name;

    private String query;

    public static Portal getDefaultPortal() {
        if (defaultPortal == null) {
            defaultPortal = new Portal("all", "");
        }
        return defaultPortal;
    }

    public Portal(String name, String query) {
        this.name = name;
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getEncodedQuery() throws UnsupportedEncodingException {
        return URLEncoder.encode(query, "UTF8");
    }

    public int compareTo(Portal that) {
        return name.compareTo(that.getName());
    }
}
