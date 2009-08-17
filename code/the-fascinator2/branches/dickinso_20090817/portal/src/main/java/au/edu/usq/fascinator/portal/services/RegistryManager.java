package au.edu.usq.fascinator.portal.services;

import java.io.File;
import java.util.List;

import org.dom4j.Document;

import au.edu.usq.fascinator.api.storage.Payload;

public interface RegistryManager {
    // uuid used for fedora....
    // storageid used for fs storage
    public Document getXmlDatastream(String id); // uuid, String dsId);

    public Payload getPayload(String Id, String payloadName);

    public List<Payload> getPayloadList(String id);

    public Object getMetadata(String id, String metaType);

    public void getClient(File jsonFile);
}
