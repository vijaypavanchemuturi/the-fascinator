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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;

@XmlAccessorType(XmlAccessType.NONE)
public class Role implements Serializable {

    @XmlAttribute
    private String id;

    @XmlElement
    @XmlList
    private List<String> users;

    @XmlElement
    private String query;

    public Role() {
    }

    public Role(String id) {
        this.id = id;
        users = new ArrayList<String>();
    }

    public String getId() {
        return id;
    }

    public void setId(String name) {
        this.id = name;
    }

    public List<String> getUserList() {
        return users;
    }

    public String getUsers() {
        String userString = "";
        for (String user : users) {
            userString += user + " ";
        }
        return userString;
    }

    public void setUsers(String userString) {
        users.clear();
        if (userString != null && !"".equals(userString)) {
            StringTokenizer st = new StringTokenizer(userString, " ");
            while (st.hasMoreElements()) {
                users.add(st.nextElement().toString());
            }
        }
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
