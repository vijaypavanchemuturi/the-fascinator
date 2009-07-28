/* 
 * The Fascinator - Portal
 * Copyright (C) 2008-2009 University of Southern Queensland
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
package au.edu.usq.fascinator.portal.services.impl;

import org.apache.tapestry5.ioc.annotations.Inject;

import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.portal.services.ContentManager;
import au.edu.usq.fascinator.portal.services.PortalManager;
import au.edu.usq.fascinator.portal.services.ScriptingServices;

public class ScriptingServicesImpl implements ScriptingServices {

    @Inject
    private ContentManager contentManager;

    @Inject
    private PortalManager portalManager;

    @Inject
    private Indexer indexerService;

    @Override
    public ContentManager getContentManager() {
        return contentManager;
    }

    @Override
    public PortalManager getPortalManager() {
        return portalManager;
    }

    @Override
    public Indexer getIndexer() {
        return indexerService;
    }

    @Override
    public Storage getStorage() {
        return null;
    }

    @Override
    public Harvester getHarvester(String id) {
        return PluginManager.getHarvester(id);
    }

}
