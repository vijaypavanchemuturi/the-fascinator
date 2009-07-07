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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.codec.digest.DigestUtils;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.impl.BasicDigitalObject;

public class FileSystemDigitalObject extends BasicDigitalObject {

    private File homeDir;

    private File path;

    private String hashId;

    public FileSystemDigitalObject(File homeDir, String id) {
        super(id);
        this.homeDir = homeDir;
    }

    public FileSystemDigitalObject(File homeDir, DigitalObject object) {
        super(object.getId());
        this.homeDir = homeDir;
        for (Payload payload : object.getPayloadList()) {
            FileSystemPayload filePayload = new FileSystemPayload(payload);
            addPayload(filePayload);
        }
    }

    public String getHashId() {
        if (hashId == null) {
            hashId = DigestUtils.md5Hex(getId());
        }
        return hashId;
    }

    public File getPath() {
        if (path == null) {
            String dir = getHashId().substring(0, 2) + File.separator
                    + getHashId().substring(2, 4);
            File parentDir = new File(homeDir, dir);
            String encodedId = getId();
            try {
                encodedId = URLEncoder.encode(encodedId, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
            }
            path = new File(parentDir, encodedId);
        }
        return path;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", getId(), getPath());
    }

}
