/* 
 * Sun of Fedora - Solr Portal
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
package au.edu.usq.solr.portal.components;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tapestry.Asset;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.contrib.services.TemplateService;
import org.apache.tapestry.contrib.utils.URIResource;
import org.apache.tapestry.contrib.utils.VelocityMarker;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.runtime.Component;

import au.edu.usq.solr.portal.services.VelocityResourceLocator;

public class VelocityPanel {

    private Logger log = Logger.getLogger(VelocityPanel.class);

    @Parameter(value = "'default'")
    private String fallbackPath;

    @Parameter(value = "'default'")
    private String path;

    @Parameter(required = true, defaultPrefix = "literal")
    private String template;

    @Inject
    @Path("context:/")
    private Asset contextPath;

    @Inject
    private ComponentResources resources;

    @Inject
    @VelocityMarker
    private TemplateService service;

    @Inject
    private VelocityResourceLocator locator;

    @BeginRender
    public void render(MarkupWriter writer) {
        locator.setDefaultPath(path);
        Resource templateResource = new URIResource(path + '/' + template);
        Component page = resources.getPage();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("contextPath", contextPath);
        context.put("page", page);
        context.put("context", page.getComponentResources());
        context.put("locator", locator);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            service.mergeDataWithResource(templateResource, out, context);
        } catch (RuntimeException re) {
            log.warn("Render failed: " + re.getMessage() + ", trying fallback");
            templateResource = new URIResource(fallbackPath + '/' + template);
            service.mergeDataWithResource(templateResource, out, context);
        }
        writer.writeRaw(out.toString());
    }
}
