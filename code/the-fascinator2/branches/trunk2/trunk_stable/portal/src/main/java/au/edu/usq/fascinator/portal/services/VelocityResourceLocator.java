/* 
 * The Fascinator - Solr Portal
 * Copyright (C) 2008  University of Southern Queensland
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
package au.edu.usq.fascinator.portal.services;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.services.AssetSource;
import org.apache.velocity.runtime.RuntimeSingleton;

public class VelocityResourceLocator {

    private VelocityService templateService;

    private String defaultPath;

    private String fallbackPath;

    private AssetSource assetSource;

    public VelocityResourceLocator(VelocityService templateService,
        AssetSource assetSource, String defaultPath) {
        this(templateService, assetSource, defaultPath, defaultPath);
    }

    public VelocityResourceLocator(VelocityService templateService,
        AssetSource assetSource, String defaultPath, String fallbackPath) {
        this.templateService = templateService;
        this.assetSource = assetSource;
        this.defaultPath = defaultPath;
        this.fallbackPath = fallbackPath;
    }

    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    public String getLocation(String name) {
        return getLocation(defaultPath, name);
    }

    public String getLocation(String path, String name) {
        String location = path + "/" + name;
        try {
            // just to make sure velocity initialised properly
            templateService.mergeDataWithResource(null, null, null);
        } catch (Exception e) {
        }
        try {
            RuntimeSingleton.getContent(location);
        } catch (Exception e) {
            location = fallbackPath + "/" + name;
        }
        if (!name.endsWith(".vm")) {
            Asset asset = assetSource.getClasspathAsset("velocity:/" + location);
            location = asset.toClientURL();
        }
        return location;
    }
}
