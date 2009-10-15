/* 
 * The Fascinator - Plugin - Backup - FileSystem

import org.junit.Before;
hern Queensland
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

package au.edu.usq.fascinator.harvester.backup;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;
import au.edu.usq.fascinator.common.storage.impl.GenericPayload;

/**
 * TODO This BackupManagerTest involved:
 * <ul>
 * <li>1. Create dummy results
 * <li>2. Retrieve the original file from the filesystemstorage based on the
 * object id in search result
 * <li>3. Backup the original file based on the objectid from solr results</li>
 * <li>4. This backup should use rsync by: - Copying the original file to temp
 * directory then do bundle rsync from the temp dir (this is not good if the
 * files are big - Directly copy files one by one to destination</li>
 * </ul>
 * 
 * Window uses cwrsync Linux/Mac OS use rsync
 * 
 * Note:
 * <ul>
 * <li>1. Keep the fullpath of the file in the destination</li>
 * </ul>
 * 
 * e.g. <md5 of emailaddress>/home/octalina/somedirectory/xxx.odt
 * 
 * @author Linda Octalina
 * 
 */

public class BackupManagerTest {
    public BackupManager backupManager;

    private GenericDigitalObject fileObject1, fileObject2, fileObject3;

    /** File system storage that is used for the testing */
    private MockFileSystemStorage fsStorage;

    private File configFile;

    public File testFile1, testFile2, testFile3;

    @Before
    public void setup() throws Exception {
        fsStorage = new MockFileSystemStorage();
        configFile = new File(getClass().getResource("/backup-config.json")
                .toURI());
        // Set up filesystem storage
        fsStorage.init(configFile);
        if (fsStorage.getHomeDir().exists()) {
            FileUtils.deleteDirectory(fsStorage.getHomeDir());
        }

        // Initialise the backup plugin
        backupManager = new BackupManager();
        backupManager.init(configFile);

        // Remove the exsiting backup directory for testing
        if (backupManager.getBackupDir().exists()) {
            FileUtils.deleteDirectory(backupManager.getBackupDir());
        }

        String file1 = "/fs-harvest-root/test.txt";
        String file2 = "/fs-harvest-root/books/book1.pdf";
        String file3 = "/fs-harvest-root/pictures/diagram with space1.gif";
        testFile1 = new File(getClass().getResource(file1).toURI());
        testFile2 = new File(getClass().getResource(file2).toURI());
        testFile3 = new File(getClass().getResource(file3).toURI());

        fileObject1 = new GenericDigitalObject(testFile1.getAbsolutePath());
        GenericPayload testPayload1 = new GenericPayload(testFile1.getName(),
                "Plain text file", "text/plain");
        testPayload1.setInputStream(getClass().getResourceAsStream(file1));
        // fsStorage.addObject(fileObject1);
        fileObject1.addPayload(testPayload1);

        fileObject2 = new GenericDigitalObject(testFile2.getAbsolutePath());
        GenericPayload testPayload2 = new GenericPayload(testFile2.getName(),
                "Pdf file", "application/pdf");
        testPayload2.setInputStream(getClass().getResourceAsStream(file2));
        fileObject2.addPayload(testPayload2);

        fileObject3 = new GenericDigitalObject(testFile3.getAbsolutePath());
        GenericPayload testPayload3 = new GenericPayload(testFile3.getName(),
                "Picture file", "image/gif");
        testPayload3.setInputStream(getClass().getResourceAsStream(file3));
        fileObject3.addPayload(testPayload3);

        fsStorage.addObject(fileObject1);
        fsStorage.addObject(fileObject2);
        fsStorage.addObject(fileObject3);
    }

    @After
    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(fsStorage.getHomeDir());
        if (backupManager.getBackupDir().exists()) {
            // FileUtils.deleteDirectory(backupManager.getBackupDir());
        }
    }

    @Test
    public void testEmailAddress() throws Exception {
        Assert.assertEquals("3c6e1c07ccd4f969bbc93f2f0f85d9f5", backupManager
                .getEmailAddress());
    }

    @Test
    public void backupTest() throws IOException, URISyntaxException {
        JsonConfigHelper jsonHelper = searchResult();
        System.out.println(jsonHelper.toString());
        backupManager.backup(jsonHelper.getList("docs").toArray());

        String userSpace = backupManager.getBackupDir().getAbsolutePath()
                + File.separator + backupManager.getEmailAddress();

        File file1BackupPath = new File(userSpace + testFile1.getAbsolutePath());
        Assert.assertTrue(file1BackupPath.exists());

        File file2BackupPath = new File(userSpace + testFile2.getAbsolutePath());
        Assert.assertTrue(file2BackupPath.exists());
    }

    private JsonConfigHelper searchResult() throws IOException,
            URISyntaxException {
        String path1 = FilenameUtils.separatorsToSystem(testFile1
                .getAbsolutePath());
        String path2 = FilenameUtils.separatorsToSystem(testFile2
                .getAbsolutePath());
        String path3 = FilenameUtils.separatorsToSystem(testFile3
                .getAbsolutePath());
        String results = "{" + "\"docs\" : [ {" + "\"id\": \"" + path1 + "\""
                + ", " + "\"storageId\": [ \"" + path1 + "\" ] " + "}, " + "{"
                + "\"id\": \"" + path2 + "\"" + ", " + "\"storageId\": [ \""
                + path2 + "\" ] " + "}, " + "{" + "\"id\": \"" + path3 + "\""
                + ", " + "\"storageId\": [ \"" + path3 + "\" ] " + "}" + " ] "
                + "}";
        return new JsonConfigHelper(results);
    }
}
