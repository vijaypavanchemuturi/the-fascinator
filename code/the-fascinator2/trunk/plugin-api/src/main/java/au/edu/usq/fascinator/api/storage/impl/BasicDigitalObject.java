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
package au.edu.usq.fascinator.api.storage.impl;

import java.util.ArrayList;
import java.util.List;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;

/**
 * Generic DigitalObject implementation.
 * 
 * @author Oliver Lucido
 */
public class BasicDigitalObject implements DigitalObject {

    private String id;

    private String metaId;

    private List<Payload> payloadList;

    public BasicDigitalObject(String id) {
        this(id, null);
    }

    public BasicDigitalObject(String id, String metaId) {
        setId(id);
        setMetaId(metaId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Payload getMetadata() {
        return getPayload(metaId);
    }

    public void setMetaId(String metaId) {
        this.metaId = metaId;
    }

    public Payload getPayload(String pid) {
        for (Payload payload : getPayloadList()) {
            if (pid.equals(payload.getId())) {
                return payload;
            }
        }
        return null;
    }

    public void addPayload(Payload payload) {
        getPayloadList().add(payload);
    }

    public List<Payload> getPayloadList() {
        if (payloadList == null) {
            payloadList = new ArrayList<Payload>();
        }
        return payloadList;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %d)", getId(), metaId, getPayloadList()
                .size());
    }

}
