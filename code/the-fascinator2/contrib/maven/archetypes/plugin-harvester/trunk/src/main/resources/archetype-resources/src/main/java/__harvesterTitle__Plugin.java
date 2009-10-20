package ${package};
/* 
 * The Fascinator - Plugin - Harvester - ${harvesterName}
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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;

import au.edu.usq.fascinator.api.Configurable;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.common.JsonConfig;

public class ${harvesterTitle}Plugin implements Harvester, Configurable {
    /** configuration */
    private JsonConfig config;

    public String getId() {
        /*
         *  TODO: Give your plugin a unique ID
         */
        return "${artifactId}"; 
    }

    public String getName() {
        /*
         * TODO: Provide a one line description of what this object can harvest 
         */
        return Messages.getString("harvester-plugin-description");
    }

    public void init(File jsonFile) throws PluginException {
        try {
            config = new JsonConfig(jsonFile);
        } catch (IOException ioe) {
            throw new HarvesterException(ioe);
        }
        String urlList = config.get("${artifactId}.param", "");
    }

    public List<DigitalObject> getObjects() throws HarvesterException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasMoreObjects() {
        // TODO Auto-generated method stub
        return false;
    }

    public void shutdown() throws PluginException {
        // TODO Auto-generated method stub
        
    }

    public String getConfig() {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(getClass().getResourceAsStream(
                    "/" + getId() + "-config.html"), writer);
        } catch (IOException ioe) {
            writer.write("<span class=\"error\">" + ioe.getMessage()
                    + "</span>");
        }
        return writer.toString();
    }
    
    public static void main(String[] args) {
        ${harvesterTitle}Plugin h = new ${harvesterTitle}Plugin();
        try {
            h.init(new File(args[0]));
            System.out.println("Plugin ID: " + h.getId());
            System.out.println("Plugin Name: " + h.getName());
        } catch (PluginException e) {
            e.printStackTrace();
        }
    }

}
