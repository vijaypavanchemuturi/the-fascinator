package au.edu.usq.solr.fedora;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;

import fedora.common.http.HttpInputStream;
import fedora.common.http.WebClient;
import fedora.server.Context;
import fedora.server.Server;
import fedora.server.errors.ModuleInitializationException;
import fedora.server.errors.ServerException;
import fedora.server.storage.DefaultDOManager;
import fedora.server.storage.types.Datastream;
import fedora.server.storage.types.DigitalObject;

public class SolrDOManager extends DefaultDOManager {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(SolrDOManager.class);

    /** Required param: URL of Solr REST interface. */
    public static final String SOLR_URL = "solrUrl";

    /** Optional param: User to authenticate to Solr as. */
    public static final String SOLR_USERNAME = "solrUsername";

    /** Optional param: Password to use for Solr authentication. */
    public static final String SOLR_PASSWORD = "solrPassword";

    public static final String SOLR_SYNC_UPDATE = "syncUpdate";

    /** Configured value for SOLR_REST_URL parameter. */
    private String solrUrl;

    private boolean syncUpdate;

    /** Credentials we'll use for Solr authentication, if enabled. */
    private UsernamePasswordCredentials solrCredentials;

    /** HTTP client we'll use for sending Solr update signals. */
    private WebClient webClient;

    /**
     * Delegates construction to the superclass.
     */
    public SolrDOManager(Map moduleParameters, Server server, String role)
        throws ModuleInitializationException {
        super(moduleParameters, server, role);
    }

    //
    // Overrides of DefaultDOManager methods
    //

    /**
     * Performs superclass post-initialization, then completes initialization
     * using Solr-specific parameters.
     */
    public void postInitModule() throws ModuleInitializationException {

        super.postInitModule();

        // validate required param: SOLR_REST_URL
        solrUrl = getParameter(SOLR_URL);
        LOG.info("Solr URL: " + solrUrl);
        syncUpdate = getParameter(SOLR_SYNC_UPDATE) != null ? Boolean.valueOf(getParameter(SOLR_SYNC_UPDATE))
            : false;
        if (solrUrl == null) {
            throw new ModuleInitializationException("Required parameter, "
                + SOLR_URL + " was not specified", getRole());
        } else {
            try {
                new URL(solrUrl);
                LOG.debug("Configured Solr REST URL: " + solrUrl);
            } catch (MalformedURLException e) {
                throw new ModuleInitializationException("Malformed URL given "
                    + "for " + SOLR_URL + " parameter: " + solrUrl, getRole());
            }
        }

        // validate credentials: if SOLR_USERNAME is given, SOLR_PASSWORD
        // should also be.
        String user = getParameter(SOLR_USERNAME);
        if (user != null) {
            LOG.debug("Will authenticate to Solr service as user: " + user);
            String pass = getParameter(SOLR_PASSWORD);
            if (pass != null) {
                solrCredentials = new UsernamePasswordCredentials(user, pass);
            } else {
                throw new ModuleInitializationException(SOLR_PASSWORD
                    + " must be specified because " + SOLR_USERNAME
                    + " was specified", getRole());
            }
        } else {
            LOG.debug(SOLR_USERNAME + " unspecified; will not attempt "
                + "to authenticate to Solr service");
        }

        // finally, init the http client we'll use
        webClient = new WebClient();
    }

    /**
     * Commits the changes to the given object as usual, then attempts to
     * propagate the change to the Solr service.
     */
    public void doCommit(boolean cachedObjectRequired, Context context,
        DigitalObject obj, String logMessage, boolean remove)
        throws ServerException {
        LOG.info("doCommit, pid: " + obj.getPid());
        super.doCommit(cachedObjectRequired, context, obj, logMessage, remove);

        String pid = obj.getPid();
        if (pid.startsWith("sof:")) {
            // no need to index sof objects
            return;
        }

        // check if SOF-META exists
        Iterable<Datastream> sofMeta = obj.datastreams("SOF-META");
        if (!sofMeta.iterator().hasNext()) {
            return;
        }

        // determine the url we need to invoke
        StringBuffer url = new StringBuffer();
        url.append(solrUrl); // Append fedora custom handler for Solr
        if (!solrUrl.endsWith("/"))
            url.append("/");
        url.append("fedora?");
        url.append("pid=" + urlEncode(pid));
        if (remove) {
            LOG.info("Signaling removal of " + pid + " to Solr");
            url.append("&action=deletePid");
        } else {
            if (LOG.isInfoEnabled()) {
                if (obj.isNew()) {
                    LOG.info("Signaling add of " + pid + " to Solr");
                } else {
                    LOG.info("Signaling mod of " + pid + " to Solr");
                }
            }
            url.append("&action=savePid");
        }
        if (syncUpdate)
            url.append("&commit=true");
        LOG.info("Send REST to URL: " + url);
        // send the signal
        sendRESTMessage(url.toString());
    }

    //
    // Private utility methods
    //

    /**
     * Performs the given HTTP request, logging a warning if we don't get a 200
     * OK response.
     */
    private void sendRESTMessage(String url) {
        HttpInputStream response = null;
        try {
            LOG.debug("Getting " + url);
            response = webClient.get(url, false, solrCredentials);
            int code = response.getStatusCode();
            if (code != 200) {
                LOG.warn("Error sending update to Solr service (url=" + url
                    + ").  HTTP response code was " + code + ". "
                    + "Body of response from Solr follows:\n"
                    + getString(response));
            }
        } catch (Exception e) {
            LOG.warn("Error sending update to Solr service via URL: " + url, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    LOG.warn("Error closing Solr response", e);
                }
            }
        }
    }

    /**
     * Read the remainder of the given stream as a String and return it, or an
     * error message if we encounter an error.
     */
    private static String getString(InputStream in) {
        try {
            StringBuffer out = new StringBuffer();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(in));
            String line = reader.readLine();
            while (line != null) {
                out.append(line + "\n");
                line = reader.readLine();
            }
            return out.toString();
        } catch (Exception e) {
            return "[Error reading response body: " + e.getClass().getName()
                + ": " + e.getMessage() + "]";
        }
    }

    /**
     * URL-encode the given string using UTF-8 encoding.
     */
    private static final String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            LOG.warn("Failed to encode '" + s + "'", e);
            return s;
        }
    }
}
