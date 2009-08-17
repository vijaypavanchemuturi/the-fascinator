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
package au.edu.usq.fascinator.portal.components;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.Request;

import au.edu.usq.fascinator.portal.State;
import au.edu.usq.fascinator.portal.services.UriResource;
import au.edu.usq.fascinator.portal.services.VelocityResourceLocator;
import au.edu.usq.fascinator.portal.services.VelocityService;

public class VelocityPanel {

    private Logger log = Logger.getLogger(VelocityPanel.class);

    @Parameter(value = "'default'")
    private String fallbackPath;

    @Parameter(value = "'default'")
    private String path;

    @Parameter(required = true, defaultPrefix = "literal")
    private String template;

    @SessionState
    private State state;

    // @Inject
    // @Path("context:/")
    // private Asset contextPath;

    private String requestContextPath;
    @Inject
    private ComponentResources resources;

    @Inject
    private VelocityService service;

    @Inject
    private Request request;

    @Inject
    private VelocityResourceLocator locator;

    public String render() {
        locator.setDefaultPath(path);
        Resource templateResource = new UriResource(path + '/' + template);
        Component page = resources.getPage();
        String pageName = resources.getPageName();
        Map<String, Object> context = new HashMap<String, Object>();
        requestContextPath = request.getContextPath();
        if (!requestContextPath.endsWith("/")) {
            requestContextPath += "/";
        }
        context.put("contextPath", requestContextPath);
        context.put("page", page);
        context.put("session", state);
        context.put("pageName", pageName);
        context.put("context", page.getComponentResources());
        context.put("locator", locator);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            service.mergeDataWithResource(templateResource, out, context);
        } catch (RuntimeException re) {
            log.warn("Render failed: " + re.getMessage() + ", trying fallback");
            templateResource = new UriResource(fallbackPath + '/' + template);
            service.mergeDataWithResource(templateResource, out, context);
        }
        return out.toString();
    }

    @BeginRender
    public void render(MarkupWriter writer) {
        writer.writeRaw(render());
    }

}
