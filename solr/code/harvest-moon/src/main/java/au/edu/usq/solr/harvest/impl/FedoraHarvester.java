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
import java.util.ArrayList;
import java.util.List;

import au.edu.usq.solr.harvest.Harvester;
import au.edu.usq.solr.harvest.HarvesterException;
import au.edu.usq.solr.harvest.Item;
import au.edu.usq.solr.harvest.fedora.FedoraRestClient;
import au.edu.usq.solr.harvest.fedora.ObjectFieldType;
import au.edu.usq.solr.harvest.fedora.ResultType;

public class FedoraHarvester implements Harvester {

    private static final int DEFAULT_REQUEST_SIZE = 25;

    private FedoraRestClient client;

    private boolean started;

    private String token;

    private int numRequests;

    private int maxRequests;

    private int requestSize;

    public FedoraHarvester(String url) {
        this(url, Integer.MAX_VALUE);
    }

    public FedoraHarvester(String url, int maxRequests) {
        this(url, maxRequests, DEFAULT_REQUEST_SIZE);
    }

    public FedoraHarvester(String url, int maxRequests, int requestSize) {
        this.maxRequests = maxRequests;
        this.requestSize = requestSize;
        client = new FedoraRestClient(url);
        started = false;
        numRequests = 0;
    }

    public boolean hasMoreItems() {
        return !started || (token != null && numRequests < maxRequests);
    }

    public List<Item> getItems() throws HarvesterException {
        List<Item> items = new ArrayList<Item>();
        ResultType results;
        try {
            numRequests++;
            if (started) {
                results = client.resumeFindObjects(token);
            } else {
                started = true;
                results = client.findObjects("uon:7??", requestSize);
            }
            for (ObjectFieldType object : results.getObjectFields()) {
                items.add(new FedoraItem(client, object));
            }
            token = results.getListSession().getToken();
        } catch (IOException ioe) {
            throw new HarvesterException(ioe);
        }
        return items;
    }
}
