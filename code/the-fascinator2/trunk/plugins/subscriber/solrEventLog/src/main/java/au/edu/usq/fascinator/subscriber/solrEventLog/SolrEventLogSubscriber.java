/*
 * The Fascinator - Solr Access Control plugin
 * Copyright (C) 2008-2010 University of Southern Queensland
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
package au.edu.usq.fascinator.subscriber.solrEventLog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginDescription;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.authentication.AuthenticationException;
import au.edu.usq.fascinator.api.subscriber.Subscriber;
import au.edu.usq.fascinator.api.subscriber.SubscriberException;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * A Fascinator Subscriber plugin implementation using a security solr core
 * 
 * @author Linda Octalina
 */
public class SolrEventLogSubscriber implements Subscriber {

    /** Logging */
    private final Logger log = LoggerFactory
            .getLogger(SolrEventLogSubscriber.class);

    /** Solr URI */
    private URI uri;

    /** Solr Core */
    private CommonsHttpSolrServer core;

    /**
     * Gets an identifier for this type of plugin. This should be a simple name
     * such as "file-system" for a storage plugin, for example.
     * 
     * @return the plugin type id
     */
    @Override
    public String getId() {
        return "solr-event-log";
    }

    /**
     * Gets a name for this plugin. This should be a descriptive name.
     * 
     * @return the plugin name
     */
    @Override
    public String getName() {
        return "Solr Event Log Subscriber";
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

    /**
     * Initializes the plugin using the specified JSON String
     * 
     * @param jsonString JSON configuration string
     * @throws PluginException if there was an error in initialization
     */
    @Override
    public void init(String jsonString) throws SubscriberException {
        try {
            JsonConfig config = new JsonConfig(new ByteArrayInputStream(
                    jsonString.getBytes("UTF-8")));
            setConfig(config);
        } catch (UnsupportedEncodingException e) {
            throw new SubscriberException(e);
        } catch (IOException e) {
            throw new SubscriberException(e);
        }
    }

    /**
     * Initializes the plugin using the specified JSON configuration
     * 
     * @param jsonFile JSON configuration file
     * @throws SubscriberException if there was an error in initialization
     */
    @Override
    public void init(File jsonFile) throws SubscriberException {
        try {
            JsonConfig config = new JsonConfig(jsonFile);
            setConfig(config);
        } catch (IOException ioe) {
            throw new SubscriberException(ioe);
        }
    }

    /**
     * Initialization of Solr Access Control plugin
     * 
     * @param config The configuration to use
     * @throws AuthenticationException if fails to initialize
     */
    private void setConfig(JsonConfig config) throws SubscriberException {
        try {
            // Find our solr index
            uri = new URI(config.get("subscriber/solr/uri"));
            core = new CommonsHttpSolrServer(uri.toURL());

            // Small sleep whilst the solr index is still coming online
            Thread.sleep(200);
            // Make sure it is online
            core.ping();
        } catch (Exception ex) {
            throw new SubscriberException(ex);
        }
    }

    /**
     * Shuts down the plugin
     * 
     * @throws SubscriberException if there was an error during shutdown
     */
    @Override
    public void shutdown() throws SubscriberException {
        // Don't need to do anything
    }

    private void addToIndex(Map<String, String> param) throws Exception {
        String doc = writeUpdateString(param);
        core.request(new DirectXmlRequest("/update", doc));
        core.commit();
    }

    private String writeUpdateString(Map<String, String> param) {
        String fieldStr = "";
        for (String paramName : param.keySet()) {
            fieldStr += "<field name=\"" + paramName + "\">"
                    + param.get(paramName) + "</field>";
        }
        return "<add><doc>" + fieldStr + "</doc></add>";
    }

    @Override
    public void onEvent(Map<String, String> param) throws SubscriberException {
        try {
            addToIndex(param);
        } catch (Exception e) {
            throw new SubscriberException("Fail to add log to solr"
                    + e.getMessage());
        }
    }

}
