package au.edu.usq.fascinator.harvester.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * MockFileSystemStorage is the replication of FileSystemStorage used in
 * fascinator and is used in BackupManagerTest unit test
 * 
 * @author Linda Octalina
 * 
 */

public class MockFileSystemStorage implements Storage {
    private static final String DEFAULT_HOME_DIR = System
            .getProperty("user.home")
            + File.separator + ".fascinator" + File.separator + "storage";

    private final Logger log = LoggerFactory
            .getLogger(MockFileSystemStorage.class);

    private File homeDir;

    public String getId() {
        return "mock-file-system";
    }

    public String getName() {
        return "Mock File System Storage";
    }

    public void init(File jsonFile) throws StorageException {
        try {
            JsonConfig config = new JsonConfig(jsonFile);
            homeDir = new File(config.get("storage/file-system/home",
                    DEFAULT_HOME_DIR));
            if (!homeDir.exists()) {
                homeDir.mkdirs();
            }
        } catch (IOException ioe) {
            throw new StorageException(ioe);
        }
    }

    public void shutdown() throws StorageException {
        // Don't need to do anything
    }

    public String addObject(DigitalObject object) throws StorageException {
        MockFileSystemDigitalObject fileObject = new MockFileSystemDigitalObject(
                homeDir, object.getId());
        log.debug("Adding object {}", fileObject);
        for (Payload payload : object.getPayloadList()) {
            addPayload(fileObject.getId(), payload);
        }
        return fileObject.getPath().getAbsolutePath();
    }

    public void removeObject(String oid) {
        log.debug("Removing object {}", oid);
        MockFileSystemDigitalObject fileObject = (MockFileSystemDigitalObject) getObject(oid);
        FileUtils.deleteQuietly(fileObject.getPath());
    }

    public void addPayload(String oid, Payload payload) {
        log.debug("Adding payload {} to {}", payload.getId(), oid);
        MockFileSystemDigitalObject fileObject = (MockFileSystemDigitalObject) getObject(oid);
        MockFileSystemPayload filePayload = new MockFileSystemPayload(
                fileObject.getPath(), payload);
        File payloadFile = filePayload.getFile();
        File parentDir = payloadFile.getParentFile();
        parentDir.mkdirs();
        try {
            FileOutputStream out = new FileOutputStream(payloadFile);
            IOUtils.copy(filePayload.getInputStream(), out);
            out.close();
        } catch (IOException ioe) {
            log.error("Failed to add payload", ioe);
        }
    }

    public void removePayload(String oid, String pid) {
        log.debug("Removing payload {} from {}", pid, oid);
    }

    public DigitalObject getObject(String oid) {
        log.debug("Getting object {}", oid);
        return new MockFileSystemDigitalObject(homeDir, oid);
    }

    public Payload getPayload(String oid, String pid) {
        log.debug("Getting payload {} from {}", pid, oid);
        return getObject(oid).getPayload(pid);
    }

    public File getHomeDir() {
        return homeDir;
    }
}
