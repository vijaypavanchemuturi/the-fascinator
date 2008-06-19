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
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.ioc.annotations.Inject;

import au.edu.usq.solr.portal.Portal;
import au.edu.usq.solr.portal.State;
import au.edu.usq.solr.portal.pages.Start;
import au.edu.usq.solr.portal.services.PortalManager;

@IncludeStylesheet("context:css/default.css")
public class Create {

    @ApplicationState
    private State state;

    @InjectPage
    private Edit editPage;

    @Inject
    private PortalManager portalManager;

    @Persist
    private Portal portal;

    Object onActivate(Object[] params) {
        if (!"admin".equals(state.getProperty("role"))) {
            return Start.class;
        }
        if (params.length == 0) {
            if (portal == null) {
                portal = new Portal("", "", "");
            }
        } else if (params.length == 1) {
            String portalName = params[0].toString();
            editPage.setPortalName(portalName);
            return editPage;
        }
        return null;
    }

    Object onSuccess() {
        portalManager.add(portal);
        portalManager.save(portal);
        portal = null;
        return Start.class;
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
}
