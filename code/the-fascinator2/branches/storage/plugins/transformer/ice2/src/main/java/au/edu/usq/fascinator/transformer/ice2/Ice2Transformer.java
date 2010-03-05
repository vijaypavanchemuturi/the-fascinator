package au.edu.usq.fascinator.transformer.ice2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.api.transformer.Transformer;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.BasicHttpClient;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.MimeTypeUtil;

/**
 * Transformer Class will send a file to ice-service to get the renditions of
 * the file
 * 
 * Configuration options:
 * <ul>
 * <li>url: ICE service url (default:
 * http://ec2-75-101-136-199.compute-1.amazonaws.com/api/convert/)</li>
 * <li>outputPath: Output Directory to store the ICE rendition zip file
 * (default: ${java.io.tmpdir}/ice2-output)</li>
 * <li>excludeRenditionExt: type of file extension to be ignored (default:
 * txt,mp3,m4a)</li>
 * <li>resize: Image resizing option (default: thumbnail and preview resize
 * option)
 * <ul>
 * <li>option: resize mode (default: fixedWidth)</li>
 * <li>ratio: resize ratio percentage if using ratio mode (default: -90)</li>
 * <li>fixedWidth: resize width if using fixedWidth mode (default: 160 for
 * thumbnail, 600 for preview)</li>
 * <li>enlarge: option to enlarge the image if the image is smaller than the
 * given width (default: false)</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @see http 
 *      ://fascinator.usq.edu.au/trac/wiki/tf2/DeveloperNotes/plugins/transformer
 *      /ice2
 * @author Linda Octalina, Oliver Lucido
 * 
 */
public class Ice2Transformer implements Transformer {

    private Logger log = LoggerFactory.getLogger(Ice2Transformer.class);

    private JsonConfig config;

    private String outputPath;

    private String convertUrl;

    private static final String ZIP_MIME_TYPE = "application/zip";

    public Ice2Transformer() {

    }

    @Override
    public DigitalObject transform(DigitalObject object)
            throws TransformerException {
        File file = new File(object.getId());
        String ext = FilenameUtils.getExtension(file.getName());
        List<String> excludeList = Arrays.asList(StringUtils.split(
                get("excludeRenditionExt"), ','));
        if (file.exists() && !excludeList.contains(ext.toLowerCase())) {
            outputPath = get("outputPath");
            File outputDir = new File(outputPath);
            outputDir.mkdirs();
            convertUrl = get("url");
            try {
                if (isSupported(file)) {
                    File outputFile = render(file);
                    object = createIcePayload(object, outputFile);
                }
            } catch (TransformerException te) {
                log.debug("Adding error payload to {}", object.getId());
                try {
                    object = createErrorPayload(object, file, te.getMessage());
                } catch (StorageException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } catch (StorageException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public DigitalObject createErrorPayload(DigitalObject object, File file,
            String message) throws StorageException,
            UnsupportedEncodingException {
        String name = FilenameUtils.getBaseName(file.getName()) + "_error.htm";
        Payload errorPayload = object.createStoredPayload(name,
                new ByteArrayInputStream(message.getBytes("UTF-8")));
        errorPayload.setType(PayloadType.Enrichment);
        errorPayload.setLabel("ICE conversion errors");
        errorPayload.setContentType("text/html");
        return object;
    }

    public DigitalObject createIcePayload(DigitalObject object, File file)
            throws StorageException, IOException {
        if (ZIP_MIME_TYPE.equals(MimeTypeUtil.getMimeType(file))) {
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    Payload icePayload = object.createStoredPayload(name,
                            zipFile.getInputStream(entry));
                    // Set to enrichment
                    icePayload.setType(PayloadType.Enrichment);
                    icePayload.setLabel(name);
                    icePayload.setContentType(MimeTypeUtil.getMimeType(name));
                }
            }
        } else {
            String name = file.getName();
            Payload icePayload = object.createStoredPayload(name,
                    new FileInputStream(file));
            icePayload.setType(PayloadType.Enrichment);
            icePayload.setLabel(name);
            icePayload.setContentType(MimeTypeUtil.getMimeType(name));
        }
        return object;
    }

    private File render(File sourceFile) throws TransformerException {
        log.info("Converting {}...", sourceFile);
        String filename = sourceFile.getName();
        String basename = FilenameUtils.getBaseName(filename);
        int status = HttpStatus.SC_OK;
        Map<String, JsonConfigHelper> resizeConfig = config
                .getJsonMap("transformer/ice2/resize");
        String resizeJson = "";
        for (String key : resizeConfig.keySet()) {
            JsonConfigHelper j = resizeConfig.get(key);
            resizeJson += "\"" + key + "\":" + j.toString() + ",";
        }

        PostMethod post = new PostMethod(convertUrl);
        try {
            Part[] parts = {
                    new StringPart("zip", "on"),
                    new StringPart("dc", "on"),
                    new StringPart("toc", "on"),
                    new StringPart("pdflink", "on"),
                    new StringPart("pathext", ""),
                    new StringPart("template", getTemplate()),
                    new StringPart("multipleImageOptions", "{"
                            + StringUtils.substringBeforeLast(resizeJson, ",")
                            + "}"), new StringPart("mode", "download"),
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

    private String get(String key) {
        return config.get("transformer/ice2/" + key);
    }

    @Override
    public void shutdown() throws PluginException {
    }
}
