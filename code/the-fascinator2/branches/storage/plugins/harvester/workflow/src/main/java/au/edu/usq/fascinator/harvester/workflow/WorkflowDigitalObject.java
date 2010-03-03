/*
 * The Fascinator - Plugin - Harvester - Workflow
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
package au.edu.usq.fascinator.harvester.workflow;

import au.edu.usq.fascinator.api.storage.PayloadType;
import java.io.File;

import au.edu.usq.fascinator.common.storage.impl.FilePayload;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;

/**
 * Represents a file on the local file system
 *
 * @author Greg Pendebury
 */
public class WorkflowDigitalObject extends GenericDigitalObject {

    /**
     * Creates an object for a local file
     *
     * @param file a file
     */
    public WorkflowDigitalObject(File file) {
        this(file, true);
    }
    public WorkflowDigitalObject(File file, boolean storeLocal) {
        super(file.getAbsolutePath().replace("\\", "/"));
        FilePayload newFile = new FilePayload(file);

        if (storeLocal) {
            // Make sure the payload is flagged as uploaded otherwise
            //   the storage API might not store it locally.
            newFile.setType(PayloadType.Uploaded);
        }

        addPayload(newFile);
    }
}
