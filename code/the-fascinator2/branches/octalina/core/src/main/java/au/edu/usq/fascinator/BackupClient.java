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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.api.indexer.IndexerException;
import au.edu.usq.fascinator.api.indexer.SearchRequest;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;

public class BackupClient {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATETIME_FORMAT = DATE_FORMAT + "'T'hh:mm:ss'Z'";

    private static final String DEFAULT_STORAGE_TYPE = "file-system";

    private static final String DEFAULT_INDEXER_TYPE = "solr";

    private static Logger log = LoggerFactory.getLogger(BackupClient.class);

    private JsonConfig config;

    private File configFile;

    public BackupClient(File jsonFile) throws IOException {
        configFile = jsonFile;
        config = new JsonConfig(jsonFile);
    }

    public BackupClient() throws IOException {
        config = new JsonConfig();
    }

    public void run() {
        DateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
        String now = df.format(new Date());
        long start = System.currentTimeMillis();
        log.info("Started at " + now);

        Storage storage, realStorage;
        Indexer indexer;
        try {
            realStorage = PluginManager.getStorage(config.get("storage/type",
                    DEFAULT_STORAGE_TYPE));
            indexer = PluginManager.getIndexer(config.get("indexer/type",
                    DEFAULT_INDEXER_TYPE));
            // storage = new IndexedStorage(realStorage, indexer);
            // storage.init(configFile);
            log.info("Loaded {} and {}", realStorage.getName(), indexer
                    .getName());
        } catch (Exception e) {
            log.error("Failed to initialise storage", e);
            return;
        }

        // Harvester backupHarvester;
        // try {
        // backupHarvester = PluginManager.getHarvester("backup");
        // if (backupHarvester == null) {
        // throw new PluginException("Backup Harvester plugin not found: "
        // + "backup");
        // }
        // backupHarvester.init(configFile);
        // log.info("Loaded harvester: " + backupHarvester.getName());
        // } catch (PluginException pe) {
        // log.error("Failed to initialise harvester plugin", pe);
        // return;
        // }

        // Get all the records from solr

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int startRow = 0;
        int numPerPage = 5;
        int numFound = 0;
        do {
            SearchRequest request = new SearchRequest("*:*");
            request.addParam("rows", String.valueOf(numPerPage));
            request.addParam("fq", "item_type:\"object\"");
            request.addParam("start", String.valueOf(startRow));
            try {
                indexer.search(request, result);
            } catch (IndexerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // backupHarvester.backup(result.toByteArray());
            JsonConfigHelper js;
            try {
                js = new JsonConfigHelper(result.toString());
                System.out.println("  " + js.getList("response/docs/id"));

                for (Object oid : js.getList("response/docs/id")) {
                    DigitalObject digobj = realStorage
                            .getObject(oid.toString());
                    Payload payload = digobj.getSource();
                    if (payload != null) {
                        IOUtils.copy(payload.getInputStream(), output)
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // realStorage.backup(js.getList("response/docs/id"));

            start += numPerPage;
            numFound = Integer.parseInt(js.get("response/numFound"));
        } while (startRow < numFound);

        // return Response.ok(result.toByteArray()).build();

        // self.__result =
        // JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))

        // do {
        // try {
        // for (DigitalObject item : harvester.getObjects()) {
        // try {
        // processObject(storage, item, rulesOid);
        // } catch (Exception e) {
        // log.warn("Processing failed: " + item.getId(), e);
        // }
        // }
        // } catch (HarvesterException he) {
        // log.error("Failed to harvest", he);
        // }
        // } while (harvester.hasMoreObjects());
        //
        // do {
        // try {
        // for (DigitalObject item : harvester.getDeletedObjects()) {
        // storage.removeObject(item.getId());
        // }
        // } catch (HarvesterException he) {
        // log.error("Failed to delete", he);
        // }
        // } while (harvester.hasMoreDeletedObjects());
        //
        // try {
        // storage.shutdown();
        // } catch (PluginException e) {
        // log.error("Failed to shutdown storage", e);
        // }

        log.info("Completed in "
                + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            log.info("Usage: backup <json-config>");
        } else {
            File jsonFile = new File(args[0]);
            try {
                BackupClient backup = new BackupClient(jsonFile);
                backup.run();
            } catch (IOException ioe) {
                log.error("Failed to initialise client: {}", ioe.getMessage());
            }
        }
    }
}
