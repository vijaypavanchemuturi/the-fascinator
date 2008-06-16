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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.apache.solr.util.SimplePostTool;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;
import au.edu.usq.solr.harvest.Harvester;
import au.edu.usq.solr.harvest.HarvesterException;
import au.edu.usq.solr.harvest.Registry;
import au.edu.usq.solr.harvest.filter.FilterManager;
import au.edu.usq.solr.harvest.filter.SolrFilter;
import au.edu.usq.solr.harvest.filter.impl.AddFieldFilter;
import au.edu.usq.solr.harvest.filter.impl.StylesheetFilter;
import au.edu.usq.solr.util.NullWriter;
import au.edu.usq.solr.util.OaiDcNsContext;

public class OaiPmhHarvester implements Harvester {

    public static final String PDF_MIME_TYPE = "application/pdf";

    private final Logger log = Logger.getLogger(OaiPmhHarvester.class);

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private URL solrUpdateUrl;

    private int requestLimit;

    private FilterManager filterManager;

    private XPath xp;

    public OaiPmhHarvester(String solrUpdateUrl, Registry registry,
        int requestLimit) throws MalformedURLException {

        this.solrUpdateUrl = new URL(solrUpdateUrl);
        this.requestLimit = requestLimit;

        XPathFactory xpf = XPathFactory.newInstance();
        xp = xpf.newXPath();
        xp.setNamespaceContext(new OaiDcNsContext());

    }

    public void setAuthentication(String username, String password) {
        // TODO
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

            AddFieldFilter addIdFilter = new AddFieldFilter("id");
            AddFieldFilter addFullTextFilter = new AddFieldFilter("full_text");

            filterManager = new FilterManager();
            filterManager.setWorkDir(new File(TMP_DIR, name));
            filterManager.addFilter(dcToSolr);
            filterManager.addFilter(addIdFilter);
            filterManager.addFilter(new AddFieldFilter("repository_name", name));

            // class - image, document, etc
            // filterManager.addFilter(new AddFieldFilter("class", "Document"));

            // discoverable
            // filterManager.addFilter(new AddFieldFilter("discoverable",
            // "true"));

            RecordsList records;
            ResumptionToken token = null;

            int count = 0;

            do {
                count++;

                // run the OAI-PMH command
                if (token == null) {
                    records = server.listRecords(OaiDcNsContext.OAI_DC_PREFIX);
                } else {
                    log.info("Harvest will continue with resumptionToken="
                        + token.getId());
                    records = server.listRecords(token);
                }
                token = records.getResumptionToken();

                // loop through the records and apply the filters
                for (Record record : records.asList()) {
                    InputStream in = new ByteArrayInputStream(
                        record.getMetadataAsString().getBytes("UTF-8"));
                    String id = record.getHeader().getIdentifier();
                    addIdFilter.setValue(id);
                    log.info("Processing " + id + "...");
                    File solrFile = File.createTempFile("solr", ".xml", workDir);
                    OutputStream out = new FileOutputStream(solrFile);
                    filterManager.run(in, out);
                    out.close();
                    postTool.postFile(solrFile, NullWriter.getInstance());
                    solrFile.delete();
                }

                postTool.commit(NullWriter.getInstance());

                // NodeList records =
                // verb.getNodeList("//*[local-name()='record']");
                // for (int i = 0; i < records.getLength(); i++) {
                // Element record = (Element) records.item(i);
                //
                // String idExpr =
                // ".//*[local-name(..)='header'][local-name()='identifier']";
                // String id = xp.evaluate(idExpr, record);
                // log.info("ID=" + id);
                // String fname = id.replace(":", ".").replace("/", "_")
                // + ".fulltext.xml";
                // File fullTextFile = new File(TMP_DIR, fname);
                // log.debug("fullTextFile=" + fullTextFile);
                // OutputStream fOut = new FileOutputStream(fullTextFile);
                // Writer osw = new OutputStreamWriter(fOut, "UTF8");
                // osw.write("<?xml version='1.0' encoding='UTF-8'?>\n");
                // osw.write("<fulltext><![CDATA[");
                // getFullText(record, osw);
                // osw.write("]]></fulltext>");
                // osw.close();
                // }

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

    private void getFullText(Element record, Writer out) {
        try {
            String expr = ".//dc:format[.='" + PDF_MIME_TYPE + "']";
            expr += "/following-sibling::dc:identifier[starts-with(.,'http')]";
            NodeList nodes = (NodeList) xp.evaluate(expr, record,
                XPathConstants.NODESET);
            PDFTextStripper stripper = new PDFTextStripper();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element elem = (Element) nodes.item(i);
                String url = elem.getTextContent().trim();
                PDDocument pddoc = PDDocument.load(new URL(url));
                if (pddoc.isEncrypted()) {
                    log.warn(url + " ENCRYPTED!");
                } else {
                    log.info("Extracting text from " + url);
                    stripper.writeText(pddoc, out);
                }
                // HACK for usq eprints throttle the pdf downloads
                if (url.startsWith("http://eprints.usq.edu.au/")) {
                    try {
                        log.debug("Waiting 5 seconds before next request");
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                pddoc.close();
            }
        } catch (XPathExpressionException e) {
            log.error("Invalid XPath expression: " + e.getMessage());
        } catch (HttpException e) {
            log.error("HTTP error: " + e.getMessage());
        } catch (IOException e) {
            log.error("IO error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 8) {
            System.out.println("Usage: "
                + VitalFedoraHarvester.class.getCanonicalName()
                + " <solrUpdateUrl> "
                + "<registryUrl> <registryUser> <registryPassword> "
                + "<repBaseUrl> <repUser> <repPassword> <repName> "
                + "[requestLimit]");
        } else {
            try {
                String solrUpdateUrl = args[0];
                String regUrl = args[1];
                String regUser = args[2];
                String regPass = args[3];
                String repUrl = args[4];
                String repUser = args[5];
                String repPass = args[6];
                String repName = args[7];
                int limit = Integer.MAX_VALUE;
                if (args.length > 8) {
                    limit = Integer.parseInt(args[8]);
                }
                Registry registry = new Fedora30Registry(regUrl, regUser,
                    regPass);
                Harvester harvester = new OaiPmhHarvester(solrUpdateUrl,
                    registry, limit);
                harvester.setAuthentication(repUser, repPass);
                harvester.harvest(repName, repUrl);
            } catch (MalformedURLException e) {
                System.err.println("Invalid Solr URL: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Failed to harvest");
                e.printStackTrace();
            }
        }
    }
}
