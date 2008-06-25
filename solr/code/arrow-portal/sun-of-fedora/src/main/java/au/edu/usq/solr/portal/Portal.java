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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.tapestry.beaneditor.Validate;

import au.edu.usq.solr.portal.services.PortalManager;
import au.edu.usq.solr.util.MapAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Portal implements Comparable<Portal> {

    @XmlAttribute
    private String name;

    @XmlElement
    private String description;

    @XmlElement
    private String query;

    @XmlElement(name = "records-per-page")
    private int recordsPerPage = 10;

    @XmlElement(name = "facet-count")
    private int facetCount = 25;

    @XmlElement(name = "item-class")
    private String itemClass;

    // @XmlElement(name = "security-query")
    // private String securityQuery;

    @XmlElement
    private String network;

    @XmlElement
    private String netmask;

    @XmlElement(name = "facet-fields")
    @XmlJavaTypeAdapter(MapAdapter.class)
    private Map<String, String> facetFields;

    public Portal() {
        this("", "", "");
    }

    public Portal(String name, String query) {
        this(name, name.substring(0, 1).toUpperCase() + name.substring(1),
            query);
    }

    public Portal(String name, String description, String query) {
        this.name = name;
        this.description = description;
        this.query = query;
        facetFields = new HashMap<String, String>();
    }

    @Validate("required")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public int getFacetCount() {
        return facetCount;
    }

    public void setFacetCount(int facetCount) {
        this.facetCount = facetCount;
    }

    public String getItemClass() {
        return itemClass;
    }

    public void setItemClass(String itemClass) {
        this.itemClass = itemClass;
    }

    // public String getSecurityQuery() {
    // return securityQuery;
    // }

    // public void setSecurityQuery(String securityQuery) {
    // this.securityQuery = securityQuery;
    // }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public Map<String, String> getFacetFields() {
        return facetFields;
    }

    public void setFacetFields(Map<String, String> facetFields) {
        this.facetFields = facetFields;
    }

    public List<String> getFacetFieldList() {
        return new ArrayList<String>(facetFields.keySet());
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof Portal) {
            return name.equals(((Portal) that).getName());
        }
        return false;
    }

    public int compareTo(Portal that) {
        if (PortalManager.DEFAULT_PORTAL_NAME.equals(name)) {
            return -1;
        }
        if (PortalManager.DEFAULT_PORTAL_NAME.equals(that.getName())) {
            return 1;
        }
        return description.compareTo(that.getDescription());
    }

    @Override
    public String toString() {
        return getDescription() + " [" + getName() + "]";
    }
}
