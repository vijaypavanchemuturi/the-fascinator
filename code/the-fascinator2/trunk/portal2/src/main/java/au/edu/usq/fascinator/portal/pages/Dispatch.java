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
package au.edu.usq.fascinator.portal.pages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import au.edu.usq.fascinator.common.MimeTypeUtil;
import au.edu.usq.fascinator.portal.JsonSessionState;
import au.edu.usq.fascinator.portal.services.DynamicPageService;
import au.edu.usq.fascinator.portal.services.GenericStreamResponse;

public class Dispatch {

    private static final String DEFAULT_PORTAL_ID = "default";

    private static final String DEFAULT_RESOURCE = "home";

    @Inject
    private Logger log;

    @SessionState
    private JsonSessionState sessionState;

    @Inject
    private DynamicPageService pageService;

    @Inject
    private Request request;

    @Persist
    private String lastResourceName;

    @Persist
    private Map<String, String[]> formData;

    public StreamResponse onActivate(Object... path) {
        String portalId = sessionState.get("portalId", DEFAULT_PORTAL_ID);
        String resourceName = DEFAULT_RESOURCE;

        if (path.length > 1) {
            portalId = path[0].toString();
            resourceName = StringUtils.join(path, "/", 1, path.length);
        }

        sessionState.set("portalId", portalId);
        log.debug("portalId = {}, resourceName = {}", portalId, resourceName);

        // save form data for POST requests
        if (formData == null) {
            formData = new HashMap<String, String[]>();
        }
        if (!resourceName.equals(lastResourceName)) {
            formData.clear();
        }
        if ("POST".equals(request.getMethod())) {
            for (String key : request.getParameterNames()) {
                formData.put(key, request.getParameters(key));
            }
        }

        String mimeType;
        InputStream stream;

        if (resourceName.indexOf(".") == -1) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pageService.render(portalId, resourceName, out, formData);
            mimeType = "text/html";
            stream = new ByteArrayInputStream(out.toByteArray());
        } else {
            mimeType = MimeTypeUtil.getMimeType(resourceName);
            stream = pageService.getResource(portalId, resourceName);
        }

        lastResourceName = resourceName;

        return new GenericStreamResponse(mimeType, stream);
    }
}
