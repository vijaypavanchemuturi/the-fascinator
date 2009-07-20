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
package au.edu.usq.fascinator.portal.pages;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.util.TextStreamResponse;

import au.edu.usq.fascinator.portal.Searcher;
import au.edu.usq.fascinator.util.BinaryStreamResponse;

public class Solr {

    private static final String SOLR_SELECT_PATH = "/solr/select?";

    private Logger log = Logger.getLogger(Solr.class);

    @InjectPage(value = "search")
    private Search searchPage;

    @Inject
    private Request httpRequest;

    Object onActivate(Object[] params) {
        String contentType = "text/xml";
        String path = httpRequest.getPath();
        if (path.startsWith(SOLR_SELECT_PATH)) {
            path = path.substring(SOLR_SELECT_PATH.length());
        }
        Searcher searcher = searchPage.getSecureSearcher();
        String query = Searcher.QUERY_ALL;
        String extras = "";
        List<String> paramNames = httpRequest.getParameterNames();
        for (String name : paramNames) {
            String[] values = httpRequest.getParameters(name);
            log.info(name + "=" + values);
            for (String value : values) {
                if ("q".equals(name)) {
                    query = value;
                } else if ("fl".equals(name)) {
                    searcher.setFieldList(value);
                } else if ("start".equals(name)) {
                    searcher.setStart(Integer.parseInt(value));
                } else if ("rows".equals(name)) {
                    searcher.setRows(Integer.parseInt(value));
                } else if ("facet.mincount".equals(name)) {
                    searcher.setFacetMinCount(Integer.parseInt(value));
                } else if ("facet.limit".equals(name)) {
                    searcher.setFacetLimit(Integer.parseInt(value));
                } else if ("facet".equals(name)) {
                    searcher.setFacet(Boolean.parseBoolean(value));
                } else {
                    if ("wt".equals(name) && "json".equals(value)) {
                        contentType = "text/plain";
                    }
                    try {
                        if (!"".equals(extras)) {
                            extras += "&";
                        }
                        extras += name + "="
                            + URLEncoder.encode(value, "UTF-8");
                    } catch (UnsupportedEncodingException uee) {
                        log.warn("Parameter '" + name
                            + "' skipped because of an encoding error");
                    }
                }
            }
        }

        InputStream results = null;
        try {
            results = searcher.findRaw(query, extras, false);
        } catch (IOException ioe) {
            return new TextStreamResponse("text/xml", "<the-fascinator><error>"
                + ioe.getMessage() + "</error></the-fascinator>");
        }

        if (results != null) {
            return new BinaryStreamResponse(contentType, results);
        } else {
            return new TextStreamResponse("text/xml",
                "<the-fascinator><error>Failed to query Solr</error></the-fascinator>");
        }
    }
}
