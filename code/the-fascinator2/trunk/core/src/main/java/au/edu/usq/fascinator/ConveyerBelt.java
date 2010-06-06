/* 
 * The Fascinator - Core
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
package au.edu.usq.fascinator;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.api.transformer.Transformer;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.JsonConfig;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConveyerBelt class to handle transformation of an object
 * 
 * @author Oliver Lucido
 * @author Linda Octalina
 */
public class ConveyerBelt {

    /** Extractor Type transformer */
    public static final String EXTRACTOR = "extractor";

    /** Render Type transformer */
    public static final String RENDER = "render";

    /** Json configuration */
    private JsonConfig config;

    /** Configuration file */
    private File jsonFile;

    /** Type of the transformer to be process e.g: extractor or render */
    private String type;

    /** Configuration string */
    private String jsonString;

    /** List of plugins to use */
    private List<Object> plugins;

    /** Logging */
    private static Logger log = LoggerFactory.getLogger(ConveyerBelt.class);

    /**
     * Find out what transformers are required to run for a particular step.
     *
     * @param props A propeties object containing item specific metadata.
     * @param config The configuration for the particular harvester.
     * @param step The transormation step required
     */
    public static List<String> getTransformList(DigitalObject object,
            JsonConfig config, String step) throws StorageException {
        List<String> plugins = new ArrayList();
        String pluginList = object.getMetadata().getProperty(step);
        if (pluginList != null && !pluginList.equals("")) {
            // Turn the string into a real list
            for (String plugin : StringUtils.split(pluginList, ",")) {
                plugins.add(StringUtils.trim(plugin));
            }
        } else {
            // The harvester specified none, fallback to the
            //  default list for this harvest source.
            for (Object obj : config.getList("transformer/" + step)) {
                plugins.add(StringUtils.trim(obj.toString()));
            }
        }
        return plugins;
    }

    /**
     * Conveyer Belt Constructor
     * 
     * @param jsonFile configuration file
     * @param type of transformer
     */
    public ConveyerBelt(File jsonFile, String type) {
        this.jsonFile = jsonFile;
        this.type = type;
        try {
            config = new JsonConfig(jsonFile);
        } catch (IOException ioe) {
            log.warn("Failed to load config from {}", jsonFile);
        }
    }

    /**
     * Conveyer Belt Constructor
     * 
     * @param jsonString configuration string
     * @param type of transformer
     */
    public ConveyerBelt(String jsonString, String type) {
        this.jsonString = jsonString;
        this.type = type;
        try {
            config = new JsonConfig(jsonString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            log.warn("Failed to load config from {}", jsonFile);
        }
    }

    /**
     * Transform digital object based on transformer type
     * 
     * @param object to be transformed
     * @return transformed obect
     * @throws TransformerException if transformation fail
     */
    public DigitalObject transform(DigitalObject object, List<String> pluginList)
            throws TransformerException {
        DigitalObject result = object;
        if (pluginList != null) {
            for (String id : pluginList) {
                Transformer plugin = PluginManager.getTransformer(id);
                String name = plugin.getName();
                log.info("Loading plugin: {} ({})", name, id);
                try {
                    log.info("Starting {} on {}...", name, object.getId());
                    if (jsonFile == null) {
                        plugin.init(jsonString);
                    } else {
                        plugin.init(jsonFile);
                    }
                    result = plugin.transform(result);
                    log.info("Finished {} on {}", name, object.getId());
                } catch (PluginException pe) {
                    log.error("Transform failed: ({}) {}", id, pe.getMessage());
                }
            }
        }
        return result;
    }
}
