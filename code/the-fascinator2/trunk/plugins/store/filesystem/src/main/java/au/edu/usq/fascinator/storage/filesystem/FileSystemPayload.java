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
import java.io.IOException;

import au.edu.usq.fascinator.api.impl.BasicPayload;
import au.edu.usq.fascinator.api.store.Payload;

public class FileSystemPayload extends BasicPayload {

    public FileSystemPayload(Payload payload) {
        super(payload.getId(), payload.getLabel(), payload.getContentType());
        setPayloadType(payload.getType());
        try {
            setInputStream(payload.getInputStream());
        } catch (IOException e) {
            // TODO get null input stream
            e.printStackTrace();
        }
    }

    public File getFile() {
        return new File(getType().name().toLowerCase(), getId());
    }

    public String toString() {
        return "[" + getType() + "] " + getId();
    }

}
