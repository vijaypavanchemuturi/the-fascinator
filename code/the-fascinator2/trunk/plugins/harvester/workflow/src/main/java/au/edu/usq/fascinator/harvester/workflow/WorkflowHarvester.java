/*
 * The Fascinator - Plugin - Harvester - Workflows
 * Copyright (C) 2010 University of Southern Queensland
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

import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.harvester.impl.GenericHarvester;
import au.edu.usq.fascinator.common.storage.StorageUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A trimmed down version of the file-system harvester.
 * 
 * Doesn't need recursion or caching.
 *
 * @author Greg Pendlebury
 */
public class WorkflowHarvester extends GenericHarvester {

    /** logging */
    private Logger log = LoggerFactory.getLogger(WorkflowHarvester.class);

    /** flag for forcing local storage */
    private boolean forceLocalStorage;

    public WorkflowHarvester() {
        super("workflow-harvester", "Workflow Harvester");
    }

    @Override
    public void init() throws HarvesterException {
        forceLocalStorage = Boolean.parseBoolean(getJsonConfig()
                .get("harvester/workflow-harvester/force-storage", "true"));
    }

    @Override
    public Set<String> getObjectIdList() throws HarvesterException {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getObjectId(File uploadedFile)
            throws HarvesterException {
            Set<String> objectIds = new HashSet<String>();
        try {
            objectIds.add(createDigitalObject(uploadedFile));
        } catch (StorageException se) {
            throw new HarvesterException(se);
        }
        return objectIds;
    }

    @Override
    public boolean hasMoreObjects() {
        return false;
    }

    private String createDigitalObject(File file) throws HarvesterException,
            StorageException {
        DigitalObject object = StorageUtils.storeFile(getStorage(), file,
                forceLocalStorage);
        String oid = object.getId();

        // update object metadata
        Properties props = object.getMetadata();
        props.setProperty("render-pending", "true");

        object.close();
        return oid;
    }
}