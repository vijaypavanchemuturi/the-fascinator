/* 
 * The Fascinator - Plugin - Harvester - OAI-PMH
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

package au.edu.usq.fascinator.harvester.backuprestore;

import java.io.File;
import java.util.List;

import au.edu.usq.fascinator.api.Configurable;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;


public class BackupRestoreHarvester implements Harvester, Configurable {

	@Override
	public List<DigitalObject> getDeletedObjects() throws HarvesterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DigitalObject> getObjects() throws HarvesterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasMoreDeletedObjects() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasMoreObjects() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(File jsonFile) throws PluginException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() throws PluginException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getConfig() {
		// TODO Auto-generated method stub
		return null;
	}


}
