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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

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

    public Map<String, Payload> getManifest() {
        return manifest;
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
        return this.getManifest().keySet();
    }

    @Override
    public Payload createStoredPayload(String pid, InputStream in)
            throws StorageException {
        GenericPayload payload = this.createPayload(pid, PayloadType.Data);
        payload.setInputStream(in);
        return payload;
    }

    @Override
    public Payload createLinkedPayload(String pid, String linkPath)
            throws StorageException {
        GenericPayload payload = this.createPayload(pid, PayloadType.External);
        try {
            payload.setInputStream(
                    new ByteArrayInputStream(linkPath.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            throw new StorageException(ex);
        }
        return payload;
    }

    private GenericPayload createPayload(String pid, PayloadType defaultType) throws StorageException {
        Map<String, Payload> man = this.getManifest();
        if (man.containsKey(pid)) {
            throw new StorageException("ID '" + pid + "' already exists.");
        }

        GenericPayload payload = new GenericPayload(pid);
        if (this.getSourceId() == null) {
            payload.setType(defaultType);
            this.setSourceId(pid);
        } else {
            payload.setType(PayloadType.Enrichment);
        }

        man.put(pid, payload);
        return payload;
    }

    @Override
    public Payload getPayload(String pid) throws StorageException {
        Map<String, Payload> man = this.getManifest();
        if (man.containsKey(pid)) {
            return man.get(pid);
        } else {
            throw new StorageException("ID '" + pid + "' does not exist.");
        }
    }

    @Override
    public void removePayload(String pid) throws StorageException {
        Map<String, Payload> man = this.getManifest();
        if (man.containsKey(pid)) {
            // Close the payload just in case,
            //  since we are about to orphan it
            man.get(pid).close();
            man.remove(pid);
        } else {
            throw new StorageException("ID '" + pid + "' does not exist.");
        }
    }

    @Override
    public Payload updatePayload(String pid, InputStream in)
            throws StorageException {
        GenericPayload payload = (GenericPayload) this.getPayload(pid);
        payload.setInputStream(in);
        return payload;
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
