package au.edu.usq.fascinator.storage.fedora;

import java.io.IOException;
import java.io.InputStream;

import au.edu.usq.fascinator.api.impl.BasicPayload;
import au.edu.usq.fedora.RestClient;

public class Fedora3Payload extends BasicPayload {

    private RestClient client;

    private String oid;

    public Fedora3Payload(RestClient client, String oid) {
        this.client = client;
        this.oid = oid;
    }

    public InputStream getInputStream() throws IOException {
        return client.getStream(oid, getId());
    }

}
