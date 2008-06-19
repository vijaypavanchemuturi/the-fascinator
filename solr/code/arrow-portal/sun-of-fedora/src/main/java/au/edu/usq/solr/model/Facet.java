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
package au.edu.usq.solr.model;

import au.edu.usq.solr.util.MapEntryType;

public class Facet {

    private String name;

    private String value;

    private int count;

    public Facet(String name, MapEntryType entry) {
        this.name = name;
        this.value = entry.getName();
        this.count = Integer.parseInt(entry.getValue());
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return getName() + ":\"" + getValue() + "\"";
    }
}
