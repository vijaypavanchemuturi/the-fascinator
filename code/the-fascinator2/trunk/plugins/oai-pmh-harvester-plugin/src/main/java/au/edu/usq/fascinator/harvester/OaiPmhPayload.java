package au.edu.usq.fascinator.harvester;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kb.oai.pmh.Record;
import au.edu.usq.fascinator.api.storage.impl.BasicPayload;

public class OaiPmhPayload extends BasicPayload {

    private Logger log = LoggerFactory.getLogger(OaiPmhPayload.class);

    private Record record;

    public OaiPmhPayload(String id, Record record) {
        this.record = record;
        setId(id);
        setLabel(id);
    }

    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public InputStream getInputStream() {
        try {
            return IOUtils.toInputStream(record.getMetadataAsString());
        } catch (IOException ioe) {
            log.error(ioe.getMessage());
        }
        return null;
    }

}
