package au.edu.usq.solr.index.rule.impl;

import au.edu.usq.solr.harvest.fedora.DatastreamType;
import au.edu.usq.solr.index.rule.BaseDatastreamFilter;

public class MimeTypeDatastreamFilter extends BaseDatastreamFilter {

    private String mimeType;

    public MimeTypeDatastreamFilter(String mimeType) {
        super("MIMEType");
        this.mimeType = mimeType;
    }

    @Override
    public boolean isFullTextStream(DatastreamType datastream) {
        return datastream.getMimeType().equals(mimeType);
    }
}
