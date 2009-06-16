package au.edu.usq.fascinator.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtils {

    private static Logger log = LoggerFactory.getLogger(ConfigUtils.class);

    private static final String CONFIG_HOME = System.getProperty("user.home")
            + File.separator + ".fascinator";

    public static JsonNode parseJson(File jsonFile) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readValue(jsonFile, JsonNode.class);
        } catch (JsonParseException jpe) {
            log.error("", jpe);
        } catch (JsonMappingException jme) {
            log.error("", jme);
        } catch (IOException ioe) {
            log.error("", ioe);
        }
        return node;
    }

    public static String getTextValue(JsonNode node, String fieldName) {
        return getTextValue(node, fieldName, null);
    }

    public static String getTextValue(JsonNode node, String fieldName,
            String defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null) {
            return defaultValue;
        } else {
            return fieldNode.getTextValue();
        }
    }

    public static JsonNode getSystemConfig() throws IOException {
        return parseJson(getSystemConfigFile());
    }

    public static File getSystemConfigFile() throws IOException {
        File configFile = new File(CONFIG_HOME, "system-config.json");
        if (!configFile.exists()) {
            OutputStream out = new FileOutputStream(configFile);
            IOUtils.copy(ConfigUtils.class
                    .getResourceAsStream("/system-config.json"), out);
            out.close();
        }
        return configFile;
    }
}
