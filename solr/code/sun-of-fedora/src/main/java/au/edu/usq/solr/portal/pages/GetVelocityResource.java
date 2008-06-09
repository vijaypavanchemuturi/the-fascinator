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
package au.edu.usq.solr.portal.pages;

import javax.activation.MimetypesFileTypeMap;

import org.apache.tapestry.StreamResponse;
import org.apache.tapestry.contrib.services.TemplateService;
import org.apache.tapestry.contrib.utils.VelocityMarker;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.util.TextStreamResponse;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.resource.ContentResource;

import au.edu.usq.solr.util.BinaryStreamResponse;

/**
 * Gets resources using Velocity resource loaders.
 * 
 * @author Oliver Lucido
 */
public class GetVelocityResource {

    @Inject
    @VelocityMarker
    private TemplateService service;

    Object onActivate(Object[] params) {
        try {
            // just to make sure velocity initialised properly
            service.mergeDataWithResource(null, null, null);
        } catch (Exception e) {
        }
        if (params.length > 0) {
            String path = "";
            for (Object part : params) {
                path += "/" + part;
            }
            try {
                ContentResource resource = RuntimeSingleton.getContent(path);
                MimetypesFileTypeMap typeMap = new MimetypesFileTypeMap();
                String mimeType = typeMap.getContentType(path);
                StreamResponse response = null;
                if (mimeType.startsWith("text/")) {
                    response = new TextStreamResponse(mimeType,
                        resource.getData().toString());
                } else {
                    response = new BinaryStreamResponse(mimeType,
                        resource.getResourceLoader().getResourceStream(path));
                }
                return response;
            } catch (Exception e) {
                // ignore exceptions
            }
        }
        return new TextStreamResponse("text/plain", "");
    }
}
