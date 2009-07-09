package au.edu.usq.fascinator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.ontoware.rdf2go.model.Syntax;
import org.semanticdesktop.aperture.rdf.RDFContainer;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;

public class RdfDigitalObject implements DigitalObject {

    private DigitalObject object;

    private RDFContainer rdf;

    public RdfDigitalObject(DigitalObject object, RDFContainer rdf) {
        this.object = object;
        this.rdf = rdf;
    }

    @Override
    public String getId() {
        return object.getId();
    }

    @Override
    public Payload getMetadata() {
        return new Payload() {
            @Override
            public String getContentType() {
                return "application/xml+rdf";
            }

            @Override
            public String getId() {
                return "rdf";
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(rdf.getModel().serialize(
                        Syntax.RdfXml).getBytes());
            }

            @Override
            public String getLabel() {
                return "RDF metadata";
            }

            @Override
            public PayloadType getType() {
                return PayloadType.Data;
            }
        };
    }

    @Override
    public Payload getPayload(String pid) {
        return object.getPayload(pid);
    }

    @Override
    public List<Payload> getPayloadList() {
        return object.getPayloadList();
    }

}
