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
import java.util.TreeSet;

import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.ioc.annotations.Inject;

import au.edu.usq.solr.portal.Portal;
import au.edu.usq.solr.portal.State;
import au.edu.usq.solr.portal.services.PortalManager;

@IncludeStylesheet("context:css/default.css")
public class Start {

    @ApplicationState
    private State state;

    @Inject
    private PortalManager portalManager;

    @InjectPage
    private Search searchPage;

    private Portal portal;

    private String query;

    @OnEvent(component = "searchForm", value = "submit")
    Object onSubmit() {
        searchPage.setQuery(query);
        return searchPage;
    }

    public State getState() {
        return state;
    }

    public Portal getPortal() {
        return portal;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Set<Portal> getPortals() {
        return new TreeSet<Portal>(portalManager.getPortals().values());
    }

    public boolean isEditable() {
        return !portal.equals(portalManager.getDefault());
    }
}
