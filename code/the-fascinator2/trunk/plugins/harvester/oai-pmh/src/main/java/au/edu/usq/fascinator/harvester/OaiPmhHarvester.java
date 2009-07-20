package au.edu.usq.fascinator.harvester;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.ErrorResponseException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.common.JsonConfig;

public class OaiPmhHarvester implements Harvester {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATETIME_FORMAT = DATE_FORMAT + "'T'hh:mm:ss'Z'";

    public static final String DEFAULT_METADATA_PREFIX = "oai_dc";

    private Logger log = LoggerFactory.getLogger(OaiPmhHarvester.class);

    private OaiPmhServer server;

    private boolean started;

    private ResumptionToken token;

    private int numRequests;

    private int maxRequests;

    private JsonConfig config;

    public String getId() {
        return "oai-pmh";
    }

    public String getName() {
        return "OAI-PMH Harvester";
    }

    public void init(File jsonFile) throws PluginException {
        try {
            config = new JsonConfig(jsonFile);
            server = new OaiPmhServer(config.get("harvest/oai-pmh/url"));
        } catch (IOException ioe) {
            throw new PluginException(ioe);
        }
        maxRequests = Integer.parseInt(config.get(
                "harvest/oai-pmh/max.requests", "-1"));
        if (maxRequests == -1) {
            maxRequests = Integer.MAX_VALUE;
        }
        started = false;
        numRequests = 0;
    }

    public void shutdown() throws PluginException {
    }

    public List<DigitalObject> getObjects() throws HarvesterException {
        DateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
        Date fromDate = null;
        String from = config.get("harvest/config/from");
        if (from != null) {
            try {
                fromDate = df.parse(from);
            } catch (ParseException pe) {
                log.warn("Failed to parse from date: {}", pe);
            }
        }
        List<DigitalObject> items = new ArrayList<DigitalObject>();
        String metadataPrefix = config.get("harvest/config/metadataPrefix",
                DEFAULT_METADATA_PREFIX);
        String setSpec = config.get("harvest/config/setSpec");
        RecordsList records;
        try {
            numRequests++;
            if (started) {
                records = server.listRecords(token);
                log.info("Resuming harvest using token {}", token.getId());
            } else {
                started = true;
                if (fromDate == null) {
                    log.info("Harvesting all records");
                    records = server.listRecords(metadataPrefix, null, null,
                            setSpec);
                } else {
                    try {
                        log.info("Harvesting records from {}", from);
                        records = server.listRecords(metadataPrefix, from,
                                null, setSpec);
                    } catch (ErrorResponseException ere) {
                        if (ere.getMessage().startsWith("Max granularity")) {
                            log.warn(ere.getMessage());
                            df = new SimpleDateFormat(DATE_FORMAT);
                            from = df.format(fromDate);
                        }
                        log.info("Harvesting records from {}", from);
                        records = server.listRecords(metadataPrefix, from,
                                null, setSpec);
                    }
                }
            }
            for (Record record : records.asList()) {
                items.add(new OaiPmhDigitalObject(record, metadataPrefix));
            }
            token = records.getResumptionToken();
        } catch (OAIException oe) {
            throw new HarvesterException(oe);
        }
        return items;
    }

    public boolean hasMoreObjects() {
        return token != null && numRequests < maxRequests;
    }

}
