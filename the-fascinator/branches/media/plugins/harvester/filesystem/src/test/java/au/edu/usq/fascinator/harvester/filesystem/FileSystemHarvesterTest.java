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
package au.edu.usq.fascinator.harvester.filesystem;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;

/**
 * Unit tests for the file system harvester plugin
 * 
 * @author Oliver Lucido
 */
public class FileSystemHarvesterTest {

    private Storage ram;

    private File testDir, cacheDir, testFile;

    /**
     * Sets the "test.dir" and "test.cache.dir" system property for use in the
     * JSON configuration
     * 
     * @throws Exception
     *             if any error occurs
     */
    @Before
    public void setup() throws Exception {
        File baseDir = new File(FileSystemHarvesterTest.class.getResource("/")
                .toURI());
        testDir = new File(baseDir, "fs-harvest-root");
        cacheDir = new File(baseDir, "fs-harvest-cache");
        cacheDir.mkdirs();
        System.setProperty("test.dir", testDir.getAbsolutePath());
        System.setProperty("test.cache.dir", cacheDir.getAbsolutePath());
        testFile = new File(testDir, "test-gen.txt");
        FileUtils.writeStringToFile(testFile, "testing");

        ram = PluginManager.getStorage("ram");
        ram.init("{}");
    }

    @After
    public void cleanup() throws Exception {
        FileUtils.deleteQuietly(testFile);
        FileUtils.deleteQuietly(cacheDir);
    }

    /**
     * Tests a non recursive harvest
     * 
     * @throws Exception
     *             if any error occurs
     */
    @Test
    public void getObjectIdList() throws Exception {
        FileSystemHarvester fsh = getHarvester("/fsh-config.json");
        Set<String> items = fsh.getObjectIdList();
        Assert.assertEquals(2, items.size());
        Assert.assertFalse(fsh.hasMoreObjects());
    }

    /**
     * Tests a non recursive harvest with checksums with no file changes on 2nd
     * harvest, and a forced harvest on 3rd
     * 
     * @throws Exception
     *             if any error occurs
     */
    @Test
    public void getObjectIdListWithCachingNoChanges() throws Exception {
        // the initial harvest will pick up the file
        FileSystemHarvester fsh = getHarvester("/fsh-config-caching.json");
        Set<String> items = fsh.getObjectIdList();
        Assert.assertEquals(2, items.size());

        // next harvest will detect no change
        FileSystemHarvester fsh2 = getHarvester("/fsh-config-caching.json");
        Set<String> items2 = fsh2.getObjectIdList();
        Assert.assertEquals(0, items2.size());

        // forced harvest
        FileSystemHarvester fsh3 = getHarvester("/fsh-config-caching-force.json");
        Set<String> items3 = fsh3.getObjectIdList();
        Assert.assertEquals(2, items3.size());
    }

    /**
     * Tests a non recursive harvest with checksums with file changes on 2nd
     * harvest
     * 
     * @throws Exception
     *             if any error occurs
     */
    @Test
    public void getObjectIdListWithCachingChanges() throws Exception {
        // the initial harvest will pick up the file
        FileSystemHarvester fsh = getHarvester("/fsh-config-caching.json");
        Set<String> items = fsh.getObjectIdList();
        Assert.assertEquals(2, items.size());

        FileUtils.writeStringToFile(testFile, "changed!");

        // next harvest will detect the change
        FileSystemHarvester fsh2 = getHarvester("/fsh-config-caching.json");
        Set<String> items2 = fsh2.getObjectIdList();
        Assert.assertEquals(1, items2.size());
    }

    /**
     * Tests a non recursive harvest with checksums with file deleted on 2nd
     * harvest
     * 
     * @throws Exception
     *             if any error occurs
     */
    @Test
    public void getObjectIdListWithCachingDeleted() throws Exception {
        // the initial harvest will pick up the file
        FileSystemHarvester fsh = getHarvester("/fsh-config-caching.json");
        Set<String> items = fsh.getObjectIdList();
        Assert.assertEquals(2, items.size());

        boolean success = FileUtils.deleteQuietly(testFile);
        if (success) {
            // next harvest will detect the change
            FileSystemHarvester fsh2 = getHarvester("/fsh-config-caching.json");
            Set<String> items2 = fsh2.getObjectIdList();
            Assert.assertEquals(0, items2.size());

            Set<String> items4 = new HashSet<String>();
            do {
                Set<String> items3 = fsh2.getDeletedObjectIdList();
                items4.addAll(items3);
            } while (fsh2.hasMoreDeletedObjects());
            Assert.assertEquals(1, items4.size());
        } else {
            Assert.fail("Failed to delete file during test");
        }
    }

    /**
     * Tests a recursive harvest
     * 
     * @throws Exception
     *             if any error occurs
     */
    @Test
    public void getObjectIdListRecursive() throws Exception {
        FileSystemHarvester fsh = getHarvester("/fsh-config-recursive.json");
        int count = 0;
        do {
            count += fsh.getObjectIdList().size();
        } while (fsh.hasMoreObjects());
        Assert.assertEquals(11, count);
    }

    /**
     * Tests a recursive harvest while ignoring *.gif and *.mp3 files
     * 
     * @throws Exception
     *             if any error occurs
     */
    @Test
    public void getObjectIdListRecursiveIgnoreJpg() throws Exception {
        FileSystemHarvester fsh = getHarvester("/fsh-config-recursive-ignore.json");
        Set<String> items = fsh.getObjectIdList();
        Assert.assertTrue(fsh.hasMoreObjects());
        while (fsh.hasMoreObjects()) {
            items = fsh.getObjectIdList();
            if (!items.isEmpty()) {
                String id = items.iterator().next();
                DigitalObject o = ram.getObject(id);
                String sid = o.getSourceId();
                Assert.assertFalse(sid.endsWith(".gif"));
                Assert.assertFalse(sid.endsWith(".mp3"));
            }
        }
    }

    private FileSystemHarvester getHarvester(String filename) throws Exception {
        Harvester fsh = PluginManager.getHarvester("file-system", ram);
        fsh.init(new File(getClass().getResource(filename).toURI()));
        return (FileSystemHarvester) fsh;
    }
}
