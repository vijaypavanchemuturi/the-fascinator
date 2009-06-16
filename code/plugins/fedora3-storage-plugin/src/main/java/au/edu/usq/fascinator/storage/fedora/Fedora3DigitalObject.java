package au.edu.usq.fascinator.storage.fedora;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.Payload;
import au.edu.usq.fascinator.api.impl.BasicDigitalObject;
import au.edu.usq.fedora.RestClient;
import au.edu.usq.fedora.types.DatastreamType;
import au.edu.usq.fedora.types.ObjectDatastreamsType;

public class Fedora3DigitalObject extends BasicDigitalObject {

    private Logger log = LoggerFactory.getLogger(Fedora3DigitalObject.class);

    private RestClient client;

    public Fedora3DigitalObject(RestClient client, String oid) {
        super(oid);
        this.client = client;
    }

    public List<Payload> getPayloadList() {
        List<Payload> dsList = new ArrayList<Payload>();
        try {
            ObjectDatastreamsType odt = client.listDatastreams(getId());
            log.debug("Found {} datastreams for {}", odt.getDatastreams()
                    .size(), getId());
            for (DatastreamType dst : odt.getDatastreams()) {
                Fedora3Payload ds = new Fedora3Payload(client, getId());
                ds.setId(dst.getDsid());
                ds.setLabel(dst.getLabel());
                ds.setContentType(dst.getMimeType());
                dsList.add(ds);
            }
        } catch (IOException ioe) {
            log.error("Failed to list datastreams for [{}]", getId());
        }
        return dsList;
    }

}
