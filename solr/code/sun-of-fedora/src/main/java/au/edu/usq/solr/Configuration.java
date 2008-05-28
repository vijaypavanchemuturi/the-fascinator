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
package au.edu.usq.solr;

import java.util.List;
import java.util.Set;

import au.edu.usq.solr.portal.Portal;

public interface Configuration {

    public void setSolrBaseUrl(String solrBaseUrl);

    public String getSolrBaseUrl();

    public void setRecordsPerPage(int recordsPerPage);

    public int getRecordsPerPage();

    public void setFacetFields(String facetFields);

    public String getFacetFields();

    public List<String> getFacetFieldList();

    public void setFacetCount(int facetCount);

    public int getFacetCount();

    public void setRegistryBaseUrl(String registryBaseUrl);

    public String getRegistryBaseUrl();

    public void setRegistryUser(String registryUser);

    public String getRegistryUser();

    public Set<Portal> getPortals();

    public void setPortals(Set<Portal> portals);
}
