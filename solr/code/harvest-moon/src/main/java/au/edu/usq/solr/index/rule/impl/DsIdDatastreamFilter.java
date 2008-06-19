package au.edu.usq.solr.index.rule.impl;

import au.edu.usq.solr.harvest.fedora.DatastreamType;
import au.edu.usq.solr.index.rule.BaseDatastreamFilter;

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
