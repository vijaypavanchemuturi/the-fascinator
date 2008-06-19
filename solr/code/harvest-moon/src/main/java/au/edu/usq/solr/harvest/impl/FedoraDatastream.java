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
package au.edu.usq.solr.harvest.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import au.edu.usq.solr.harvest.Datastream;
import au.edu.usq.solr.harvest.fedora.DatastreamType;
import au.edu.usq.solr.harvest.fedora.FedoraRestClient;

public class FedoraDatastream implements Datastream {

    private String pid;

    private FedoraRestClient client;

    private DatastreamType dsType;

    public FedoraDatastream(FedoraRestClient client, String pid,
        DatastreamType dsType) {
        this.client = client;
        this.pid = pid;
        this.dsType = dsType;
    }

    public String getId() {
        return dsType.getDsid();
    }

    public String getLabel() {
        return dsType.getLabel();
    }

    public String getMimeType() {
        return dsType.getMimeType();
    }

    public byte[] getContent() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.get(pid, getId(), out);
        return out.toByteArray();
    }

    @Override
    public String toString() {
        return pid + "/" + getId() + "/" + getLabel();
    }
}
