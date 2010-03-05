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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.transformer.Transformer;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * ConveyerBelt class to handle transformation of an object
 * 
 * @author Oliver Lucido
 * @author Linda Octalina
 */
public class ConveyerBelt {

    public static final String EXTRACTOR = "extractor";

    public static final String RENDER = "render";

    private JsonConfig config;

    private File jsonFile;

    private String type;

    private String jsonString;

    private static Logger log = LoggerFactory.getLogger(ConveyerBelt.class);

    public ConveyerBelt(File jsonFile, String type) {
        this.jsonFile = jsonFile;
        this.type = type;
        try {
            config = new JsonConfig(jsonFile);
        } catch (IOException ioe) {
            log.warn("Failed to load config from {}", jsonFile);
        }
    }

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

    public DigitalObject transform(DigitalObject object)
            throws TransformerException {
        List<Object> pluginList = config.getList("transformer/" + type);
        DigitalObject result = object;
        if (pluginList != null) {
            for (Object pluginId : pluginList) {
                String id = StringUtils.trim(pluginId.toString());
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
