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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.storage.StorageUtils;

public class HarvestClient {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATETIME_FORMAT = DATE_FORMAT + "'T'hh:mm:ss'Z'";

    private static final String DEFAULT_STORAGE_TYPE = "file-system";

    private static Logger log = LoggerFactory.getLogger(HarvestClient.class);

    private File configFile;
    private DigitalObject configObject;

    private File rulesFile;
    private DigitalObject rulesObject;

    private File uploadedFile;

    private JsonConfig config;

    private ConveyerBelt conveyerBelt;

    private Storage storage;

    private Connection connection;

    private Session session;

    private MessageProducer producer;

    public HarvestClient() throws HarvesterException {
        this(null, null);
    }

    public HarvestClient(File configFile) throws HarvesterException {
        this(configFile, null);
    }

    public HarvestClient(File configFile, File uploadedFile)
            throws HarvesterException {
        MDC.put("name", "client");
        this.configFile = configFile;
        this.uploadedFile = uploadedFile;
        try {
            config = new JsonConfig(configFile);
        } catch (IOException ioe) {
            throw new HarvesterException("Failed to read configuration file: '"
                    + configFile + "'");
        }
        // initialise storage system
        String storageType = config.get("storage/type", DEFAULT_STORAGE_TYPE);
        storage = PluginManager.getStorage(storageType);
        if (storage == null) {
            throw new HarvesterException("Storage plugin '" + storageType
                    + "'. Ensure it is in the classpath.");
        }
        try {
            storage.init(configFile);
            log.info("Loaded {}", storage.getName());
        } catch (PluginException pe) {
            throw new HarvesterException("Failed to initialise storage", pe);
        }

        rulesFile = new File(configFile.getParent(), config
                .get("indexer/script/rules"));

        initConnection();
    }

    private void initConnection() {
        try {
            String brokerUrl = config.get("messaging/url",
                    ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                    brokerUrl);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session
                    .createQueue(HarvestQueueConsumer.HARVEST_QUEUE);
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        } catch (JMSException jmse) {
            log.error("Failed to start connection: {}", jmse.getMessage());
        }
    }

    public void start() throws PluginException {
        DateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
        String now = df.format(new Date());
        long start = System.currentTimeMillis();
        log.info("Started at " + now);

        // cache harvester config and indexer rules
        configObject = StorageUtils.storeFile(storage, configFile);
        rulesObject  = StorageUtils.storeFile(storage, rulesFile);

        // initialise the harvester
        Harvester harvester = null;
        String harvesterType = config.get("harvester/type");
        harvester = PluginManager.getHarvester(harvesterType, storage);
        if (harvester == null) {
            throw new HarvesterException("Harvester plugin '" + harvesterType
                    + "'. Ensure it is in the classpath.");
        }
        harvester.init(configFile);
        log.info("Loaded harvester: " + harvester.getName());

        // initialise the extractor conveyer belt
        conveyerBelt = new ConveyerBelt(configFile, ConveyerBelt.EXTRACTOR);

        if (uploadedFile != null) {
            // process the uploaded file only
            Set<String> objectIds = harvester.getObjectId(uploadedFile);
            if (!objectIds.isEmpty()) {
                processObject(objectIds.iterator().next());
            }
        } else {
            // process harvested objects
            do {
                for (String oid : harvester.getObjectIdList()) {
                    processObject(oid);
                }
            } while (harvester.hasMoreObjects());
            // process deleted objects
            do {
                for (String oid : harvester.getDeletedObjectIdList()) {
                    storage.removeObject(oid);
                    queueDelete(oid, configFile);
                }
            } while (harvester.hasMoreObjects());
        }

        log.info("Completed in "
                + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    public void reharvest(String oid) {
        /*
         * try { log.info("Reharvest '{}'...", oid); storage =
         * PluginManager.getStorage(config.get("storage/type",
         * DEFAULT_STORAGE_TYPE)); log.debug("Loaded {}", storage.getName());
         * storage.init(configFile);
         * 
         * // Get the Object from storage DigitalObject object =
         * storage.getObject(oid);
         * 
         * // Get the configFile from SOF-META Properties sofMeta =
         * getSofMeta(object); String sofMetaConfigFileOid =
         * sofMeta.getProperty("jsonConfigOid"); if (sofMetaConfigFileOid ==
         * null) { log .error(
         * "Fail to locate json config for {}, Using default config from system-config.json"
         * , oid);
         * 
         * } else { configFile = new File(sofMetaConfigFileOid); config = new
         * JsonConfig(configFile); }
         * 
         * // Aperture transform cb = new ConveyerBelt(configFile, "extractor");
         * object = cb.transform(object);
         * 
         * // Set up queueStorage & start reharvest queueStorage = new
         * QueueStorage(storage, configFile); queueStorage.init(configFile);
         * queueStorage.addObject(object);
         * log.info("Successfully Reharvest + Reindex {} ", oid); } catch
         * (Exception e) { log.error("Failed to initialise storage", e); return;
         * }
         */
    }

    public void reHarvestView(String portalQuery) {
        /*
         * DateFormat df = new SimpleDateFormat(DATETIME_FORMAT); String now =
         * df.format(new Date()); long start = System.currentTimeMillis();
         * log.info("Started at " + now);
         * 
         * // Get all the records from solr int startRow = 0; int numPerPage =
         * 5; int numFound = 0; do { ByteArrayOutputStream result = new
         * ByteArrayOutputStream(); SearchRequest request = new
         * SearchRequest("*:*"); request.addParam("rows",
         * String.valueOf(numPerPage)); request.addParam("fq",
         * "item_type:\"object\""); request.setParam("start",
         * String.valueOf(startRow));
         * 
         * if (portalQuery != "" && portalQuery != null) {
         * request.addParam("fq", portalQuery); }
         * 
         * try { Indexer indexer = PluginManager.getIndexer(config.get(
         * "indexer/type", DEFAULT_INDEXER_TYPE)); indexer.init(configFile);
         * 
         * indexer.search(request, result); JsonConfigHelper js;
         * 
         * js = new JsonConfigHelper(result.toString()); for (Object oid :
         * js.getList("response/docs/id")) { reharvest(oid.toString()); }
         * 
         * startRow += numPerPage; numFound =
         * Integer.parseInt(js.get("response/numFound")); } catch
         * (PluginException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
         * } while (startRow < numFound);
         * 
         * log.info("Completed in " + ((System.currentTimeMillis() - start) /
         * 1000.0) + " seconds");
         */
    }

    public void shutdown() {
        if (storage != null) {
            try {
                storage.shutdown();
            } catch (PluginException pe) {
                log.error("Failed to shutdown storage", pe);
            }
        }
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close producer", jmse);
            }
        }
        if (session != null) {
            try {
                session.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close session", jmse);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close connection", jmse);
            }
        }
    }

    private void processObject(String oid) throws StorageException,
            TransformerException {
        // get the object
        DigitalObject object = storage.getObject(oid);

        // transform it with just the extractor transformers
        object = conveyerBelt.transform(object);

        // update object metadata
        Properties props = object.getMetadata();
        // FIXME objectId is redundant now?
        props.setProperty("objectId", object.getId());
        props.setProperty("scriptType", config.get("indexer/script/type"));
        props.setProperty("rulesOid", rulesObject.getId());
        props.setProperty("rulesPid", rulesObject.getSourceId());
        props.setProperty("jsonConfigOid", configObject.getId());
        props.setProperty("jsonConfigPid", configObject.getSourceId());
        Map<String, Object> params = config.getMap("indexer/params");
        for (String key : params.keySet()) {
            props.setProperty(key, params.get(key).toString());
        }

        // done with the object
        object.close();

        // queue the object for indexing
        queueHarvest(oid, configFile);
    }

    private void queueHarvest(String oid, File jsonFile) {
        try {
            JsonConfigHelper json = new JsonConfigHelper(jsonFile);
            json.set("oid", oid);
            TextMessage message = session.createTextMessage(json.toString());
            producer.send(message);
        } catch (IOException ioe) {
            log.error("Failed to parse message: {}", ioe.getMessage());
        } catch (JMSException jmse) {
            log.error("Failed to send message: {}", jmse.getMessage());
        }
    }

    private void queueDelete(String oid, File jsonFile) {
        try {
            JsonConfigHelper json = new JsonConfigHelper(jsonFile);
            json.set("oid", oid);
            json.set("deleted", "true");
            TextMessage message = session.createTextMessage(json.toString());
            producer.send(message);
        } catch (IOException ioe) {
            log.error("Failed to parse message: {}", ioe.getMessage());
        } catch (JMSException jmse) {
            log.error("Failed to send message: {}", jmse.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            log.info("Usage: harvest <json-config>");
        } else {
            File jsonFile = new File(args[0]);
            try {
                HarvestClient harvest = new HarvestClient(jsonFile);
                harvest.start();
                harvest.shutdown();
            } catch (PluginException pe) {
                log.error("Failed to initialise client: ", pe);
            }
        }
    }
}
