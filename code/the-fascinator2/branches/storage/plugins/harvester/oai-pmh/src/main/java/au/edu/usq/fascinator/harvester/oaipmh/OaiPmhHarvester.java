/* 
 * The Fascinator - Plugin - Harvester - OAI-PMH
 * Copyright (C) 2008-2009 University of Southern Queensland
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
package au.edu.usq.fascinator.harvester.oaipmh;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.ErrorResponseException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;
import au.edu.usq.fascinator.api.Configurable;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * Harvests metadata records from an OAI-PMH server
 * <p>
 * Configuration options:
 * <ul>
 * <li>url: OAI-PMH server</li>
 * <li>maxRequests: number of requests to do (default: no limit)</li>
 * <li>metadataPrefix: type of metadata to get (default: oai_dc)</li>
 * <li>setSpec: set to limit records to (optional)</li>
 * <li>from: lower bound of date range (optional)</li>
 * <li>until: upper bound of date range (optional)</li>
 * </ul>
 * 
 * @author Oliver Lucido
 */
public class OaiPmhHarvester implements Harvester, Configurable {

    /** Date format */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /** Date and time format */
    public static final String DATETIME_FORMAT = DATE_FORMAT + "'T'hh:mm:ss'Z'";

    /** Default metadataPrefix (Dublin Core) */
    public static final String DEFAULT_METADATA_PREFIX = "oai_dc";

    /** Logging */
    private Logger log = LoggerFactory.getLogger(OaiPmhHarvester.class);

    /** OAI-PMH server */
    private OaiPmhServer server;

    /** Whether or not the harvest has started */
    private boolean started;

    /** Session resumption token */
    private ResumptionToken token;

    /** Current number of requests/objects done */
    private int numRequests;
    private int numObjects;

    /** Maximum requests/objects to do */
    private int maxRequests;
    private int maxObjects;

    /** Request for a specific document */
    private String recordID;

    /** Configuration */
    private JsonConfig config;

    @Override
    public String getId() {
        return "oai-pmh";
    }

    @Override
    public String getName() {
        return "OAI-PMH Harvester";
    }

    @Override
    public void init(File jsonFile) throws HarvesterException {
        try {
            config = new JsonConfig(jsonFile);
            server = new OaiPmhServer(config.get("harvester/oai-pmh/url"));
        } catch (IOException ioe) {
            throw new HarvesterException(ioe);
        }

        /** Check for request on a specific ID */
        recordID = config.get("harvester/oai-pmh/recordID", null);

        /** Check for any specified result set size limits */
        maxRequests = Integer.parseInt(config.get(
                "harvester/oai-pmh/maxRequests", "-1"));
        if (maxRequests == -1) {
            maxRequests = Integer.MAX_VALUE;
        }
        maxObjects = Integer.parseInt(config.get(
                "harvester/oai-pmh/maxObjects", "-1"));
        if (maxObjects == -1) {
            maxObjects = Integer.MAX_VALUE;
        }

        started = false;
        numRequests = 0;
        numObjects = 0;
    }

    @Override
    public void shutdown() throws HarvesterException {
    }

    @Override
    public List<DigitalObject> getObjects() throws HarvesterException {
        DateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
        String from = config.get("harvester/oai-pmh/from");
        String until = config.get("harvester/oai-pmh/until");
        Date fromDate = null;
        if (from != null) {
            try {
                fromDate = df.parse(from);
            } catch (ParseException pe) {
                log.warn("Failed to parse date {}", from, pe);
            }
        }
        List<DigitalObject> items = new ArrayList<DigitalObject>();
        String metadataPrefix = config.get("harvester/oai-pmh/metadataPrefix",
                DEFAULT_METADATA_PREFIX);
        String setSpec = config.get("harvester/oai-pmh/setSpec");
        RecordsList records;
        try {
            numRequests++;
            /** Request for a specific ID */
            if (recordID != null) {
                log.info("Requesting record {}", recordID);
                Record record = server.getRecord(recordID, metadataPrefix);
                /** Add the item and return now, so we don't confuse
                    the record loop further down */
                items.add(new OaiPmhDigitalObject(record, metadataPrefix));
                return items;

            /** Continue an already running request */
            } else if (started) {
                records = server.listRecords(token);
                log.info("Resuming harvest using token {}", token.getId());

            /** Start a new request */
            } else {
                started = true;
                if (fromDate == null) {
                    log.info("Harvesting all records");
                    records = server.listRecords(metadataPrefix, null, null,
                            setSpec);
                } else {
                    try {
                        log.info("Harvesting records from {} to {}", from,
                                until == null ? until : " now");
                        records = server.listRecords(metadataPrefix, from,
                                until, setSpec);
                    } catch (ErrorResponseException ere) {
                        if (ere.getMessage().startsWith("Max granularity")) {
                            log.warn(ere.getMessage());
                            df = new SimpleDateFormat(DATE_FORMAT);
                            from = df.format(fromDate);
                        }
                        log.info("Harvesting records from {} to {}", from,
                                until == null ? until : " now");
                        records = server.listRecords(metadataPrefix, from,
                                until, setSpec);
                    }
                }
            }
            for (Record record : records.asList()) {
                if (numObjects < maxObjects) {
                    numObjects++;
                    items.add(new OaiPmhDigitalObject(record, metadataPrefix));
                }
            }
            token = records.getResumptionToken();
        } catch (OAIException oe) {
            throw new HarvesterException(oe);
        }
        return items;
    }

    @Override
    public List<DigitalObject> getObject(File uploadedFile)
            throws HarvesterException {
        throw new HarvesterException("This plugin does not harvest uploaded files");
    }

    @Override
    public boolean hasMoreObjects() {
        return token != null && numRequests < maxRequests && numObjects < maxObjects;
    }

    @Override
    public List<DigitalObject> getDeletedObjects() {
        // empty for now
        return Collections.emptyList();
    }

    @Override
    public boolean hasMoreDeletedObjects() {
        return false;
    }

    @Override
    public String getConfig() {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(getClass().getResourceAsStream(
                    "/" + getId() + "-config.html"), writer);
        } catch (IOException ioe) {
            writer.write("<span class=\"error\">" + ioe.getMessage()
                    + "</span>");
        }
        return writer.toString();
    }

    @Override
    public void init(String jsonString) throws PluginException {
        // TODO Auto-generated method stub

    }
}
