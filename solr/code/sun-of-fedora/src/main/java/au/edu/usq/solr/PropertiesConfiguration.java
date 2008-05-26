package au.edu.usq.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesConfiguration implements Configuration {

    private static final String DEFAULT_RESOURCE = "/config.properties";

    private static final String SOLR_BASE_URL_KEY = "solr.base.url";

    private static final String RECORDS_PER_PAGE_KEY = "records.per.page";

    private static final String FACET_FIELDS_KEY = "facet.fields";

    private static final String SOLR_BASE_URL = "http://localhost:8983/solr";

    private static final int RECORDS_PER_PAGE = 10;

    private static final String FACET_FIELDS = "repository_name,dc_subject,dc_creator";

    private static final String FACET_COUNT_KEY = "facet.count";

    private static final int FACET_COUNT = 25;

    private static final String REGISTRY_BASE_URL_KEY = "registry.base.url";

    private static final String REGISTRY_USER_KEY = "registry.user";

    private static final String REGISTRY_PASSWORD_KEY = "registry.password";

    private Logger log = Logger.getLogger(PropertiesConfiguration.class);

    private Properties props;

    public PropertiesConfiguration() {
        loadDefaults();
    }

    private void loadDefaults() {
        props = new Properties();
        try {
            props.load(getClass().getResourceAsStream(DEFAULT_RESOURCE));
        } catch (IOException e) {
            log.warn("Failed to load defaults", e);

            setSolrBaseUrl(SOLR_BASE_URL);
            setRecordsPerPage(RECORDS_PER_PAGE);
            setFacetFields(FACET_FIELDS);
            setFacetCount(FACET_COUNT);
        }
    }

    public void setSolrBaseUrl(String solrBaseUrl) {
        setProperty(SOLR_BASE_URL_KEY, solrBaseUrl);
    }

    public String getSolrBaseUrl() {
        return getProperty(SOLR_BASE_URL_KEY);
    }

    public void setRecordsPerPage(int recordsPerPage) {
        setIntProperty(RECORDS_PER_PAGE_KEY, recordsPerPage);
    }

    public int getRecordsPerPage() {
        return getIntProperty(RECORDS_PER_PAGE_KEY);
    }

    public void setFacetFields(String facetFields) {
        setProperty(FACET_FIELDS_KEY, facetFields);
    }

    public String getFacetFields() {
        return getProperty(FACET_FIELDS_KEY);
    }

    public List<String> getFacetFieldList() {
        List<String> facetFields = new ArrayList<String>();
        String[] tokens = getFacetFields().split(",");
        for (String token : tokens) {
            facetFields.add(token);
        }
        return facetFields;
    }

    public void setFacetCount(int facetCount) {
        setIntProperty(FACET_COUNT_KEY, facetCount);
    }

    public int getFacetCount() {
        return getIntProperty(FACET_COUNT_KEY);
    }

    public void setRegistryBaseUrl(String registryBaseUrl) {
        setProperty(REGISTRY_BASE_URL_KEY, registryBaseUrl);
    }

    public String getRegistryBaseUrl() {
        return getProperty(REGISTRY_BASE_URL_KEY);
    }

    public void setRegistryUser(String registryUser) {
        setProperty(REGISTRY_USER_KEY, registryUser);
    }

    public String getRegistryUser() {
        return getProperty(REGISTRY_USER_KEY);
    }

    private void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    private String getProperty(String key) {
        return props.getProperty(key);
    }

    private void setIntProperty(String key, int value) {
        setProperty(key, Integer.toString(value));
    }

    private int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }
}
