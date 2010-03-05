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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.io.IOUtils;

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

    private static String METADATA_LABEL = "The Fascinator Indexer Metadata";
    private static String METADATA_PAYLOAD = "TF-OBJ-META";
    private Map<String, Payload> manifest;
    private Properties metadata;
    private String id;
    private String sourceId;


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
    public Properties getMetadata() throws StorageException {
        if (metadata == null) {
            Map<String, Payload> man = this.getManifest();
            if (!man.containsKey(METADATA_PAYLOAD)) {
                Payload payload = this.createStoredPayload(
                        METADATA_PAYLOAD, IOUtils.toInputStream(""));
                if (this.getSourceId().equals(METADATA_PAYLOAD)) {
                    this.setSourceId(null);
                }
                payload.setType(PayloadType.Annotation);
                payload.setLabel(METADATA_LABEL);
            }

            try {
                Payload metaPayload = man.get(METADATA_PAYLOAD);
                metadata = new Properties();
                metadata.load(metaPayload.open());
                metaPayload.close();
            } catch (IOException ex) {
                throw new StorageException(ex);
            }
        }
        return metadata;
    }

    @Override
    public Set<String> getPayloadIdList() {
        return this.getManifest().keySet();
    }

    @Override
    public Payload createStoredPayload(String pid, InputStream in)
            throws StorageException {
        GenericPayload payload = this.createPayload(pid, false);
        payload.setInputStream(in);
        return payload;
    }

    @Override
    public Payload createLinkedPayload(String pid, String linkPath)
            throws StorageException {
        GenericPayload payload = this.createPayload(pid, true);
        try {
            payload.setInputStream(
                    new ByteArrayInputStream(linkPath.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            throw new StorageException(ex);
        }
        return payload;
    }

    private GenericPayload createPayload(String pid, boolean linked) throws StorageException {
        Map<String, Payload> man = this.getManifest();
        if (man.containsKey(pid)) {
            throw new StorageException("ID '" + pid + "' already exists.");
        }

        GenericPayload payload = new GenericPayload(pid);
        if (this.getSourceId() == null) {
            payload.setType(PayloadType.Source);
            this.setSourceId(pid);
        } else {
            payload.setType(PayloadType.Enrichment);
        }
        payload.setLinked(linked);

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
        if (metadata != null) {
            Map<String, Payload> man = this.getManifest();
            if (!man.containsKey(METADATA_PAYLOAD)) {
                throw new StorageException("Metadata payload not found");
            }
            try {
                ByteArrayOutputStream metaOut = new ByteArrayOutputStream();
                metadata.store(metaOut, METADATA_LABEL);
                this.updatePayload(METADATA_PAYLOAD,
                        new ByteArrayInputStream(metaOut.toByteArray()));
            } catch (IOException ex) {
                throw new StorageException(ex);
            }
        }
    }

    @Override
    public String toString() {
        return getId();
    }
}
