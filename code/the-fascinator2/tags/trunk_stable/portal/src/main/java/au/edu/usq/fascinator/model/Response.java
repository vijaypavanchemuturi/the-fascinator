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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import au.edu.usq.fascinator.model.types.ListType;
import au.edu.usq.fascinator.model.types.ResultType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Response {

    @XmlElement(name = "lst")
    private List<ListType> lists;

    private Map<String, ListType> listMap;

    @XmlElement
    private ResultType result;

    private Set<FacetList> facetLists;

    public Response() {
    }

    public double getQueryTime() {
        return Long.parseLong(getListValue("responseHeader", "QTime")) / 1000.0;
    }

    private String getListValue(String listName, String valueName) {
        return getList(listName).getValue(valueName);
    }

    private ListType getList(String name) {
        if (listMap == null) {
            listMap = new HashMap<String, ListType>();
            for (ListType list : lists) {
                listMap.put(list.getName(), list);
            }
        }
        return listMap.get(name);
    }

    public ResultType getResult() {
        return result;
    }

    public Set<FacetList> getFacetLists() {
        if (facetLists == null) {
            facetLists = new TreeSet<FacetList>();
            ListType fields = getList("facet_counts").getList("facet_fields");
            for (ListType list : fields.getLists()) {
                facetLists.add(new FacetList(list.getName(), list.getValues()));
            }
        }
        return facetLists;
    }
}
