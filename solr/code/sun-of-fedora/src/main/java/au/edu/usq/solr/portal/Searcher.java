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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import au.edu.usq.solr.Configuration;
import au.edu.usq.solr.model.Response;

public class Searcher {

    private Logger log = Logger.getLogger(Searcher.class);

    private String solrBaseUrl;

    private int start;

    private int recordsPerPage;

    private List<String> searchFields;

    private List<String> facetLimits;

    private List<String> facetFields;

    private int facetCount;

    private Configuration config;

    private Portal portal;

    public Searcher(Configuration config) {
        this.config = config;
        solrBaseUrl = config.getSolrBaseUrl();
        recordsPerPage = config.getRecordsPerPage();
        start = 0;
        portal = null;
        searchFields = new ArrayList<String>();
        facetLimits = new ArrayList<String>();
        facetFields = new ArrayList<String>();
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setRecordsPerPage(int numResults) {
        this.recordsPerPage = numResults;
    }

    public void addSearchField(String field) {
        searchFields.add(field);
    }

    public void addFacetLimit(String facetLimit) {
        facetLimits.add(facetLimit);
    }

    public void addFacetField(String field) {
        facetFields.add(field);
    }

    public void setFacetCount(int facetCount) {
        this.facetCount = facetCount;
    }

    public void setFacetFields(List<String> facetFields) {
        this.facetFields = facetFields;
    }

    public Response find(String query) {
        Response response = null;
        try {
            String queryUrl = getUrl(query);
            log.info("queryUrl = " + queryUrl);
            GetMethod method = new GetMethod(queryUrl);
            HttpClient client = new HttpClient();
            int status = client.executeMethod(method);
            if (status == 200) {
                response = new Response(method.getResponseBodyAsStream());
            }
        } catch (HttpException e) {
            log.error("HTTP exception", e);
        } catch (IOException e) {
            log.error("IO exception", e);
        }
        return response;
    }

    private String getUrl(String query) throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder(solrBaseUrl);
        url.append("/select?q=");
        url.append(URLEncoder.encode(query, "UTF-8"));
        if (portal != null) {
            url.append("&fq=");
            url.append(portal.getEncodedQuery());
        }
        for (String limit : facetLimits) {
            url.append("&fq=");
            url.append(URLEncoder.encode(limit, "UTF-8"));
        }
        if (start > 0) {
            url.append("&start=");
            url.append(start);
        }
        if (recordsPerPage > -1) {
            url.append("&rows=");
            url.append(recordsPerPage);
        }
        url.append("&facet=true");
        url.append("&facet.mincount=1");
        if (facetCount > 0) {
            url.append("&facet.limit=");
            url.append(facetCount);
        }
        for (String field : facetFields) {
            url.append("&facet.field=");
            url.append(URLEncoder.encode(field, "UTF-8"));
        }
        return url.toString();
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }
}
