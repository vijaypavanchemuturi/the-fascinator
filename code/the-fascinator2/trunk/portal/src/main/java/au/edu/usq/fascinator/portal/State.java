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
package au.edu.usq.fascinator.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.services.Context;
import org.dom4j.DocumentFactory;

import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.portal.services.PortalManager;
import au.edu.usq.fascinator.portal.services.RoleManager;

public class State {

    private static final String DEFAULT_RESOURCE = "/WEB-INF/config.properties";

    private static final String SOLR_BASE_URL_KEY = "solr.base.url";

    private static final String SOLR_BASE_URL = "http://localhost:8080/solr";

    private static final String USERNAME_KEY = "user.name";

    private Logger log = Logger.getLogger(State.class);

    private PortalManager portalManager;

    private RoleManager roleManager;

    private Properties props;

    private JsonConfig config;

    private Portal portal;

    private List<Role> userRoles;

    @Persist
    private HashMap<String, String> nsMap;

    public void setNamespaceUri(String prefix, String uri) {
        nsMap.put(prefix, uri);
    }

    public State(Context context, PortalManager portalManager,
        RoleManager roleManager) {
        this.portalManager = portalManager;
        this.roleManager = roleManager;
        props = new Properties();
        try {
            config = new JsonConfig();
        } catch (Exception e) {
            log.warn("Failed to load config", e);
        }
        nsMap = new HashMap<String, String>();
        nsMap.put("dc", "http://purl.org/dc/elements/1.1/");
        nsMap.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        nsMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        DocumentFactory.getInstance().setXPathNamespaceURIs(nsMap);
    }

    public String getPortalName() {
        if (portal == null) {
            return PortalManager.DEFAULT_PORTAL_NAME;
        }
        return portal.getName();
    }

    public Portal getPortal() {
        if (portal == null) {
            portal = portalManager.getDefault();
        }
        return portal;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public String getUserName() {
        return getProperty(USERNAME_KEY);
    }

    public void setUserName(String username) {
        setProperty(USERNAME_KEY, username);
    }

    public List<Role> getUserRoles() {
        String username = getUserName();
        if (username == null) {
            username = RoleManager.GUEST_ROLE;
        }
        return roleManager.getUserRoles(username, getPortal());
    }

    public void setUserRoles(List<Role> userRoles) {
        this.userRoles = userRoles;
    }

    public boolean userInRole(String roleName) {
        for (Role role : getUserRoles()) {
            if (roleName.equals(role.getId())) {
                return true;
            }
        }
        return false;
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public void remove(String key) {
        props.remove(key);
    }

    public void login(String username) {
        setUserName(username);
        setUserRoles(roleManager.getUserRoles(username, getPortal()));
    }

    public void logout() {
        props.remove(USERNAME_KEY);
        userRoles = null;
    }

    public boolean isLoggedIn() {
        return props.containsKey(USERNAME_KEY);
    }

    public Properties getProperties() {
        return props;
    }

    public String getSolrBaseUrl() {
        return config.get("indexer/solr/uri", "http://localhost:8080/solr");
    }

}
