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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "roles")
@XmlAccessorType(XmlAccessType.NONE)
public class RoleList implements Serializable {

    @XmlElement(name = "role")
    private List<Role> roles;

    private Map<String, Role> roleMap;

    public RoleList() {
    }

    public List<Role> getRoles() {
        return roles;
    }

    public Role getRole(String name) {
        if (roleMap != null && !roleMap.containsKey(name)) {
            roleMap = null;
        }
        if (roleMap == null) {
            roleMap = new HashMap<String, Role>();
            for (Role role : roles) {
                roleMap.put(role.getId(), role);
            }
        }
        return roleMap.get(name);
    }
}
