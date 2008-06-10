/* 
 * Sun of Fedora - Solr Portal
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
package au.edu.usq.solr.portal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import au.edu.usq.solr.model.Response;

public class Searcher {

    private Logger log = Logger.getLogger(Searcher.class);

    private String solrBaseUrl;

    private String baseFacetQuery;

    private int start;

    private int rows;

    private int facetMinCount;

    private int facetLimit;

    private List<String> facetQueries;

    private List<String> facetFields;

    public Searcher(String solrBaseUrl) {
        this.solrBaseUrl = solrBaseUrl;
        baseFacetQuery = "";
        start = 0;
        rows = 25;
        facetMinCount = 1;
        facetQueries = new ArrayList<String>();
        facetFields = new ArrayList<String>();
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

    public Response find(String query) throws IOException, JAXBException {
        Response response = null;
        String queryUrl = getUrl(query);
        log.info("Query URL: " + queryUrl);
        GetMethod method = new GetMethod(queryUrl);
        HttpClient client = new HttpClient();
        int status = client.executeMethod(method);
        if (status == 200) {
            JAXBContext jc = JAXBContext.newInstance(Response.class);
            Unmarshaller um = jc.createUnmarshaller();
            response = (Response) um.unmarshal(method.getResponseBodyAsStream());
        }
        return response;
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
        if (start > 0) {
            url.append("&start=");
            url.append(start);
        }
        if (rows > 0) {
            url.append("&rows=");
            url.append(rows);
        }
        url.append("&facet=true");
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
        return url.toString();
    }
}
