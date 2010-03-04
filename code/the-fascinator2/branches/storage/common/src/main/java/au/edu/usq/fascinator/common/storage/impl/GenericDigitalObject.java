/* 
 * The Fascinator - Common Library
 * Copyright (C) 2008 University of Southern Queensland
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
package au.edu.usq.fascinator.common.storage.impl;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.storage.StorageException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic DigitalObject implementation
 * 
 * @author Oliver Lucido
 */
public class GenericDigitalObject implements DigitalObject {

    private static Logger log = LoggerFactory
            .getLogger(GenericDigitalObject.class);

    /** Unique identifier */
    private String id;

    private String sourceId;

    private Map<String, Payload> manifest;

    /**
     * Creates a DigitalObject with the specified identifier and no metadata
     * 
     * @param id unique identifier
     */
    public GenericDigitalObject(String id) {
        setId(id);
        manifest = new HashMap<String, Payload>();
    }

    public void setInManifest(String key, Payload value) {
        manifest.put(key, value);
    }

    public Payload getFromManifest(String key) {
        if (manifest.containsKey(key)) {
            return manifest.get(key);
        } else {
            return null;
        }
    }

    public Set<String> getManifestKeys() {
        return manifest.keySet();
    }

    public void removeFromManifest(String key) {
        manifest.remove(key);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String oid) {
        // TODO : #554 Unique ID generation.
        // Stop assuming IDs are file paths
        this.id = oid.replace("\\", "/");
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    @Override
    public void setSourceId(String pid) {
        this.sourceId = pid;
    }

    @Override
    public Set<String> getPayloadIdList() {
        return this.getManifestKeys();
    }

    @Override
    public Payload createStoredPayload(String pid) throws StorageException {
        Payload payload = this.createPayload(pid);
        payload.setType(PayloadType.Data);
        return payload;
    }

    @Override
    public Payload createLinkedPayload(String pid) throws StorageException {
        Payload payload = this.createPayload(pid);
        payload.setType(PayloadType.External);
        return payload;
    }

    private Payload createPayload(String pid) throws StorageException {
        if (this.getFromManifest(pid) != null) {
            throw new StorageException("ID '" + pid + "' already exists.");
        }
        GenericPayload payload = new GenericPayload(pid);
        this.setInManifest(pid, payload);
        return payload;
    }

    @Override
    public Payload getPayload(String pid) throws StorageException {
        Payload p = this.getFromManifest(pid);
        if (p != null) {
            return p;
        } else {
            throw new StorageException("ID '" + pid + "' does not exist.");
        }
    }

    @Override
    public void removePayload(String pid) throws StorageException {
        Payload p = this.getFromManifest(pid);
        if (p != null) {
            // Close the payload just in case,
            //  since we are about to orphan it
            p.close();
            this.removeFromManifest(pid);
        } else {
            throw new StorageException("ID '" + pid + "' does not exist.");
        }
    }

    @Override
    public void close() throws StorageException {
        // By default do nothing
    }

    @Override
    public String toString() {
        return getId();
    }
}
