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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import au.edu.usq.solr.fedora.FedoraRestClient;
import au.edu.usq.solr.fedora.ObjectFieldType;
import au.edu.usq.solr.fedora.ResultType;
import au.edu.usq.solr.harvest.impl.FedoraHarvester;
import au.edu.usq.solr.harvest.impl.OaiOreHarvester;
import au.edu.usq.solr.harvest.impl.OaiPmhHarvester;
import au.edu.usq.solr.index.Indexer;
import au.edu.usq.solr.index.IndexerException;
import au.edu.usq.solr.index.impl.SolrIndexer;

public class Harvest {

    private static Logger log = Logger.getLogger(Harvest.class);

    private Harvester harvester;

    private Indexer indexer;

    private FedoraRestClient registry;

    private File rulesFile;

    public Harvest(Harvester harvester, FedoraRestClient registry,
        Indexer indexer, File rulesFile) {
        this.harvester = harvester;
        this.registry = registry;
        this.indexer = indexer;
        this.rulesFile = rulesFile;
    }

    public void run(Date since) {
        DateFormat df = new SimpleDateFormat(Harvester.DATETIME_FORMAT);
        String now = df.format(new Date());
        log.info("Started at " + now);
        if (since != null) {
            log.info("Items modified since " + df.format(since)
                + " will be harvested");
        }

        long start = System.currentTimeMillis();

        // cache the ruleset
        String rulesPid = null;
        try {
            log.info("Caching rules file: " + rulesFile);
            String rulesLabel = rulesFile.getAbsolutePath();
            ResultType result = registry.findObjects(rulesLabel, 1);
            List<ObjectFieldType> objects = result.getObjectFields();
            if (objects.isEmpty()) {
                rulesPid = registry.createObject(rulesLabel, "sof");
                log.info("CREATE: " + rulesPid);
            } else {
                rulesPid = objects.get(0).getPid();
                log.info("UPDATE: " + rulesPid);
            }
            // rulesPid = registry.createObject(rulesFile.getAbsolutePath(),
            // "sof");
            registry.addDatastream(rulesPid, "RULES.PY", "Indexing Rules",
                "text/plain", rulesFile);
        } catch (IOException ioe) {
            log.error(ioe.getMessage());
        }

        while (harvester.hasMoreItems()) {
            try {
                List<Item> items = harvester.getItems(since);
                if (items.isEmpty()) {
                    log.info("No more items found");
                } else {
                    for (Item item : items) {
                        try {
                            processItem(item, rulesPid);
                        } catch (Exception e) {
                            log.warn("Processing failed: " + e.getMessage());
                        }
                    }
                    indexer.commit();
                }
            } catch (HarvesterException he) {
                log.error(he.getMessage());
            } catch (IndexerException ie) {
                log.error(ie.getMessage());
            }
        }

        log.info("Completed in "
            + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    private void processItem(Item item, String rulesPid) throws IOException {
        String itemId = item.getId();
        String pid = null;
        String meta = item.getMetadataAsString();
        try {
            log.info("Processing " + itemId + "...");

            // FIXME checking if an object exists hangs after 100 or so
            ResultType result = registry.findObjects(itemId, 1);
            List<ObjectFieldType> objects = result.getObjectFields();
            if (objects.isEmpty()) {
                pid = registry.createObject(itemId, "uuid");
                log.info("CREATE: " + pid);
            } else {
                pid = objects.get(0).getPid();
                log.info("UPDATE: " + pid);
            }

            // create the digital object
            // pid = registry.createObject(itemId, "uuid");
            registry.addDatastream(pid, "DC0", "Dublin Core Metadata",
                "text/xml", meta);
            log.info("Created object: " + pid);

            // harvest datastreams
            for (Datastream ds : item.getDatastreams()) {
                String dsId = ds.getId();
                String type = ds.getMimeType();
                String label = ds.getLabel();
                if (!"DC".equals(dsId)) {
                    log.info("Caching " + ds);
                    if (type.startsWith("text/xml")) {
                        registry.addDatastream(pid, dsId, label, type,
                            ds.getContentAsString());
                    } else {
                        File dsf = File.createTempFile("datastream", null);
                        ds.getContent(dsf);
                        if (dsf.length() > 0) {
                            registry.addDatastream(pid, dsId, label, type, dsf);
                        } else {
                            log.info("Ignored zero byte datastream");
                        }
                        dsf.delete();
                    }
                }
            }

            // add SoF specific metadata
            StringBuilder sofMeta = new StringBuilder();
            sofMeta.append("item.pid=");
            sofMeta.append(itemId);
            sofMeta.append("\nrules.pid=");
            sofMeta.append(rulesPid);
            registry.addDatastream(pid, "SOF-META", "Sun of Fedora Metadata",
                "text/plain", sofMeta.toString());
        } catch (IOException ioe) {
            if (pid != null) {
                try {
                    log.info("Cleaning up failed index");
                    registry.purgeObject(pid);
                } catch (IOException ioe2) {
                    log.warn(pid + " was not deleted after indexing failed: "
                        + ioe2.getMessage());
                }
            }
            throw ioe;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            log.info("Usage: harvest <configFile> [-all]");
            log.info("Specifying -all will harvest everything.");
        } else {
            boolean all = false;
            for (String arg : args) {
                if ("-all".equals(arg)) {
                    all = true;
                    break;
                }
            }
            File configFile = new File(args[0]);
            Properties props = new Properties();
            props.load(new FileInputStream(configFile));

            String solrUrl = props.getProperty("solr.url");
            String regUrl = props.getProperty("registry.url");
            String regUser = props.getProperty("registry.user");
            String regPass = props.getProperty("registry.pass");
            String repType = props.getProperty("repository.type");
            String repUrl = props.getProperty("repository.url");
            String indexerRules = props.getProperty("indexer.rules");

            int maxRequests = Integer.MAX_VALUE;
            if (props.containsKey("max.requests")) {
                maxRequests = Integer.parseInt(props.getProperty("max.requests"));
            }

            Date since = null;
            if (!all) {
                if (props.containsKey("modified.since")) {
                    DateFormat df = new SimpleDateFormat(
                        Harvester.DATETIME_FORMAT);
                    String from = props.getProperty("modified.since");
                    since = df.parse(from);
                } else {
                    since = new Date();
                }
            }

            Harvester harvester;
            if ("oai-pmh".equals(repType)) {
                harvester = new OaiPmhHarvester(repUrl, maxRequests);
            } else if ("oai-ore".equals(repType)) {
                harvester = new OaiOreHarvester(repUrl);
            } else if ("fedora".equals(repType)) {
                harvester = new FedoraHarvester(repUrl, maxRequests);
                String searchTerms = props.getProperty("fedora.search.terms",
                    "").trim();
                if (searchTerms != null && !"".equals(searchTerms)) {
                    log.info(" *** Setting search terms to: " + searchTerms);
                    ((FedoraHarvester) harvester).setSearchTerms(searchTerms);
                }
            } else {
                log.info("Unknown repType: " + repType);
                return;
            }
            FedoraRestClient registry = new FedoraRestClient(regUrl);
            registry.authenticate(regUser, regPass);
            Indexer indexer = new SolrIndexer(solrUrl);
            File rulesFile = new File(configFile.getParentFile(), indexerRules);
            Harvest harvest = new Harvest(harvester, registry, indexer,
                rulesFile);
            harvest.run(since);
        }
    }
}
