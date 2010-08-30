/*
 * The Fascinator - Plugin - Transformer - Customised
 * Copyright (C) 2009  University of Southern Queensland
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
package au.edu.usq.fascinator.transformer.customised;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginDescription;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.transformer.Transformer;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.JsonConfigHelper;

/**
 * Provides methods for batch processing of digital objects
 * 
 * @author Linda Octalina
 */
public class CustomisedTransformer implements Transformer {
    /** Logger */
    private static Logger log = LoggerFactory
            .getLogger(CustomisedTransformer.class);

    /** Json config file **/
    private JsonConfigHelper config;

    /**
     * Extractor Constructor
     */
    public CustomisedTransformer() {
    }

    /**
     * Overridden method init to initialize
     * 
     * @param jsonString of configuration for Extractor
     * @throws PluginException if fail to parse the config
     */
    @Override
    public void init(String jsonString) throws PluginException {
        try {
            config = new JsonConfigHelper(jsonString);
            reset();
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    /**
     * Overridden method init to initialize
     * 
     * @param jsonFile to retrieve the configuration for Extractor
     * @throws PluginException if fail to read the config file
     */
    @Override
    public void init(File jsonFile) throws PluginException {
        try {
            config = new JsonConfigHelper(jsonFile);
            reset();
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    /**
     * Reset the transformer in preparation for a new object
     */
    private void reset() throws TransformerException {
        log.info("--Initializing Extractor plugin--");
    }

    /**
     * Overridden method getId
     * 
     * @return plugin id
     */
    @Override
    public String getId() {
        return "customised";
    }

    /**
     * Overridden method getName
     * 
     * @return plugin name
     */
    @Override
    public String getName() {
        return "Customised Extractor";
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
     * Overridden method shutdown method
     * 
     * @throws PluginException
     */
    @Override
    public void shutdown() throws PluginException {
    }

    /**
     * Overridden transform method
     * 
     * @param DigitalObject to be processed
     * @return processed DigitalObject with the rdf metadata
     */
    @Override
    public DigitalObject transform(DigitalObject in, String jsonConfig)
            throws TransformerException {
        // Purge old data
        reset();
        try {
            Properties props = in.getMetadata();
            JsonConfigHelper json = new JsonConfigHelper(jsonConfig);
            String scriptFile = json.get("transformer/customised/script",
                    props.getProperty("customisedScript"));
            if (scriptFile != null) {
                log.info("Running script: '{}'", scriptFile);
                // Run the script
                PythonInterpreter python = new PythonInterpreter();
                python.set("object", in);
                python.set("config", json);
                python.execfile(scriptFile);
                python.cleanup();
                // Modify the property
                props.setProperty("modified", "true");
            } else {
                log.info("");
            }

            restoreProperty(props, "indexOnHarvest");
            restoreProperty(props, "harvestQueue");
            restoreProperty(props, "renderQueue");
            restoreProperty(props, "customisedScript");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return in;
    }

    private void restoreProperty(Properties props, String key) {
        String copyKey = "copyOf_" + key;
        if (props.containsKey(copyKey)) {
            String copyValue = props.remove(copyKey).toString();
            props.setProperty(key, copyValue);
        }
    }
}
