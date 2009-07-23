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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.common.MimeTypeUtil;
import au.edu.usq.fascinator.common.storage.impl.GenericPayload;

public class FileSystemPayload extends GenericPayload {

    private File file;

    private File meta;
    private File homeDir;

    public FileSystemPayload(Payload payload) {
        super(payload);
        System.err.println("id=" + getId());
        updateMeta();
    }

    public FileSystemPayload(File homeDir, File payloadFile) {
        this(payloadFile);
        this.homeDir = homeDir;
        updateMeta();
    }

    public FileSystemPayload(File payloadFile) {
        this.file = payloadFile;
        if (file.isAbsolute()) {
            setId(payloadFile.getName());
        } else {
            setId(payloadFile.getPath());
        }
        setLabel(payloadFile.getAbsolutePath()); // TODO get from meta?
        setContentType(MimeTypeUtil.getMimeType(payloadFile));
        updateMeta();
    }

    private void updateMeta() {
        // store things like label and payload type
    }

    public File getFile() {
        if (file == null) {
            return new File(getId());
        } else {
            if (file.isAbsolute()) {
                return new File(homeDir, file.getName());
            } else {
                return new File(homeDir, file.getPath());
            }
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (file == null) {
            return super.getInputStream();
        }
        return new FileInputStream(getFile());
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getId(), getFile().getAbsolutePath());
    }
}
