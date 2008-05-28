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
package au.edu.usq.solr.model;

import org.w3c.dom.Element;

public class Facet {

    private FacetList facetList;

    private String value;

    private int count;

    public Facet(FacetList facetList, Element elem) {
        this(facetList, elem.getAttribute("name"),
            Integer.parseInt(elem.getTextContent()));
    }

    public Facet(FacetList facetList, String value, int count) {
        this.facetList = facetList;
        this.value = value;
        this.count = count;
    }

    public String getName() {
        return facetList.getName();
    }

    public String getValue() {
        return value;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return getName() + ":\"" + getValue() + "\"";
    }
}
