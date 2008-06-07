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
package au.edu.usq.solr.portal.pages.portal;

import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.ioc.annotations.Inject;

import au.edu.usq.solr.portal.Portal;
import au.edu.usq.solr.portal.State;
import au.edu.usq.solr.portal.pages.Start;
import au.edu.usq.solr.portal.services.PortalManager;

public class Edit {

    @ApplicationState
    private State state;

    @Inject
    private PortalManager portalManager;

    private String portalName;

    private Portal portal;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    Object onActivate(Object[] params) {
        if (params.length > 0) {
            portalName = params[0].toString();
            portal = portalManager.get(portalName);
        }
        return portal == null ? Start.class : null;
    }

    String onPassivate() {
        return portalName;
    }

    Object onSuccess() {
        portalManager.save(portalName);
        return Start.class;
    }

    public String getPortalName() {
        return portalName;
    }

    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

    public Portal getPortal() {
        return portal;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }
}
