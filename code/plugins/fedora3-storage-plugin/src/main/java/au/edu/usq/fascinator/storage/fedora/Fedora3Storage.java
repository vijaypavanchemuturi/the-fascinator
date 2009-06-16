package au.edu.usq.fascinator.storage.fedora;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.DigitalObject;
import au.edu.usq.fascinator.api.Payload;
import au.edu.usq.fascinator.api.Storage;
import au.edu.usq.fascinator.api.StorageException;
import au.edu.usq.fedora.RestClient;
import au.edu.usq.fedora.types.ListSessionType;
import au.edu.usq.fedora.types.ObjectFieldType;
import au.edu.usq.fedora.types.ResultType;

public class Fedora3Storage implements Storage {

    private static final String DEFAULT_URL = "http://localhost:8080/fedora";

    private static final String DEFAULT_NAMESPACE = "uuid";

    private Logger log = LoggerFactory.getLogger(Fedora3Storage.class);

    private RestClient client;

    public String getId() {
        return "fedora3";
    }

    public String getName() {
        return "Fedora Commons 3.x Storage Module";
    }

    public void init(File jsonFile) throws StorageException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(jsonFile, JsonNode.class);
            JsonNode storageNode = rootNode.get("storage");
            if (storageNode != null) {
                String type = storageNode.get("type").getTextValue();
                if (getId().equals(type)) {
                    JsonNode configNode = storageNode.get("config");
                    String url = configNode.get("uri").getTextValue();
                    client = new RestClient(url);
                    JsonNode usernameNode = configNode.get("username");
                    JsonNode passwordNode = configNode.get("password");
                    if (usernameNode != null && passwordNode != null) {
                        client.authenticate(usernameNode.getTextValue(),
                                passwordNode.getTextValue());
                    }
                } else {
                    throw new StorageException("Not Fedora 3");
                }
            } else {
                log.info("No configuration defined, using defaults");
                client = new RestClient(DEFAULT_URL);
            }
        } catch (JsonParseException jpe) {
            throw new StorageException(jpe);
        } catch (IOException ioe) {
            throw new StorageException(ioe);
        }
    }

    public void shutdown() throws StorageException {
        // Don't need to do anything
    }

    public void addObject(DigitalObject object) throws StorageException {
        try {
            String fedoraId = getFedoraId(object.getId());
            if (fedoraId == null) {
                log.debug("Creating object {}", fedoraId);
                client.createObject(object.getId(), DEFAULT_NAMESPACE);
            } else {
                log.debug("Updating object {}", fedoraId);
            }
            for (Payload payload : object.getPayloadList()) {
                addPayload(object.getId(), payload);
            }
        } catch (Exception e) {
            throw new StorageException("Failed to add object", e);
        }
    }

    public void removeObject(String oid) {
        try {
            client.purgeObject(getFedoraId(oid));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void addPayload(String oid, Payload payload) {
        try {
            String fedoraId = getFedoraId(oid);
            String dsId = payload.getId();
            String dsLabel = payload.getLabel();
            String type = payload.getContentType();
            File tmpFile = File.createTempFile("f3_", ".tmp");
            FileOutputStream fos = new FileOutputStream(tmpFile);
            IOUtils.copy(payload.getInputStream(), fos);
            fos.close();
            client.addDatastream(fedoraId, dsId, dsLabel, type, tmpFile);
            tmpFile.delete();
            // TODO managed content
        } catch (IOException ioe) {
            log.debug("Failed to add " + payload + " to item " + oid, ioe);
        }
    }

    public void removePayload(String oid, String pid) {
        // TODO
    }

    public DigitalObject getObject(String oid) {
        try {
            String fedoraId = getFedoraId(oid);
            if (fedoraId != null) {
                return new Fedora3DigitalObject(client, fedoraId);
            }
        } catch (IOException ioe) {
        }
        return null;
    }

    public Payload getPayload(String oid, String pid) {
        return null;
    }

    private String getFedoraId(String oid) throws IOException {
        // TODO cache oid lookups?
        String pid = null;
        ResultType result = client.findObjects(oid, 1);
        List<ObjectFieldType> objects = result.getObjectFields();
        if (!objects.isEmpty()) {
            pid = objects.get(0).getPid();
        }
        // Note: Fedora resumeFindObjects has to be called until the search
        // session is completed or the server will hang after approximately 100
        // requests
        ListSessionType session = result.getListSession();
        while (session != null) {
            log.debug("resumeFindObjects session to close connection..");
            result = client.resumeFindObjects(session.getToken());
            session = result.getListSession();
        }
        log.debug("findObjects done.");
        return pid;
    }
}
