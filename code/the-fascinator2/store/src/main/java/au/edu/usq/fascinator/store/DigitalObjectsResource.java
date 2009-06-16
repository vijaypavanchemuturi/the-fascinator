package au.edu.usq.fascinator.store;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.DigitalObject;
import au.edu.usq.fascinator.api.Payload;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.Storage;
import au.edu.usq.fascinator.common.ConfigUtils;

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
    @Path("")
    public void handlePost() {

    }

}
