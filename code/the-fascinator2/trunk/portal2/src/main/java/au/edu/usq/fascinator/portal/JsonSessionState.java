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
package au.edu.usq.fascinator.portal;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.JsonConfig;

public class JsonSessionState extends HashMap<String, Object> {

    private Logger log = LoggerFactory.getLogger(JsonSessionState.class);

    private JsonConfig config;

    private Date created;

    private Map<String, Object> objects;

    public JsonSessionState() {
        created = new Date();
        try {
            config = new JsonConfig();
        } catch (IOException ioe) {
            log.warn("Failed to load system config: {}", ioe.getMessage());
        }
        objects = new HashMap<String, Object>();
    }

    public JsonConfig getSystemConfig() {
        return config;
    }

    public Date getCreated() {
        return created;
    }

    public Object get(String name) {
        return get(name, null);
    }

    public Object get(String name, Object defaultValue) {
        Object value = objects.get(name);
        return value == null ? defaultValue : value;
    }

    public void set(String name, Object object) {
        objects.put(name, object);
    }
}
