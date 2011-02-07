/*
 * The Fascinator - Plugin - Transformer - Json Velocity Transformer
 * Copyright (C) <your copyright here>
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
package au.edu.usq.fascinator.transformer.jsonVelocity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginDescription;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.api.transformer.Transformer;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.JsonConfigHelper;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * <h3>Introduction</h3>
 * <p>
 * This plugin transform Json Payload to another format based on the provided
 * Velocity templates. The transformed format will then be stored as Payload.
 * </p>
 * 
 * <h3>Configuration</h3>
 * 
 * <table border="1">
 * <tr>
 * <th>Option</th>
 * <th>Description</th>
 * <th>Required</th>
 * <th>Default</th>
 * </tr>
 * 
 * <tr>
 * <td>id</td>
 * <td>Id of the transformer</td>
 * <td><b>Yes</b></td>
 * <td>jsonVelocity</td>
 * </tr>
 * 
 * <tr>
 * <td>sourcePayload</td>
 * <td>Source payload from which the object will be transformed</td>
 * <td><b>Yes</b></td>
 * <td>object.tfpackage</td>
 * </tr>
 * 
 * <tr>
 * <td>templatesPath</td>
 * <td>Velocity template file or directory</td>
 * <td><b>Yes</b></td>
 * <td>src/main/resources/templates</td>
 * </tr>
 * 
 * <h3>Examples</h3>
 * <ol>
 * <li>
 * Adding JsonVelocity Transformer to The Fascinator
 * 
 * <pre>
 * "jsonVelocity": {
 *         "id" : "jsonVelocity",
 *         "sourcePayload" : "object.tfpackage",
 *         "templatesPath" : "src/main/resources/templates"
 *      }
 * </pre>
 * 
 * </li>
 * </ol>
 * 
 * <h3>Wiki Link</h3>
 * <p>None
 * </p>
 * 
 * @author Linda Octalina
 */
public class JsonVelocityTransformer implements Transformer {
    /** Logger */
    static Logger log = LoggerFactory
            .getLogger(JsonVelocityTransformer.class);

    /** Json config file **/
    private JsonConfigHelper config;

    /** Template file or folder **/
    private File templates;

    /** Source payload to be transformed **/
    private String sourcePayload;

    /** Individual template to be processed */
    private String individualTemplate;
    
    /** Utility class for json velocity transformer */
    public Util util;

    /**
     * Overridden method init to initialize
     * 
     * @param jsonString of configuration for transformer
     * @throws PluginException if fail to parse the config
     */
    @Override
    public void init(String jsonString) throws PluginException {
        try {
            config = new JsonConfigHelper(jsonString);
            init();
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    /**
     * Overridden method init to initialize
     * 
     * @param jsonString of configuration for transformer
     * @throws PluginException if fail to parse the config
     */
    @Override
    public void init(File jsonFile) throws PluginException {
        try {
            config = new JsonConfigHelper(jsonFile);
            init();
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    /**
     * Initialise the plugin
     */
    private void init() throws TransformerException {
        templates = new File(config.get("jsonVelocity/templatesPath",
                "templates"));
        sourcePayload = config.get("jsonVelocity/sourcePayload",
                "workflow.metadata");
        
        individualTemplate = config.get("jsonVelocity/individualTemplate", "");
        util = new Util();

        try {
            /*Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS,
                    "au.edu.usq.fascinator.portal.velocity.Slf4jLogChute");*/
            Velocity.setProperty(Velocity.RESOURCE_LOADER, "file, class");
            Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_CACHE, "false");
            Velocity.setProperty("class.resource.loader.class",
                    "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.setProperty("directive.set.null.allowed", "true");
            
            File templateDir = templates;
            if (templates.isFile()) {
                templateDir = templates.getParentFile();
            }
            Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,
                    templateDir.getAbsolutePath());
            Velocity.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets plugin Id
     * 
     * @return pluginId
     */
    @Override
    public String getId() {
        return "jsonVelocity";
    }

    /**
     * Gets plugin name
     * 
     * @return pluginName
     */
    @Override
    public String getName() {
        return "Json Velocity Transformer";
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
     * Overridden shutdown method
     * 
     * @throws PluginException
     */
    @Override
    public void shutdown() throws PluginException {
        // clean up any resources if required
    }

    public List<File> getListOfTemplates() {
        List<File> templateList = new ArrayList<File>();
        if (templates.isDirectory()) {
            for (File template : templates.listFiles()) {
                templateList.add(template);
            }
        } else {
            if (templates.isFile()) {
                templateList.add(templates);
            }
        }
        return templateList;
    }

    /**
     * Overridden transform method
     * 
     * @param in to be processed
     * @param jsonConfig configuration
     * @return processed DigitalObject
     * 
     * @throws TransformerException if fail to transform
     */
    @Override
    public DigitalObject transform(DigitalObject in, String jsonConfig)
            throws TransformerException {

        try {
            Payload source = in.getPayload(sourcePayload);
            if (source != null) {
                //Setup the velocity context

                Map<String, Object> sourceMap = new JsonConfigHelper(
                        source.open()).getMap("/");
                VelocityContext vc = new VelocityContext();

                vc.put("item", sourceMap);
                vc.put("util", util);
                vc.put("oid", in.getId());
                vc.put("urlBase", config.get("urlBase") + "default"); // For now just hardcode the portal name
                source.close();

                StringWriter pageContentWriter = new StringWriter();
                
                if (!individualTemplate.equals("")) {
                    // Process individual template
                    Template pageContent = Velocity.getTemplate(individualTemplate);
                    // Transform the source
                    pageContent.merge(vc, pageContentWriter);

                    // Save to new payload
                    in.createStoredPayload(payloadName(individualTemplate), new ByteArrayInputStream(
                            pageContentWriter.toString().getBytes()));
                } else {
                    for (File template : templates.listFiles()) {
                        // Process each template
                        Template pageContent = Velocity.getTemplate(template.getName());
                        // Transform the source
                        pageContent.merge(vc, pageContentWriter);

                        // Save to new payload
                        in.createStoredPayload(payloadName(template.getName()), new ByteArrayInputStream(
                                pageContentWriter.toString().getBytes()));
                    }
                }
                
            }
        } catch (StorageException e) {
            log.error(e.toString());
        } catch (ResourceNotFoundException e) {
            log.error("Template not found");
        } catch (ParseErrorException e) {
            log.error("Template parse error");
        } catch (Exception e) {
            log.error(e.toString());
        }

        return in;
    }
    
    private String payloadName (String templateName) {
        return templateName.substring(0, templateName.indexOf(".")) + ".xml";
    }
    
}
