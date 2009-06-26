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
package au.edu.usq.fascinator.api.impl;

import java.util.ArrayList;
import java.util.List;

import au.edu.usq.fascinator.api.store.DigitalObject;
import au.edu.usq.fascinator.api.store.Payload;

public class BasicDigitalObject implements DigitalObject {

    private String id;

    private List<Payload> payloadList;

    public BasicDigitalObject() {
    }

    public BasicDigitalObject(String id) {
        setId(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

}
