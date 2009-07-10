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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JsonConfig unit tests
 * 
 * @author Oliver Lucido
 */
public class JsonConfigTest {

    private JsonConfig config;

    @Before
    public void setup() throws Exception {
        config = new JsonConfig(getClass().getResourceAsStream(
                "/test-config.json"), false);
    }

    @Test
    public void get() throws Exception {
        Assert.assertEquals("testing", config.get("test"));
        Assert.assertEquals("fedora3", config.get("storage/type"));
        Assert.assertEquals("http://localhost:8080/fedora", config
                .get("storage/config/uri"));
        Assert.assertEquals("http://localhost:8080/solr", config
                .get("indexer/config/uri"));
        Assert.assertEquals("true", config.get("indexer/config/autocommit"));
    }

    @Test
    public void getWithSystemProperties() throws Exception {
        System.setProperty("sample.property", "Sample Value");
        Assert.assertEquals("Sample Value", config.get("sample/property"));
    }

    @Ignore
    @Test
    public void set() throws Exception {
        config.set("new", "value");
        config.set("test", "new value");
        config.set("hello/world", "!!!");
        config.set("storage/config/uri", "http://localhost:9000/fedora3");
        config.save(System.out);
        Assert.assertEquals("value", config.get("new"));
        Assert.assertEquals("new value", config.get("testing"));
        Assert.assertEquals("!!!", config.get("hello/world"));
        Assert.assertEquals("http://localhost:9000/fedora3", config
                .get("storage/config/uri"));
    }

}
