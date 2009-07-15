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

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.impl.GenericDigitalObject;

/**
 * Provides IceDigitalObject for rendered Document from ICE 
 * of a given file.
 * 
 * @author Linda Octalina & Oliver Lucido
 * 
 */
public class IceDigitalObject extends GenericDigitalObject {

	private IcePayload icePayload;
	
	/**
     * IceDigitalObject constructor
     * 
     * @param object, DigitalObject type
     * @param zipPath as String
     */
	public IceDigitalObject(DigitalObject object, String zipPath) {
		super(object.getId());
        icePayload = new IcePayload(zipPath);
        //For testing we are using the GenericDigtialObject instead of RDFDigitalObject
        if (object instanceof GenericDigitalObject) setMetadataId(object.getId());
        else setMetadataId(object.getMetadata().getId());
        
        addPayload(icePayload);
        for (Payload payload : object.getPayloadList()) {
            addPayload(payload);
        }
	}
	
	/**
     * Getting the Payload object
     */
    @Override
    public Payload getMetadata() {
        return icePayload;
    }
}
