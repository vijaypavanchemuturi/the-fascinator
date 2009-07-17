package au.edu.usq.fascinator.harvester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.common.BasicHttpClient;
import au.edu.usq.fascinator.common.JsonConfig;

public class JsonQHarvester implements Harvester {

    private Logger log = LoggerFactory.getLogger(JsonQHarvester.class);

    private String url;

    private long lastModified;

    public String getId() {
        return "jsonq";
    }

    public String getName() {
        return "JSON Queue Harvester";
    }

    public void init(File jsonFile) throws PluginException {
        try {
            JsonConfig config = new JsonConfig(jsonFile);
            url = config.get("harvest/jsonq/url");
            String lastMod = config.get("harvest/jsonq/lastModified", "0");
            lastModified = Long.parseLong(lastMod);
            log.debug("QueueHarvester url: {}", url);
        } catch (IOException ioe) {
            log.error("Failed to load config", ioe);
            throw new PluginException(ioe);
        }
    }

    public void shutdown() throws PluginException {
    }

    @SuppressWarnings("unchecked")
    public List<DigitalObject> getObjects() throws HarvesterException {
        List<DigitalObject> objectList = new ArrayList<DigitalObject>();
        try {
            BasicHttpClient client = new BasicHttpClient(url);
            GetMethod method = new GetMethod(url);
            int status = client.executeMethod(method);
            if (status == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStream in = method.getResponseBodyAsStream();
                IOUtils.copy(in, out);
                in.close();
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Map<String, String>> map = mapper.readValue(
                        new ByteArrayInputStream(out.toByteArray()), Map.class);
                for (String key : map.keySet()) {
                    DigitalObject object = new JsonQDigitalObject(key, map
                            .get(key));
                    objectList.add(object);
                }
            }
            method.releaseConnection();
        } catch (IOException ioe) {
            log.error("Failed to GET", ioe);
        }
        return objectList;
    }

    public boolean hasMoreObjects() {
        return false;
    }

}
