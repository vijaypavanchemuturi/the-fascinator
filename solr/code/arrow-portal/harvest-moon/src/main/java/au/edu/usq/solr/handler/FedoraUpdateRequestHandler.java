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
import javax.xml.transform.TransformerException;

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
import au.edu.usq.solr.harvest.impl.FedoraItem;
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
                buildSolrDocument(req, contentType, streams, pid);
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
        Collection<ContentStream> streams, String pid) throws IOException,
        JAXBException {
        ByteArrayOutputStream foxmlOut = new ByteArrayOutputStream();
        client.export(pid, foxmlOut);
        JAXBContext jc = JAXBContext.newInstance("au.edu.usq.fedora.foxml");
        Unmarshaller u = jc.createUnmarshaller();
        DigitalObject obj = (DigitalObject) u.unmarshal(new ByteArrayInputStream(
            foxmlOut.toByteArray()));
        FedoraItem item = new FedoraItem(client, pid);
        String rulesPid = getRulesPid(pid);
        File rulesFile = File.createTempFile("rules", ".py");
        FileOutputStream rulesOut = new FileOutputStream(rulesFile);
        client.get(rulesPid, "RULES.PY", rulesOut);
        rulesOut.close();
        for (DatastreamType ds : obj.getDatastream()) {
            String dsId = ds.getID();
            try {
                if ("DC".equals(dsId) || "SOF-META".equals(dsId)
                    || "AUDIT".equals(dsId)) {
                    // ignore
                } else if ("DC0".equals(dsId)) {
                    ByteArrayOutputStream dcOut = new ByteArrayOutputStream();
                    client.get(pid, "DC0", dcOut);
                    Reader in = new InputStreamReader(new ByteArrayInputStream(
                        dcOut.toByteArray()));
                    File solrFile = index(item, pid, null, in, rulesFile);
                    ContentStream stream = new ContentStreamBase.FileStream(
                        solrFile);
                    streams.add(stream);
                } else {
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

    private void buildSolrDocument(SolrQueryRequest req, String contentType,
        Collection<ContentStream> streams, InputStream is) throws IOException,
        TransformerException {

        // // :TODO: NOTE: no transform caching at the moment
        // Transformer t =
        // TransformerProvider.instance.getTransformer(foxmlXslt,
        // 0);
        // // Set parameters
        // t.setParameter("FEDORASOAP", fedoraUrl);
        // t.setParameter("FEDORAUSER", fedoraUser);
        // t.setParameter("FEDORAPASS", fedoraPass);
        //
        // StringWriter transformResult = new StringWriter();
        // t.transform(new StreamSource(is), new StreamResult(transformResult));
        // // Build stream body
        // ContentStreamBase stream = new ContentStreamBase.StringStream(
        // transformResult.toString());
        // if (contentType != null) {
        // stream.setContentType(contentType);
        // }
        // streams.add(stream);
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
        Collection<ContentStream> streams = new ArrayList<ContentStream>();
        buildSolrDocument(req, req.getParams().get("stream.contentType"),
            streams, foxmlStream);
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

    private String getRulesPid(String pid) throws IOException {
        String rulesPid = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.get(pid, "SOF-META", out);
        Properties props = new Properties();
        props.load(new ByteArrayInputStream(out.toByteArray()));
        rulesPid = props.getProperty("rules.pid");
        log.info("Found rules.pid: " + rulesPid);
        return rulesPid;
    }
}
