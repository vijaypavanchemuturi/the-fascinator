package au.edu.usq.fascinator.harvester.backup;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.Configurable;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * Backup files in a specified directory
 * <p>
 * Configuration options:
 * <ul>
 * <li>email: Email address for determining the user space</li>
 * <li>backupDir: destination backup directory</li>
 * <li>ignoreFilter: wildcard patterns of files to ignore separated by '|'
 * (default: .svn)</li>
 * <li>tf-storage-type: storage type used by fascinator (default: file-system)</li>
 * </ul>
 * 
 * @author Linda Octalina
 * 
 */

public class BackupManager implements Harvester, Configurable {

    private static Logger log = LoggerFactory.getLogger(BackupManager.class);

    /** default ignore list */
    private static final String DEFAULT_IGNORE_PATTERNS = ".svn";

    /** Default storage type */
    private static final String DEFAULT_STORAGE_TYPE = "file-system";

    /** Default email */
    private static final String DEFAULT_EMAIL_ADDRESS = "email@usq.edu.au";

    /** filter used to ignore files matching specified patterns */
    private IgnoreFilter ignoreFilter;

    /** configuration */
    private JsonConfig config;

    private JsonConfig systemConfig;

    /** Email address to determine the user space */
    private String emailAddress;

    /** directory of backup destination */
    private File backupDir;

    /** Storage where the original files are stored */
    public Storage realStorage;

    private String storageType;

    /**
     * File filter used to ignore specified files
     */
    private class IgnoreFilter implements FileFilter {

        /** wildcard patterns of files to ignore */
        private String[] patterns;

        public IgnoreFilter(String[] patterns) {
            this.patterns = patterns;
        }

        public boolean accept(File path) {
            for (String pattern : patterns) {
                if (FilenameUtils.wildcardMatch(path.getName(), pattern)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public String getId() {
        return "backup";
    }

    @Override
    public String getName() {
        return "Backup";
    }

    @Override
    public void init(File jsonFile) throws PluginException {
        try {
            config = new JsonConfig(jsonFile);
            /**
             * Set up the default storage plugin that is used, this information
             */
            storageType = config.get("backup/tf-storage-type", "");
            if (storageType == "") {
                systemConfig = new JsonConfig(config.getSystemFile());
                storageType = systemConfig.get("storage/type",
                        DEFAULT_STORAGE_TYPE);
            }
            realStorage = PluginManager.getStorage(storageType);
            realStorage.init(jsonFile);
            log.info("Loaded {}", realStorage.getName());

            /** Initialise default properties for backup plugin */
            setEmailAddress(config.get("backup/email", DEFAULT_EMAIL_ADDRESS));
            setBackupDir(config.get("backup/backupDir", "."));
            ignoreFilter = new IgnoreFilter(config.get("backup/ignoreFilter",
                    DEFAULT_IGNORE_PATTERNS).split("\\|"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Create md5 for emailAddress */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = DigestUtils.md5Hex(emailAddress);
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setBackupDir(String backupDir) {
        this.backupDir = new File(backupDir);

        if (!this.backupDir.exists()) {
            this.backupDir.mkdir();
        }
    }

    public File getBackupDir() {
        return backupDir;
    }

    public void backup(Object[] resultList) {
        // Do backup here
        for (Object doc : resultList) {
            LinkedHashMap document = (LinkedHashMap) doc;
            String oid = document.get("id").toString();
            log.info(" * Backup: " + oid);

            // Retrieve payload from storage (This code might be needed in the
            // future to do the full backup
            // DigitalObject obj = realStorage.getObject(oid);
            //
            // File oidF = new File(oid);
            // Payload payload = realStorage.getPayload(oid, oidF.getName());

            String filePath = backupDir.getAbsolutePath() + File.separator
                    + emailAddress + oid;

            File output = new File(filePath);
            output.getParentFile().mkdirs();

            // Using rsync for linux/mac or cwrsync for window
            String cmd = "rsync";
            if (System.getProperty("os.name").startsWith("Windows")) {
                cmd = "cwrsync";
            }
            try {
                Process proc = Runtime.getRuntime().exec(
                        new String[] { cmd, oid, filePath });
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Using normal copy
            // FileOutputStream fos;
            // try {
            // fos = new FileOutputStream(filePath);
            // IOUtils.copy(payload.getInputStream(), fos);
            // fos.close();
            // } catch (FileNotFoundException e1) {
            // // TODO Auto-generated catch block
            // e1.printStackTrace();
            // } catch (IOException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
        }
    }

    @Override
    public List<DigitalObject> getObjects() throws HarvesterException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasMoreObjects() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void shutdown() throws PluginException {
        // TODO Auto-generated method stub
    }

    @Override
    public List<DigitalObject> getDeletedObjects() {
        // empty for now
        return Collections.emptyList();
    }

    @Override
    public boolean hasMoreDeletedObjects() {
        return false;
    }

    @Override
    public String getConfig() {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(getClass().getResourceAsStream(
                    "/" + getId() + "-config.html"), writer);
        } catch (IOException ioe) {
            writer.write("<span class=\"error\">" + ioe.getMessage()
                    + "</span>");
        }
        return writer.toString();
    }
}
