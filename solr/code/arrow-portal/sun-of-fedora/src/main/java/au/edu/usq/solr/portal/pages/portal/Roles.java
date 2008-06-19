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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tapestry.PrimaryKeyEncoder;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.ioc.annotations.Inject;

import au.edu.usq.solr.portal.Portal;
import au.edu.usq.solr.portal.Role;
import au.edu.usq.solr.portal.State;
import au.edu.usq.solr.portal.pages.Start;
import au.edu.usq.solr.portal.services.PortalManager;

@IncludeStylesheet("context:css/default.css")
public class Roles {

    private Logger log = Logger.getLogger(Roles.class);

    @ApplicationState
    private State state;

    @Inject
    private PortalManager portalManager;

    private String portalName;

    private Portal portal;

    private Role role;

    private Role newRole;

    private PrimaryKeyEncoder<String, Role> encoder = new PrimaryKeyEncoder<String, Role>() {
        public String toKey(Role value) {
            return value.getId();
        }

        public void prepareForKeys(List<String> keys) {
        }

        public Role toValue(String key) {
            return portal.getRole(key);
        }
    };

    public PrimaryKeyEncoder<String, Role> getEncoder() {
        return encoder;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    Object onActivate(Object[] params) {
        if (!"admin".equals(state.getProperty("role"))) {
            return Start.class;
        }
        if (params.length > 0) {
            portalName = params[0].toString();
            portal = portalManager.get(portalName);
        }
        return portal == null ? Start.class : null;
    }

    String onPassivate() {
        return portalName;
    }

    void onSuccessFromEditForm() {
        portalManager.save(portal);
    }

    void onSuccessFromNewForm() {
        portal.getRoles().add(newRole);
    }

    @OnEvent(component = "delete")
    void onDelete(String id) {
        Role roleToDelete = portal.getRole(id);
        if (roleToDelete != null) {
            log.info("Deleting " + roleToDelete.getId());
            portal.getRoles().remove(roleToDelete);
            portalManager.save(portal);
        }
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Role getNewRole() {
        if (newRole == null) {
            newRole = new Role("");
        }
        return newRole;
    }

    public void setNewRole(Role newRole) {
        this.newRole = newRole;
    }
}
