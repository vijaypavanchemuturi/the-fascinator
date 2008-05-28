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
package au.edu.usq.solr.portal.pages;

import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.tapestry.Asset;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.ioc.annotations.Inject;

import au.edu.usq.solr.Configuration;
import au.edu.usq.solr.portal.Portal;

public class Index {

    private Logger log = Logger.getLogger(Index.class);

    @Inject
    @Path(value = "context:css/default.css")
    private Asset stylesheet;

    @ApplicationState
    private Configuration config;

    private Portal portal;

    private String name;

    private String query;

    String onActivate(String portalName) {
        log.info("set portal to " + portalName);
        return "search";
    }

    void onSubmit() {
        if (name != null && query != null) {
            Set<Portal> portals = config.getPortals();
            Portal newPortal = new Portal(name, query);
            if (portals.contains(newPortal)) {
                portals.remove(newPortal);
            }
            portals.add(newPortal);
        }
        log.info(String.format("name=%s,query=%s", name, query));
    }

    public Asset getStylesheet() {
        return stylesheet;
    }

    public void setStylesheet(Asset stylesheet) {
        this.stylesheet = stylesheet;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public Portal getPortal() {
        return portal;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
