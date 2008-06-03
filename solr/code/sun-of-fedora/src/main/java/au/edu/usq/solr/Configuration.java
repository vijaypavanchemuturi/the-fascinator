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

import java.util.Set;

import au.edu.usq.solr.portal.Portal;
import au.edu.usq.solr.portal.PortalManager;

public interface Configuration {

    public PortalManager getPortalManager();

    public void setSolrBaseUrl(String solrBaseUrl);

    public String getSolrBaseUrl();

    public void setCurrentPortal(Portal portal);

    public Portal getCurrentPortal();

    public Set<Portal> getPortals();

    public void setRegistryBaseUrl(String registryBaseUrl);

    public String getRegistryBaseUrl();

    public void setRegistryUser(String registryUser);

    public String getRegistryUser();

    public String getProperty(String key);

    public void setProperty(String key, String value);
}
