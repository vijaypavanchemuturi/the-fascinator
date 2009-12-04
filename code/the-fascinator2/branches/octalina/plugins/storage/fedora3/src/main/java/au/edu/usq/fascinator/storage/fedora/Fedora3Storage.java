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
package au.edu.usq.fascinator.storage.fedora;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.JsonConfig;
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
            JsonConfig config = new JsonConfig(jsonFile);
            String url = config.get("storage/fedora3/url");

            client = new RestClient(url);
            String userName = config.get("storage/fedora3/username");
            String password = config.get("storage/fedora3/password");

            if (userName != null && password != null) {
                client.authenticate(userName, password);
            } else {
                new StorageException("Not Fedora 3");
            }
        } catch (IOException ioe) {
            throw new StorageException(ioe);
        }
    }

    public void shutdown() throws StorageException {
        // Don't need to do anything
    }

    public String addObject(DigitalObject object) throws StorageException {
        String fedoraId = null;
        String oid = object.getId();
        try {
            fedoraId = getFedoraId(oid);
            if (fedoraId == null) {
                log.debug("Creating object {}", fedoraId);
                // fedoraId = client.createObject(oid, DEFAULT_NAMESPACE);
                fedoraId = createObject(oid);
                log.debug("Client returned Fedora PID: {}", fedoraId);
            } else {
                log.debug("Updating object {}", fedoraId);
            }
            for (Payload payload : object.getPayloadList()) {
                addPayload(oid, payload);
            }
        } catch (Exception e) {
            throw new StorageException("Failed to add object", e);
        }
        return fedoraId;
    }

    public void removeObject(String oid) {
        try {
            client.purgeObject(getFedoraId(oid));
        } catch (IOException ioe) {
            log.error("Failed to remove object {}", ioe);
        }
    }

    private String createObject(String oid) {
        try {
            return client.createObject(oid, DEFAULT_NAMESPACE);
        } catch (IOException e) {
            log.debug("Failed to creatObject: " + oid, e);
        }
        return null;
    }

    public void addPayload(String oid, Payload payload) {
        try {
            String fedoraId = getFedoraId(oid);
            if (fedoraId == null) {
                fedoraId = createObject(oid);
            }
            String dsId = payload.getId();
            String dsLabel = payload.getLabel();
            String type = payload.getContentType();
            String payloadType = payload.getType().toString();
            File tmpFile = File.createTempFile("f3_", ".tmp");
            FileOutputStream fos = new FileOutputStream(tmpFile);
            IOUtils.copy(payload.getInputStream(), fos);
            fos.close();

            // Fedora does not like the id to have slash
            // The payload type and the original id is stored in AltIDs in
            // payloadType:dsId format
            client.addDatastream(fedoraId, dsId.replace('/', '_'), dsLabel,
                    type, payloadType + ":" + dsId, tmpFile);
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
                log.debug("Successfully getting object: " + oid
                        + ", with fedoraid: " + fedoraId);
                return new Fedora3DigitalObject(client, fedoraId, oid);
            }
        } catch (IOException ioe) {
            log.debug("Failed to getObject: " + oid);
        }
        return null;
    }

    public Payload getPayload(String oid, String pid) {
        DigitalObject object = getObject(oid);
        if (object != null) {
            return object.getPayload(pid);
        }
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
            log.debug("resumeFindObjects session to close connection...");
            result = client.resumeFindObjects(session.getToken());
            if (result != null) {
                session = result.getListSession();
            } else {
                session = null;
            }
        }
        return pid;
    }

    @Override
    public void init(String jsonString) throws PluginException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<DigitalObject> getObjectList() {
        // TODO Auto-generated method stub
        return null;
    }
}
