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

import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemDigitalObject extends GenericDigitalObject {

    private Logger log = LoggerFactory.getLogger(FileSystemDigitalObject.class);

    private static String METADATA_SUFFIX = ".meta";

    private File homeDir;

    public FileSystemDigitalObject(File homeDir, String oid) {
        super(oid);
        this.homeDir = homeDir;
        this.buildManifest();
    }

    // Unit testing
    public String getPath() {
        return homeDir.getAbsolutePath();
    }

    private void buildManifest() {
        Map<String, Payload> manifest = this.getManifest();
        this.readFromDisk(manifest, homeDir, 0);
    }

    private void readFromDisk(Map<String, Payload> manifest, File dir, int depth) {
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.endsWith(".meta");
            }
        });
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    File payloadFile = file;
                    if (depth > 0) {
                        File parentFile = file.getParentFile();
                        String relPath = "";
                        for (int i = 0; i < depth; i++) {
                            relPath = parentFile.getName() + File.separator
                                    + relPath;
                            parentFile = parentFile.getParentFile();
                        }
                        payloadFile = new File(relPath, file.getName());
                    }
                    FileSystemPayload payload =
                            new FileSystemPayload(payloadFile.getName(), payloadFile);
                    payload.readExistingMetadata();
                    if (payload.getType().equals(PayloadType.Source)) {
                        setSourceId(payload.getId());
                    }
                    manifest.put(payload.getId(), payload);
                } else if (file.isDirectory()) {
                    readFromDisk(manifest, file, depth + 1);
                }
            }
        }
    }

    @Override
    public Payload createStoredPayload(String pid, InputStream in)
            throws StorageException {
        Payload payload = this.createPayload(pid, in, true);
        return payload;
    }

    @Override
    public Payload createLinkedPayload(String pid, String linkPath)
            throws StorageException {
        try {
            ByteArrayInputStream in =
                    new ByteArrayInputStream(linkPath.getBytes("UTF-8"));
            Payload payload = this.createPayload(pid, in, false);
            return payload;
        } catch (UnsupportedEncodingException ex) {
            throw new StorageException(ex);
        }
    }

    private Payload createPayload(String pid, InputStream in, boolean linked)
            throws StorageException {
        // Manifest check
        Map<String, Payload> manifest = this.getManifest();
        if (manifest.containsKey(pid)) {
            throw new StorageException("ID '" + pid + "' already exists.");
        }

        // File creation
        File newFile = new File(homeDir, pid);
        if (newFile.exists()) {
            throw new StorageException("ID '" + pid + "' already exists.");
        } else {
            newFile.getParentFile().mkdirs();
            try {
                newFile.createNewFile();
            } catch (IOException ex) {
                throw new StorageException("Failed to create file", ex);
            }
        }

        // File storage
        try {
            FileOutputStream out = new FileOutputStream(newFile);
            IOUtils.copy(in, out);
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            log.error("Failed saving payload to disk", ex);
        } catch (IOException ex) {
            log.error("Failed saving payload to disk", ex);
        }

        // Payload creation
        FileSystemPayload payload = new FileSystemPayload(pid, newFile);
        if (this.getSourceId() == null) {
            payload.setType(PayloadType.Source);
            this.setSourceId(pid);
        } else {
            payload.setType(PayloadType.Enrichment);
        }
        payload.setLinked(linked);
        payload.writeMetadata();
        manifest.put(pid, payload);

        return payload;
    }

    @Override
    public void removePayload(String pid) throws StorageException {
        Map<String, Payload> manifest = this.getManifest();
        if (!manifest.containsKey(pid)) {
            throw new StorageException("pID '" + pid + "' not found.");
        }

        // Close the payload first in case
        manifest.get(pid).close();
        File realFile = new File(homeDir, pid);
        File metaFile = new File(homeDir, pid + METADATA_SUFFIX);

        boolean result = false;
        if (realFile.exists()) {
            result = FileUtils.deleteQuietly(realFile);
            if (!result) {
                System.out.println("Deleting : " + realFile.getAbsolutePath());
                throw new StorageException("Failed to delete : "
                        + realFile.getAbsolutePath());
            }
        }
        if (metaFile.exists()) {
            result = FileUtils.deleteQuietly(metaFile);
            if (!result) {
                System.out.println("Deleting : " + realFile.getAbsolutePath());
                throw new StorageException("Failed to delete : "
                        + metaFile.getAbsolutePath());
            }
        }

        manifest.remove(pid);
    }

    @Override
    public Payload updatePayload(String pid, InputStream in)
            throws StorageException {
        File oldFile = new File(homeDir, pid);
        if (!oldFile.exists()) {
            throw new StorageException("pID '" + pid + "': file not found");
        }

        // File update
        try {
            FileOutputStream out = new FileOutputStream(oldFile);
            IOUtils.copy(in, out);
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            log.error("Failed saving payload to disk", ex);
        } catch (IOException ex) {
            log.error("Failed saving payload to disk", ex);
        }

        // Payload update
        FileSystemPayload payload = (FileSystemPayload) this.getPayload(pid);
        payload.writeMetadata();
        return payload;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", getId(), homeDir);
    }
}
