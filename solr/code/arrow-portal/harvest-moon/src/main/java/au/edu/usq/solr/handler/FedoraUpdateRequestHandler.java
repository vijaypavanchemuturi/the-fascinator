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
import org.python.util.PythonInterpreter;

import au.edu.usq.fedora.foxml.DatastreamType;
import au.edu.usq.fedora.foxml.DigitalObject;
import au.edu.usq.solr.fedora.FedoraRestClient;
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

    public void init(NamedList args) {
        super.init(args);
        log.info("Initialising Fedora handler...");
        String fedoraUrl = (String) args.get("fedoraUrl");
        String fedoraUser = (String) args.get("fedoraUser");
        String fedoraPass = (String) args.get("fedoraPass");
        client = new FedoraRestClient(fedoraUrl);
        client.authenticate(fedoraUser, fedoraPass);
        log.info("Done initialising Fedora handler...");
    }

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
                String deleteQuery = "<delete><id>" + pid + "</id></delete>";
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
                if ("DC".equals(dsId) || "SOF-META".equals(dsId)
                    || "AUDIT".equals(dsId)) {
                    // ignore
                    log.info("Ignoring datastream: " + dsId);
                } else if ("DC0".equals(dsId)) {
                    log.info("Indexing Dublin Core: " + dsId);
                    ByteArrayOutputStream dcOut = new ByteArrayOutputStream();
                    client.get(pid, "DC0", dcOut);
                    Reader in = new InputStreamReader(new ByteArrayInputStream(
                        dcOut.toByteArray()));
                    File solrFile = index(item, pid, null, in, rulesFile);
                    ContentStream stream = new ContentStreamBase.FileStream(
                        solrFile);
                    streams.add(stream);
                } else {
                    log.info("Indexing Datastream: " + dsId);
                    Reader in = new StringReader("<add><doc/></add>");
                    File solrFile = index(item, pid, dsId, in, rulesFile);
                    ContentStream stream = new ContentStreamBase.FileStream(
                        solrFile);
                    streams.add(stream);
                }
            } catch (RuleException re) {
                log.warning(re.getMessage());
            }
        }
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

    private File index(Item item, String pid, String dsId, Reader in,
        File ruleScript) throws IOException, RuleException {
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
            python.set("item", item);
            python.execfile(ruleScript.getAbsolutePath());
            rules.run(in, out);
            python.cleanup();
        } catch (Exception e) {
            throw new RuleException(e);
        } finally {
            in.close();
            out.close();
        }
        return solrFile;
    }

    public InputStream getResource(String path) {
        return getClass().getResourceAsStream(path);
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
