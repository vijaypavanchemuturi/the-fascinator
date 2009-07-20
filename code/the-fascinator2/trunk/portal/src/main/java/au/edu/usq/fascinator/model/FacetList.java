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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import au.edu.usq.fascinator.common.jaxb.MapEntryType;

public class FacetList implements Comparable<FacetList> {

    private Logger log = Logger.getLogger(FacetList.class);

    private String name;

    private List<MapEntryType> entries;

    private List<Facet> facets;

    private Map<String, Facet> facetMap;

    public FacetList(String name, List<MapEntryType> entries) {
        this.name = name;
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public List<Facet> getFacets() {
        if (facets == null) {
            facetMap = new HashMap<String, Facet>();
            facets = new ArrayList<Facet>();
            for (MapEntryType entry : entries) {
                Facet facet = new Facet(name, entry);
                facetMap.put(facet.getValue(), facet);
                List<String> tokens = facet.getTokens();
                if (tokens.isEmpty()) {
                    facets.add(facet);
                } else {
                    String parents = "";
                    for (String token : tokens) {
                        parents += token + Facet.DEFAULT_DELIMITER;
                    }
                    parents = parents.substring(0, parents.length() - 1);
                    Facet parent = facetMap.get(parents);
                    if (parent != null) {
                        parent.addSubFacet(facet);
                    }
                }
            }
        }
        return facets;
    }

    public int compareTo(FacetList that) {
        if ("repository_name".equals(name)) {
            return -1;
        }
        if ("repository_name".equals(that.getName())) {
            return 1;
        }
        return name.compareTo(that.getName());
    }

    public Map<String, Facet> getFacetMap() {
        getFacets();
        return facetMap;
    }

    public Facet findFacet(String facetValue) {
        return getFacetMap().get(facetValue);
    }
}
