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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

import au.edu.usq.solr.fedora.FedoraRestClient;
import au.edu.usq.solr.fedora.ListSessionType;
import au.edu.usq.solr.fedora.ObjectFieldType;
import au.edu.usq.solr.fedora.ResultType;
import au.edu.usq.solr.harvest.impl.FedoraHarvester;
import au.edu.usq.solr.harvest.impl.OaiOreHarvester;
import au.edu.usq.solr.harvest.impl.OaiPmhHarvester;

public class Harvest {

    private static Logger log = Logger.getLogger(Harvest.class);

    private Harvester harvester;

    private FedoraRestClient registry;

    private File rulesFile;

    private SAXReader saxReader;

    public Harvest(Harvester harvester, FedoraRestClient registry,
        File rulesFile) {
        this.harvester = harvester;
        this.registry = registry;
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

        // SAXReader for RELS-EXT streams
        Map<String, String> nsmap = new HashMap<String, String>();
        nsmap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        nsmap.put("rel", "info:fedora/fedora-system:def/relations-external#");
        saxReader = new SAXReader();
        saxReader.getDocumentFactory().setXPathNamespaceURIs(nsmap);

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
                }
            } catch (HarvesterException he) {
                log.error(he.getMessage());
            }
        }

        log.info("Completed in "
            + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    private void processItem(Item item, String rulesPid) throws IOException {
        String itemId = item.getId();
        String pid = null;
        try {
            log.info("Processing " + itemId + "...");

            // SoF specific metadata
            StringBuilder sofMeta = new StringBuilder();
            sofMeta.append("item.pid=");
            sofMeta.append(itemId);
            sofMeta.append("\nrules.pid=");
            sofMeta.append(rulesPid);
            pid = createUpdateObject(itemId);
            registry.addDatastream(pid, "DC0", "Dublin Core Metadata",
                "text/xml", item.getMetadataAsString());

            // harvest datastreams
            for (Datastream ds : item.getDatastreams()) {
                String dsId = ds.getId();
                String type = ds.getMimeType();
                String label = ds.getLabel();
                log.info("Processing " + ds);
                if ("DC".equals(dsId)) {
                    dsId = "DC0";
                }
                if ("RELS-EXT".equals(dsId)) {
                    try {
                        Document doc = saxReader.read(ds.getContentAsStream());
                        Element elem = (Element) doc.selectSingleNode("//rdf:RDF/rdf:Description[@rdf:about='info:fedora/"
                            + itemId + "']");
                        if (elem != null) {
                            log.info("replacing " + itemId + " with " + pid);
                            elem.addAttribute(QName.get("about", "rdf",
                                "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
                                "info:fedora/" + pid);
                        }
                        registry.addDatastream(pid, dsId, label, type,
                            doc.asXML());
                    } catch (Exception e) {
                        log.error("Failed parsing RELS-EXT: " + e.getMessage());
                    }
                } else {
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

    private String createUpdateObject(String itemId) throws IOException {
        String pid = null;
        ResultType result = registry.findObjects(itemId, 1);
        List<ObjectFieldType> objects = result.getObjectFields();
        if (objects.isEmpty()) {
            pid = registry.createObject(itemId, "uuid");
            log.info("CREATE: " + pid);
        } else {
            pid = objects.get(0).getPid();
            log.info("UPDATE: " + pid);
        }

        // FIXME bug in fedora where resumeFindObjects has to be called until
        // there is no list session or the server will hang after about 100
        // requests
        ListSessionType session = result.getListSession();
        while (session != null) {
            log.info(" ** resuming to close connection");
            result = registry.resumeFindObjects(session.getToken());
            session = result.getListSession();
        }

        return pid;
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
            File rulesFile = new File(configFile.getParentFile(), indexerRules);
            Harvest harvest = new Harvest(harvester, registry, rulesFile);
            harvest.run(since);
        }
    }
}
