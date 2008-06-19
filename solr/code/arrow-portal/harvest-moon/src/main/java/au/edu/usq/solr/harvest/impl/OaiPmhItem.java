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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import se.kb.oai.pmh.Record;
import au.edu.usq.solr.harvest.Datastream;
import au.edu.usq.solr.harvest.Item;

public class OaiPmhItem implements Item {

    private Logger log = Logger.getLogger(OaiPmhItem.class);

    private Record record;

    public OaiPmhItem(Record record) {
        this.record = record;
    }

    public String getId() {
        return record.getHeader().getIdentifier();
    }

    public Element getMetadata() {
        return record.getMetadata();
    }

    public String getMetadataAsString() {
        String metadata = null;
        try {
            metadata = record.getMetadataAsString();
        } catch (IOException ioe) {
            log.warn(ioe);
        }
        return metadata;
    }

    public boolean hasDatastreams() {
        return false;
    }

    public List<Datastream> getDatastreams() {
        return Collections.emptyList();
    }

    public Datastream getDatastream(String dsId) {
        return null;
    }
}
