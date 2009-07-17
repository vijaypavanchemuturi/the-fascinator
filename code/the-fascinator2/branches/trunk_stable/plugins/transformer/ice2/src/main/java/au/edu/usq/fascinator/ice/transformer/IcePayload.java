/*
 * The Fascinator
 * Copyright (C) 2009  University of Southern Queensland
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

package au.edu.usq.fascinator.ice.transformer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;

/**
 * IcePayload class to store rendition zip info
 * returned by ice-service
 * 
 * @author Linda Octalina & Oliver Lucido
 * 
 */
public class IcePayload implements Payload {

	private String zipPath;
	
	/**
     * IcePayload Constructor
     * 
     * @param zipPath as String
     */
	public IcePayload(String zipPath) {
		this.zipPath = zipPath;
	}
	
	/**
     * getContentType method
     * 
     * @return metadata type
     */
	@Override
	public String getContentType() {
		return "application/zip";
	}

	/**
     * getId method
     * 
     * @return metadata id
     */
	@Override
	public String getId() {
		return "ice-rendition.zip";
	}

	/**
     * getInputStream method
     * 
     * @return metadata content as InputStream
     */
	@Override
	public InputStream getInputStream() throws IOException {
		if (zipPath != null ) {
			FileInputStream fis = new FileInputStream(zipPath);
			return fis;
		}
		return null;
	}

	/**
     * getLabel method
     * 
     * @return metadata label
     */
	@Override
	public String getLabel() {
		return "ICE rendition";
	}

	/**
     * getType method
     * 
     * @return payload type
     */
	@Override
	public PayloadType getType() {
		return PayloadType.Data;
	}	
	
}
