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
package au.edu.usq.fascinator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import au.edu.usq.fascinator.common.jaxb.MapEntryType;

public class Facet {

    public static final String DEFAULT_DELIMITER = "/";

    private Logger log = Logger.getLogger(Facet.class);

    private String name;

    private String value;

    private int count;

    private boolean selected;

    private String userData;

    private List<Facet> subFacets;

    public Facet(String name, MapEntryType entry) {
        this.name = name;
        this.value = entry.getName();
        if ("".equals(value)) {
            value = "[undefined]";
        }
        this.count = Integer.parseInt(entry.getValue());
        this.selected = false;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getCount() {
        return count;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getTokenValue() {
        return getTokenValue(DEFAULT_DELIMITER);
    }

    public String getTokenValue(String delim) {
        List<String> tokens = getTokens(delim, true);
        return tokens.get(tokens.size() - 1);
    }

    public List<String> getTokens() {
        return getTokens(DEFAULT_DELIMITER);
    }

    public List<String> getTokens(String delim) {
        return getTokens(delim, false);
    }

    public List<String> getTokens(String delim, boolean keepLast) {
        List<String> parents = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(value, delim);
        while (st.hasMoreTokens()) {
            parents.add(st.nextToken());
        }
        if (!keepLast) {
            parents.remove(parents.size() - 1);
        }
        return parents;
    }

    public List<Facet> getSubFacets() {
        if (subFacets == null) {
            subFacets = new ArrayList<Facet>();
        }
        return subFacets;
    }

    public void addSubFacet(Facet facet) {
        getSubFacets().add(facet);
    }

    @Override
    public String toString() {
        return getName() + ":\"" + getValue() + "\"";
    }

    public String toStringEncoded() {
        return getName() + ":%22" + getValue() + "%22";
    }
}
