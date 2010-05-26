/*
 * The Fascinator - Plugin - Harvester - HCA Data
 * Copyright (C) 2010 University of Southern Queensland
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
package au.edu.usq.fascinator.harvester.arts;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.storage.Storage;

/**
 * Unit tests for the HCA Data Harvester plugin.
 * 
 * @author Oliver Lucido
 */
public class HcaDataHarvesterTest {

    private File baseDir;

    private Storage ram;

    private Harvester harvester;

    @Before
    public void init() throws Exception {
        baseDir = new File(getClass().getResource("/").toURI());
        ram = PluginManager.getStorage("ram");
        ram.init("{}");
        harvester = PluginManager.getHarvester("hca-data", ram);
    }

    @After
    public void cleanup() throws Exception {
        if (ram != null) {
            ram.shutdown();
        }
        if (harvester != null) {
            harvester.shutdown();
        }
    }

    /**
     * Tests that a valid PI.xml and it's attachments will be harvested
     */
    @Test
    public void harvestProperData() throws Exception {
        File testDir = new File(baseDir, "hca-data/valid");
        System.setProperty("test.dir", testDir.getAbsolutePath());
        harvester.init(new File(baseDir, "hca-std.json"));
        Set<String> idList = harvester.getObjectIdList();
        Assert.assertEquals(4, idList.size());
    }

    /**
     * Tests that an invalid PI.xml will not be harvested
     */
    @Test
    public void harvestInvalidPiXml() throws Exception {
        setTestDir("hca-data/invalid");
        harvester.init(new File(baseDir, "hca-std.json"));
        Set<String> idList = harvester.getObjectIdList();
        Assert.assertTrue(idList.isEmpty());
    }

    /**
     * Tests that a PI.xml with all missing links will not be harvested
     */
    @Test
    public void harvestMissingAll() throws Exception {
        setTestDir("hca-data/missing-all");
        harvester.init(new File(baseDir, "hca-std.json"));
        Set<String> idList = harvester.getObjectIdList();
        System.out.println(idList);
        Assert.assertTrue(idList.isEmpty());
    }

    /**
     * Tests that a PI.xml with some missing links will be half harvested
     */
    @Test
    public void harvestMissingSome() throws Exception {
        setTestDir("hca-data/missing-some");
        harvester.init(new File(baseDir, "hca-std.json"));
        Set<String> idList = harvester.getObjectIdList();
        Assert.assertEquals(2, idList.size());
    }

    /**
     * Tests recursive harvest
     */
    @Test
    public void harvestRecursive() throws Exception {
        setTestDir("hca-data");
        harvester.init(new File(baseDir, "hca-rec.json"));
        Set<String> idList = new HashSet<String>();
        do {
            idList.addAll(harvester.getObjectIdList());
        } while (harvester.hasMoreObjects());
        Assert.assertEquals(6, idList.size());
    }

    private void setTestDir(String relPath) {
        File testDir = new File(baseDir, relPath);
        System.setProperty("test.dir", testDir.getAbsolutePath());
    }
}
