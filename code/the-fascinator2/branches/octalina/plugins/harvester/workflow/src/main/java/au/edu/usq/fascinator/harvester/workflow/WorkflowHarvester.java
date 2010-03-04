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

import au.edu.usq.fascinator.api.Configurable;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.common.JsonConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A trimmed down version of the file-system harvester.
 * 
 * Doesn't need recursion or caching.
 *
 * @author Greg Pendlebury
 */
public class WorkflowHarvester implements Harvester, Configurable {

    /** logging */
    private Logger log = LoggerFactory.getLogger(WorkflowHarvester.class);

    /** configuration */
    private JsonConfig config;

    /** flag for forcing local storage */
    private boolean forceLocalStorage = true;

    @Override
    public String getId() {
        return "workflow-harvester";
    }

    @Override
    public String getName() {
        return "Workflow Harvester";
    }

    @Override
    public void init(String jsonString) throws HarvesterException {
        try {
            config = new JsonConfig(new ByteArrayInputStream(
                    jsonString.getBytes("UTF-8")));
            setConfig();
        } catch (UnsupportedEncodingException e) {
            throw new HarvesterException(e);
        } catch (IOException e) {
            throw new HarvesterException(e);
        }
    }

    @Override
    public void init(File jsonFile) throws HarvesterException {
        try {
            config = new JsonConfig(jsonFile);
            setConfig();
        } catch (IOException ioe) {
            throw new HarvesterException(ioe);
        }
    }

    private void setConfig() throws IOException {
        forceLocalStorage = Boolean.parseBoolean(
                String.valueOf(config.get(
                "harvester/workflow-harvester/force-storage",
                String.valueOf(forceLocalStorage))));
    }

    @Override
    public void shutdown() throws HarvesterException {
        // Do nothing
    }

    @Override
    public List<DigitalObject> getObjects() throws HarvesterException {
        return Collections.emptyList();
    }

    @Override
    public List<DigitalObject> getObject(File uploadedFile)
            throws HarvesterException {
        List<DigitalObject> fileObjects = new ArrayList<DigitalObject>();
        fileObjects.add(new WorkflowDigitalObject(uploadedFile, forceLocalStorage));
        return fileObjects;
    }

    @Override
    public String getConfig() {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(getClass().getResourceAsStream(
                    "/" + getId() + "-config.html"), writer);
        } catch (IOException ioe) {
            writer.write("<span class=\"error\">" + ioe.getMessage()
                    + "</span>");
        }
        return writer.toString();
    }

    @Override
    public List<DigitalObject> getDeletedObjects() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasMoreObjects() {
        return false;
    }

    @Override
    public boolean hasMoreDeletedObjects() {
        return false;
    }
}
