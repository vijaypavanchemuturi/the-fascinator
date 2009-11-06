/* 
 * The Fascinator - Plugin - Harvester - JSON Queue
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
package au.edu.usq.fascinator.harvester.backuprestore;

import java.io.File;

import au.edu.usq.fascinator.common.storage.impl.FilePayload;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;

/**
 * Represents a backup files from the backup Directory
 * 1) original files
 * 2) metadata/rendition  (optional)
 * 3) portal			  (optional)
 * 
 * @author Linda Octlaina
 */

public class BackupRestoreDigitalObject extends GenericDigitalObject {
	
	public BackupRestoreDigitalObject(File file) {
		super(file.getAbsolutePath().replace("\\", "/"));
//        addPayload(new FilePayload(file));
	}
//	/**
//     * Creates an objectfor the file located at the specified URI
//     * 
//     * @param uri URI to a local file
//     * @param info file state information
//     */
//    public JsonQDigitalObject(String uri, Map<String, String> info) {
//        super(uri);
//        try {
//            URL url = new URL(URLDecoder.decode(uri, "UTF-8"));
//            File file = new File(url.getPath());
//            setId(file.getAbsolutePath());
//            Payload metadata = new JsonQMetadataPayload(file, info);
//            setMetadataId(metadata.getId());
//            addPayload(metadata);
//            addPayload(new FilePayload(file));
//        } catch (UnsupportedEncodingException uee) {
//        } catch (MalformedURLException e) {
//        }
//    }
}





