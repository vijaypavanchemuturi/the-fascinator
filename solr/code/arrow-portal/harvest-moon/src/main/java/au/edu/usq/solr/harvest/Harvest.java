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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.python.util.PythonInterpreter;

import au.edu.usq.solr.fedora.FedoraRestClient;
import au.edu.usq.solr.fedora.ObjectFieldType;
import au.edu.usq.solr.fedora.ResultType;
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

    public void run(String ruleScript, Date since) {

        this.ruleScript = ruleScript;

        DateFormat df = new SimpleDateFormat(Harvester.DATETIME_FORMAT);
        String now = df.format(new Date());
        log.info("Started at " + now);
        if (since != null) {
            log.info("Items modified since " + df.format(since)
                + " will be harvested");
        }

        long start = System.currentTimeMillis();

        python = new PythonInterpreter();
        python.set("self", this);

        int count = 0;
        while (harvester.hasMoreItems()) {
            try {
                List<Item> items = harvester.getItems(since);
                if (items.isEmpty()) {
                    log.info("No more items found");
                } else {
                    for (Item item : items) {
                        try {
                            count++;
                            log.info("count = " + count);
                            processItem(item);
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

    private void processItem(Item item) throws Exception {

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

            // harvest/index the main item
            // pid = registry.createObject(itemId, "uuid");
            registry.addDatastream(pid, "DC0", "Dublin Core", "text/xml", meta);
            File solrFile = index(item, pid, null, new InputStreamReader(
                new ByteArrayInputStream(meta.getBytes("UTF-8")), "UTF-8"));
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
                    Reader in = new InputStreamReader(new FileInputStream(
                        solrFile), "UTF-8");
                    File dsSolrFile = index(item, pid, dsId, in);
                    dsSolrFile.delete();
                }
            }
            solrFile.delete();
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

    private File index(Item item, String pid, String dsId, Reader in)
        throws IOException, RuleException {
        File solrFile = File.createTempFile("solr", ".xml");
        Writer out = new OutputStreamWriter(new FileOutputStream(solrFile),
            "UTF-8");
        try {
            RuleManager rules = new RuleManager();
            python.set("rules", rules);
            python.set("pid", pid);
            python.set("dsId", dsId);
            python.set("item", item);
            python.execfile(ruleScript);
            rules.run(in, out);
        } catch (Exception e) {
            throw new RuleException(e);
        } finally {
            in.close();
            out.close();
        }
        try {
            indexer.index(solrFile);
        } catch (IndexerException ie) {
            throw new RuleException(ie);
        }
        return solrFile;
    }

    // Convenience methods for Jython scripts

    public InputStream getResource(String name) {
        return getClass().getResourceAsStream(name);
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
            String script = props.getProperty("indexer.rules");

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
            if ("oai".equals(repType)) {
                harvester = new OaiPmhHarvester(repUrl, maxRequests);
            } else {
                harvester = new FedoraHarvester(repUrl, maxRequests);
                ((FedoraHarvester) harvester).setSearchTerms("uon:669");
            }
            Indexer solr = new SolrIndexer(solrUrl);
            FedoraRestClient registry = new FedoraRestClient(regUrl);
            registry.authenticate(regUser, regPass);
            Harvest harvest = new Harvest(harvester, solr, registry);
            harvest.run(script, since);
        }
    }
}
