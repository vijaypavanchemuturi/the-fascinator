package au.edu.usq.fascinator.harvester.backup;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.common.JsonConfig;

public class BackupManager implements Harvester {

    private static Logger log = LoggerFactory.getLogger(BackupManager.class);

    /** configuration */
    private JsonConfig config;

    /** Default storage type */
    private static final String DEFAULT_STORAGE_TYPE = "file-system";

    public Storage realStorage;

    private String emailAddress;

    private String backupLocation;

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
    public String getId() {
        // TODO Auto-generated method stub
        return "backup";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Backup";
    }

    @Override
    public void init(File jsonFile) throws PluginException {
        try {
            config = new JsonConfig(jsonFile);
            realStorage = PluginManager.getStorage(config.get("storage/type",
                    DEFAULT_STORAGE_TYPE));
            realStorage.init(jsonFile);
            log.info("Loaded {}", realStorage.getName());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO Auto-generated method stub

    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = DigestUtils.md5Hex(emailAddress);
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setBackupLocation(String backupLocation) {
        this.backupLocation = backupLocation;

        File backupDir = new File(backupLocation);
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }
    }

    public String getBackupLocation() {
        return backupLocation;
    }

    public void backup(Object[] resultList) {
        // Do backup here
        // log.info(" * Backup ()" + resultList);
        for (Object doc : resultList) {
            LinkedHashMap document = (LinkedHashMap) doc;
            String oid = document.get("id").toString();
            log.info(" * Backup: " + oid);
            // Retrieve payload from storage
            DigitalObject obj = realStorage.getObject(oid);

            File oidF = new File(oid);
            Payload payload = realStorage.getPayload(oid, oidF.getName());

            String filePath = backupLocation + File.separator + emailAddress
                    + oid;

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
}
