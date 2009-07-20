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
package au.edu.usq.fascinator.pages.portal;

import java.util.Set;
import java.util.TreeSet;

import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import au.edu.usq.fascinator.portal.Portal;
import au.edu.usq.fascinator.portal.State;
import au.edu.usq.fascinator.portal.pages.Start;
import au.edu.usq.fascinator.portal.services.PortalManager;

@IncludeStylesheet("context:css/default.css")
public class List {

    @SessionState
    private State state;

    @InjectPage
    private Create createPage;

    @Inject
    private PortalManager portalManager;

    @Persist
    private Portal portal;

    Object onActivate(Object[] params) {
        if (!state.userInRole("admin")) {
            return Start.class;
        }
        return null;
    }

    Object onSuccess() {
        return createPage;
    }

    @OnEvent(component = "delete")
    void onDelete(String portalName) {
        portalManager.remove(portalName);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Portal getPortal() {
        return portal;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public Set<Portal> getPortals() {
        return new TreeSet<Portal>(portalManager.getPortals().values());
    }

    public boolean isEditable() {
        return state.userInRole("admin");
    }

    public boolean isDeletable() {
        return isEditable() && !portal.equals(portalManager.getDefault());
    }
}
