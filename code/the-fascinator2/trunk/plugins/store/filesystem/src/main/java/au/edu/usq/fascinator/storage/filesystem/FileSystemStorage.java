/* 
 * The Fascinator - File System storage plugin
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
package au.edu.usq.fascinator.storage.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.store.DigitalObject;
import au.edu.usq.fascinator.api.store.Payload;
import au.edu.usq.fascinator.api.store.Storage;
import au.edu.usq.fascinator.api.store.StorageException;

/**
 * File system storage plugin based on Dflat/Pairtree
 * 
 * @author Oliver Lucido
 */
public class FileSystemStorage implements Storage {

    private static final String DEFAULT_HOME_DIR = System
            .getProperty("user.home")
            + File.separator + ".fascinator" + File.separator + "store";

    private Logger log = LoggerFactory.getLogger(FileSystemStorage.class);

    private File homeDir;

    public String getId() {
        return "filesystem";
    }

    public String getName() {
        return "File System Storage Module";
    }

    public void init(File jsonFile) throws StorageException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(jsonFile, JsonNode.class);
            JsonNode storageNode = rootNode.get("storage");
            if (storageNode != null) {
                String type = storageNode.get("type").getTextValue();
                JsonNode configNode = storageNode.get("config");
                String home = configNode.get("home").getTextValue();
                homeDir = new File(home);
            } else {
                log.info("No configuration defined, using defaults");
                homeDir = new File(DEFAULT_HOME_DIR);
            }
            if (!homeDir.exists()) {
                homeDir.mkdirs();
            }
        } catch (JsonParseException jpe) {
            throw new StorageException(jpe);
        } catch (IOException ioe) {
            throw new StorageException(ioe);
        }
    }

    public void shutdown() throws StorageException {
        // Don't need to do anything
    }

    public String addObject(DigitalObject object) throws StorageException {
        FileSystemDigitalObject fileObject = new FileSystemDigitalObject(object);
        log.debug("addObject({})", fileObject);
        for (Payload payload : fileObject.getPayloadList()) {
            addPayload(fileObject.getId(), payload);
        }
        return fileObject.getPath().getAbsolutePath();
    }

    public void removeObject(String oid) {
    }

    public void addPayload(String oid, Payload payload) {
        log.debug("oid={}, payload={}", oid, payload);
        FileSystemPayload filePayload = new FileSystemPayload(payload);
        FileSystemDigitalObject fileObject = (FileSystemDigitalObject) getObject(oid);
        File payloadFile = new File(fileObject.getPath(), filePayload.getFile()
                .toString());
        File parentDir = payloadFile.getParentFile();
        log.debug("payloadFile:{}", payloadFile, parentDir);
        parentDir.mkdirs();
        try {
            FileOutputStream out = new FileOutputStream(payloadFile);
            IOUtils.copy(filePayload.getInputStream(), out);
            out.close();
        } catch (IOException ioe) {
            log.error("", ioe);
        }
    }

    public void removePayload(String oid, String pid) {
    }

    public DigitalObject getObject(String oid) {
        FileSystemDigitalObject fileObject = new FileSystemDigitalObject(
                homeDir, oid);
        return fileObject;
    }

    public Payload getPayload(String oid, String pid) {
        return null;
    }

}
