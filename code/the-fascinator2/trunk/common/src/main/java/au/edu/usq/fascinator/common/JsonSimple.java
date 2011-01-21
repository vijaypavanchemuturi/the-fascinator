/*
 * The Fascinator - JSON Simple
 * Copyright (C) 2011 University of Southern Queensland
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This object wraps objects and methods of the JSON.simple library:
 * 
 * http://code.google.com/p/json-simple/
 *
 * @author Greg Pendlebury
 */
public class JsonSimple {

    /** Logging */
    private static Logger log = LoggerFactory.getLogger(JsonSimple.class);

    /** Holds this object's JSON */
    private JSONObject jsonObject;

    /**
     * Creates an empty JSON object
     *
     * @throws IOException if there was an error during creation
     */
    public JsonSimple() throws IOException {
        this((InputStream) null);
    }

    /**
     * Creates a JSON object from the specified file
     *
     * @param jsonFile a JSON file
     * @throws IOException if there was an error parsing or reading the file
     */
    public JsonSimple(File jsonFile) throws IOException {
        if (jsonFile == null) {
            jsonObject = new JSONObject();
        } else {
            InputStream is = new FileInputStream(jsonFile);
            String json = IOUtils.toString(is);
            parse(json);
        }
    }

    /**
     * Creates a JSON object from the specified input stream
     *
     * @param jsonIn a JSON stream
     * @throws IOException if there was an error parsing or reading the stream
     */
    public JsonSimple(InputStream jsonIn) throws IOException {
        if (jsonIn == null) {
            jsonObject = new JSONObject();
        } else {
            // Stream the data into a string
            String json = IOUtils.toString(jsonIn);
            parse(json);
        }
    }

    /**
     * Creates a JSON object from the specified string
     *
     * @param jsonIn a JSON string
     * @throws IOException if there was an error parsing the string
     */
    public JsonSimple(String jsonString) throws IOException {
        if (jsonString == null) {
            jsonObject = new JSONObject();
        } else {
            parse(jsonString);
        }
    }

    /**
     * Parse the provided JSON
     *
     * @param jsonString a JSON string
     * @throws IOException if there was an error parsing the string
     */
    private void parse(String jsonString) throws IOException {
        JSONParser parser = new JSONParser();
        Object object;
        try {
            // Parse the string
            object = parser.parse(jsonString);
        } catch (ParseException ex) {
            log.error("JSON Parsing error: ", ex);
            throw new IOException(ex);
        }
        // Take a look at what we have now
        if (object instanceof JSONObject) {
            jsonObject = (JSONObject) object;
        } else {
            if (object instanceof JSONArray) {
                jsonObject = getFromArray((JSONArray) object);
            } else {
                log.error("Expected JSONObject or at least JSONArray, but" +
                        " found neither. Please check JSON syntax: '{}'",
                        jsonString);
                jsonObject = null;
            }
        }
    }

    /**
     * Find the first valid JSONObject in a JSONArray.
     *
     * @param array : The array to search
     * @return JSONObject : A JSON object
     * @throws IOException if there was an error
     */
    private JSONObject getFromArray(JSONArray array) {
        if (array.isEmpty()) {
            log.warn("Found only empty array, starting new object");
            return new JSONObject();
        }
        // Grab the first element
        Object object = array.get(0);
        if (object == null) {
            log.warn("Null entry, starting new object");
            return new JSONObject();
        }
        // Nested array, go deeper
        if (object instanceof JSONArray) {
            return getFromArray((JSONArray) object);
        }
        return (JSONObject) object;
    }

    /**
     * Retrieve the given node from the provided object.
     *
     * @param
     * @return JSONObject : The JSON representation
     */
    private Object getNode(Object object, Object path) {
        if (isArray(object)) {
            return ((JSONArray) object).get((Integer) path);
        }
        if (isObject(object)) {
            return ((JSONObject) object).get(path);
        }
        return null;
    }

    /**
     * Return the JSONObject holding this object's JSON representation
     *
     * @return JSONObject : The JSON representation
     */
    public JSONObject getObject() {
        return jsonObject;
    }

    /**
     * Walk down the JSON nodes specified by the path and retrieve the target.
     *
     * @param path : Variable length array of path segments
     * @return Object : The target node, or NULL if invalid
     */
    public Object getPath(Object... path) {
        Object object = jsonObject;
        boolean valid = true;
        for (Object node : path) {
            if (isValidPath(object, node)) {
                object = getNode(object, node);
            } else {
                valid = false;
                break;
            }
        }
        if (valid) {
            return object;
        }
        return null;
    }

    /**
     * Retrieve the Boolean value on the given path.
     *
     * <strong>IMPORTANT:</strong> The default value only applies if the path is
     * not found. If a string on the path is found it will be considered
     * <b>false</b> unless the value is 'true' (ignoring case). This is the
     * default behaviour of the Boolean.parseBoolean() method.
     *
     * @param defaultValue : The fallback value to use if the path is
     * invalid or not found
     * @return Boolean : The Boolean value found on the given path, or null if
     * no default provided
     */
    public Boolean getBoolean(Boolean defaultValue, Object... path) {
        Object object = getPath(path);
        if (object == null) {
            return defaultValue;
        }
        if (isNumber(object)) {
            log.warn("getBoolean() : Integer value targeted. Expected Boolean");
            return defaultValue;
        }
        if (object instanceof String) {
            return Boolean.parseBoolean((String) object);
        }
        if (object instanceof Boolean) {
            return (Boolean) object;
        }
        return null;
    }

    /**
     * Retrieve the Integer value on the given path.
     *
     * @param defaultValue : The fallback value to use if the path is
     * invalid or not found
     * @return Integer : The Integer value found on the given path, or null if
     * no default provided
     */
    public Integer getInteger(Integer defaultValue, Object... path) {
        Object object = getPath(path);
        if (object == null) {
            return defaultValue;
        }
        if (isNumber(object)) {
            return makeNumber(object);
        }
        if (object instanceof String) {
            try {
                return Integer.parseInt((String) object);
            } catch (NumberFormatException ex) {
                log.warn("getInteger() : String is not a parsable Integer '{}'",
                        (String) object);
                return defaultValue;
            }
        }
        if (object instanceof Boolean) {
            log.warn("getInteger() : Boolean value targeted. Expected Integer");
            return defaultValue;
        }
        return null;
    }

    /**
     * Retrieve the String value on the given path.
     *
     * @param defaultValue : The fallback value to use if the path is
     * invalid or not found
     * @return String : The String value found on the given path, or null if
     * no default provided
     */
    public String getString(String defaultValue, Object... path) {
        Object object = getPath(path);
        if (object == null) {
            return defaultValue;
        } else {
        }
        if (isNumber(object)) {
            return Integer.toString(makeNumber(object));
        }
        if (object instanceof String) {
            return (String) object;
        }
        if (object instanceof Boolean) {
            return Boolean.toString((Boolean) object);
        }
        return null;
    }

    /*===============================================
     *
     * A series of small wrappers for type testing.
     *
     * ==============================================
     */

    // JSONArray
    private boolean isArray(Object object) {
        return (object instanceof JSONArray);
    }

    // Integer or Long
    private boolean isNumber(Object object) {
        return (object instanceof Integer || object instanceof Long);
    }

    // JSONObject
    private boolean isObject(Object object) {
        return (object instanceof JSONObject);
    }

    // Test that path is valid for the object
    private boolean isValidPath(Object object, Object path) {
        if (isArray(object) && path instanceof Integer) {
            return true;
        }
        if (isObject(object) && path instanceof String) {
            return true;
        }
        return false;
    }

    // Integer or Long
    private Integer makeNumber(Object object) {
        if (object instanceof Integer) {
            return (Integer) object;
        }
        if (object instanceof Long) {
            return Integer.parseInt(Long.toString((Long) object));
        }
        return null;
    }

    /**
     * Return the String representation of this object's JSON
     *
     * @return String : The JSON String
     */
    @Override
    public String toString() {
        return jsonObject.toJSONString();
    }
}
