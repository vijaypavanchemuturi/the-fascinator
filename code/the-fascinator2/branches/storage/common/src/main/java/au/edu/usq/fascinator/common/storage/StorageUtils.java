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
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;

/**
 * Storage API utility methods.
 * 
 * @author Oliver Lucido
 */
public class StorageUtils {

    private static final Logger log = LoggerFactory
            .getLogger(StorageUtils.class);

    /**
     * This method stores a File as a DigitalObject into the specified Storage
     * 
     * @param storage
     *            a Storage instance
     * @param file
     *            the File to store
     * @throws StorageException
     *             if there was an error storing the file
     */
    public static void storeFile(Storage storage, File file)
            throws StorageException {
        DigitalObject object = null;
        Payload payload = null;
        InputStream in = null;
        String oid = file.getAbsolutePath();
        String pid = file.getName();
        try {
            log.info("Storing file '{}'...", file);
            in = new FileInputStream(file);
            try {
                // try to update existing object
                object = storage.getObject(oid);
                try {
                    payload = object.updatePayload(pid, in);
                } catch (StorageException se) {
                    payload = object.createStoredPayload(pid, in);
                }
            } catch (StorageException se) {
                // create new object
                object = storage.createObject(oid);
                payload = object.createStoredPayload(pid, in);
            }
        } catch (FileNotFoundException fnfe) {
            throw new StorageException("File not found '" + oid + "'");
        } finally {
            if (payload != null) {
                payload.close();
            }
            if (object != null) {
                object.close();
            }
        }
    }

}
