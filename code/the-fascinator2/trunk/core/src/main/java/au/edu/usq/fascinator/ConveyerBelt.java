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
import au.edu.usq.fascinator.common.JsonConfigHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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

    /** Default transformer config file */
    private static String DEFAULT_CONFIG_FILE = "render-config.json";

    /** Logging */
    private static Logger log = LoggerFactory.getLogger(ConveyerBelt.class);

    /** Json configuration */
    private JsonConfigHelper sysConfig;

    /** Type of the transformer to be process e.g: extractor or render */
    private String type;

    /** List of plugins to use */
    private Map<String, Transformer> transformers;

    /**
     * Find out what transformers are required to run for this step.
     *
     * @param object The digital object to transform.
     * @param config The configuration for the particular harvester.
     * @return List<String> A list of names for instantiated transformers.
     */
    public static List<String> getTransformList(DigitalObject object,
            JsonConfigHelper config, String thisType) throws StorageException {
        List<String> plugins = new ArrayList();
        String pluginList = object.getMetadata().getProperty(thisType);
        if (pluginList != null && !pluginList.equals("")) {
            // Turn the string into a real list
            for (String plugin : StringUtils.split(pluginList, ",")) {
                plugins.add(StringUtils.trim(plugin));
            }
        } else {
            // The harvester specified none, fallback to the
            //  default list for this harvest source.
            for (Object obj : config.getList("transformer/" + thisType)) {
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
    public ConveyerBelt(String newType) throws TransformerException {
        try {
            sysConfig = getTransformerConfig();
            type = newType;
            // More than meets the eye
            transformers = new LinkedHashMap();
            // Loop through all the system's transformers
            Map<String, JsonConfigHelper> map =
                    sysConfig.getJsonMap("transformers");
            if (map != null && map.size() > 0) {
                for (String name : map.keySet()) {
                    String id = map.get(name).get("id");
                    if (id != null) {
                        // Instantiate the transformer
                        Transformer transformer =
                                PluginManager.getTransformer(id);
                        try {
                            transformer.init(map.get(name).toString());
                            // Finally, store it for use later
                            transformers.put(name, transformer);
                            log.info("Transformer warmed: '{}'", name);

                        } catch (PluginException ex) {
                            throw new TransformerException(ex);
                        }
                    } else {
                        log.warn("Invalid transformer config, no ID.");
                    }
                }
            } else {
                log.warn("Conveyer Belt instantiated with no Transformers!");
            }
        } catch (IOException ioe) {
            log.warn("Failed to load system configuration", ioe);
        }
    }

    /**
     * Get the transformer configuration file, or the default
     *
     * @return JsonConfigHelper transformer configuration
     * @throws IOException if unable to read the file(s)
     */
    private JsonConfigHelper getTransformerConfig() throws IOException {
        // Where SHOULD it be
        JsonConfig config = new JsonConfig();
        String config_path = config.get("portal/transformers");
        File config_file = new File(config_path);

        // Is it there?
        if (config_file.exists()) {
            return new JsonConfigHelper(config_file);

        } else {
            // Make sure the path exists
            config_file.getParentFile().mkdirs();
            // Get an output stream to the file
            OutputStream out;
            out = new FileOutputStream(config_file);
            // Copy the resource to disk
            IOUtils.copy(getClass().getResourceAsStream("/" +
                    DEFAULT_CONFIG_FILE), out);
            // Close output and return
            out.close();
            return new JsonConfigHelper(config_file);
        }
    }

    /**
     * Transform digital object based on transformer type
     *
     * @param object The object to be transformed
     * @param config Configuration for this item transformation
     * @return DigitalObject Transformed obect
     * @throws TransformerException if transformation fails
     */
    public DigitalObject transform(DigitalObject object,
            JsonConfigHelper config) throws TransformerException {
        Map<String, JsonConfigHelper> itemConfigs;
        JsonConfigHelper itemConfig;

        // Get the list of transformers to run
        List<String> pluginList;
        try {
            pluginList = getTransformList(object, config, type);
        } catch (StorageException ex) {
            throw new TransformerException(ex);
        }

        // Loop through the list
        if (pluginList != null) {
            for (String name : pluginList) {
                if (transformers.containsKey(name)) {
                    log.info("Starting '{}' on '{}'...", name, object.getId());

                    // Grab any overriding transformer config this item has
                    itemConfigs = config.getJsonMap("transformerConfigs");
                    if (itemConfigs != null && itemConfigs.containsKey(name)) {
                        itemConfig = itemConfigs.get(name);
                    } else {
                        itemConfig = new JsonConfigHelper();
                    }

                    // Perform the transformation
                    object = transformers.get(name).
                            transform(object, itemConfig.toString());
                    log.info("Finished '{}' on '{}'", name, object.getId());

                } else {
                    log.error("Transformer not in conveyer belt! '{}'", name);
                }
            }
        }
        return object;
    }
}
