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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.storage.impl.GenericDigitalObject;
import au.edu.usq.fascinator.api.storage.impl.GenericPayload;

public class JsonQDigitalObject extends GenericDigitalObject {

    private Logger log = LoggerFactory.getLogger(JsonQDigitalObject.class);

    public JsonQDigitalObject(String uri, Map<String, String> info) {
        super(uri);
        try {
            URL url = new URL(URLDecoder.decode(uri, "UTF-8"));
            setId(url.toString());
            GenericPayload payload = new GenericPayload();
            payload.setId("CONTENT");
            payload.setLabel(uri);
            payload.setPayloadType(PayloadType.External);
            payload.setInputStream(url.openStream());
            addPayload(payload);
        } catch (UnsupportedEncodingException uee) {
            log.warn("Unsupported encoding: {}", uee);
        } catch (MalformedURLException e) {
            log.error("Malformed URL: {}", uri);
        } catch (IOException ioe) {
            log.error("Failed to get input stream: {}", ioe);
        }
    }

}
