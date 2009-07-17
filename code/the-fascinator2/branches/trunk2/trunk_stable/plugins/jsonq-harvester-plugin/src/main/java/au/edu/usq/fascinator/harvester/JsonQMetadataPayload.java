package au.edu.usq.fascinator.harvester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import au.edu.usq.fascinator.api.storage.impl.GenericPayload;

public class JsonQMetadataPayload extends GenericPayload {

    private Map<String, String> info;

    public JsonQMetadataPayload(File file, Map<String, String> info) {
        this.info = info;
        info.put("uri", file.getAbsolutePath());
        setId(file.getName() + ".properties");
        setLabel("File Metadata");
        setContentType("text/plain");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Properties props = new Properties();
        props.setProperty("uri", info.get("uri"));
        props.setProperty("state", info.get("state"));
        props.setProperty("time", info.get("time"));
        props.store(out, "File Metadata");
        return new ByteArrayInputStream(out.toByteArray());
    }

}
