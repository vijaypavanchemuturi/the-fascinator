/* 
 * The Fascinator - Core
 * Copyright (C) 2009 University of Southern Queensland
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package au.edu.usq.fascinator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.api.indexer.IndexerException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.storage.impl.GenericPayload;

/**
 * Index Client class to index the storage
 * 
 * TODO: to index single item and portal
 * 
 * @author Linda Octalina
 * 
 */
public class IndexClient {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATETIME_FORMAT = DATE_FORMAT + "'T'hh:mm:ss'Z'";

    private static final String DEFAULT_INDEXER_TYPE = "solr";

    private static final String DEFAULT_STORAGE_TYPE = "file-system";

    private static Logger log = LoggerFactory.getLogger(IndexClient.class);

    private File configFile;

    private JsonConfig config;

    private File rulesFile;

    private Indexer indexer;

    private Storage storage, realStorage;

    /**
     * IndexClient Constructor
     * 
     * @throws IOException
     */
    public IndexClient() throws IOException {
        config = new JsonConfig();
        configFile = config.getSystemFile();
        setSetting();
    }

    /**
     * IndexClient Constructor
     * 
     * @param jsonFile
     * @throws IOException
     */
    public IndexClient(File jsonFile) throws IOException {
        configFile = jsonFile;
        config = new JsonConfig(jsonFile);
        setSetting();
    }

    /**
     * Set the default setting
     */
    public void setSetting() {
        // Get the storage type to be indexed...
        try {
            realStorage = PluginManager.getStorage(config.get("storage/type",
                    DEFAULT_STORAGE_TYPE));
            indexer = PluginManager.getIndexer(config.get("indexer/type",
                    DEFAULT_INDEXER_TYPE));
            storage = new IndexedStorage(realStorage, indexer);
            storage.init(configFile);
            log.info("Loaded {} and {}", realStorage.getName(), indexer
                    .getName());
        } catch (Exception e) {
            log.error("Failed to initialise storage", e);
            return;
        }
    }

    /**
     * Start to run the indexing
     */
    public void run() {
        DateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
        String now = df.format(new Date());
        long start = System.currentTimeMillis();
        log.info("Started at " + now);

        rulesFile = new File(configFile.getParentFile(), config
                .get("indexer/script/rules"));
        log.debug("rulesFile=" + rulesFile);

        String rulesOid;
        try {
            log.debug("Caching rules file " + rulesFile);
            DigitalObject rulesObject = new RulesDigitalObject(rulesFile);
            realStorage.addObject(rulesObject);
            log.debug("Realstorage: " + realStorage.getId());
            rulesOid = rulesObject.getId();
        } catch (StorageException se) {
            log.error("Failed to cache indexing rules, stopping", se);
            return;
        }

        // List all the DigitalObject in the storages
        List<DigitalObject> objectList = realStorage.getObjectList();
        for (DigitalObject object : objectList) {
            try {
                // realStorage.removePayload(object.getId(), "SOF-META");
                processObject(object, rulesOid, config.getMap("indexer/params"));
            } catch (StorageException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        log.info("Completed in "
                + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    /**
     * Indexing single object
     * 
     * @param objectId
     */
    public void indexObject(String objectId) {
        DigitalObject object = realStorage.getObject(objectId);
        // Get the rules from SOF-META
        Properties sofMeta = getSofMeta(object);
        String sofMetaRulesOid = sofMeta.getProperty("rulesOid");
        rulesFile = new File(sofMetaRulesOid);

        String rulesOid;
        try {
            log.debug("Caching rules file " + rulesFile);
            DigitalObject rulesObject = new RulesDigitalObject(rulesFile);
            realStorage.addObject(rulesObject);
            log.debug("Realstorage: " + realStorage.getId());
            rulesOid = rulesObject.getId();
        } catch (StorageException se) {
            log.error("Failed to cache indexing rules, stopping", se);
            return;
        }
        try {
            processObject(object, rulesOid, null);
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Index portal
     * 
     * @param portalName
     */
    public void indexPortal(String portalName) {

    }

    /**
     * Start to process indexing
     * 
     * @param object
     * @param rulesOid
     * @param indexerParams
     * @return
     * @throws StorageException
     * @throws IOException
     */
    private String processObject(DigitalObject object, String rulesOid,
            Map<String, Object> indexerParams) throws StorageException,
            IOException {
        String oid = object.getId();
        String sid = null;

        log.info("Processing " + oid + "...");
        Properties oldSofMeta = getSofMeta(object);
        Properties sofMeta = new Properties();
        sofMeta.setProperty("objectId", oid);
        Payload metadata = object.getMetadata();
        String metaPid = "";
        if (metadata != null) {
            metaPid = metadata.getId();
        } else {
            // get the meta id and the repository information from the old
            // sof-meta
            if (oldSofMeta != null) {
                metaPid = oldSofMeta.getProperty("metaPid");
                sofMeta.setProperty("repository.name", oldSofMeta
                        .getProperty("repository.name"));
                sofMeta.setProperty("repository.type", oldSofMeta
                        .getProperty("repository.type"));
            }
        }
        sofMeta.setProperty("metaPid", metaPid);
        sofMeta.setProperty("scriptType", config.get("indexer/script/type",
                "python"));
        sofMeta.setProperty("rulesOid", rulesOid);
        sofMeta.setProperty("rulesPid", rulesFile.getName());

        if (indexerParams != null) {
            for (String key : indexerParams.keySet()) {
                sofMeta.setProperty(key, indexerParams.get(key).toString());
            }
        }

        ByteArrayOutputStream sofMetaOut = new ByteArrayOutputStream();
        // Remove the old sof-meta
        realStorage.removePayload(object.getId(), "SOF-META");

        // Store new sof-meta
        sofMeta.store(sofMetaOut, "The Fascinator Indexer Metadata2");
        GenericPayload sofMetaDs = new GenericPayload("SOF-META",
                "The Fascinator Indexer Metadata", "text/plain");
        sofMetaDs.setInputStream(new ByteArrayInputStream(sofMetaOut
                .toByteArray()));
        sofMetaDs.setType(PayloadType.Annotation);
        storage.addPayload(oid, sofMetaDs);
        try {
            indexer.index(oid);
        } catch (IndexerException e) {
            e.printStackTrace();
        }

        return sid;
    }

    /**
     * Main function of IndexClient
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            log.info("Usage: index <json-config>");
        } else {
            File jsonFile = new File(args[0]);
            try {
                IndexClient index = new IndexClient(jsonFile);
                index.run();
            } catch (IOException ioe) {
                log.error("Failed to initialise client: {}", ioe.getMessage());
            }
        }
    }

    private Properties getSofMeta(DigitalObject object) {
        try {
            Payload sofMetaPayload = object.getPayload("SOF-META");
            Properties sofMeta = new Properties();
            sofMeta.load(sofMetaPayload.getInputStream());
            return sofMeta;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
