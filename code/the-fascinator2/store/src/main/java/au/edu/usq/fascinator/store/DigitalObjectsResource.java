/* 
 * The Fascinator - Fedora Commons 3.x storage plugin
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
package au.edu.usq.fascinator.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipFile;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.DigitalObject;
import au.edu.usq.fascinator.api.Payload;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.Storage;
import au.edu.usq.fascinator.api.StorageException;
import au.edu.usq.fascinator.common.ConfigUtils;

/**
 * 
 * @author Oliver Lucido
 */
@Path("/objects")
public class DigitalObjectsResource {

    private Logger log = LoggerFactory.getLogger(DigitalObjectsResource.class);

    private Storage storage;

    public DigitalObjectsResource() {
        try {
            JsonNode node = ConfigUtils.getSystemConfig();
            storage = PluginManager.getStorage(ConfigUtils.getTextValue(node
                    .get("storage"), "type"));
            log.info("Initialising storage plugin: {}...", storage.getName());
            storage.init(ConfigUtils.getSystemConfigFile());
        } catch (PluginException pe) {
            log.error("Failed to initialise storage", pe);
        } catch (IOException ioe) {
            log.error("Failed to read configuration", ioe);
        }
    }

    @GET
    @Path("{objectId}")
    public Response getObject(@PathParam("objectId") String objectId) {
        log.debug("GET object: {}", objectId);
        DigitalObject object = storage.getObject(objectId);
        ResponseBuilder response = Response.ok("[object " + object.getId()
                + " : <rdf goes here>]", "text/plain");
        // TODO need an object page
        return response.build();
    }

    @GET
    @Path("{objectId}/{payloadId}")
    public Response getPayload(@PathParam("objectId") String objectId,
            @PathParam("payloadId") String payloadId) {
        log.debug("GET payload: {}#{}", objectId, payloadId);
        DigitalObject object = storage.getObject(objectId);
        Payload payload = null;
        ResponseBuilder response = Response.noContent();
        if (!"".equals(payloadId)) {
            payload = object.getPayload(payloadId);
            if (payload == null) {
                response = Response.status(Status.NOT_FOUND);
            } else {
                try {
                    response = Response.ok(payload.getInputStream(), payload
                            .getContentType());
                } catch (IOException ioe) {
                    log.error("Failed to get payload", ioe);
                    response = Response.serverError();
                }
            }
        }
        return response.build();
    }

    @POST
    public Response handlePost(@Context HttpHeaders headers,
            @Context UriInfo uriInfo, InputStream data) {
        try {
            log.info("uri info: {}", uriInfo.getRequestUri());
            MultivaluedMap<String, String> mm = headers.getRequestHeaders();
            for (String k : mm.keySet()) {
                log.info("{}={}", k, mm.get(k));
            }
            File tmpFile = File.createTempFile("_store_", ".zip");
            OutputStream tmpOut = new FileOutputStream(tmpFile);
            IOUtils.copy(data, tmpOut);
            tmpOut.close();
            ZipFile zipFile = new ZipFile(tmpFile);
            DigitalObject object = new ZipDigitalObject(zipFile);
            log.info("DigitalObject id: {}", object.getId());
            String storeId = storage.addObject(object);
            log.info("storeId: ", storeId);
            URI location = new URI("");
            zipFile.close();
            tmpFile.delete();
        } catch (IOException ioe) {
            log.error("Failed to read zip content", ioe);
            return Response.noContent().build();
        } catch (StorageException se) {
            se.printStackTrace();
        } catch (URISyntaxException urise) {
            urise.printStackTrace();
        }
        return null;// Response.created(location).build();
    }

    @DELETE
    @Path("{objectId}")
    public Response handleDelete(@PathParam("objectId") String objectId) {
        storage.removeObject(objectId);
        return Response.ok().build();
    }

}
