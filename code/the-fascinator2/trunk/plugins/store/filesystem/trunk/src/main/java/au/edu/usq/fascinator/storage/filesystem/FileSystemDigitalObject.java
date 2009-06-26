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

import au.edu.usq.fascinator.api.DigitalObject;
import au.edu.usq.fascinator.api.Payload;
import au.edu.usq.fascinator.api.impl.BasicDigitalObject;

public class FileSystemDigitalObject extends BasicDigitalObject {

    private File homeDir;

    private PairTree pairTree;

    private String pairPath;

    public FileSystemDigitalObject(File homeDir, String id) {
        super(id);
        init();
    }

    public FileSystemDigitalObject(DigitalObject object) {
        super(object.getId());
        init();
        for (Payload payload : object.getPayloadList()) {
            FileSystemPayload filePayload = new FileSystemPayload(payload);
            addPayload(filePayload);
        }
    }

    private void init() {
        this.homeDir = new File(homeDir, "pairtree_root");
        pairTree = new PairTree(homeDir);
        pairPath = pairTree.getPairPath(getId());
    }

    public File getPath() {
        return new File(homeDir, pairPath);
    }

    public String toString() {
        return getId() + " [" + pairPath + "]";
    }
}
