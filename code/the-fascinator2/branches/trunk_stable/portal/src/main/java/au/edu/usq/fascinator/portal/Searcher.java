/* 
 * The Fascinator - Solr Portal
 * Copyright (C) 2008  University of Southern Queensland
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package au.edu.usq.fascinator.portal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import au.edu.usq.fascinator.common.BasicHttpClient;
import au.edu.usq.fascinator.model.Response;

public class Searcher {

    public static String QUERY_ALL = "*:*";

    private Logger log = Logger.getLogger(Searcher.class);

    private String solrBaseUrl;

    private String baseFacetQuery;

    private int start;

    private int rows;

    private int facetMinCount;

    private int facetLimit;

    private String fieldList;

    private boolean enableFacets;

    private boolean facetSort;

    private List<String> facetQueries;

    private List<String> facetFields;

    private List<String> sortFields;

    private BasicHttpClient client;

    public Searcher(String solrBaseUrl) {
        this.solrBaseUrl = solrBaseUrl;
        enableFacets = true;
        facetSort = true;
        fieldList = "score";
        baseFacetQuery = "";
        start = 0;
        rows = 25;
        facetMinCount = 1;
        facetQueries = new ArrayList<String>();
        facetFields = new ArrayList<String>();
        sortFields = new ArrayList<String>();
        client = new BasicHttpClient(solrBaseUrl);
    }

    public void authenticate(String username, String password) {
        client.authenticate(username, password);
    }

    public void setFacet(boolean enableFacets) {
        this.enableFacets = enableFacets;
    }

    public void setFacetSort(boolean facetSort) {
        this.facetSort = facetSort;
    }

    public void setFieldList(String fieldList) {
        this.fieldList = fieldList;
    }

    public void setBaseFacetQuery(String baseFacetQuery) {
        this.baseFacetQuery = baseFacetQuery;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setFacetMinCount(int facetMinCount) {
        this.facetMinCount = facetMinCount;
    }

    public void setFacetLimit(int facetLimit) {
        this.facetLimit = facetLimit;
    }

    public void setFacetFields(List<String> facetFields) {
        this.facetFields = facetFields;
    }

    public void addFacetQuery(String facetQuery) {
        facetQueries.add(facetQuery);
    }

    public void addFacetField(String facetField) {
        facetFields.add(facetField);
    }

    public void addSortField(String sortField) {
        sortFields.add(sortField);
    }

    public Response findAll() throws IOException, JAXBException {
        return find(null);
    }

    public Response find(String query) throws IOException, JAXBException {
        return find(query, true);
    }

    public Response find(String query, boolean escape) throws IOException,
        JAXBException {
        InputStream results = findRaw(query, escape);
        if (results != null) {
            JAXBContext jc = JAXBContext.newInstance(Response.class);
            Unmarshaller um = jc.createUnmarshaller();
            return (Response) um.unmarshal(results);
        }
        return null;
    }

    public InputStream findRaw(String query, String extras, boolean escape)
        throws IOException {
        if (query == null) {
            query = "*:*";
        } else if (!QUERY_ALL.equals(query) && escape) {
            query = query.replaceAll(":", "\\\\:");
        }
        String queryUrl = getUrl(query, extras);
        log.debug("Query URL: " + queryUrl);
        GetMethod method = new GetMethod(queryUrl);
        int status = client.executeMethod(method, true);
        if (status == 200) {
            return method.getResponseBodyAsStream();
        }
        return null;
    }

    public InputStream findRaw(String query, boolean escape) throws IOException {
        return findRaw(query, null, escape);
    }

    private String getUrl(String query) throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder(solrBaseUrl);
        url.append("/select?q=");
        url.append(URLEncoder.encode(query, "UTF-8"));
        if (baseFacetQuery != null && !"".equals(baseFacetQuery)) {
            url.append("&fq=");
            url.append(URLEncoder.encode(baseFacetQuery, "UTF-8"));
        }
        for (String facetQuery : facetQueries) {
            url.append("&fq=");
            url.append(URLEncoder.encode(facetQuery, "UTF-8"));
        }
        url.append("&fl=" + fieldList);
        if (start > 0) {
            url.append("&start=");
            url.append(start);
        }
        if (rows > 0) {
            url.append("&rows=");
            url.append(rows);
        }
        url.append("&facet=" + enableFacets);
        url.append("&facet.sort=" + facetSort);
        if (facetMinCount > 0) {
            url.append("&facet.mincount=");
            url.append(facetMinCount);
        }
        if (facetLimit > 0) {
            url.append("&facet.limit=");
            url.append(facetLimit);
        }
        for (String facetField : facetFields) {
            url.append("&facet.field=");
            url.append(URLEncoder.encode(facetField, "UTF-8"));
        }
        for (String sortField : sortFields) {
            url.append("&sort=");
            url.append(URLEncoder.encode(sortField, "UTF-8"));
        }
        return url.toString();
    }

    private String getUrl(String query, String extras)
        throws UnsupportedEncodingException {
        String url = getUrl(query);
        if (!"".equals(extras) && extras != null) {
            int qmark = url.indexOf('?') + 1;
            url = url.substring(0, qmark) + extras + "&" + url.substring(qmark);
        }
        return url;
    }
}
