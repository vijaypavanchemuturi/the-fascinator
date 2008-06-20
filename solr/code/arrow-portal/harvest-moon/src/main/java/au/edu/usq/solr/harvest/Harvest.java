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
import au.edu.usq.solr.index.Indexer;
import au.edu.usq.solr.index.IndexerException;
import au.edu.usq.solr.index.impl.SolrIndexer;
import au.edu.usq.solr.index.rule.RuleException;
import au.edu.usq.solr.index.rule.RuleManager;

public class Harvest {

    private Logger log = Logger.getLogger(Harvest.class);

    private Harvester harvester;

    private Indexer indexer;

    private FedoraRestClient registry;

    private PythonInterpreter python;

    public Harvest(Harvester harvester, Indexer indexer,
        FedoraRestClient registry) {
        this.harvester = harvester;
        this.indexer = indexer;
        this.registry = registry;
    }

    public void run(String name, String ruleScript) {

        python = new PythonInterpreter();
        python.set("self", this);

        while (harvester.hasMoreItems()) {
            try {
                List<Item> items = harvester.getItems();
                for (Item item : items) {
                    try {
                        processItem(name, item, ruleScript);
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

    private void processItem(String name, Item item, String ruleScript)
        throws Exception {

        String itemId = item.getId();
        String pid = null;
        File solrFile = null;
        String meta = item.getMetadataAsString();
        log.info("Processing " + itemId + "...");
        try {
            pid = registry.createObject(itemId, "uuid");
            InputStream dcIn = new ByteArrayInputStream(meta.getBytes("UTF-8"));
            solrFile = File.createTempFile("solr", ".xml");
            OutputStream solrOut = new FileOutputStream(solrFile);
            try {
                python.set("pid", pid);
                python.set("name", name);
                python.set("item", item);
                python.execfile(ruleScript);
                RuleManager rules = (RuleManager) python.get("rules")
                    .__tojava__(RuleManager.class);
                rules.run(dcIn, solrOut);
            } catch (Exception e) {
                throw new RuleException(e);
            }
            solrOut.close();
            try {
                indexer.index(solrFile);
            } catch (IndexerException ie) {
                throw new RuleException(ie);
            }
            solrFile.delete();
            registry.addDatastream(pid, "DC0", "Dublin Core (Source)", meta,
                "text/xml");
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
        Harvester fedora = new FedoraHarvester(
            "http://rubric-vitalnew.usq.edu.au:8080/fedora");
        Indexer solr = new SolrIndexer("http://139.86.13.108:8080/solr");
        FedoraRestClient registry = new FedoraRestClient(
            "http://139.86.13.108:8080/fedora");
        registry.authenticate("fedoraAdmin", "fedoraAdmin");
        Harvest harvest = new Harvest(fedora, solr, registry);
        harvest.run("RUBRIC", "harvest-moon/src/main/config/rubric-rules.py");
    }
}
