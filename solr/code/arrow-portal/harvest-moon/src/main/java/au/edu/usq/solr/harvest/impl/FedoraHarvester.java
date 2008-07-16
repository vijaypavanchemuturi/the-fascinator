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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import au.edu.usq.solr.fedora.FedoraRestClient;
import au.edu.usq.solr.fedora.ListSessionType;
import au.edu.usq.solr.fedora.ObjectFieldType;
import au.edu.usq.solr.fedora.ResultType;
import au.edu.usq.solr.fedora.FedoraRestClient.FindObjectsType;
import au.edu.usq.solr.harvest.Harvester;
import au.edu.usq.solr.harvest.HarvesterException;
import au.edu.usq.solr.harvest.Item;

public class FedoraHarvester implements Harvester {

    private static final int DEFAULT_REQUEST_SIZE = 25;

    private Logger log = Logger.getLogger(FedoraHarvester.class);

    private JAXBContext jc;

    private Unmarshaller um;

    private FedoraRestClient client;

    private boolean started;

    private int maxRequests;

    private int requestSize;

    private String searchTerms;

    private File workDir;

    private Queue<File> searchFiles;

    public FedoraHarvester(String url) throws IOException {
        this(url, Integer.MAX_VALUE);
    }

    public FedoraHarvester(String url, int maxRequests) throws IOException {
        this(url, maxRequests, DEFAULT_REQUEST_SIZE);
    }

    public FedoraHarvester(String url, int maxRequests, int requestSize)
        throws IOException {
        this.maxRequests = maxRequests;
        this.requestSize = requestSize;
        client = new FedoraRestClient(url);
        searchFiles = new LinkedList<File>();
        started = false;
        searchTerms = "*";
        URL u = new URL(url);
        String tmpDir = System.getProperty("java.io.tmpdir");
        String tmpName = u.getHost() + "_" + u.getPort();
        workDir = new File(tmpDir, tmpName);
        workDir.mkdirs();
        try {
            jc = JAXBContext.newInstance(ResultType.class);
            um = jc.createUnmarshaller();
        } catch (JAXBException jaxbe) {
            throw new IOException(jaxbe.getMessage());
        }
    }

    public void setSearchTerms(String searchTerms) {
        this.searchTerms = searchTerms;
    }

    public boolean hasMoreItems() {
        boolean more = !(started && searchFiles.isEmpty());
        if (!more) {
            log.info("Deleting work directory: " + workDir.getAbsolutePath());
            workDir.delete();
        }
        return more;
    }

    public List<Item> getItems(Date since) throws HarvesterException {
        if (!started) {
            started = true;
            cacheResults(since);
        }
        return getCachedItems();
    }

    private void cacheResults(Date since) throws HarvesterException {
        log.info("Caching search results in " + workDir.getAbsolutePath());
        try {
            Marshaller m = jc.createMarshaller();
            boolean done = false;
            boolean first = true;
            int count = 0;
            ResultType results;
            String token = null;
            while (!done) {
                if (first) {
                    first = false;
                    if (since == null) {
                        log.info("Search for ALL records");
                        results = client.findObjects(FindObjectsType.TERMS,
                            searchTerms, requestSize);
                    } else {
                        DateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
                        String from = df.format(since);
                        log.info("Search for records from [" + from + "]");
                        results = client.findObjects(FindObjectsType.QUERY,
                            "mDate>" + from, requestSize);
                    }
                } else {
                    results = client.resumeFindObjects(token);
                    log.info("Resuming search using token: " + token);
                }

                File tmpFile = new File(workDir, count + ".xml");
                m.marshal(results, tmpFile);
                searchFiles.add(tmpFile);

                ListSessionType session = results.getListSession();
                if (session != null) {
                    token = results.getListSession().getToken();
                } else {
                    token = null;
                }
                done = token == null || ++count > maxRequests;
            }
            log.info("Cached " + (searchFiles.size() - 1) + " search results");
        } catch (Exception e) {
            throw new HarvesterException(e);
        }
    }

    private List<Item> getCachedItems() throws HarvesterException {
        List<Item> items = new ArrayList<Item>();
        File searchFile = searchFiles.poll();
        log.info("Loading search results: " + searchFile);
        try {
            ResultType results = (ResultType) um.unmarshal(searchFile);
            for (ObjectFieldType object : results.getObjectFields()) {
                items.add(new FedoraItem(client, object.getPid()));
            }
        } catch (JAXBException jaxbe) {
            throw new HarvesterException(jaxbe);
        } finally {
            searchFile.delete();
        }
        return items;
    }
}
