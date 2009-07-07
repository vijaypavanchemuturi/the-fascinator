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

import java.io.IOException;
import java.io.InputStream;

import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;

/**
 * Generic Payload implementation.
 * 
 * @author Oliver Lucido
 */
public class BasicPayload implements Payload {

    private static final String UNDEFINED = "[undefined]";

    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    private PayloadType payloadType;

    private String id;

    private String label;

    private String contentType;

    private InputStream inputStream;

    public BasicPayload() {
        this(UNDEFINED, UNDEFINED, DEFAULT_CONTENT_TYPE);
    }

    public BasicPayload(String id, String label, String contentType) {
        setId(id);
        setLabel(label);
        setContentType(contentType);
        setPayloadType(PayloadType.Data);
    }

    public PayloadType getType() {
        return payloadType;
    }

    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %s)", getId(), getLabel(),
                getContentType());
    }
}
