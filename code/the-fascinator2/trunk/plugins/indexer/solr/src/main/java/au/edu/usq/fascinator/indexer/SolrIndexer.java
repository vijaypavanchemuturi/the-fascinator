/* 
 * The Fascinator - Indexer
 * Copyright (C) 2009 University of Southern Queensland
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
package au.edu.usq.fascinator.indexer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.JSONResponseWriter;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXReader;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.access.AccessControlException;
import au.edu.usq.fascinator.api.access.AccessControlManager;
import au.edu.usq.fascinator.api.access.AccessControlSchema;
import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.api.indexer.IndexerException;
import au.edu.usq.fascinator.api.indexer.SearchRequest;
import au.edu.usq.fascinator.api.indexer.rule.RuleException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;

public class SolrIndexer implements Indexer {

    private static final String DEFAULT_SOLR_HOME = System
            .getProperty("user.home")
            + File.separator + ".fascinator" + File.separator + "solr";
    private static String DEFAULT_METADATA_PAYLOAD = "TF-OBJ-META";

    private Logger log = LoggerFactory.getLogger(SolrIndexer.class);
    private JsonConfig config;

    private AccessControlManager access;
    private Storage storage;

    private SolrServer solr;
    private SolrServer anotar;
    private CoreContainer coreContainer;
    private boolean autoCommit;
    private boolean anotarAutoCommit;

    private String propertiesId;

    private List<File> tempFiles;

    private SAXReader saxReader;

    private Map<String, String> namespaces;

    private String username;
    private String password;

    private boolean loaded;

    private Map<String, String> customParams;

    // Cache paths of rules/conf files we've seen
    private Map<String, File> rulesList;
    private Map<String, File> confList;

    private PythonInterpreter python;

    @Override
    public String getId() {
        return "solr";
    }

    @Override
    public String getName() {
        return "Apache Solr Indexer";
    }

    public SolrIndexer() {
        loaded = false;
    }

    @Override
    public void init(String jsonString) throws PluginException {
        // TODO Auto-generated method stub
    }

    @Override
    public void init(File jsonFile) throws IndexerException {
        if (!loaded) {
            loaded = true;
            try {
                config = new JsonConfig(jsonFile);
            } catch (IOException ioe) {
                throw new IndexerException(ioe);
            }

            String accessControlType = "accessmanager";
            try {
                access = PluginManager.getAccessManager(accessControlType);
                access.init(jsonFile);
            } catch (PluginException pe) {
                log.error("Failed to load access manager: {}",
                        accessControlType);
            }

            String storageType = config.get("storage/type");
            try {
                storage = PluginManager.getStorage(storageType);
                storage.init(jsonFile);
            } catch (PluginException pe) {
                log.error("Failed to load storage plugin: {}", storageType);
            }

            solr = initCore("solr");
            anotar = initCore("anotar");

            autoCommit = Boolean.parseBoolean(config.get(
                    "indexer/solr/autocommit", "true"));
            anotarAutoCommit = Boolean.parseBoolean(config.get(
                    "indexer/anotar/autocommit", "true"));
            propertiesId = config.get("indexer/propertiesId",
                    DEFAULT_METADATA_PAYLOAD);

            namespaces = new HashMap<String, String>();
            DocumentFactory docFactory = new DocumentFactory();
            docFactory.setXPathNamespaceURIs(namespaces);

            saxReader = new SAXReader(docFactory);

            customParams = new HashMap<String, String>();

            rulesList = new HashMap<String, File>();
            confList = new HashMap<String, File>();

            python = new PythonInterpreter();
        }
        loaded = true;
    }

    public void setCustomParam(String property, String value) {
        customParams.put(property, value);
    }

    private SolrServer initCore(String coreName) {
        try {
            boolean isEmbedded = Boolean.parseBoolean(config.get("indexer/"
                    + coreName + "/embedded"));
            if (isEmbedded) {
                /* TODO - Fix embedded for Solr 1.4 */
                String home = config.get("indexer/" + coreName + "/home",
                        DEFAULT_SOLR_HOME);
                log.info("home={}", home);
                File homeDir = new File(home);
                if (!homeDir.exists()) {
                    log.info("Preparing default Solr home...");
                    prepareHome(homeDir);
                }
                System.setProperty("solr.solr.home", homeDir.getAbsolutePath());
                File coreXmlFile = new File(homeDir, "solr.xml");
                coreContainer = new CoreContainer(homeDir.getAbsolutePath(),
                        coreXmlFile);
                for (SolrCore core : coreContainer.getCores()) {
                    log.info("loaded core: {}", core.getName());
                }
                return new EmbeddedSolrServer(coreContainer, "search");
            } else {
                URI solrUri = new URI(config
                        .get("indexer/" + coreName + "/uri"));
                CommonsHttpSolrServer thisCore = new CommonsHttpSolrServer(
                        solrUri.toURL());
                username = config.get("indexer/" + coreName + "/username");
                password = config.get("indexer/" + coreName + "/password");
                if (username != null && password != null) {
                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                            username, password);
                    HttpClient hc = ((CommonsHttpSolrServer) solr)
                            .getHttpClient();
                    hc.getParams().setAuthenticationPreemptive(true);
                    hc.getState().setCredentials(AuthScope.ANY, credentials);
                }
                return thisCore;
            }
        } catch (MalformedURLException mue) {
            log.error(coreName + " : Malformed URL", mue);
        } catch (URISyntaxException urise) {
            log.error(coreName + " : Invalid URI", urise);
        } catch (IOException ioe) {
            log.error(coreName + " : Failed to read Solr configuration", ioe);
        } catch (ParserConfigurationException pce) {
            log.error(coreName + " : Failed to parse Solr configuration", pce);
        } catch (SAXException saxe) {
            log.error(coreName + " : Failed to load Solr configuration", saxe);
        }
        return null;
    }

    private void prepareHome(File homeDir) throws IOException {
        try {
            URI defaultSolrUri = getClass().getResource("/solr").toURI();
            log.info("defaultSolrUri={},url={}", defaultSolrUri, defaultSolrUri
                    .getScheme());
            FileUtils.copyURLToFile(defaultSolrUri.toURL(), homeDir);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() throws PluginException {
        if (coreContainer != null) {
            coreContainer.shutdown();
        }
        cleanupTempFiles();
    }

    public Storage getStorage() {
        return storage;
    }

    @Override
    public void search(SearchRequest request, OutputStream response)
            throws IndexerException {
        if (solr instanceof EmbeddedSolrServer) {
            EmbeddedSolrServer ess = ((EmbeddedSolrServer) solr);
            SolrQuery query = new SolrQuery();
            query.setQuery(request.getQuery());
            for (String name : request.getParamsMap().keySet()) {
                Set<String> values = request.getParams(name);
                query.setParam(name, values.toArray(new String[] {}));
            }
            try {
                QueryResponse resp = ess.query(query);
                JSONResponseWriter jrw = new JSONResponseWriter();
                jrw.init(resp.getResponse());
                SolrQueryRequest a = new LocalSolrQueryRequest(coreContainer
                        .getCore("search"), query);
                SolrQueryResponse b = new SolrQueryResponse();
                b.setAllValues(resp.getResponse());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Writer w = new OutputStreamWriter(out);
                jrw.write(w, a, b);
                w.close();
                log.info("out={}", out.toString("UTF-8"));
                IOUtils.copy(new ByteArrayInputStream(out.toByteArray()),
                        response);
            } catch (SolrServerException sse) {
                sse.printStackTrace();
                throw new IndexerException(sse);
            } catch (IOException ioe) {
                throw new IndexerException(ioe);
            }
        } else {
            SolrSearcher searcher = new SolrSearcher(
                    ((CommonsHttpSolrServer) solr).getBaseURL());
            if (username != null && password != null) {
                searcher.authenticate(username, password);
            }
            InputStream result;
            try {
                StringBuilder extras = new StringBuilder();
                for (String name : request.getParamsMap().keySet()) {
                    for (String param : request.getParams(name)) {
                        extras.append(name);
                        extras.append("=");
                        extras.append(URLEncoder.encode(param, "UTF-8"));
                        extras.append("&");
                    }
                }
                extras.append("wt=json");
                result = searcher.get(request.getQuery(), extras.toString(),
                        false);
                IOUtils.copy(result, response);
                result.close();
            } catch (IOException ioe) {
                throw new IndexerException(ioe);
            }
        }
    }

    @Override
    public void remove(String oid) throws IndexerException {
        log.debug("Deleting " + oid + " from index");
        try {
            solr.deleteByQuery("storage_id:\"" + oid + "\"");
            solr.commit();
        } catch (SolrServerException sse) {
            throw new IndexerException(sse);
        } catch (IOException ioe) {
            throw new IndexerException(ioe);
        }
    }

    @Override
    public void remove(String oid, String pid) throws IndexerException {
        log.debug("Deleting {}::{} from index", oid, pid);
        try {
            solr.deleteByQuery("storage_id:\"" + oid + "\" AND identifer:\""
                    + pid + "\"");
            solr.commit();
        } catch (SolrServerException sse) {
            throw new IndexerException(sse);
        } catch (IOException ioe) {
            throw new IndexerException(ioe);
        }
    }

    @Override
    public void index(String oid) throws IndexerException {
        try {
            DigitalObject object = storage.getObject(oid);
            Set<String> payloadIdList = object.getPayloadIdList();
            for (String payloadId : payloadIdList) {
                Payload payload = object.getPayload(payloadId);
                index(object, payload);
            }
        } catch (StorageException ex) {
            throw new IndexerException(ex);
        }
    }

    @Override
    public void index(String oid, String pid) throws IndexerException {
        try {
            DigitalObject object = storage.getObject(oid);
            Payload payload = object.getPayload(pid);
            index(object, payload);
        } catch (StorageException ex) {
            throw new IndexerException(ex);
        }
    }

    public void index(DigitalObject object, Payload payload)
            throws IndexerException {
        String oid = object.getId();
        String pid = payload.getId();

        // if (propertiesId.equals(pid)) {
        // return;
        // }

        // Don't proccess annotations through this function
        if (pid.startsWith("anotar.")) {
            annotate(object, payload);
            return;
        }
        log.info("Indexing OID:'" + oid + "', PID: '" + pid + "'");

        // get the indexer properties or we can't index
        Properties props;
        try {
            props = object.getMetadata();
        } catch (StorageException ex) {
            throw new IndexerException("Failed loading properties : ", ex);
        }

        try {
            // Get the harvester config
            String confOid = props.getProperty("jsonConfigOid");
            File confFile = null;
            // Have we already cached this one?
            if (confList.containsKey(confOid)) {
                confFile = confList.get(confOid);
                // No... cache it
            } else {
                confFile = copyObjectToFile(confOid);
                confList.put(confOid, confFile);
            }

            // Get the rules file
            String rulesOid = props.getProperty("rulesOid");
            File rulesFile = null;
            // Have we already cached this one?
            if (rulesList.containsKey(rulesOid)) {
                rulesFile = rulesList.get(rulesOid);
                // No... cache it
            } else {
                rulesFile = copyObjectToFile(rulesOid);
                rulesList.put(rulesOid, rulesFile);
            }

            // index the object
            File solrFile = index(object, payload, confFile, rulesFile, props);

            if (solrFile != null) {
                InputStream inputDoc = new FileInputStream(solrFile);
                String xml = IOUtils.toString(inputDoc, "UTF-8");
                inputDoc.close();
                SolrRequest update = new DirectXmlRequest("/update", xml);
                solr.request(update);
                if (autoCommit) {
                    solr.commit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Indexing failed!\n-----\n{}\n-----\n", e.getMessage());
        }
    }

    @Override
    public void annotate(String oid, String pid) throws IndexerException {
        try {
            DigitalObject object = storage.getObject(oid);
            Payload payload = object.getPayload(pid);
            index(object, payload);
        } catch (StorageException ex) {
            throw new IndexerException(ex);
        }
    }

    private void annotate(DigitalObject object, Payload payload)
            throws IndexerException {
        String oid = object.getId();
        String pid = payload.getId();
        if (propertiesId.equals(pid)) {
            return;
        }

        try {
            // Get the rules file
            String rulesOid = "anotar.py";
            File rulesFile = null;
            // Have we already cached this one?
            if (rulesList.containsKey(rulesOid)) {
                rulesFile = rulesList.get(rulesOid);
                // No... cache it
            } else {
                rulesFile = createTempFile("rules", ".script");
                FileOutputStream rulesOut = new FileOutputStream(rulesFile);
                IOUtils.copy(getClass().getResourceAsStream("/anotar.py"),
                        rulesOut);
                rulesOut.close();
                rulesList.put(rulesOid, rulesFile);
            }

            File solrFile = null;
            Properties props = new Properties();
            props.setProperty("metaPid", pid);

            solrFile = index(object, payload, null, rulesFile, props);
            if (solrFile != null) {
                InputStream inputDoc = new FileInputStream(solrFile);
                String xml = IOUtils.toString(inputDoc, "UTF-8");
                inputDoc.close();
                SolrRequest update = new DirectXmlRequest("/update", xml);
                anotar.request(update);
                if (anotarAutoCommit) {
                    anotar.commit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Indexing failed!\n-----\n{}\n-----\n", e.getMessage());
        } finally {
            cleanupTempFiles();

        }
    }

    @Override
    public void annotateSearch(SearchRequest request, OutputStream response)
            throws IndexerException {
        if (anotar instanceof EmbeddedSolrServer) {
            EmbeddedSolrServer ess = ((EmbeddedSolrServer) anotar);
            SolrQuery query = new SolrQuery();
            query.setQuery(request.getQuery());
            for (String name : request.getParamsMap().keySet()) {
                Set<String> values = request.getParams(name);
                query.setParam(name, values.toArray(new String[] {}));
            }
            try {
                QueryResponse resp = ess.query(query);
                JSONResponseWriter jrw = new JSONResponseWriter();
                jrw.init(resp.getResponse());
                SolrQueryRequest a = new LocalSolrQueryRequest(coreContainer
                        .getCore("search"), query);
                SolrQueryResponse b = new SolrQueryResponse();
                b.setAllValues(resp.getResponse());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Writer w = new OutputStreamWriter(out);
                jrw.write(w, a, b);
                w.close();
                log.info("out={}", out.toString("UTF-8"));
                IOUtils.copy(new ByteArrayInputStream(out.toByteArray()),
                        response);
            } catch (SolrServerException sse) {
                sse.printStackTrace();
                throw new IndexerException(sse);
            } catch (IOException ioe) {
                throw new IndexerException(ioe);
            }
        } else {
            SolrSearcher searcher = new SolrSearcher(
                    ((CommonsHttpSolrServer) anotar).getBaseURL());
            if (username != null && password != null) {
                searcher.authenticate(username, password);
            }
            InputStream result;
            try {
                StringBuilder extras = new StringBuilder();
                for (String name : request.getParamsMap().keySet()) {
                    for (String param : request.getParams(name)) {
                        extras.append(name);
                        extras.append("=");
                        extras.append(URLEncoder.encode(param, "UTF-8"));
                        extras.append("&");
                    }
                }
                extras.append("wt=json");
                result = searcher.get(request.getQuery(), extras.toString(),
                        false);
                IOUtils.copy(result, response);
                result.close();
            } catch (IOException ioe) {
                throw new IndexerException(ioe);
            }
        }
    }

    private File index(DigitalObject object, Payload payload, File confFile,
            File rulesFile, Properties props) throws IOException, RuleException {
        Reader in = new StringReader("<add><doc/></add>");
        return index(object, payload, in, confFile, rulesFile, props);
    }

    private File index(DigitalObject object, Payload payload, Reader in,
            File confFile, File ruleScript, Properties props)
            throws IOException, RuleException {
        File solrFile = createTempFile("solr", ".xml");
        Writer out = new OutputStreamWriter(new FileOutputStream(solrFile),
                "UTF-8");
        InputStream rulesIn = null;
        try {
            // Make our harvest config more useful
            JsonConfigHelper jsonConfig = null;
            if (confFile == null) {
                jsonConfig = new JsonConfigHelper();
            } else {
                jsonConfig = new JsonConfigHelper(confFile);
            }

            rulesIn = new FileInputStream(ruleScript);
            RuleManager rules = new RuleManager();
            python.set("indexer", this);
            python.set("jsonConfig", jsonConfig);
            python.set("rules", rules);
            python.set("object", object);
            python.set("payload", payload);
            python.set("params", props);
            python.set("inputReader", in);
            python.execfile(rulesIn);
            rules.run(in, out);
            if (rules.cancelled()) {
                log.info("Indexing rules were cancelled");
                return null;
            }
            python.cleanup();
        } catch (Exception e) {
            throw new RuleException(e);
        } finally {
            if (rulesIn != null) {
                rulesIn.close();
            }
            in.close();
            out.close();
        }
        return solrFile;
    }

    private File createTempFile(String prefix, String postfix)
            throws IOException {
        File tempFile = File.createTempFile(prefix, postfix);
        if (tempFiles == null) {
            tempFiles = Collections.synchronizedList(new ArrayList<File>());
        }
        tempFiles.add(tempFile);
        return tempFile;
    }

    private void cleanupTempFiles() {
        if (tempFiles != null) {
            for (File tempFile : tempFiles) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
            tempFiles = null;
        }

        // Clear the lists of specific types we keep
        rulesList = new HashMap<String, File>();
        confList = new HashMap<String, File>();
    }

    // Helper methods

    public InputStream getResource(String path) {
        return getClass().getResourceAsStream(path);
    }

    public Document getXmlDocument(Payload payload) {
        try {
            Document doc = getXmlDocument(payload.open());
            payload.close();
            return doc;
        } catch (StorageException ex) {
            log.error("Failed to access payload", ex);
        }
        return null;
    }

    public Document getXmlDocument(InputStream xmlIn) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(xmlIn, "UTF-8");
            return saxReader.read(reader);
        } catch (UnsupportedEncodingException uee) {
        } catch (DocumentException de) {
            log.error("Failed to parse XML", de);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                }
            }
        }
        return null;
    }

    public JsonConfigHelper getJsonObject(InputStream in) {
        try {
            return new JsonConfigHelper(in);
        } catch (IOException ex) {
            log.error("Failure during stream access", ex);
            return null;
        }
    }

    public Model getRdfModel(Payload payload) {
        try {
            Model model = getRdfModel(payload.open());
            payload.close();
            return model;
        } catch (StorageException ioe) {
            log.info("Failed to read payload stream", ioe);
        }
        return null;
    }

    public Model getRdfModel(InputStream rdfIn) {
        Model model = null;
        Reader reader = null;
        try {
            reader = new InputStreamReader(rdfIn, "UTF-8");
            model = RDF2Go.getModelFactory().createModel();
            model.open();
            model.readFrom(reader);
        } catch (ModelRuntimeException mre) {
            log.error("Failed to create RDF model", mre);
        } catch (IOException ioe) {
            log.error("Failed to read RDF input", ioe);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                }
            }
        }
        return model;
    }

    public AccessControlSchema getAccessSchema(String plugin) {
        if (access == null) {
            return null;
        }
        access.setActivePlugin(plugin);
        return access.getEmptySchema();
    }

    public void setAccessSchema(AccessControlSchema schema, String plugin) {
        if (access == null) {
            return;
        }

        try {
            access.setActivePlugin(plugin);
            access.applySchema(schema);
        } catch (AccessControlException ex) {
            log.error("Failed to query security plugin for roles", ex);
        }
    }

    public List<String> getRolesWithAccess(String recordId) {
        if (access == null) {
            return null;
        }
        try {
            return access.getRoles(recordId);
        } catch (AccessControlException ex) {
            log.error("Failed to query security plugin for roles", ex);
            return null;
        }
    }

    public void registerNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
    }

    public void unregisterNamespace(String prefix) {
        namespaces.remove(prefix);
    }

    private File copyObjectToFile(String oid) throws IOException,
            StorageException {
        // Temp file
        File tempFile = createTempFile("objectOutput", ".temp");
        FileOutputStream tempFileOut = new FileOutputStream(tempFile);
        // Payload from storage
        DigitalObject tempObj = storage.getObject(oid);
        String tempPid = tempObj.getSourceId();
        Payload tempPayload = tempObj.getPayload(tempPid);
        // Copy and close
        IOUtils.copy(tempPayload.open(), tempFileOut);
        tempPayload.close();
        tempFileOut.close();

        // log.debug("tempFile size: {}, {}", oid, tempFile.length());

        return tempFile;
    }
}
