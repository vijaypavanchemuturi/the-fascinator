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

package au.edu.usq.fascinator.harvester.restore.backup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.common.MimeTypeUtil;
import au.edu.usq.fascinator.common.storage.impl.GenericPayload;

/**
 * Represents an Backup Storage metadata payload
 * 
 * @author Linda Octalina
 */

public class BackupRestorePayload extends GenericPayload {
	
	private static Logger log = LoggerFactory.getLogger(BackupRestorePayload.class);
	
	/** the file this payload represents */
    private boolean isZip = false;
    private ZipEntry zipEntry;
    private File filePath;
	
    /**
     * BackupRestorePayload constructor
     * 
     * @param filePath
     * @param zipEntry
     */
	public BackupRestorePayload(File filePath, ZipEntry zipEntry) {
		this.filePath = filePath;
        String name = filePath.getName();
        if (zipEntry != null) {
        	this.zipEntry = zipEntry;
        	isZip = true;
            name = zipEntry.getName();
        }
        setId(name);
        setLabel(name);
        setContentType(MimeTypeUtil.getMimeType(name));
        if (isZip) {
        	setType(PayloadType.Enrichment);
        }
        if (filePath.getAbsolutePath().endsWith(".render")) {
        	String notRender = "Do not render";
        	InputStream in;
			try {
				in = new ByteArrayInputStream(notRender.getBytes("UTF-8"));
				setInputStream(in);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	setType(PayloadType.Enrichment);
        }
        
    	if (!filePath.getAbsolutePath().endsWith(".render") && !isZip) {
    		setType(PayloadType.Data);
    	}
        
	}
	
	/**
	 * Get Input Stream of the Payload
	 */
    @Override
    public InputStream getInputStream() throws IOException {
    	if (isZip) {
            ZipFile zipFile = new ZipFile(filePath);
            return zipFile.getInputStream(zipEntry);
        } else {
            return new FileInputStream(filePath);
        }
    	
    }
}
