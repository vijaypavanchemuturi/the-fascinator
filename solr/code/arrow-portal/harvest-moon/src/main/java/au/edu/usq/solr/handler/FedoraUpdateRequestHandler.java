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
 * 
 * Based on http://drama.ramp.org.au/cgi-bin/trac.cgi/wiki/InstallMuradoraSolr
 */
package au.edu.usq.solr.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.solr.handler.XmlUpdateRequestHandler;
import org.apache.solr.request.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryRequestBase;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.util.ContentStream;
import org.apache.solr.util.ContentStreamBase;
import org.apache.solr.util.NamedList;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.python.util.PythonInterpreter;

import au.edu.usq.fedora.foxml.DatastreamType;
import au.edu.usq.fedora.foxml.DigitalObject;
import au.edu.usq.solr.fedora.FedoraRestClient;
import au.edu.usq.solr.fedora.ListSessionType;
import au.edu.usq.solr.fedora.ObjectFieldType;
import au.edu.usq.solr.fedora.ResultType;
import au.edu.usq.solr.harvest.Item;
import au.edu.usq.solr.harvest.impl.FedoraRegistryItem;
import au.edu.usq.solr.index.rule.RuleException;
import au.edu.usq.solr.index.rule.RuleManager;

public class FedoraUpdateRequestHandler extends XmlUpdateRequestHandler {

    private Logger log = Logger.getLogger(FedoraUpdateRequestHandler.class.getName());

    private static final String FROM_PID = "savePid";

    private static final String DELETE_PID = "deletePid";

    private static final String FROM_FOXML = "fromFoxml";

    private FedoraRestClient client;

    private SAXReader saxReader;

    @Override
    public void init(NamedList args) {
        super.init(args);
        log.info("Initialising Fedora handler...");
        String fedoraUrl = (String) args.get("fedoraUrl");
        String fedoraUser = (String) args.get("fedoraUser");
        String fedoraPass = (String) args.get("fedoraPass");
        client = new FedoraRestClient(fedoraUrl);
        client.authenticate(fedoraUser, fedoraPass);

        // SAXReader for RELS-EXT streams
        Map<String, String> nsmap = new HashMap<String, String>();
        nsmap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        nsmap.put("rel", "info:fedora/fedora-system:def/relations-external#");
        DocumentFactory df = new DocumentFactory();
        df.setXPathNamespaceURIs(nsmap);
        saxReader = new SAXReader(df);

        log.info("Done initialising Fedora handler...");
    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp)
        throws Exception {
        SolrParams params = req.getParams();
        String action = params.get("action");
        String contentType = params.get(SolrParams.STREAM_CONTENTTYPE);

        if (action == null) {
            // No action defined, do normal processing
            super.handleRequestBody(req, rsp);
            return;
        }

        if (action.equals(FROM_PID) || action.equals(DELETE_PID)) {
            String pid = null;
            Collection<ContentStream> streams = new ArrayList<ContentStream>();
            pid = params.get("pid");
            if (pid == null)
                throw new IllegalArgumentException(
                    "PID cannot be null for update and delete by PID operations");
            if (action.equals(FROM_PID)) {
                ByteArrayOutputStream foxmlOut = new ByteArrayOutputStream();
                client.export(pid, foxmlOut);
                JAXBContext jc = JAXBContext.newInstance("au.edu.usq.fedora.foxml");
                Unmarshaller u = jc.createUnmarshaller();
                DigitalObject obj = (DigitalObject) u.unmarshal(new ByteArrayInputStream(
                    foxmlOut.toByteArray()));
                buildSolrDocument(req, contentType, streams, obj);
                ((SolrQueryRequestBase) req).setContentStreams(streams);
            } else if (action.equals(DELETE_PID)) {
                String deleteQuery = "<delete><query>pid:\"" + pid
                    + "\"</query></delete>";
                ContentStreamBase stream = new ContentStreamBase.StringStream(
                    deleteQuery);
                if (contentType != null) {
                    stream.setContentType(contentType);
                }
                streams.add(stream);
                ((SolrQueryRequestBase) req).setContentStreams(streams);
            }
            super.handleRequestBody(req, rsp);
        } else if (action.equals(FROM_FOXML)) {
            String path = params.get("foxmlPath");
            if (path == null)
                throw new IllegalArgumentException(
                    "Path cannot be null when update from Foxml files");
            fromFoxmlFiles(path, req, rsp);
        } else {
            throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    private void buildSolrDocument(SolrQueryRequest req, String contentType,
        Collection<ContentStream> streams, DigitalObject obj)
        throws IOException, JAXBException {
        String collection = null;
        String pid = obj.getPID();
        Properties props = getMeta(pid);
        String itemPid = props.getProperty("item.pid");
        String rulesPid = props.getProperty("rules.pid");
        File rulesFile = File.createTempFile("rules", ".py");
        FileOutputStream rulesOut = new FileOutputStream(rulesFile);
        client.get(rulesPid, "RULES.PY", rulesOut);
        rulesOut.close();
        FedoraRegistryItem item = new FedoraRegistryItem(client, itemPid, pid);
        for (DatastreamType ds : obj.getDatastream()) {
            String dsId = ds.getID();
            try {
                if ("AUDIT".equals(dsId) || "DC".equals(dsId)
                    || "DC0".equals(dsId) || "SOF-META".equals(dsId)) {
                    // ignored - these do not need to be indexed
                    // except DC0 which is indexed last to support
                    // collections
                    log.info("Ignoring datastream: " + dsId);
                } else if ("RELS-EXT".equals(dsId)) {
                    log.info("Processing " + dsId);
                    Reader in = getDatastreamReader(pid, dsId);
                    try {
                        Document doc = saxReader.read(in);
                        Element elem = (Element) doc.selectSingleNode("//rel:isMemberOf");
                        if (elem != null) {
                            String memberOf = elem.attributeValue(QName.get(
                                "resource", "rdf",
                                "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
                            log.info("memberOf: " + memberOf);
                            ResultType result = client.findObjects(
                                memberOf.replaceAll("info:fedora/", ""), 1,
                                new String[] { "label" });
                            List<ObjectFieldType> objects = result.getObjectFields();
                            if (objects.size() > 0) {
                                ObjectFieldType object = objects.get(0);
                                String cPid = object.getPid();
                                String cLabel = object.getLabel();
                                FedoraRegistryItem cItem = new FedoraRegistryItem(
                                    client, cLabel, cPid);
                                collection = cItem.getMetadata()
                                    .selectSingleNode("//dc:title")
                                    .getText();
                            }
                            // FIXME bug in fedora where resumeFindObjects has
                            // to be called until there is no list session or
                            // the server will hang after about 100 requests
                            ListSessionType session = result.getListSession();
                            while (session != null) {
                                log.info(" ** resuming to close connection");
                                result = client.resumeFindObjects(session.getToken());
                                session = result.getListSession();
                            }
                        }
                    } catch (DocumentException de) {
                        log.severe("Failed parsing RELS-EXT: "
                            + de.getMessage());
                    }
                } else {
                    log.info("Indexing Datastream: " + dsId);
                    Reader in = new StringReader("<add><doc/></add>");
                    File solrFile = index(item, pid, dsId, collection, in,
                        rulesFile);
                    if (solrFile != null) {
                        ContentStream stream = new ContentStreamBase.FileStream(
                            solrFile);
                        streams.add(stream);
                    }
                }
            } catch (RuleException re) {
                log.warning(re.getMessage());
            }
        }

        try {
            log.info("Indexing Dublin Core");
            Reader in = getDatastreamReader(pid, "DC0");
            File solrFile = index(item, pid, null, collection, in, rulesFile);
            if (solrFile != null) {
                ContentStream stream = new ContentStreamBase.FileStream(
                    solrFile);
                streams.add(stream);
            }
        } catch (RuleException re) {
            log.warning(re.getMessage());
        }
    }

    private Reader getDatastreamReader(String pid, String dsId)
        throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.get(pid, dsId, out);
        return new InputStreamReader(
            new ByteArrayInputStream(out.toByteArray()));
    }

    private void fromFoxmlFiles(String filePath, SolrQueryRequest req,
        SolrQueryResponse rsp) throws Exception {
        log.info("fromFoxmlFiles filePath=" + filePath);
        File objectDir = new File(filePath);
        indexDocs(objectDir, req, rsp);
    }

    private void indexDocs(File file, SolrQueryRequest req,
        SolrQueryResponse rsp) throws Exception {
        if (file.isHidden())
            return;
        // if (logger.isDebugEnabled())
        // logger.debug("indexDocs file="+file+"
        // repositoryName="+repositoryName+" indexName="+indexName);
        if (file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                indexDocs(new File(file, files[i]), req, rsp);
            }
        } else {
            try {
                indexDoc(file.getName(), new FileInputStream(file), req, rsp);
            } catch (RemoteException e) {
                log.severe("Error indexing file '" + file.getAbsolutePath()
                    + "':  " + e.getMessage());
                rsp.add("indexErrors", "Error indexing file '"
                    + file.getAbsolutePath() + "':  " + e.getMessage());
            } catch (FileNotFoundException e) {
                log.severe("File not found '" + file.getAbsolutePath() + "':  "
                    + e.getMessage());
                rsp.add("indexErrors", "File not found '"
                    + file.getAbsolutePath() + "':  " + e.getMessage());
            }
        }
    }

    private void indexDoc(String pidOrFilename, InputStream foxmlStream,
        SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        JAXBContext jc = JAXBContext.newInstance("au.edu.usq.fedora.foxml");
        Unmarshaller u = jc.createUnmarshaller();
        DigitalObject obj = (DigitalObject) u.unmarshal(foxmlStream);
        Collection<ContentStream> streams = new ArrayList<ContentStream>();
        buildSolrDocument(req, req.getParams().get("stream.contentType"),
            streams, obj);
        ((SolrQueryRequestBase) req).setContentStreams(streams);
        try {
            super.handleRequestBody(req, rsp);
        } catch (Exception e) {
            log.severe("Error indexing file '" + pidOrFilename + "': "
                + e.getMessage());
            rsp.add("indexErrors", "Error indexing file '" + pidOrFilename
                + "': " + e.getMessage());
        }
    }

    private File index(Item item, String pid, String dsId, String collection,
        Reader in, File ruleScript) throws IOException, RuleException {
        File solrFile = File.createTempFile("solr", ".xml");
        Writer out = new OutputStreamWriter(new FileOutputStream(solrFile),
            "UTF-8");
        try {
            PythonInterpreter python = new PythonInterpreter();
            RuleManager rules = new RuleManager();
            python.set("self", this);
            python.set("rules", rules);
            python.set("pid", pid);
            python.set("dsId", dsId);
            python.set("collection", collection);
            python.set("item", item);
            python.execfile(ruleScript.getAbsolutePath());
            rules.run(in, out);
            if (rules.cancelled()) {
                log.info("Indexing rules were cancelled");
                return null;
            }
            python.cleanup();
        } catch (Exception e) {
            throw new RuleException(e);
        } finally {
            in.close();
            out.close();
        }
        return solrFile;
    }

    // Helper methods for use in rules.py

    public InputStream getResource(String path) {
        return getClass().getResourceAsStream(path);
    }

    public String getFullText(String contentType, InputStream content) {
        // only support full text from pdfs right now
        if ("application/pdf".equals(contentType)) {
            PDDocument pdfDoc = null;
            try {
                pdfDoc = PDDocument.load(content);
                if (pdfDoc.isEncrypted()) {
                    log.info("Attempting to decrypt PDF...");
                    pdfDoc.decrypt("");
                }
                PDFTextStripper stripper = new PDFTextStripper();
                File textFile = File.createTempFile("fulltext", ".txt");
                FileWriter writer = new FileWriter(textFile);
                writer.write(stripper.getText(pdfDoc));
                return textFile.getAbsolutePath();
            } catch (IOException ioe) {
                log.severe("Failed to get full text: " + ioe.getMessage());
            } catch (InvalidPasswordException ipe) {
                log.warning("Failed to decrypt: " + ipe.getMessage());
            } catch (CryptographyException ce) {
                log.warning("Failed to decrypt: " + ce.getMessage());
            } finally {
                if (pdfDoc != null) {
                    try {
                        pdfDoc.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        } else {
            log.warning("Unable to extract full text from content with type: "
                + contentType);
        }
        return null;
    }

    private Properties getMeta(String pid) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.get(pid, "SOF-META", out);
        Properties props = new Properties();
        props.load(new ByteArrayInputStream(out.toByteArray()));
        String itemPid = props.getProperty("item.pid");
        String rulesPid = props.getProperty("rules.pid");
        log.info("item.pid: " + itemPid);
        log.info("rules.pid: " + rulesPid);
        return props;
    }
}
