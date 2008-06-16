package au.edu.usq.solr.harvest.filter.impl;

import au.edu.usq.solr.harvest.fedora.types.DatastreamType;
import au.edu.usq.solr.harvest.filter.BaseDatastreamFilter;

public class DsIdDatastreamFilter extends BaseDatastreamFilter {

    private String dsId;

    public DsIdDatastreamFilter(String dsId) {
        super("DSID");
        this.dsId = dsId;
    }

    @Override
    public boolean isFullTextStream(DatastreamType datastream) {
        return datastream.getDsid().equals(dsId);
    }
}
