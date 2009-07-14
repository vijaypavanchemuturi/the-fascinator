/* 
 * The Fascinator - Common Library
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
package au.edu.usq.fascinator.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Helper class for working with JSON configuration.
 * 
 * @author Oliver Lucido
 */
public class JsonConfig {

    /** Empty config */
    private static final byte[] EMPTY = "{}".getBytes();

    /** Default configuration directory */
    private static final String DEFAULT_CONFIG_DIR = System
            .getProperty("user.home")
            + File.separator + ".fascinator";

    /** Default system configuration file name */
    private static final String SYSTEM_CONFIG_FILE = "system-config.json";

    /** Configuration directory */
    private String configDir;

    /** JSON system root node */
    private JsonNode systemRootNode;

    /** JSON root node */
    private JsonNode rootNode;

    /** Whether or not to load system properties */
    private boolean useSystem;

    /**
     * Construct an instance with only the system settings
     * 
     * @throws IOException if there was an error loading the JSON configuration
     */
    public JsonConfig() throws IOException {
        this(new ByteArrayInputStream(EMPTY), null, true);
    }

    /**
     * Construct an instance with only the system settings
     * 
     * @param useSystem true to load system properties
     * @throws IOException if there was an error loading the JSON configuration
     */
    public JsonConfig(boolean useSystem) throws IOException {
        this(new ByteArrayInputStream(EMPTY), null, useSystem);
    }

    /**
     * Construct an instance from a JSON inputstream
     * 
     * @param jsonIn a JSON input stream
     * @throws IOException if there was an error loading the JSON configuration
     */
    public JsonConfig(InputStream jsonIn) throws IOException {
        this(jsonIn, true);
    }

    /**
     * Construct an instance from a JSON file
     * 
     * @param jsonIn a JSON input stream
     * @param useSystem true to load system properties
     * @throws IOException if there was an error loading the JSON configuration
     */
    public JsonConfig(InputStream jsonIn, boolean useSystem) throws IOException {
        this(jsonIn, null, useSystem);
    }

    /**
     * Construct an instance from a JSON file
     * 
     * @param jsonFile a JSON file
     * @throws IOException if there was an error loading the JSON configuration
     */
    public JsonConfig(File jsonFile) throws IOException {
        this(jsonFile, true);
    }

    /**
     * Construct an instance from a JSON file
     * 
     * @param jsonFile a JSON file
     * @param useSystem true to load system properties
     * @throws IOException if there was an error loading the JSON configuration
     */
    public JsonConfig(File jsonFile, boolean useSystem) throws IOException {
        this(jsonFile, null, useSystem);
    }

    /**
     * Construct an instance from a JSON inputstream
     * 
     * @param jsonIn a JSON input stream
     * @param configDir directory to read the system and user configuration
     *        files. If null, DEFAULT_CONFIG_DIR is used.
     * @param useSystem true to load system properties
     * @throws IOException if there was an error loading the JSON configuration
     */
    public JsonConfig(InputStream jsonIn, String configDir, boolean useSystem)
            throws IOException {
        if (configDir == null) {
            configDir = DEFAULT_CONFIG_DIR;
        }
        this.configDir = configDir;
        this.useSystem = useSystem;

        ObjectMapper mapper = new ObjectMapper();
        rootNode = mapper.readValue(jsonIn, JsonNode.class);
        if (useSystem) {
            systemRootNode = mapper.readValue(getSystemFile(), JsonNode.class);
        }
    }

    /**
     * Construct an instance from a JSON file
     * 
     * @param jsonFile a JSON file
     * @param configDir directory to read the system and user configuration
     *        files. If null, DEFAULT_CONFIG_DIR is used.
     * @param useSystem true to load system properties
     * @throws IOException if there was an error loading the JSON configuration
     */
    public JsonConfig(File jsonFile, String configDir, boolean useSystem)
            throws IOException {
        this(new FileInputStream(jsonFile), configDir, useSystem);
    }

    /**
     * Gets the text value of the field. Convenience method for getText(String
     * fieldName, String defaultValue) with a null defaultValue.
     * 
     * @param fieldName the field to get the value of
     * @return a text value
     */
    public String get(String fieldName) {
        return get(fieldName, null);
    }

    /**
     * Gets the text value of the field, with a specified default if the field
     * was not found. If the field was not found in this JSON, the system-wide
     * configuration is searched.
     * 
     * @param fieldName the field to get the value of
     * @param defaultValue default value for null fields
     * @return a text value (possibly null)
     */
    public String get(String fieldName, String defaultValue) {
        String value = get(rootNode, fieldName, null);
        if (value == null) {
            if (useSystem) {
                value = get(systemRootNode, fieldName, defaultValue);
            } else {
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * Gets the text value of the field. If the node has no text value, the
     * defaultValue is returned. For non textual values, a string representation
     * is returned. The fieldName can use a special path notation, using "/" to
     * represent hierarchy.
     * 
     * <pre>
     * { &quot;storage&quot;: { &quot;type&quot;: &quot;file-system&quot; }}
     * </pre>
     * 
     * In the above case, getText("storage/type") would return "file-system".
     * 
     * The value is first searched in the
     * 
     * @param node the parent JSON node
     * @param fieldName the field to get the value of
     * @param defaultValue default value for null fields
     * @return a text value, if field was not found, defaultValue
     */
    public String get(JsonNode node, String fieldName, String defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null) {
            if (fieldName.indexOf("/") != -1) {
                String[] fields = fieldName.split("/");
                JsonNode currentNode = node;
                for (String field : fields) {
                    fieldNode = currentNode.get(field);
                    if (fieldNode != null) {
                        currentNode = fieldNode;
                        if (fieldNode.isValueNode()) {
                            String value = defaultValue;
                            if (fieldNode.isTextual()) {
                                value = fieldNode.getTextValue();
                            } else {
                                value = fieldNode.getValueAsText();
                            }
                            return StrSubstitutor
                                    .replaceSystemProperties(value);
                        }
                    } else {
                        break;
                    }
                }
            }
            return defaultValue;
        }
        return fieldNode.getTextValue();
    }

    public void set(String fieldName, String value) {
        set(rootNode, fieldName, value);
    }

    public void set(JsonNode node, String fieldName, String value) {
        if (fieldName.indexOf("/") != -1) {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode == null) {
                String[] fields = fieldName.split("/");
                StringBuilder rawJson = new StringBuilder();
                int last = fields.length - 1;
                for (int i = 0; i < last; i++) {
                    String field = fields[i];
                    rawJson.append("\"");
                    rawJson.append(field);
                    rawJson.append("\":{");
                }
                rawJson.append("\"");
                rawJson.append(fields[last]);
                rawJson.append("\":\"");
                rawJson.append(value);
                rawJson.append("\"");
                for (int i = 0; i < last; i++) {
                    rawJson.append("}");
                }
                System.out.println(rawJson.toString());
            }
        } else {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.put(fieldName, value);
        }
    }

    public void save(OutputStream out) throws IOException {
        JsonGenerator jgen = new JsonFactory().createJsonGenerator(out,
                JsonEncoding.UTF8);
        jgen.setCodec(new ObjectMapper());
        jgen.useDefaultPrettyPrinter();
        jgen.writeTree(rootNode);
        jgen.close();
    }

    /**
     * Gets the system-wide configuration file from the configDir. If the file
     * doesn't exist, a default is copied to the configDir.
     * 
     * @return a file
     * @throws IOException if there was an error reading or writing the system
     *         configuration file
     */
    public File getSystemFile() throws IOException {
        File configFile = new File(configDir, SYSTEM_CONFIG_FILE);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            OutputStream out = new FileOutputStream(configFile);
            IOUtils.copy(getClass().getResourceAsStream(
                    "/" + SYSTEM_CONFIG_FILE), out);
            out.close();
        }
        return configFile;
    }

}
