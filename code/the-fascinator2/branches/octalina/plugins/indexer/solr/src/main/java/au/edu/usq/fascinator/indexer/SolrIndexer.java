/* 
 * The Fascinator - Indexer
 * Copyright (C) 2009-2010 University of Southern Queensland
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginDescription;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
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
import au.edu.usq.fascinator.common.PythonUtils;

public class SolrIndexer implements Indexer {

    /** Default payload for object metadata */
    private static String DEFAULT_METADATA_PAYLOAD = "TF-OBJ-META";

    /** Actual payload for object metadata */
    private String propertiesId;

    /** Logging */
    private Logger log = LoggerFactory.getLogger(SolrIndexer.class);

    /** Configuration */
    private JsonConfig config;

    /** Storage API */
    private Storage storage;

    /** Main Solr core */
    private SolrServer solr;

    /** Anotar core */
    private SolrServer anotar;

    /** Auto-commit flag for main core */
    private boolean autoCommit;

    /** Auto-commit flag for anotar core */
    private boolean anotarAutoCommit;

    /** List of temporary files to clean up later */
    private List<File> tempFiles;

    /** Username for Solr */
    private String username;

    /** Password for Solr */
    private String password;

    /** Flag if init() has been run before */
    private boolean loaded;

    /** Keep track of custom parameters */
    private Map<String, String> customParams;

    /** Utility function for use inside python scripts */
    private PythonUtils pyUtils;

    /** Cache of instantiated python scripts */
    private HashMap<String, PyObject> scriptCache;

    /** Flag for use of the cache */
    private boolean useCache;

    /**
     * Get the ID of the plugin
     * 
     * @return String : The ID of this plugin
     */
    @Override
    public String getId() {
        return "solr";
    }

    /**
     * Get the name of the plugin
     * 
     * @return String : The name of this plugin
     */
    @Override
    public String getName() {
        return "Apache Solr Indexer";
    }

    /**
     * Gets a PluginDescription object relating to this plugin.
     * 
     * @return a PluginDescription
     */
    @Override
    public PluginDescription getPluginDetails() {
        return new PluginDescription(this);
    }

    public SolrIndexer() {
        loaded = false;
    }

    /**
     * Initialize the plugin
     * 
     * @param jsonString The JSON configuration to use as a string
     * @throws IndexerException if errors occur during initialization
     */
    @Override
    public void init(String jsonString) throws IndexerException {
        try {
            config = new JsonConfig(new ByteArrayInputStream(
                    jsonString.getBytes("UTF-8")));
            init();
        } catch (UnsupportedEncodingException e) {
            throw new IndexerException(e);
        } catch (IOException e) {
            throw new IndexerException(e);
        }
    }

    /**
     * Initialize the plugin
     * 
     * @param jsonFile A file containing the JSON configuration
     * @throws IndexerException if errors occur during initialization
     */
    @Override
    public void init(File jsonFile) throws IndexerException {
        try {
            config = new JsonConfig(jsonFile);
            init();
        } catch (IOException ioe) {
            throw new IndexerException(ioe);
        }
    }

    /**
     * Private method wrapped by the above two methods to perform the actual
     * initialization after the JSON config is accessed.
     * 
     * @throws IndexerException if errors occur during initialization
     */
    private void init() throws IndexerException {
        if (!loaded) {
            loaded = true;

            String storageType = config.get("storage/type");
            try {
                storage = PluginManager.getStorage(storageType);
                storage.init(config.toString());
            } catch (PluginException pe) {
                throw new IndexerException(pe);
            }

            solr = initCore("solr");
            anotar = initCore("anotar");

            autoCommit = Boolean.parseBoolean(config.get(
                    "indexer/solr/autocommit", "true"));
            anotarAutoCommit = Boolean.parseBoolean(config.get(
                    "indexer/anotar/autocommit", "true"));
            propertiesId = config.get("indexer/propertiesId",
                    DEFAULT_METADATA_PAYLOAD);

            customParams = new HashMap<String, String>();

            try {
                pyUtils = new PythonUtils(config);
            } catch (PluginException ex) {
                throw new IndexerException(ex);
            }
            scriptCache = new HashMap<String, PyObject>();
            useCache = Boolean.parseBoolean(config.get("indexer/useCache",
                    "true"));
        }
        loaded = true;
    }

    /**
     * Set a value in the custom parameters of the indexer.
     * 
     * @param property : The index to use
     * @param value : The value to store
     */
    public void setCustomParam(String property, String value) {
        customParams.put(property, value);
    }

    /**
     * Initialize a Solr core object.
     * 
     * @param coreName : The core to initialize
     * @return SolrServer : The initialized core
     */
    private SolrServer initCore(String coreName) {
        try {
            URI solrUri = new URI(config.get("indexer/" + coreName + "/uri"));
            CommonsHttpSolrServer thisCore = new CommonsHttpSolrServer(
                    solrUri.toURL());
            username = config.get("indexer/" + coreName + "/username");
            password = config.get("indexer/" + coreName + "/password");
            if (username != null && password != null) {
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                        username, password);
                HttpClient hc = ((CommonsHttpSolrServer) solr).getHttpClient();
                hc.getParams().setAuthenticationPreemptive(true);
                hc.getState().setCredentials(AuthScope.ANY, credentials);
            }
            return thisCore;
        } catch (MalformedURLException mue) {
            log.error(coreName + " : Malformed URL", mue);
        } catch (URISyntaxException urise) {
            log.error(coreName + " : Invalid URI", urise);
        } catch (IOException ioe) {
            log.error(coreName + " : Failed to read Solr configuration", ioe);
        }
        return null;
    }

    /**
     * Shutdown the plugin
     * 
     * @throws PluginException if any errors occur during shutdown
     */
    @Override
    public void shutdown() throws PluginException {
        pyUtils.shutdown();
        cleanupTempFiles();
    }

    /**
     * Return a reference to this plugins instantiated storage layer
     * 
     * @return Storage : the storage API being used
     */
    public Storage getStorage() {
        return storage;
    }

    /**
     * Perform a Solr search and stream the results into the provided output
     * 
     * @param request : A prepared SearchRequest object
     * @param response : The OutputStream to send results to
     * @throws IndexerException if there were errors during the search
     */
    @Override
    public void search(SearchRequest request, OutputStream response)
            throws IndexerException {
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
            result = searcher.get(request.getQuery(), extras.toString(), false);
            IOUtils.copy(result, response);
            result.close();
        } catch (IOException ioe) {
            throw new IndexerException(ioe);
        }
    }

    /**
     * Remove the specified object from the index
     * 
     * @param oid : The identifier of the object to remove
     * @throws IndexerException if there were errors during removal
     */
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

    /**
     * Remove the specified payload from the index
     * 
     * @param oid : The identifier of the payload's object
     * @param pid : The identifier of the payload to remove
     * @throws IndexerException if there were errors during removal
     */
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

    /**
     * Remove all annotations from the index against an object
     * 
     * @param oid : The identifier of the object
     * @throws IndexerException if there were errors during removal
     */
    @Override
    public void annotateRemove(String oid) throws IndexerException {
        log.debug("Deleting " + oid + " from Anotar index");
        try {
            anotar.deleteByQuery("rootUri:\"" + oid + "\"");
            anotar.commit();
        } catch (SolrServerException sse) {
            throw new IndexerException(sse);
        } catch (IOException ioe) {
            throw new IndexerException(ioe);
        }
    }

    /**
     * Remove the specified annotation from the index
     * 
     * @param oid : The identifier of the object
     * @param annoId : The identifier of the annotation
     * @throws IndexerException if there were errors during removal
     */
    @Override
    public void annotateRemove(String oid, String annoId)
            throws IndexerException {
        log.debug("Deleting {}::{} from Anotar index", oid, annoId);
        try {
            anotar.deleteByQuery("rootUri:\"" + oid + "\" AND id:\"" + annoId
                    + "\"");
            anotar.commit();
        } catch (SolrServerException sse) {
            throw new IndexerException(sse);
        } catch (IOException ioe) {
            throw new IndexerException(ioe);
        }
    }

    /**
     * Index an object and all of its payloads
     * 
     * @param oid : The identifier of the object
     * @throws IndexerException if there were errors during indexing
     */
    @Override
    public void index(String oid) throws IndexerException {
        try {
            DigitalObject object = storage.getObject(oid);
            // Some workflow actions create payloads, so we can't iterate
            // directly against the object.
            String[] oldManifest = {};
            oldManifest = object.getPayloadIdList().toArray(oldManifest);
            for (String payloadId : oldManifest) {
                Payload payload = object.getPayload(payloadId);
                index(object, payload);
            }
        } catch (StorageException ex) {
            throw new IndexerException(ex);
        }
    }

    /**
     * Index a specific payload
     * 
     * @param oid : The identifier of the payload's object
     * @param pid : The identifier of the payload
     * @throws IndexerException if there were errors during indexing
     */
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

    /**
     * Index a specific payload
     * 
     * @param object : The payload's object
     * @param pid : The payload
     * @throws IndexerException if there were errors during indexing
     */
    public void index(DigitalObject object, Payload payload)
            throws IndexerException {
        String oid = object.getId();
        String pid = payload.getId();

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
            DigitalObject confObj = storage.getObject(confOid);
            Payload confPayload = confObj.getPayload(confObj.getSourceId());
            // Get the rules script
            String rulesOid = props.getProperty("rulesOid");
            DigitalObject rulesObj = storage.getObject(rulesOid);
            Payload rulesPayload = rulesObj.getPayload(rulesObj.getSourceId());

            // index the object
            File solrFile = index(object, payload, confPayload.open(),
                    rulesPayload.open(), props);

            // Did the indexer alter metadata?
            String toClose = props.getProperty("objectRequiresClose");
            if (toClose != null) {
                log.debug("Indexing has altered metadata, closing object.");
                // log.debug("===> {}", props.getProperty("renderQueue"));
                props.remove("objectRequiresClose");
                object.close();
                try {
                    props = object.getMetadata();
                    // log.debug("===> {}", props.getProperty("renderQueue"));
                } catch (StorageException ex) {
                    throw new IndexerException("Failed loading properties : ",
                            ex);
                }
            }

            if (solrFile != null) {
                InputStream inputDoc = new FileInputStream(solrFile);
                String xml = IOUtils.toString(inputDoc, "UTF-8");
                inputDoc.close();
                SolrRequest update = new DirectXmlRequest("/update", xml);
                solr.request(update);
                if (autoCommit) {
                    log.debug("Running forced commit!");
                    solr.commit();
                }
            }
        } catch (Exception e) {
            log.error("Indexing failed!\n-----\n", e);
        } finally {
            cleanupTempFiles();
        }
    }

    /**
     * Call a manual commit against the index
     * 
     */
    @Override
    public void commit() {
        try {
            solr.commit();
        } catch (Exception e) {
            log.warn("Solr forced commit failed. Document will not be visible"
                    + " until Solr autocommit fires. Error message: {}", e);
        }
    }

    /**
     * Index an annotation
     * 
     * @param oid : The identifier of the annotation's object
     * @param pid : The identifier of the annotation
     * @throws IndexerException if there were errors during indexing
     */
    @Override
    public void annotate(String oid, String pid) throws IndexerException {
        // At this stage this is identical to the 'index()' method
        // above, but there may be changes at a later date.
        try {
            DigitalObject object = storage.getObject(oid);
            Payload payload = object.getPayload(pid);
            annotate(object, payload);
        } catch (StorageException ex) {
            throw new IndexerException(ex);
        }
    }

    /**
     * Index a specific annotation
     * 
     * @param object : The annotation's object
     * @param pid : The annotation payload
     * @throws IndexerException if there were errors during indexing
     */
    private void annotate(DigitalObject object, Payload payload)
            throws IndexerException {
        String oid = object.getId();
        String pid = payload.getId();
        if (propertiesId.equals(pid)) {
            return;
        }

        try {
            // Get the rules file
            InputStream rulesStream = getClass().getResourceAsStream(
                    "/anotar.py");

            File solrFile = null;
            Properties props = new Properties();
            props.setProperty("metaPid", pid);

            solrFile = index(object, payload, null, rulesStream, props);
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
            log.error("Indexing failed!\n-----\n", e);
        } finally {
            cleanupTempFiles();
        }
    }

    /**
     * Search for annotations and return the result to the provided stream
     * 
     * @param request : The SearchRequest object
     * @param response : The OutputStream to send responses to
     * @throws IndexerException if there were errors during indexing
     */
    @Override
    public void annotateSearch(SearchRequest request, OutputStream response)
            throws IndexerException {
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
            result = searcher.get(request.getQuery(), extras.toString(), false);
            IOUtils.copy(result, response);
            result.close();
        } catch (IOException ioe) {
            throw new IndexerException(ioe);
        }
    }

    /**
     * Index a payload using the provided data
     * 
     * @param object : The DigitalObject to index
     * @param payload : The Payload to index
     * @param inConf : An InputStream holding the config file
     * @param inRules : An InputStream holding the rules file
     * @param props : Properties object containing the object's metadata
     * @return File : Temporary file containing the output to index
     * @throws IOException if there were errors accessing files
     * @throws RuleException if there were errors during indexing
     */
    private File index(DigitalObject object, Payload payload,
            InputStream inConf, InputStream inRules, Properties props)
            throws IOException, RuleException {
        Reader in = new StringReader("<add><doc/></add>");
        return index(object, payload, in, inConf, inRules, props);
    }

    /**
     * Index a payload using the provided data
     * 
     * @param object : The DigitalObject to index
     * @param payload : The Payload to index
     * @param in : Reader containing the new empty document
     * @param inConf : An InputStream holding the config file
     * @param inRules : An InputStream holding the rules file
     * @param props : Properties object containing the object's metadata
     * @return File : Temporary file containing the output to index
     * @throws IOException if there were errors accessing files
     * @throws RuleException if there were errors during indexing
     */
    private File index(DigitalObject object, Payload payload, Reader in,
            InputStream inConf, InputStream inRules, Properties props)
            throws IOException, RuleException {
        File solrFile = createTempFile("solr", ".xml");
        Writer out = new OutputStreamWriter(new FileOutputStream(solrFile),
                "UTF-8");
        InputStream rulesIn = null;
        try {
            // Make our harvest config more useful
            JsonConfigHelper jsonConfig = null;
            if (inConf == null) {
                jsonConfig = new JsonConfigHelper();
            } else {
                jsonConfig = new JsonConfigHelper(inConf);
                inConf.close();
            }

            PythonInterpreter python = new PythonInterpreter();

            RuleManager rules = new RuleManager();
            python.set("indexer", this);
            python.set("jsonConfig", jsonConfig);
            python.set("rules", rules);
            python.set("object", object);
            python.set("payload", payload);
            python.set("params", props);
            python.set("inputReader", in);
            python.set("pyUtils", pyUtils);
            python.execfile(inRules);
            rules.run(in, out);
            if (rules.cancelled()) {
                log.info("Indexing rules were cancelled");
                return null;
            }
            python.cleanup();
        } catch (Exception e) {
            throw new RuleException(e);
        } finally {
            if (inRules != null) {
                inRules.close();
            }
        }
        return solrFile;
    }

    /**
     * Create a temporary file
     * 
     * @param prefix : String to use at the beginning of the file's name
     * @param postfix : String to finish the file's name with
     * @return File : A new file in the system's temp directory
     * @throws IOException if there was an error creating the file
     */
    private File createTempFile(String prefix, String postfix)
            throws IOException {
        File tempFile = File.createTempFile(prefix, postfix);
        tempFile.deleteOnExit();
        if (tempFiles == null) {
            tempFiles = Collections.synchronizedList(new ArrayList<File>());
        }
        tempFiles.add(tempFile);
        return tempFile;
    }

    /**
     * Remove all temp files the plugin has
     * 
     */
    private void cleanupTempFiles() {
        if (tempFiles != null) {
            for (File tempFile : tempFiles) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
            tempFiles = null;
        }
    }

    /**
     * Add a python object to the cache if caching if configured
     * 
     * @param oid : The rules OID to use as an index
     * @param pyObject : The compiled PyObject to cache
     */
    private void cachePythonObject(String oid, PyObject pyObject) {
        if (useCache) {
            scriptCache.put(oid, pyObject);
        }
    }

    /**
     * Return a python object from the cache if configured
     * 
     * @param oid : The rules OID to retrieve if cached
     * @return PyObject : The cached object, null if not found
     */
    private PyObject getPythonObject(String oid) {
        if (useCache && scriptCache.containsKey(oid)) {
            return scriptCache.get(oid);
        }
        return null;
    }
}
