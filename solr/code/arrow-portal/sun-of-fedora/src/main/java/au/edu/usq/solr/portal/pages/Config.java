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

import java.util.List;

import org.apache.tapestry.PrimaryKeyEncoder;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.ioc.annotations.Inject;

import au.edu.usq.solr.portal.Role;
import au.edu.usq.solr.portal.State;
import au.edu.usq.solr.portal.services.RoleManager;

@IncludeStylesheet("context:css/default.css")
public class Config {

    @ApplicationState
    private State state;

    @Inject
    private RoleManager roleManager;

    private Role role;

    private Role newRole;

    private PrimaryKeyEncoder<String, Role> encoder = new PrimaryKeyEncoder<String, Role>() {
        public String toKey(Role value) {
            return value.getId();
        }

        public void prepareForKeys(List<String> keys) {
        }

        public Role toValue(String key) {
            return roleManager.get(key);
        }
    };

    public PrimaryKeyEncoder<String, Role> getEncoder() {
        return encoder;
    }

    Object onActivate(Object[] params) {
        if (!state.userInRole("admin")) {
            return Start.class;
        }
        return null;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<Role> getRoles() {
        return roleManager.getList().getRoles();
    }

    void onSuccessFromEditForm() {
        roleManager.save();
    }

    void onSuccessFromNewForm() {
        getRoles().add(newRole);
    }

    @OnEvent(component = "delete")
    void onDelete(String id) {
        Role roleToDelete = roleManager.get(id);
        if (roleToDelete != null) {
            getRoles().remove(roleToDelete);
            roleManager.save();
        }
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

    public boolean isShowUsersField() {
        String roleId = getRole().getId();
        return !(RoleManager.GUEST_ROLE.equals(roleId) || RoleManager.ON_CAMPUS_ROLE.equals(roleId));
    }

    public boolean isDeletable() {
        return isShowUsersField()
            && !RoleManager.ADMIN_ROLE.equals(getRole().getId());
    }
}
