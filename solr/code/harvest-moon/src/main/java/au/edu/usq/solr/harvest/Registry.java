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
package au.edu.usq.solr.harvest;

import java.io.InputStream;
import java.util.Map;

/**
 * Generic registry interface used for harvesting.
 * 
 * @author Oliver Lucido
 */
public interface Registry {
    /**
     * Setup a connection to the registry.
     * 
     * @throws RegistryException If a connection could not be established.
     */
    public void connect() throws RegistryException;

    /**
     * Creates a new digital object in the registry and returns it's identifier.
     * 
     * @param comment Optional log comment (can be null).
     * @return The new object identifier.
     * @throws RegistryException If the object could not be created.
     */
    public String createObject(Map<String, String> options)
        throws RegistryException;

    /**
     * Adds a datastream to an existing digital object in the registry.
     * 
     * @param pid The container object identifier.
     * @param dsId The datastream identifier.
     * @param data The datastream contents.
     * @param options Key-Value map for implementation specific options (if
     * necessary).
     * @throws RegistryException If the datastream could not be added.
     */
    public void addDatastream(String pid, String dsId, InputStream data,
        Map<String, String> options) throws RegistryException;
}
