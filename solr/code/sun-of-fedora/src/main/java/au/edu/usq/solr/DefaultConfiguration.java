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

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import au.edu.usq.solr.portal.Portal;
import au.edu.usq.solr.portal.PortalManager;

public class DefaultConfiguration implements Configuration {

    private static final String DEFAULT_RESOURCE = "/config.properties";

    private static final String SOLR_BASE_URL_KEY = "solr.base.url";

    private static final String SOLR_BASE_URL = "http://localhost:8983/solr";

    private static final String REGISTRY_BASE_URL_KEY = "registry.base.url";

    private static final String REGISTRY_USER_KEY = "registry.user";

    private Logger log = Logger.getLogger(DefaultConfiguration.class);

    private Properties props;

    private PortalManager portalManager;

    private Portal currentPortal;

    public DefaultConfiguration() {
        loadDefaults();
    }

    private void loadDefaults() {
        props = new Properties();
        try {
            props.load(getClass().getResourceAsStream(DEFAULT_RESOURCE));
        } catch (IOException e) {
            log.warn("Failed to load defaults", e);
            setSolrBaseUrl(SOLR_BASE_URL);
        }
    }

    public PortalManager getPortalManager() {
        if (portalManager == null) {
            portalManager = new PortalManager();
        }
        return portalManager;
    }

    public void setSolrBaseUrl(String solrBaseUrl) {
        setProperty(SOLR_BASE_URL_KEY, solrBaseUrl);
    }

    public String getSolrBaseUrl() {
        return getProperty(SOLR_BASE_URL_KEY);
    }

    public Portal getCurrentPortal() {
        if (currentPortal == null) {
            currentPortal = getPortalManager().getDefaultPortal();
        }
        return currentPortal;
    }

    public void setCurrentPortal(Portal currentPortal) {
        this.currentPortal = currentPortal;
    }

    public Set<Portal> getPortals() {
        return new TreeSet<Portal>(getPortalManager().getPortals().values());
    }

    public void setRegistryBaseUrl(String registryBaseUrl) {
        setProperty(REGISTRY_BASE_URL_KEY, registryBaseUrl);
    }

    public String getRegistryBaseUrl() {
        return getProperty(REGISTRY_BASE_URL_KEY);
    }

    public void setRegistryUser(String registryUser) {
        setProperty(REGISTRY_USER_KEY, registryUser);
    }

    public String getRegistryUser() {
        return getProperty(REGISTRY_USER_KEY);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }
}
