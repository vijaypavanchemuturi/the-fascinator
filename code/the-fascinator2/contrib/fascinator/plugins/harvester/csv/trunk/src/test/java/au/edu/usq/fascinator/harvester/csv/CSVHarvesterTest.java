/* 
 * The Fascinator - Plugin - Harvester - File System
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
package au.edu.usq.fascinator.harvester.csv;

import java.io.File;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.storage.Storage;

/**
 * Unit tests for the CSV harvester plugin
 * 
 * @author Linda Octalina
 */
public class CSVHarvesterTest {

    /** logging */
    private Logger log = LoggerFactory.getLogger(CSVHarvesterTest.class);

    private Storage ram;

    /**
     * Sets the "test.dir" and "test.cache.dir" system property for use in the
     * JSON configuration
     * 
     * @throws Exception if any error occurs
     */
    @Before
    public void setup() throws Exception {
        File baseDir = new File(CSVHarvesterTest.class.getResource("/")
                .toURI());
        System.setProperty("test.dir", baseDir.getAbsolutePath());

        ram = PluginManager.getStorage("ram");
        ram.init("{}");
    }

    /**
     * Test on getting object list
     * 
     * @throws Exception if any error occurs
     */
    @Test
    public void getObjectIdList() throws Exception {
        CSVHarvester csvHarvester = getHarvester("/csv-config.json");
        Set<String> geonames = csvHarvester.getObjectIdList();
        Assert.assertEquals(4, geonames.size());
    }
    
    /**
     * Test on getting object list
     * 
     * @throws Exception if any error occurs
     */
    @Test
    public void getObjectIdListNoHeader() throws Exception {
        CSVHarvester csvHarvester = getHarvester("/csv-no-header-config.json");
        Set<String> geonames = csvHarvester.getObjectIdList();
        Assert.assertEquals(5, geonames.size());
    }

    private CSVHarvester getHarvester(String filename) throws Exception {
        Harvester csvHarvester = PluginManager.getHarvester("csv",
                ram);
        File f = new File(getClass().getResource(filename).toURI());
        csvHarvester.init(f);
        return (CSVHarvester) csvHarvester;
    }
}
