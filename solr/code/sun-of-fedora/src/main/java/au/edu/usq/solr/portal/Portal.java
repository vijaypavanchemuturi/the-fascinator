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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Portal implements Comparable<Portal> {

    private static Portal defaultPortal = null;

    private String name;

    private String query;

    public static Portal getDefaultPortal() {
        if (defaultPortal == null) {
            defaultPortal = new Portal("all", "");
        }
        return defaultPortal;
    }

    public Portal(String name, String query) {
        this.name = name;
        this.query = query;
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

    public String getEncodedQuery() throws UnsupportedEncodingException {
        return URLEncoder.encode(query, "UTF8");
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof Portal) {
            return name.equals(((Portal) that).getName());
        }
        return false;
    }

    public int compareTo(Portal that) {
        return name.compareTo(that.getName());
    }
}
