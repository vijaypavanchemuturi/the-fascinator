package au.edu.usq.solr;

import java.util.List;

public interface Configuration {

    public void setSolrBaseUrl(String solrBaseUrl);

    public String getSolrBaseUrl();

    public void setRecordsPerPage(int recordsPerPage);

    public int getRecordsPerPage();

    public void setFacetFields(String facetFields);

    public String getFacetFields();

    public List<String> getFacetFieldList();

    public void setFacetCount(int facetCount);

    public int getFacetCount();
}
