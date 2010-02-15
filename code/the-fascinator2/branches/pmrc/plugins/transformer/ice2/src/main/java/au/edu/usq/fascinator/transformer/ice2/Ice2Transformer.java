package au.edu.usq.fascinator.transformer.ice2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.transformer.Transformer;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.BasicHttpClient;
import au.edu.usq.fascinator.common.JsonConfig;

public class Ice2Transformer implements Transformer {

    private static final String DEFAULT_OUTPUT_PATH = System
            .getProperty("user.home")
            + File.separator + ".fascinator" + File.separator + "ice2-output";

    private static final String DEFAULT_CONVERT_URL = "http://localhost:8000/api/convert";

    private static final String DEFAULT_EXCLUDE_EXT = ".txt,.mp3,.m4a";

    private Logger log = LoggerFactory.getLogger(Ice2Transformer.class);

    private JsonConfig config;

    private String outputPath;

    private String convertUrl;

    @Override
    public DigitalObject transform(DigitalObject object)
            throws TransformerException {
        File file = new File(object.getId());
        String ext = FilenameUtils.getExtension(file.getName());
        List<String> excludeList = Arrays.asList(StringUtils.split(get(
                "excludeRenditionExt", DEFAULT_EXCLUDE_EXT), ','));
        if (file.exists() && !excludeList.contains(ext)) {
            outputPath = get("outputPath", DEFAULT_OUTPUT_PATH);
            File outputDir = new File(outputPath);
            outputDir.mkdirs();
            convertUrl = get("url", DEFAULT_CONVERT_URL);
            try {
                if (isSupported(file)) {
                    File outputFile = render(file);
                    return new IceDigitalObject(object, outputFile);
                }
            } catch (TransformerException te) {
                log.debug("Adding error payload to {}", object.getId());
                IceDigitalObject iceObject = new IceDigitalObject(object);
                iceObject
                        .addPayload(new IceErrorPayload(file, te.getMessage()));
                return iceObject;
            }
        }
        return object;
    }

    private File render(File sourceFile) throws TransformerException {
        log.info("Converting {}...", sourceFile);
        String filename = sourceFile.getName();
        String basename = FilenameUtils.getBaseName(filename);
        int status = HttpStatus.SC_OK;
        String resizeMode = get("resize.image.mode", "fixedWidth");
        String imageRatio = get("resize.image.ratio", "-90");
        String resizeFixedWidth = get("resize.image.fixedWidth", "150");
        String enlargeImage = get("enlargeImage", "false");
        PostMethod post = new PostMethod(convertUrl);
        try {
            Part[] parts = { new StringPart("zip", "on"),
                    new StringPart("dc", "on"), new StringPart("toc", "on"),
                    new StringPart("pdflink", "on"),
                    new StringPart("pathext", ""),
                    new StringPart("template", getTemplate()),
                    new StringPart("resize", imageRatio),
                    new StringPart("resizeOption", resizeMode),
                    new StringPart("fixedWidth", resizeFixedWidth),
                    new StringPart("enlargeImage", enlargeImage),
                    new StringPart("mode", "download"),
                    new FilePart("file", sourceFile) };
            post.setRequestEntity(new MultipartRequestEntity(parts, post
                    .getParams()));
            BasicHttpClient client = new BasicHttpClient(convertUrl);
            log.debug("Using conversion URL: {}", convertUrl);
            status = client.executeMethod(post);
            log.debug("HTTP status: {} {}", status, HttpStatus
                    .getStatusText(status));
        } catch (IOException ioe) {
            throw new TransformerException(
                    "Failed to send ICE conversion request", ioe);
        }
        try {
            if (status != HttpStatus.SC_OK) {
                String xmlError = post.getResponseBodyAsString();
                log.debug("Error: {}", xmlError);
                throw new TransformerException(xmlError);
            }
            String type = post.getResponseHeader("Content-Type").getValue();
            if ("application/zip".equals(type)) {
                filename = basename + ".zip";
            } else if (type.startsWith("image/")) {
                filename = basename + "_thumbnail.jpg";
            } else if ("video/x-flv".equals(type)) {
                filename = basename + ".flv";
            } else if ("audio/mpeg".equals(type)) {
                filename = basename + ".mp3";
            }
            File outputFile = new File(outputPath, filename);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            InputStream in = post.getResponseBodyAsStream();
            FileOutputStream out = new FileOutputStream(outputFile);
            IOUtils.copy(in, out);
            in.close();
            out.close();
            log.debug("ICE output file: {}", outputFile);
            return outputFile;
        } catch (IOException ioe) {
            throw new TransformerException("Failed to process ICE output", ioe);
        }
    }

    private boolean isSupported(File sourceFile) throws TransformerException {
        String ext = FilenameUtils.getExtension(sourceFile.getName());
        String url = convertUrl + "/query?pathext=" + ext.toLowerCase();
        try {
            GetMethod getMethod = new GetMethod(url);
            BasicHttpClient extClient = new BasicHttpClient(url);
            extClient.executeMethod(getMethod);
            String response = getMethod.getResponseBodyAsString().trim();
            return "OK".equals(response);
        } catch (IOException ioe) {
            throw new TransformerException(
                    "Failed to query if file type is supported", ioe);
        }
    }

    private String getTemplate() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(getClass().getResourceAsStream("/template.xhtml"), out);
        return out.toString("UTF-8");
    }

    @Override
    public String getId() {
        return "ice2";
    }

    @Override
    public String getName() {
        return "ICE Transformer";
    }

    @Override
    public void init(File jsonFile) throws PluginException {
        try {
            config = new JsonConfig(jsonFile);
        } catch (IOException ioe) {
            throw new PluginException(ioe);
        }
    }

    @Override
    public void init(String jsonString) throws PluginException {
        try {
            config = new JsonConfig(jsonString);
        } catch (IOException ioe) {
            throw new PluginException(ioe);
        }
    }

    private String get(String key, String defaultValue) {
        return config.get("transformer/ice2/" + key, defaultValue);
    }

    @Override
    public void shutdown() throws PluginException {
    }
}
