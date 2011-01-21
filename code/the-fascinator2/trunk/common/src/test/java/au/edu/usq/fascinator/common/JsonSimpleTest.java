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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for JsonSimple
 *
 * @author Greg Pendlebury
 */
public class JsonSimpleTest {

    private JsonSimple json;

    @Before
    public void setup() throws Exception {
        json = new JsonSimple(
                getClass().getResourceAsStream("/test-config.json"));
    }

    /**
     * Tests simple String retrieval
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void simpleString() throws Exception {
        Assert.assertEquals("testing", json.getString(null, "test"));
        Assert.assertEquals("fedora3", json.getString(null, "storage", "type"));
        Assert.assertEquals("http://localhost:8080/fedora",
                json.getString(null, "storage", "config", "uri"));
        Assert.assertEquals("http://localhost:8080/solr",
                json.getString(null, "indexer", "config", "uri"));
        Assert.assertEquals("true",
                json.getString(null, "indexer", "config", "autocommit"));
    }

    /**
     * Tests simple Integer retrieval
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void simpleInteger() throws Exception {
        Assert.assertEquals((Integer) 10,
                json.getInteger(null, "portal", "records-per-page"));
        Assert.assertEquals((Integer) 25,
                json.getInteger(null, "portal", "facet-count"));
    }

    /**
     * Tests simple Boolean retrieval
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void simpleBoolean() throws Exception {
        // Genuine Boolean
        Assert.assertTrue(
                json.getBoolean(null, "indexer", "config", "autocommit"));
        // "true" String
        Assert.assertTrue(
                json.getBoolean(null, "portal", "facet-sort-by-count"));
    }

    /**
     * Tests default value handling is working as intended
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void defaultValues() throws Exception {
        // Boolean
        Assert.assertNull(json.getBoolean(null, "invalid", "path"));
        Assert.assertTrue(json.getBoolean(true, "invalid", "path"));
        Assert.assertFalse(json.getBoolean(false, "invalid", "path"));
        // String => Boolean (valid)
        Assert.assertTrue(
                json.getBoolean(false, "portal", "facet-sort-by-count"));
        // Integer => Boolean (invalid)
        Assert.assertFalse(
                json.getBoolean(false, "portal", "records-per-page"));
        // Boolean - Parsing a random string (on valid path) will be false
        Assert.assertFalse(json.getBoolean(true, "test"));

        // Integer
        Assert.assertEquals((Integer) 10,
                json.getInteger(10, "invalid", "path"));
        // Integer - Parse error
        Assert.assertEquals((Integer) 10, json.getInteger(10, "test"));

        // String
        Assert.assertNull(json.getString(null, "invalid", "path"));
        Assert.assertEquals("random",
                json.getString("random", "invalid", "path"));
        // Boolean => String, will find genuine boolean 'true' on this path
        Assert.assertEquals("true",
                json.getString(null, "indexer", "config", "autocommit"));
    }

    /**
     * Test more complicated pathing
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void complexPaths() throws Exception {
        Assert.assertEquals("3",
                json.getString(null, "transformer", "ints", 2));
        Assert.assertEquals((Integer) 3,
                json.getInteger(null, "transformer", "ints", 2));
        Assert.assertEquals("two",
                json.getString(null, "numbers", 1));

        Assert.assertEquals("map-one",
                json.getString(null, "map-list", 0, "name"));
        Assert.assertEquals((Integer) 3,
                json.getInteger(null, "map-list", 0, "sub-list", 2));
        Assert.assertEquals("map-two",
                json.getString(null, "map-list", 1, "name"));
        Assert.assertTrue(
                json.getBoolean(false, "map-list", 1, "sub-list", 3));
    }

    /**
     * Test dropping out to the JSON.simple API at a specific node
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void simpleAPI() throws Exception {
        Object object = json.getPath("map-list", 0);
        Assert.assertTrue(object instanceof JSONObject);

        JSONObject jsonObject = (JSONObject) object;
        Assert.assertEquals(2, jsonObject.size());

        object = jsonObject.get("sub-list");
        Assert.assertTrue(object instanceof JSONArray);

        JSONArray jsonArray = (JSONArray) object;
        Assert.assertEquals(4, jsonArray.size());
    }

    /**
     * Build an object using API and round-trip it through the wrapping object
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void apiTest() throws Exception {
        Map object = new LinkedHashMap();
        object.put("name", "Random Name");

        // A simple list of strings
        LinkedList listOne = new LinkedList();
        listOne.add("one");
        listOne.add("two");
        listOne.add("three");
        object.put("simple-list", listOne);

        // A complex list of objects containing strings
        Map objectOne = new LinkedHashMap();
        objectOne.put("name", "object-one");
        Map objectTwo = new LinkedHashMap();
        objectTwo.put("name", "object-two");
        Map objectThree = new LinkedHashMap();
        objectThree.put("name", "object-three");

        LinkedList listTwo = new LinkedList();
        listTwo.add(objectOne);
        listTwo.add(objectTwo);
        listTwo.add(objectThree);
        object.put("complex-list", listTwo);

        // Get the string version of this JSON
        String jsonText = JSONValue.toJSONString(object);
        // And parse through our utility
        json = new JsonSimple(jsonText);

        // Verification
        Assert.assertEquals("Random Name", json.getString(null, "name"));

        Assert.assertEquals("one", json.getString(null, "simple-list", 0));
        Assert.assertEquals("two", json.getString(null, "simple-list", 1));
        Assert.assertEquals("three", json.getString(null, "simple-list", 2));

        Assert.assertEquals("object-one",
                json.getString(null, "complex-list", 0, "name"));
        Assert.assertEquals("object-two",
                json.getString(null, "complex-list", 1, "name"));
        Assert.assertEquals("object-three",
                json.getString(null, "complex-list", 2, "name"));
    }
}
