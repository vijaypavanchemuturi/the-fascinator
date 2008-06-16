package au.edu.usq.solr.harvest.filter.impl;

import au.edu.usq.solr.harvest.fedora.types.DatastreamType;
import au.edu.usq.solr.harvest.filter.BaseDatastreamFilter;

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
