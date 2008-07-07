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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import au.edu.usq.solr.fedora.DatastreamType;
import au.edu.usq.solr.fedora.FedoraRestClient;
import au.edu.usq.solr.fedora.ObjectDatastreamsType;
import au.edu.usq.solr.harvest.Datastream;
import au.edu.usq.solr.harvest.Item;

public class FedoraItem implements Item {

    private Logger log = Logger.getLogger(FedoraItem.class);

    private FedoraRestClient client;

    private String pid;

    public FedoraItem(FedoraRestClient client, String pid) {
        this.client = client;
        this.pid = pid;
    }

    public String getId() {
        return pid;
    }

    public Element getMetadata() {
        Element elem = null;
        SAXReader reader = new SAXReader();
        try {
            String metadata = getMetadataAsString();
            Document doc = reader.read(new ByteArrayInputStream(
                metadata.getBytes("UTF-8")));
            elem = doc.getRootElement();
        } catch (UnsupportedEncodingException uee) {
            log.warn(uee);
        } catch (DocumentException de) {
            log.warn(de);
        }
        return elem;
    }

    public String getMetadataAsString() {
        String metadata = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            client.get(getPid(), getMetaId(), out);
            metadata = out.toString("UTF-8");
        } catch (IOException ioe) {
            log.warn(ioe);
        }
        return metadata;
    }

    public boolean hasDatastreams() {
        return true;
    }

    public List<Datastream> getDatastreams() {
        List<Datastream> streams = new ArrayList<Datastream>();
        try {
            ObjectDatastreamsType objectStreams = client.listDatastreams(getPid());
            for (DatastreamType ds : objectStreams.getDatastreams()) {
                streams.add(new FedoraDatastream(client, getPid(), ds));
            }
        } catch (IOException ioe) {
            log.warn(ioe);
        }
        return streams;
    }

    public Datastream getDatastream(String dsId) {
        List<Datastream> datastreams = getDatastreams();
        for (Datastream ds : datastreams) {
            if (dsId.equals(ds.getId())) {
                return ds;
            }
        }
        return null;
    }

    // Non-interface methods

    public String getPid() {
        return getId();
    }

    public String getMetaId() {
        return "DC";
    }
}
