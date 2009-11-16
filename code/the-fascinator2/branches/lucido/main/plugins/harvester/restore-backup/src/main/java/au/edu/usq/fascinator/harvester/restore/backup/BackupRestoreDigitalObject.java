/* 
 * The Fascinator - Plugin - Harvester - File System
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

package au.edu.usq.fascinator.harvester.restore.backup;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;

/**
 * Represents a file on the backup directory
 * 
 * @author Linda Octalina
 */
public class BackupRestoreDigitalObject extends GenericDigitalObject {
	
	private static Logger log = LoggerFactory.getLogger(BackupRestoreDigitalObject.class);
	
	public BackupRestoreDigitalObject(File file, Boolean includeMeta) {
		super(file.getAbsolutePath().replace("\\", "/"));
		addPayload(new BackupRestorePayload(file, null));
		
		// Process zip here with payload type enrichment
		File zipPathFile = new File(file.getAbsolutePath() + ".zip");
		if (includeMeta) {
			if (zipPathFile.exists()) {
				Boolean addedZipfile = false;
	            ZipFile zipFile;
				try {
					zipFile = new ZipFile(zipPathFile);
					Enumeration<? extends ZipEntry> entries = zipFile.entries();
		            while (entries.hasMoreElements()) {
		                ZipEntry entry = entries.nextElement();
		                if (!entry.isDirectory()) {
		                    addPayload(new BackupRestorePayload(zipPathFile, entry));
		                    addedZipfile = true;
		                }
		            }
		            
		            if (addedZipfile) {
		            	//Create a new payload with the value: to NOT re-transform in ICE and aperture
		            	addPayload(new BackupRestorePayload(new File(file.getAbsoluteFile()+".render"), null));
		            }
				} catch (ZipException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
