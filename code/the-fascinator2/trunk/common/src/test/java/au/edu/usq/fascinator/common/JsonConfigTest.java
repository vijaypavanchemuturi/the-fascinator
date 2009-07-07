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
import org.junit.Test;

/**
 * JsonConfig unit tests
 * 
 * @author Oliver Lucido
 */
public class JsonConfigTest {

    @Test
    public void getText() throws Exception {
        JsonConfig jc = new JsonConfig(getClass().getResourceAsStream(
                "/config.json"));
        Assert.assertEquals("testing", jc.get("test"));
        Assert.assertEquals("fedora3", jc.get("storage/type"));
        Assert.assertEquals("http://localhost:8080/fedora", jc
                .get("storage/config/uri"));
        Assert.assertEquals("http://localhost:8080/solr", jc
                .get("indexer/config/uri"));
        Assert.assertEquals("true", jc.get("indexer/config/autocommit"));
    }

}
