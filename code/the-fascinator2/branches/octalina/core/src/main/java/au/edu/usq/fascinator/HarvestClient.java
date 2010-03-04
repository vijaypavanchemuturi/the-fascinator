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
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.api.indexer.SearchRequest;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.storage.impl.GenericPayload;

public class HarvestClient {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATETIME_FORMAT = DATE_FORMAT + "'T'hh:mm:ss'Z'";

    private static final String DEFAULT_STORAGE_TYPE = "file-system";

    private static final String DEFAULT_INDEXER_TYPE = "solr";

    private static Logger log = LoggerFactory.getLogger(HarvestClient.class);

    private JsonConfig config;

    private File configFile;

    private File rulesFile;

    private File uploadedFile;

    private ConveyerBelt cb;

    private QueueStorage queueStorage;

    private Storage storage;

    public HarvestClient(File jsonFile, File uploadFile) throws IOException {
        uploadedFile = uploadFile;
        MDC.put("name", "client");
        configFile = jsonFile;
        config = new JsonConfig(jsonFile);
        cb = new ConveyerBelt(jsonFile, "extractor");
    }

    public HarvestClient(File jsonFile) throws IOException {
        uploadedFile = null;
        MDC.put("name", "client");
        configFile = jsonFile;
        config = new JsonConfig(jsonFile);
        cb = new ConveyerBelt(jsonFile, "extractor");
    }

    public HarvestClient() throws IOException {
        uploadedFile = null;
        MDC.put("name", "client");
        config = new JsonConfig();
        configFile = config.getSystemFile();
    }

    public void reHarvest(String objectId) {
        try {
            log.info("Reharvesting + Reindexing: {}", objectId);
            storage = PluginManager.getStorage(config.get("storage/type",
                    DEFAULT_STORAGE_TYPE));
            log.debug("Loaded {}", storage.getName());
            storage.init(configFile);

            // Get the Object from storage
            DigitalObject object = storage.getObject(objectId);

            // Get the configFile from SOF-META
            Properties sofMeta = getSofMeta(object);
            String sofMetaConfigFileOid = sofMeta.getProperty("jsonConfigOid");
            if (sofMetaConfigFileOid == null) {
                log
                        .error(
                                "Fail to locate json config for {}, Using default config from system-config.json",
                                objectId);

            } else {
                configFile = new File(sofMetaConfigFileOid);
                config = new JsonConfig(configFile);
            }

            // Aperture transform
            cb = new ConveyerBelt(configFile, "extractor");
            object = cb.transform(object);

            // Set up queueStorage & start reharvest
            queueStorage = new QueueStorage(storage, configFile);
            queueStorage.init(configFile);
            queueStorage.addObject(object);
            log.info("Successfully Reharvest + Reindex {} ", objectId);
        } catch (Exception e) {
            log.error("Failed to initialise storage", e);
            return;
        }
    }

    public void reHarvestView(String portalQuery) {
        DateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
        String now = df.format(new Date());
        long start = System.currentTimeMillis();
        log.info("Started at " + now);

        // Get all the records from solr
        int startRow = 0;
        int numPerPage = 5;
        int numFound = 0;
        do {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            SearchRequest request = new SearchRequest("*:*");
            request.addParam("rows", String.valueOf(numPerPage));
            request.addParam("fq", "item_type:\"object\"");
            request.setParam("start", String.valueOf(startRow));

            if (portalQuery != "" && portalQuery != null) {
                request.addParam("fq", portalQuery);
            }

            try {
                Indexer indexer = PluginManager.getIndexer(config.get(
                        "indexer/type", DEFAULT_INDEXER_TYPE));
                indexer.init(configFile);

                indexer.search(request, result);
                JsonConfigHelper js;

                js = new JsonConfigHelper(result.toString());
                for (Object oid : js.getList("response/docs/id")) {
                    reHarvest(oid.toString());
                }

                startRow += numPerPage;
                numFound = Integer.parseInt(js.get("response/numFound"));
            } catch (PluginException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (startRow < numFound);

        log.info("Completed in "
                + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    public void run() {
        DateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
        String now = df.format(new Date());
        long start = System.currentTimeMillis();
        log.info("Started at " + now);

        try {
            storage = PluginManager.getStorage(config.get("storage/type",
                    DEFAULT_STORAGE_TYPE));
            queueStorage = new QueueStorage(storage, configFile);
            queueStorage.init(configFile);
            log.info("Loaded {}", storage.getName());
        } catch (Exception e) {
            log.error("Failed to initialise storage", e);
            return;
        }

        rulesFile = new File(configFile.getParentFile(), config
                .get("indexer/script/rules"));
        log.debug("rulesFile=" + rulesFile);

        String rulesOid, jsonConfigId;
        try {
            log.debug("Caching rules file " + rulesFile);
            DigitalObject rulesObject = new RulesDigitalObject(rulesFile);
            storage.addObject(rulesObject);
            rulesOid = rulesObject.getId();

            log.debug("Caching json config file " + configFile);
            DigitalObject jsonConfigObject = new RulesDigitalObject(configFile);
            storage.addObject(jsonConfigObject);
            jsonConfigId = jsonConfigObject.getId();

        } catch (StorageException se) {
            log.error("Failed to cache indexing rules, stopping", se);
            return;
        }

        String harvesterType = config.get("harvester/type");
        Harvester harvester;
        try {
            harvester = PluginManager.getHarvester(harvesterType);
            if (harvester == null) {
                throw new PluginException("Harvester plugin not found: "
                        + harvesterType);
            }
            harvester.init(configFile);
            log.info("Loaded harvester: " + harvester.getName());
        } catch (PluginException pe) {
            log.error("Failed to initialise harvester plugin", pe);
            return;
        }

        // Uploaded file to be processed
        if (uploadedFile != null) {
            try {
                for (DigitalObject item : harvester.getObject(uploadedFile)) {
                    try {
                        processObject(queueStorage, item, rulesOid,
                                jsonConfigId);
                    } catch (Exception e) {
                        log.warn("Processing failed: " + item.getId(), e);
                    }
                }
            } catch (HarvesterException he) {
                log.error("Failed to harvest", he);
            }

        // Normal operations
        } else {
            do {
                try {
                    for (DigitalObject item : harvester.getObjects()) {
                        try {
                            processObject(queueStorage, item, rulesOid,
                                    jsonConfigId);
                        } catch (Exception e) {
                            log.warn("Processing failed: " + item.getId(), e);
                        }
                    }
                } catch (HarvesterException he) {
                    log.error("Failed to harvest", he);
                }
            } while (harvester.hasMoreObjects());
        }

        do {
            try {
                for (DigitalObject item : harvester.getDeletedObjects()) {
                    queueStorage.removeObject(item.getId());
                }
            } catch (HarvesterException he) {
                log.error("Failed to delete", he);
            }
        } while (harvester.hasMoreDeletedObjects());

        try {
            queueStorage.shutdown();
        } catch (PluginException e) {
            log.error("Failed to shutdown storage", e);
        }

        log.info("Completed in "
                + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    private String processObject(Storage storage, DigitalObject object,
            String rulesOid, String jsonConfigId) throws StorageException,
            IOException {
        String oid = object.getId();
        String sid = null;
        try {
            log.info("Processing " + oid + "...");

            // Calling conveyer to perform aperture transformation
            object = cb.transform(object);

            Properties sofMeta = new Properties();
            sofMeta.setProperty("objectId", oid);
            Payload metadata = object.getMetadata();
            if (metadata != null) {
                sofMeta.setProperty("metaPid", metadata.getId());
            }
            sofMeta.setProperty("scriptType", config.get("indexer/script/type",
                    "python"));
            sofMeta.setProperty("rulesOid", rulesOid);
            sofMeta.setProperty("rulesPid", rulesFile.getName());

            sofMeta.setProperty("jsonConfigOid", jsonConfigId);
            sofMeta.setProperty("jsonConfigPid", configFile.getName());

            Map<String, Object> indexerParams = config.getMap("indexer/params");
            for (String key : indexerParams.keySet()) {
                sofMeta.setProperty(key, indexerParams.get(key).toString());
            }
            ByteArrayOutputStream sofMetaOut = new ByteArrayOutputStream();
            log.debug("** sofmeta: " + sofMeta.toString());
            sofMeta.store(sofMetaOut, "The Fascinator Indexer Metadata");
            GenericPayload sofMetaDs = new GenericPayload("SOF-META",
                    "The Fascinator Indexer Metadata", "text/plain");
            sofMetaDs.setInputStream(new ByteArrayInputStream(sofMetaOut
                    .toByteArray()));
            sofMetaDs.setType(PayloadType.Annotation);
            log.debug("-- adding softmeta to: " + oid);
            storage.addPayload(oid, sofMetaDs);

            storage.addObject(object);

        } catch (StorageException re) {
            throw new IOException(re.getMessage());
        } catch (TransformerException te) {
            throw new IOException(te.getMessage());
        }
        return sid;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            log.info("Usage: harvest <json-config>");
        } else {
            File jsonFile = new File(args[0]);
            try {
                HarvestClient harvest = new HarvestClient(jsonFile);
                harvest.run();
            } catch (IOException ioe) {
                log.error("Failed to initialise client: {}", ioe.getMessage());
            }
        }
    }

    /**
     * Getting the sofMeta properties for the DigitalObject
     * 
     * @param object
     * @return properties
     */
    private Properties getSofMeta(DigitalObject object) {
        try {
            Payload sofMetaPayload = object.getPayload("SOF-META");
            Properties sofMeta = new Properties();
            sofMeta.load(sofMetaPayload.getInputStream());
            return sofMeta;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
