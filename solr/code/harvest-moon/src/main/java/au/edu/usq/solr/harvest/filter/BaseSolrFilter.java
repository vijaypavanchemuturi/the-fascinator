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
package au.edu.usq.solr.harvest.filter;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class BaseSolrFilter implements SolrFilter {

    private String name;

    private boolean stopOnFailure;

    public BaseSolrFilter(String name) {
        this(name, false);
    }

    public BaseSolrFilter(String name, boolean stopOnFailure) {
        this.name = name;
        this.stopOnFailure = stopOnFailure;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getStopOnFailure() {
        return stopOnFailure;
    }

    public void setStopOnFailure(boolean stopOnFailure) {
        this.stopOnFailure = stopOnFailure;
    }

    @Override
    public String toString() {
        return getName();
    }

    public abstract void filter(InputStream in, OutputStream out)
        throws FilterException;
}
