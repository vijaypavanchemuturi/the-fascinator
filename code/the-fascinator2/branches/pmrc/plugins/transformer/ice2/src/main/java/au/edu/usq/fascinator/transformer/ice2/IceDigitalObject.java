/*
 * The Fascinator - Plugin - Transformer - ICE 2
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
package au.edu.usq.fascinator.transformer.ice2;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.common.MimeTypeUtil;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;

/**
 * Digital object representation for ICE renditions
 * 
 * @author Linda Octalina
 * @author Oliver Lucido
 */
public class IceDigitalObject extends GenericDigitalObject {

    private static final String ZIP_MIME_TYPE = "application/zip";

    /** Logging */
    private Logger log = LoggerFactory.getLogger(IceDigitalObject.class);

    public IceDigitalObject(DigitalObject object) {
        super(object);
    }

    /**
     * Creates a DigitalObject with rendition payloads from the specified ZIP or
     * regular file
     * 
     * @param object original object
     * @param file path to the ICE rendition
     */
    public IceDigitalObject(DigitalObject object, File file) {
        super(object);
        try {
            if (ZIP_MIME_TYPE.equals(MimeTypeUtil.getMimeType(file))) {
                ZipFile zipFile = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory()) {
                        addPayload(new IcePayload(file, entry));
                    }
                }
            } else {
                addPayload(new IcePayload(file));
            }
        } catch (IOException ioe) {
            log.error("Failed to add rendition payloads: {}", ioe);
        }
    }
}
