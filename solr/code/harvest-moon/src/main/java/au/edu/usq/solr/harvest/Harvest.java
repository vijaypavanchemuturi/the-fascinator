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

import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.rule.RuleManager;
import org.python.core.Py;
import org.python.util.PythonInterpreter;

import au.edu.usq.solr.harvest.fedora.FedoraRestClient;
import au.edu.usq.solr.harvest.impl.OaiPmhHarvester;
import au.edu.usq.solr.index.Indexer;
import au.edu.usq.solr.index.impl.SolrIndexer;
import au.edu.usq.solr.index.rule.RuleException;

public class Harvest {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private Logger log = Logger.getLogger(Harvest.class);

    private Harvester harvester;

    private Indexer indexer;

    private FedoraRestClient registry;

    public Harvest(Harvester harvester, Indexer indexer,
        FedoraRestClient registry) {
        this.harvester = harvester;
        this.indexer = indexer;
        this.registry = registry;
    }

    public void run(String name) {
        while (harvester.hasMoreItems()) {
            try {
                List<Item> items = harvester.getItems();
                for (Item item : items) {
                    try {
                        processItem(name, item);
                    } catch (RuleException re) {
                        re.printStackTrace();
                    }
                }
            } catch (HarvesterException he) {
                log.error(he);
            }
        }
    }

    private void processItem(String name, Item item) throws RuleException {
        String pid = "uuid:blah";

        PythonInterpreter pi = new PythonInterpreter();
        pi.set("self", this);
        pi.set("pid", pid);
        pi.set("name", name);
        pi.set("item", item);
        pi.execfile("src/main/config/rubric-rules.py");
        Object obj = pi.get("rules").__tojava__(RuleManager.class);
        if (obj == Py.NoConversion) {
            throw new RuleException("Failed to create indexing rules");
        }
        RuleManager rules = (RuleManager) obj;
    }

    // Convenience methods for Jython scripts

    public InputStream getResource(String name) {
        return getClass().getResourceAsStream(name);
    }

    public static void main(String[] args) throws Exception {
        Harvester oaiPmh = new OaiPmhHarvester(
            "http://rubric-vitalnew:8080/fedora/oai", 0);
        Indexer solr = new SolrIndexer("http://localhost:8080/solr");
        FedoraRestClient registry = new FedoraRestClient(
            "http://localhost:8080/fedora");
        registry.authenticate("fedoraAdmin", "fedoraAdmin");
        Harvest harvest = new Harvest(oaiPmh, solr, registry);
        harvest.run("RUBRIC");
    }
}
