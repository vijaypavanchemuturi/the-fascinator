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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.solr.util.SimplePostTool;

import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;
import au.edu.usq.solr.harvest.Harvester;
import au.edu.usq.solr.harvest.HarvesterException;
import au.edu.usq.solr.harvest.fedora.FedoraRestClient;
import au.edu.usq.solr.harvest.filter.FilterManager;
import au.edu.usq.solr.harvest.filter.SolrFilter;
import au.edu.usq.solr.harvest.filter.impl.AddFieldFilter;
import au.edu.usq.solr.harvest.filter.impl.StylesheetFilter;
import au.edu.usq.solr.util.NullWriter;
import au.edu.usq.solr.util.OaiDcNsContext;

public class OaiPmhHarvester implements Harvester {

    public static final String PDF_MIME_TYPE = "application/pdf";

    private static Logger log = Logger.getLogger(OaiPmhHarvester.class);

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private FedoraRestClient registry;

    private URL solrUpdateUrl;

    private int requestLimit;

    private FilterManager filterManager;

    public OaiPmhHarvester(FedoraRestClient registry, String solrUpdateUrl,
        int requestLimit) throws MalformedURLException {
        this.registry = registry;
        this.solrUpdateUrl = new URL(solrUpdateUrl);
        this.requestLimit = requestLimit;
    }

    public void authenticate(String username, String password) {
        // OAI-PMH is normally open access
    }

    public void harvest(String name, String url) throws HarvesterException {
        long startTime = System.currentTimeMillis();
        SimplePostTool postTool = new SimplePostTool(solrUpdateUrl);
        OaiPmhServer server = new OaiPmhServer(url);
        File workDir = new File(TMP_DIR, name);
        workDir.mkdirs();
        try {
            log.info("Starting harvest from [" + url + "]");

            SolrFilter dcToSolr = new StylesheetFilter(
                getClass().getResourceAsStream("/xsl/dc_solr.xsl"));
            dcToSolr.setName("Dublin Core To Solr");

            AddFieldFilter addId = new AddFieldFilter("id");
            AddFieldFilter addItemClass = new AddFieldFilter("item_class");
            AddFieldFilter addItemType = new AddFieldFilter("item_type");

            filterManager = new FilterManager();
            filterManager.setWorkDir(new File(TMP_DIR, name));
            filterManager.addFilter(dcToSolr);
            filterManager.addFilter(addId);
            filterManager.addFilter(new AddFieldFilter("repository_name", name));
            filterManager.addFilter(new AddFieldFilter("group_access", "guest"));
            filterManager.addFilter(addItemClass);
            filterManager.addFilter(addItemType);

            RecordsList records;
            ResumptionToken token = null;
            int count = 0;
            do {
                // run the OAI-PMH command
                count++;
                if (token == null) {
                    records = server.listRecords(OaiDcNsContext.OAI_DC_PREFIX);
                } else {
                    log.info("Continue harvest with token=" + token.getId());
                    records = server.listRecords(token);
                }
                token = records.getResumptionToken();

                // loop through the records, index and store in registry
                for (Record record : records.asList()) {
                    String oaiId = record.getHeader().getIdentifier();
                    log.info("Processing " + oaiId + "...");
                    InputStream dcIn = new ByteArrayInputStream(
                        record.getMetadataAsString().getBytes("UTF-8"));
                    File solrFile = File.createTempFile("solr", ".xml", workDir);
                    OutputStream solrOut = new FileOutputStream(solrFile);
                    String pid = registry.createObject(oaiId, "uuid");
                    addId.setValue(pid);
                    addItemClass.setValue("document");
                    addItemType.setValue("object");
                    filterManager.run(dcIn, solrOut);
                    solrOut.close();
                    postTool.postFile(solrFile, NullWriter.getInstance());
                    solrFile.delete();
                    registry.addDatastream(pid, "DC0",
                        "Dublin Core (Original)", record.getMetadataAsString(),
                        "text/xml");
                }
                postTool.commit(NullWriter.getInstance());
            } while ((token != null) && (count < requestLimit));

            log.info("Harvest from [" + url + "] completed in "
                + (System.currentTimeMillis() - startTime) / 1000.0
                + " seconds");
        } catch (Exception e) {
            throw new HarvesterException(e);
        } finally {
            if (workDir != null) {
                workDir.delete();
            }
        }
    }

    public static void main(String[] args) {

        Option solrUrl = new Option("s", true, "solr update url");
        Option registryUrl = new Option("R", true, "registry base url");
        Option registryUser = new Option("U", true, "registry username");
        Option registryPass = new Option("P", true, "registry password");
        Option repositoryUrl = new Option("r", true, "repository base url");
        Option repositoryUser = new Option("u", true, "repository username");
        Option repositoryPass = new Option("p", true, "repository password");
        Option repositoryName = new Option("n", true, "repository name");
        Option requestLimit = new Option("l", true, "request limit");

        solrUrl.setArgName("url");
        registryUrl.setArgName("url");
        registryUser.setArgName("username");
        registryPass.setArgName("password");
        repositoryUrl.setArgName("url");
        repositoryUser.setArgName("username");
        repositoryPass.setArgName("password");
        repositoryName.setArgName("name");
        requestLimit.setArgName("number");

        Options options = new Options();
        options.addOption(solrUrl);
        options.addOption(registryUrl);
        options.addOption(registryUser);
        options.addOption(registryPass);
        options.addOption(repositoryUrl);
        options.addOption(repositoryUser);
        options.addOption(repositoryPass);
        options.addOption(repositoryName);
        options.addOption(requestLimit);

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmdLine = parser.parse(options, args);
            if (cmdLine.hasOption('s') && cmdLine.hasOption('r')
                && cmdLine.hasOption('R')) {
                int limit = Integer.MAX_VALUE;
                if (cmdLine.hasOption('l')) {
                    limit = Integer.parseInt(requestLimit.getValue());
                }
                FedoraRestClient registry = new FedoraRestClient(
                    registryUrl.getValue());
                String user = registryUser.getValue();
                String pass = registryPass.getValue();
                if (user != null && pass != null) {
                    registry.authenticate(user, pass);
                }
                Harvester harvester = new OaiPmhHarvester(registry,
                    solrUrl.getValue(), limit);
                harvester.harvest(repositoryName.getValue(),
                    repositoryUrl.getValue());
            } else {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp(OaiPmhHarvester.class.getCanonicalName(), options);
            }
        } catch (ParseException pe) {
            log.error(pe);
        } catch (MalformedURLException mue) {
            log.error(mue);
        } catch (HarvesterException he) {
            log.error(he);
        }
    }
}
