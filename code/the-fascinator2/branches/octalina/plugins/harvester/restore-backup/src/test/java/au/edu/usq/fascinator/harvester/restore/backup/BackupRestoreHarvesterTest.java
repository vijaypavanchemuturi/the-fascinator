package au.edu.usq.fascinator.harvester.restore.backup;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import au.edu.usq.fascinator.api.storage.DigitalObject;

/**
 * Unit test for Backup Restore Harvester plugin
 * 
 * @author Linda Octalina
 */
public class BackupRestoreHarvesterTest{
	
	/**
     * Tests a non recursive harvest
     * 
     * @throws Exception if any error occurs
     */
    @Test
    public void getObjectsWithOriginalFilesOnly() throws Exception {
        BackupRestoreHarvester backupRestore = new BackupRestoreHarvester();
        backupRestore.init(getConfig("/test-restore-backup-folder-original-files-only.json"));
        
        List<DigitalObject> items = backupRestore.getObjects();
        //System.out.println(items.size());
        Assert.assertEquals(8, items.size());
    }
	
    @Test
    public void getObjectsWithRendition() throws Exception {
        BackupRestoreHarvester backupRestore = new BackupRestoreHarvester();
        backupRestore.init(getConfig("/test-restore-backup-with-portal.json"));
        
        List<DigitalObject> items = backupRestore.getObjects();
        //System.out.println(items.size());
        //Will ignore the rendition zip file
        Assert.assertEquals(3, items.size());
    }
	
	private File getConfig(String filename) throws Exception {
        return new File(getClass().getResource(filename).toURI());
    }
}
