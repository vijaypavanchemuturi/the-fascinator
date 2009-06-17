/* 
 * The Fascinator - Plugin API
 * Copyright (C) 2009 University of Southern Queensland
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
package au.edu.usq.fascinator.api;

/**
 * High level storage layer API
 * 
 * @author Oliver Lucido
 */
public interface Storage extends Plugin {

    /**
     * Adds a digital object to the back end storage
     * 
     * @param object the object to add
     * @return the internal identifier used by the storage implementation
     * @throws StorageException if there was an error adding the object
     */
    public String addObject(DigitalObject object) throws StorageException;

    public void removeObject(String oid);

    public void addPayload(String oid, Payload payload);

    public void removePayload(String oid, String pid);

    public DigitalObject getObject(String oid);

    public Payload getPayload(String oid, String pid);

}
