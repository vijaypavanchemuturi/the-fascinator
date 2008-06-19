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

import java.util.ArrayList;
import java.util.List;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;
import au.edu.usq.solr.harvest.Harvester;
import au.edu.usq.solr.harvest.HarvesterException;
import au.edu.usq.solr.harvest.Item;
import au.edu.usq.solr.util.OaiDcNsContext;

public class OaiPmhHarvester implements Harvester {

    private OaiPmhServer server;

    private boolean started;

    private ResumptionToken token;

    private int numRequests;

    private int maxRequests;

    public OaiPmhHarvester(String url) {
        this(url, Integer.MAX_VALUE);
    }

    public OaiPmhHarvester(String url, int maxRequests) {
        this.maxRequests = maxRequests;
        server = new OaiPmhServer(url);
        started = false;
        numRequests = 0;
    }

    public boolean hasMoreItems() {
        return !started || (token != null && numRequests < maxRequests);
    }

    public List<Item> getItems() throws HarvesterException {
        List<Item> items = new ArrayList<Item>();
        RecordsList records;
        try {
            numRequests++;
            if (started) {
                records = server.listRecords(token);
            } else {
                started = true;
                records = server.listRecords(OaiDcNsContext.OAI_DC_PREFIX);
            }
            for (Record record : records.asList()) {
                items.add(new OaiPmhItem(record));
            }
            token = records.getResumptionToken();
        } catch (OAIException oe) {
            throw new HarvesterException(oe);
        }
        return items;
    }
}
