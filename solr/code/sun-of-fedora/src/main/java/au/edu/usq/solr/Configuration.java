package au.edu.usq.solr;

import java.util.List;

import au.edu.usq.solr.portal.Portal;

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

    public void setRegistryBaseUrl(String registryBaseUrl);

    public String getRegistryBaseUrl();

    public void setRegistryUser(String registryUser);

    public String getRegistryUser();

    public List<Portal> getPortals();

    public void setPortals(List<Portal> portals);
}
