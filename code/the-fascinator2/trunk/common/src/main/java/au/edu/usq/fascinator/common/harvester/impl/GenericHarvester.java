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
package au.edu.usq.fascinator.common.harvester.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * Generic Harvester implementation that provides common functionality for
 * subclasses.
 * 
 * @author Oliver Lucido
 */
public abstract class GenericHarvester implements Harvester {

    private String id, name;

    private JsonConfig config;

    private Storage storage;

    public GenericHarvester(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public Set<String> getObjectId(File uploadedFile) throws HarvesterException {
        // By default don't support uploaded files
        throw new HarvesterException(
                "This plugin does not support uploaded files");
    }

    @Override
    public Set<String> getDeletedObjectIdList() throws HarvesterException {
        // By default, don't support deleted objects
        return Collections.emptySet();
    }

    @Override
    public boolean hasMoreDeletedObjects() {
        // By default, don't support deleted objects
        return false;
    }

    public Storage getStorage() throws HarvesterException {
        if (storage == null) {
            throw new HarvesterException("Storage plugin has not been set!");
        }
        return storage;
    }

    @Override
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void init(File jsonFile) throws PluginException {
        try {
            config = new JsonConfig(jsonFile);
            init();
        } catch (IOException ioe) {
            throw new HarvesterException(ioe);
        }
    }

    @Override
    public void init(String jsonString) throws PluginException {
        try {
            config = new JsonConfig(jsonString);
            init();
        } catch (IOException ioe) {
            throw new HarvesterException(ioe);
        }
    }

    public abstract void init() throws HarvesterException;

    @Override
    public void shutdown() throws PluginException {
        // By default do nothing
    }

    public JsonConfig getJsonConfig() throws HarvesterException {
        if (config == null) {
            try {
                config = new JsonConfig();
            } catch (IOException ioe) {
                throw new HarvesterException(ioe);
            }
        }
        return config;
    }
}
