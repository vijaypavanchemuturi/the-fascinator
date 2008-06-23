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

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.tapestry.services.Context;

import au.edu.usq.solr.portal.services.PortalManager;
import au.edu.usq.solr.portal.services.RoleManager;

public class State {

    private static final String DEFAULT_RESOURCE = "/WEB-INF/config.properties";

    private static final String SOLR_BASE_URL_KEY = "solr.base.url";

    private static final String SOLR_BASE_URL = "http://localhost:8080/solr";

    private static final String USERNAME_KEY = "user.name";

    private Logger log = Logger.getLogger(State.class);

    private RoleManager roleManager;

    private Properties props;

    private Portal portal;

    private List<Role> userRoles;

    public State(Context context, RoleManager roleManager) {
        this.roleManager = roleManager;
        props = new Properties();
        try {
            URL configProps = context.getResource(DEFAULT_RESOURCE);
            InputStream configIn = configProps.openStream();
            props.load(configIn);
            configIn.close();
        } catch (Exception e) {
            log.warn("Failed to load defaults", e);
            setSolrBaseUrl(SOLR_BASE_URL);
        }
    }

    public void setSolrBaseUrl(String solrBaseUrl) {
        setProperty(SOLR_BASE_URL_KEY, solrBaseUrl);
    }

    public String getSolrBaseUrl() {
        return getProperty(SOLR_BASE_URL_KEY);
    }

    public String getPortalName() {
        if (portal == null) {
            return PortalManager.DEFAULT_PORTAL_NAME;
        }
        return portal.getName();
    }

    public Portal getPortal() {
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
        if (userRoles == null) {
            userRoles = roleManager.getUserRoles(RoleManager.GUEST_ROLE);
        }
        return userRoles;
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
        setUserRoles(roleManager.getUserRoles(username));
    }

    public void logout() {
        props.remove(USERNAME_KEY);
        userRoles = null;
    }

    public boolean isLoggedIn() {
        return props.containsKey(USERNAME_KEY);
    }
}
