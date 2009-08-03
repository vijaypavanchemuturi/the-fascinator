/* 
 * The Fascinator - Plugin API
 * Copyright (C) 2008-2009 University of Southern Queensland
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
package au.edu.usq.fascinator.api.indexer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic search request
 * 
 * @author Oliver Lucido
 */
public class SearchRequest {

    /** Search query */
    public String query;

    /**  */
    public Map<String, String[]> params;

    /**
     * Creates an empty search request
     */
    public SearchRequest() {
    }

    /**
     * Creates a search request with the specified query
     * 
     * @param query a query
     */
    public SearchRequest(String query) {
        this.query = query;
    }

    /**
     * Gets the search query
     * 
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Gets the values for the specified parameter
     * 
     * @param name parameter key
     * @return parameter values
     */
    public String[] getParam(String name) {
        return getParams().get(name);
    }

    /**
     * Sets the value for the specified parameter. Note this the parameter is
     * still multi-valued, this is convenience method to wrap the value as an
     * array with one item.
     * 
     * @param name parameter key
     * @param value parameter value
     */
    public void setParam(String name, String value) {
        getParams().put(name, new String[] { encode(value) });
    }

    /**
     * Sets the values for the specified parameter
     * 
     * @param name parameter key
     * @param values parameter values
     */
    public void setParam(String name, String[] values) {
        List<String> encodedValues = new ArrayList<String>();
        for (String value : values) {
            if (value != null) {
                encodedValues.add(encode(value));
            }
        }
        getParams().put(name, encodedValues.toArray(new String[] {}));
    }

    /**
     * Gets all the parameters
     * 
     * @return parameter map
     */
    public Map<String, String[]> getParams() {
        if (params == null) {
            params = new HashMap<String, String[]>();
        }
        return params;
    }

    /**
     * Gets the URL encoded version of a string
     * 
     * @param value a string to encode
     * @return URL encoded string
     */
    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return value;
    }
}
