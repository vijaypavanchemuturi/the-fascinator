package au.edu.usq.solr.harvest.filter;

import au.edu.usq.solr.harvest.fedora.types.DatastreamType;

public abstract class BaseDatastreamFilter implements DatastreamFilter {

    private String name;

    public BaseDatastreamFilter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract boolean isFullTextStream(DatastreamType datastream);
}
