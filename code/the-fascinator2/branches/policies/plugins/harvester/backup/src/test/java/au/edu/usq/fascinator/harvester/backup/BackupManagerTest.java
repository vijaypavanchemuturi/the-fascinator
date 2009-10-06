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
import org.junit.Ignore;
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
 * Note: This test is ignored for now because it fails in Windows due to no
 * "rsync" command
 * 
 * Note:
 * <ul>
 * <li>1. Keep the fullpath of the file in the destination</li>
 * </ul>
 * 
 * e.g. <md5 of emailaddress/home/octalina/somedirectory/xxx.odt
 * 
 * @author Linda Octalina
 * 
 */
@Ignore
public class BackupManagerTest {
    public BackupManager backupManager;

    private GenericDigitalObject fileObject1, fileObject2;

    private MockFileSystemStorage fsStorage;

    private String tmpDir = System.getProperty("java.io.tmpdir");

    private static final String BACKUPLOCATION = System
            .getProperty("user.home")
            + File.separator + ".backup";
    private File backupFile;

    public File testFile1;
    public File testFile2;

    @Before
    public void setup() throws Exception {
        fsStorage = new MockFileSystemStorage();
        fsStorage.init(getConfig("/backup-config.json"));
        System.out.println(fsStorage.getHomeDir());
        if (fsStorage.getHomeDir().exists()) {
            FileUtils.deleteDirectory(fsStorage.getHomeDir());
        }

        backupFile = new File(BACKUPLOCATION);
        if (backupFile.exists()) {
            FileUtils.deleteDirectory(backupFile);
        }

        String file1 = "/fs-harvest-root/test.txt";
        String file2 = "/fs-harvest-root/books/book1.pdf";
        testFile1 = new File(getClass().getResource(file1).toURI());
        testFile2 = new File(getClass().getResource(file2).toURI());

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

        fsStorage.addObject(fileObject1);
        fsStorage.addObject(fileObject2);

        backupManager = new BackupManager();
        backupManager.init(getConfig("/backup-config.json"));

        backupManager.setEmailAddress("someEmail@usq.edu.au");
        backupManager.setBackupLocation(BACKUPLOCATION);

    }

    @After
    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(fsStorage.getHomeDir());
        if (backupFile.exists()) {
            FileUtils.deleteDirectory(backupFile);
        }
    }

    @Test
    public void testEmailAddress() throws Exception {
        backupManager.setEmailAddress("someEmail@usq.edu.au");
        Assert.assertEquals("92069bf2eafca5e28488be4bd77ba225", backupManager
                .getEmailAddress());
    }

    @Test
    public void backupTest() throws IOException, URISyntaxException {
        JsonConfigHelper jsonHelper = searchResult();

        backupManager.backup(jsonHelper.getList("docs").toArray());
        // Should have more checking.
    }

    private File getConfig(String filename) throws Exception {
        return new File(getClass().getResource(filename).toURI());
    }

    private JsonConfigHelper searchResult() throws IOException,
            URISyntaxException {
        String path1 = FilenameUtils.separatorsToUnix(testFile1
                .getAbsolutePath());
        String path2 = FilenameUtils.separatorsToUnix(testFile2
                .getAbsolutePath());
        String results = "{" + "\"docs\" : [ {" + "\"id\": \"" + path1 + "\""
                + ", " + "\"storageId\": [ \"" + path1 + "\" ] " + "}, " + "{"
                + "\"id\": \"" + path2 + "\"" + ", " + "\"storageId\": [ \""
                + path2 + "\" ] " + "}" + " ] " + "}";
        return new JsonConfigHelper(results);
    }
}
