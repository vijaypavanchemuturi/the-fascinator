/*
 * The Fascinator - JSON Queue Harvester
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
package au.edu.usq.fascinator.harvester;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import au.edu.usq.fascinator.api.impl.BasicDigitalObject;
import au.edu.usq.fascinator.api.impl.BasicPayload;
import au.edu.usq.fascinator.api.store.PayloadType;

public class JsonQDigitalObject extends BasicDigitalObject {

    public JsonQDigitalObject(String uri, Map<String, String> info) {
        setId(uri);
        BasicPayload payload = new BasicPayload();
        payload.setId("CONTENT");
        payload.setLabel(uri);
        payload.setPayloadType(PayloadType.External);
        try {
            payload.setInputStream(new FileInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        addPayload(payload);
    }

}
