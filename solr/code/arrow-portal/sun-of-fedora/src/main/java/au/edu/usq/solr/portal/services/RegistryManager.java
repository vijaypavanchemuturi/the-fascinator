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
package au.edu.usq.solr.portal.services;

import java.io.InputStream;
import java.util.List;

import au.edu.usq.solr.fedora.DatastreamType;
import au.edu.usq.solr.model.DublinCore;

public interface RegistryManager {

    public DublinCore getMetadata(String uuid);

    public DatastreamType getDatastream(String uuid, String dsId);

    public List<DatastreamType> getDatastreams(String uuid);

    public InputStream getDatastreamAsStream(String uuid, String dsId);

    public String getDatastreamAsString(String uuid, String dsId);

}
