/* 
 * The Fascinator - Common Library
 * Copyright (C) 2008 University of Southern Queensland
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
package au.edu.usq.fascinator.common.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import org.apache.commons.io.FilenameUtils;

/**
 * Storage API utility methods.
 * 
 * @author Oliver Lucido
 */
public class StorageUtils {

    private static final Logger log = LoggerFactory
            .getLogger(StorageUtils.class);

    /**
     * This method stores a copy of a File as a DigitalObject into the
     * specified Storage
     *
     * @param storage a Storage instance
     * @param file the File to store
     * @return a DigitalObject
     * @throws StorageException if there was an error storing the file
     */
    public static DigitalObject storeFile(Storage storage, File file)
            throws StorageException {
        return storeFile(storage, file, false);
    }
    
    /**
     * This method stores a link to a File as a DigitalObject into the
     * specified Storage
     *
     * @param storage a Storage instance
     * @param file the File to store
     * @return a DigitalObject
     * @throws StorageException if there was an error storing the file
     */
    public static DigitalObject linkFile(Storage storage, File file)
            throws StorageException {
        return storeFile(storage, file, true);
    }

    /**
     * This method stores a File as a DigitalObject into the specified Storage.
     * The File can be stored as a linked Payload if specified.
     * 
     * @param storage a Storage instance
     * @param file the File to store
     * @param linked set true to link to the original file, false to copy
     * @return a DigitalObject
     * @throws StorageException if there was an error storing the file
     */
    public static DigitalObject storeFile(Storage storage, File file, boolean linked)
            throws StorageException {
        DigitalObject object = null;
        Payload payload = null;
        String oid = FilenameUtils.separatorsToUnix(file.getAbsolutePath());
        String pid = FilenameUtils.separatorsToUnix(file.getName());
        try {
            try {
                object = getDigitalObject(storage, oid);
                if (linked) {
                    payload = createLinkedPayload(object, pid, oid);
                } else {
                    payload = createOrUpdatePayload(object, pid,
                            new FileInputStream(file));
                }
            } catch (StorageException se) {
                throw se;
            }
        } catch (FileNotFoundException fnfe) {
            throw new StorageException("File not found '" + oid + "'");
        } finally {
            if (payload != null) {
                payload.close();
            }
        }
        return object;
    }

    /**
     * Gets a DigitalObject from the specified Storage instance. If the object
     * does not exist, this method will attempt to create it.
     *
     * @param storage a Storage instance
     * @param oid the object identifier to get (or create)
     * @return a DigitalObject
     * @throws StorageException if the object could not be retrieved or created
     */
    public static DigitalObject getDigitalObject(Storage storage, String oid)
            throws StorageException {
        DigitalObject object = null;
        try {
            // try to create a new object
            object = storage.createObject(oid);
        } catch (StorageException ex) {
            // object exists, try and get it
            try {
                object = storage.getObject(oid);
            } catch (StorageException ex1) {
                // could not be created and not found
                throw new StorageException(ex1);
            }
        }
        return object;
    }

    /**
     * Create or update a stored Payload in the specified DigitalObject
     *
     * @param object the DigitalObject to create the Payload in
     * @param pid the Payload ID
     * @param in the InputStream for the Payload's data
     * @return a Payload
     * @throws StorageException if the Payload could not be created
     */
    public static Payload createOrUpdatePayload(DigitalObject object,
            String pid, InputStream in) throws StorageException {
        Payload payload = null;
        try {
            payload = object.createStoredPayload(pid, in);
        } catch (StorageException ex) {
            try {
                payload = object.updatePayload(pid, in);
            } catch (StorageException ex1) {
                throw ex1;
            }
        }
        return payload;
    }

    /**
     * Creates a linked Payload in the specified DigitalObject
     *
     * @param object the DigitalObject to create the Payload in
     * @param pid the Payload ID
     * @param path the absolute path to the file the Payload links to
     * @return a Payload
     * @throws StorageException if the Payload could not be created
     */
    public static Payload createLinkedPayload(DigitalObject object,
            String pid, String path) throws StorageException {
        Payload payload = null;
        try {
            payload = object.createLinkedPayload(pid, path);
        } catch (StorageException ex) {
            throw ex;
        }
        return payload;
    }
}
