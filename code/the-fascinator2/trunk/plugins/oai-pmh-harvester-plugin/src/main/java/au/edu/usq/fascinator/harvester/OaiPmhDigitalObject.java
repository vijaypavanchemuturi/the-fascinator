package au.edu.usq.fascinator.harvester;

import se.kb.oai.pmh.Record;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.impl.BasicDigitalObject;

public class OaiPmhDigitalObject extends BasicDigitalObject {

    private Record record;

    private OaiPmhPayload metadata;

    public OaiPmhDigitalObject(Record record, String metaId) {
        super(record.getHeader().getIdentifier(), metaId);
        this.record = record;
        metadata = new OaiPmhPayload(metaId, record);
        addPayload(metadata);
    }

    public Record getRecord() {
        return record;
    }

    @Override
    public Payload getPayload(String pid) {
        if (metadata.getId().equals(pid)) {
            return metadata;
        }
        return super.getPayload(pid);
    }

}
