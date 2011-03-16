/* 
 * The Fascinator - Portal
 * Copyright (C) 2011 University of Southern Queensland
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
package au.edu.usq.fascinator.portal.services.impl;

import au.edu.usq.fascinator.portal.services.DynamicPageCache;
import au.edu.usq.fascinator.portal.services.PortalManager;
import au.edu.usq.fascinator.portal.services.VelocityService;

import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VelocityServiceImpl implements VelocityService {

    private Logger log = LoggerFactory.getLogger(VelocityServiceImpl.class);

    private String portalPath;

    private String defaultPortal;

    private String defaultDisplay;

    private List<String> skinPriority;

    private DynamicPageCache pageCache;

    public VelocityServiceImpl(PortalManager portalManager,
            DynamicPageCache pageCache) {
        try {
            this.pageCache = pageCache;
            // Default templates
            defaultPortal = portalManager.getDefaultPortal();
            defaultDisplay = portalManager.getDefaultDisplay();
            skinPriority = portalManager.getSkinPriority();
            // setup Velocity template engine
            File homePath = portalManager.getHomeDir();
            portalPath = homePath.getAbsolutePath();
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/velocity.properties"));
            props.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, portalPath);
            Velocity.init(props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getResource(String resourcePath) {
        try {
            return RuntimeSingleton.getContent(resourcePath).getResourceLoader().getResourceStream(resourcePath);
        } catch (Exception e) {
            log.error("Failed to get resource: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public InputStream getResource(String portalId, String resourceName) {
        return getResource(resourceExists(portalId, resourceName));
    }

    @Override
    public String resourceExists(String portalId, String resourceName) {
        String index = portalId + "/" + resourceName;
        // Check the cache first
        String path = pageCache.getPath(index);
        if (path != null) {
            return path;
        }
        // Look through the skins of the specified portal
        path = getSkinPath(portalId, resourceName);
        if (path != null) {
            pageCache.putPath(index, path);
            return path;
        }
        // Check if it's a display skin
        Pattern p = Pattern.compile("^(?:(.*)/)?display/(?:([a-zA-Z][^/]*))?/(.*)$");
        Matcher m = p.matcher(resourceName);
        if (m.matches()) {
            String displayType = m.group(2);
            if (!defaultDisplay.equals(displayType)) {
                String relPath = m.group(3);
                String fallbackResourceName = "display/" + defaultDisplay + "/" + relPath;
                if (m.group(1) != null) {
                    fallbackResourceName = "scripts/" + fallbackResourceName;
                }
                path = getSkinPath(portalId, fallbackResourceName);
                if (path != null) {
                    pageCache.putPath(index, path);
                    return path;
                }
            }
        }
        // Check if we can fall back to default portal
        if (!defaultPortal.equals(portalId)) {
            return resourceExists(defaultPortal, resourceName);
        }
        return null;
    }

    @Override
    public void renderTemplate(String portalId, String templateName,
            Context context, Writer writer) throws Exception {
        String path = resourceExists(portalId, templateName + ".vm");
        Template template = Velocity.getTemplate(path);
        template.merge(context, writer);
    }

    private String getSkinPath(String portalId, String resourceName) {
        String path = null;
        // Loop through our skins
        for (String skin : skinPriority) {
            path = portalId + "/" + skin + "/" + resourceName;
            // Check raw resource
            if (Velocity.resourceExists(path)) {
                // But make sure it's not a directory, resourceExists()
                // will return directories as valid resources.
                File file = new File(portalPath, path);
                if (!file.isDirectory()) {
                    return path;
                }
            }
            // Look for templates and scripts if it had no extension
            if ("".equals(FilenameUtils.getExtension(resourceName))) {
                path = path + ".vm";
                if (Velocity.resourceExists(path)) {
                    return path;
                }
                path = portalId + "/" + skin + "/scripts/" + resourceName + ".py";
                if (Velocity.resourceExists(path)) {
                    return path;
                }
            }
        }
        // We didn't find it
        return null;
    }
}
