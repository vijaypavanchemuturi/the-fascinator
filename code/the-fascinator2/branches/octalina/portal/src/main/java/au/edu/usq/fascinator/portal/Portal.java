package au.edu.usq.fascinator.portal;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.JsonConfigHelper;

public class Portal {
    private JsonConfigHelper jsonConfig;
    private static Logger log = LoggerFactory.getLogger(Portal.class);

    /**
     * <p>
     * Portal Configuration options:
     * </p>
     * <ul>
     * <li>name</li>
     * <li>description</li>
     * <li>query</li>
     * <li>records-per-page</li>
     * <li>facet-count</li>
     * <li>facet-sort-by-count</li>
     * <li>backup-email</li>
     * <li>backup-paths</li>
     * 
     * </ul>
     * private String clusterFacet;
     * 
     * private String clusterFacetLabel;
     * 
     * private String clusterFacetData;
     * 
     * private String itemClass;
     * 
     * private String network;
     * 
     * private String netmask;
     **/
    /**
     * Portal Constructor
     * 
     * @throws IOException
     */
    public Portal(File portalConfig) throws IOException {
        jsonConfig = new JsonConfigHelper(portalConfig);
    }

    /**
     * Return portal name
     * 
     * @return name
     */
    public String getName() {
        return jsonConfig.get("portal/name", "");
    }

    /**
     * Set portal name
     * 
     * @param name
     */
    public void setName(String name) {
        jsonConfig.set("name", name.replace(' ', '_'));
    }

    /**
     * Return portal description
     * 
     * @return description
     */
    public String getDescription() {
        return jsonConfig.get("portal/description", "");
    }

    /**
     * Set portal description
     * 
     * @param description
     */
    public void setDescription(String description) {
        jsonConfig.set("portal/description", description);
    }

    /**
     * Return portal query
     * 
     * @return query
     */
    public String getQuery() {
        return jsonConfig.get("portal/query", "");
    }

    /**
     * Set portal query
     * 
     * @param query
     */
    public void setQuery(String query) {
        jsonConfig.set("portal/query", query);
    }

    /**
     * Return records per page
     * 
     * @return records-per-page
     */
    public int getRecordsPerPage() {
        return Integer
                .parseInt(jsonConfig.get("portal/records-per-page", "10"));
    }

    /**
     * Set records per page
     * 
     * @param recordsPerPage
     */
    public void setRecordsPerPage(int recordsPerPage) {
        jsonConfig.set("portal/records-per-page", Integer
                .toString(recordsPerPage));
    }

    /**
     * Return facet-count
     * 
     * @return facet-count
     */
    public int getFacetCount() {
        return Integer.parseInt(jsonConfig.get("portal/facet-count", "25"));
    }

    /**
     * Set facet count
     * 
     * @param facetCount
     */
    public void setFacetCount(int facetCount) {
        jsonConfig.set("portal/facet-count", Integer.toString(facetCount));
    }

    /**
     * Return facet sort by count
     * 
     * @return facet-sort-by-count
     */
    public boolean getFacetSort() {
        return Boolean.parseBoolean(jsonConfig.get(
                "portal/facet-sort-by-count", "false"));
    }

    /**
     * Set facet sort by count
     * 
     * @param facetSort
     */
    public void setFacetSort(boolean facetSort) {
        jsonConfig.set("portal/facet-sort-by-count", Boolean
                .toString(facetSort));
    }

    /**
     * Return map of facet-fields
     * 
     * @return facet-fields
     */
    public Map<String, Object> getFacetFields() {
        return jsonConfig.getMap("portal/facet-fields");
    }

    /**
     * Set facet name and value
     * 
     * @param name
     * @param value
     */
    public void setFacetFields(String name, String value) {
        String key = "portal/facet-fields/" + name;
        jsonConfig.set(key, value);
    }

    /**
     * Return List of facet fields
     * 
     * @return facet fields
     */
    public List<String> getFacetFieldList() {
        return new ArrayList<String>(getFacetFields().keySet());
    }

    /**
     * Email setting for backup to determine userspace in backup server
     * 
     * @param email
     */
    public void setEmail(String email) {
        jsonConfig.set("portal/backup-email", email);
    }

    /**
     * Return email for userspace
     * 
     * @return email
     */
    public String getEmail() {
        return jsonConfig.get("portal/backup-email", "");
    }

    /**
     * Return map of facet-fields
     * 
     * @return facet-fields
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Object>> getBackupPaths() {
        Map<String, Object> backupPaths = jsonConfig
                .getMap("portal/backup-paths");
        Map<String, Map<String, Object>> backupPathsDict = new HashMap<String, Map<String, Object>>();
        for (String key : backupPaths.keySet()) {
            Map<String, Object> dict = new HashMap<String, Object>();
            dict = (Map<String, Object>) backupPaths.get(key);
            backupPathsDict.put(key.toString(),
                    (Map<String, Object>) backupPaths.get(key));
        }
        return backupPathsDict;
    }

    /**
     * Set facet name and value
     * 
     * @param name
     * @param value
     */
    public void setBackupPaths(String path, String name, String value) {
        String key = "portal/backup-paths/" + path + "/";
        jsonConfig.set(key, value);
    }

    /**
     * Return List of facet fields
     * 
     * @return facet fields
     */
    public List<String> getBackupPathsList() {
        return new ArrayList<String>(getBackupPaths().keySet());
    }

    /**
     * Serialises the current state of the JSON configuration to the specified
     * writer. By default this doesn't use a pretty printer.
     * 
     * @param writer
     *            a writer
     * @throws IOException
     *             if there was an error writing the configuration
     */
    public void store(Writer writer) throws IOException {
        store(writer, false);
    }

    /**
     * Serialises the current state of the JSON configuration to the specified
     * writer. The output can be set to be pretty printed if required.
     * 
     * @param writer
     *            a writer
     * @param pretty
     *            use pretty printer
     * @throws IOException
     *             if there was an error writing the configuration
     */
    public void store(Writer writer, boolean pretty) throws IOException {
        jsonConfig.store(writer, pretty);
    }

}
