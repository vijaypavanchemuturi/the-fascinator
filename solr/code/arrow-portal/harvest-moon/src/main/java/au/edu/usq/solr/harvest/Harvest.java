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
package au.edu.usq.solr.harvest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.python.util.PythonInterpreter;

import au.edu.usq.solr.fedora.FedoraRestClient;
import au.edu.usq.solr.harvest.impl.FedoraHarvester;
import au.edu.usq.solr.harvest.impl.OaiPmhHarvester;
import au.edu.usq.solr.index.Indexer;
import au.edu.usq.solr.index.IndexerException;
import au.edu.usq.solr.index.impl.SolrIndexer;
import au.edu.usq.solr.index.rule.RuleException;
import au.edu.usq.solr.index.rule.RuleManager;

public class Harvest {

    private static Logger log = Logger.getLogger(Harvest.class);

    private Harvester harvester;

    private Indexer indexer;

    private FedoraRestClient registry;

    private PythonInterpreter python;

    private String ruleScript;

    public Harvest(Harvester harvester, Indexer indexer,
        FedoraRestClient registry) {
        this.harvester = harvester;
        this.indexer = indexer;
        this.registry = registry;
    }

    public void run(String name, String ruleScript) {

        this.ruleScript = ruleScript;

        python = new PythonInterpreter();
        python.set("self", this);

        while (harvester.hasMoreItems()) {
            try {
                List<Item> items = harvester.getItems();
                for (Item item : items) {
                    try {
                        processItem(name, item);
                    } catch (Exception e) {
                        log.warn("Processing failed", e);
                    }
                }
                indexer.commit();
            } catch (HarvesterException he) {
                log.error("Harvester failed", he);
            } catch (IndexerException ie) {
                log.error("Indexer failed", ie);
            }
        }
    }

    private void processItem(String name, Item item) throws Exception {

        String itemId = item.getId();
        String pid = null;
        String meta = item.getMetadataAsString();
        byte[] metaBytes = meta.getBytes("UTF-8");

        try {
            log.info("Processing " + itemId + "...");

            // FIXME checking if an object exists hangs after 100 or so
            // ResultType result = registry.findObjects(itemId, 1);
            // List<ObjectFieldType> objects = result.getObjectFields();
            // if (objects.isEmpty()) {
            // pid = registry.createObject(itemId, "uuid");
            // log.info("CREATE: " + pid);
            // } else {
            // pid = objects.get(0).getPid();
            // log.info("UPDATE: " + pid);
            // }

            // harvest/index the main item
            pid = registry.createObject(itemId, "uuid");
            registry.addDatastream(pid, "DC0", "Dublin Core", "text/xml", meta);
            index(name, item, pid, null, metaBytes);

            // harvest/index datastreams
            for (Datastream ds : item.getDatastreams()) {
                String dsId = ds.getId();
                String mimeType = ds.getMimeType();
                String label = ds.getLabel();
                if (!"DC".equals(dsId)) {
                    log.info("Caching " + ds);
                    if (mimeType.startsWith("text/xml")) {
                        registry.addDatastream(pid, dsId, label, mimeType,
                            ds.getContentAsString());
                    } else {
                        File dsFile = File.createTempFile("data", ".tmp");
                        ds.getContent(dsFile);
                        registry.addDatastream(pid, dsId, label, mimeType,
                            dsFile);
                        dsFile.delete();
                    }
                    index(name, item, pid, dsId, metaBytes);
                }
            }
        } catch (Exception e) {
            if (pid != null) {
                try {
                    log.info("Cleaning up failed index");
                    registry.purgeObject(pid);
                } catch (IOException ioe) {
                    log.warn(pid + " was not deleted after indexing failed: "
                        + ioe.getMessage());
                }
            }
            throw e;
        }
    }

    private void index(String name, Item item, String pid, String dsId,
        byte[] metadata) throws IOException, RuleException {
        InputStream dcIn = new ByteArrayInputStream(metadata);
        File solrFile = File.createTempFile("solr", ".xml");
        OutputStream solrOut = new FileOutputStream(solrFile);
        try {
            python.set("pid", pid);
            python.set("name", name);
            python.set("item", item);
            python.set("dsId", dsId);
            python.execfile(ruleScript);
            RuleManager rules = (RuleManager) python.get("rules").__tojava__(
                RuleManager.class);
            rules.run(dcIn, solrOut);
        } catch (Exception e) {
            throw new RuleException(e);
        }
        solrOut.close();
        try {
            indexer.index(solrFile);
        } catch (IndexerException ie) {
            throw new RuleException(ie);
        } finally {
            if (solrFile != null) {
                solrFile.delete();
            }
        }
    }

    // Convenience methods for Jython scripts

    public InputStream getResource(String name) {
        return getClass().getResourceAsStream(name);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 8) {
            log.info("Usage: harvest <solrurl> <regurl> <reguser> <regpass> <reptype> <repurl> <repname> <script> [maxRequests]");
        } else {
            String solrUrl = args[0];
            String regUrl = args[1];
            String regUser = args[2];
            String regPass = args[3];
            String repType = args[4];
            String repUrl = args[5];
            String repName = args[6];
            String script = args[7];
            int maxRequests = Integer.MAX_VALUE;
            if (args.length > 8) {
                maxRequests = Integer.parseInt(args[8]);
            }

            Harvester harvester;
            if ("oai".equals(repType)) {
                harvester = new OaiPmhHarvester(repUrl, maxRequests);
            } else {
                harvester = new FedoraHarvester(repUrl, maxRequests);
            }
            Indexer solr = new SolrIndexer(solrUrl);
            FedoraRestClient registry = new FedoraRestClient(regUrl);
            registry.authenticate(regUser, regPass);
            Harvest harvest = new Harvest(harvester, solr, registry);
            harvest.run(repName, script);
        }
    }
}
