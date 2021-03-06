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
import java.util.Set;
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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginDescription;
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
import au.edu.usq.fascinator.common.sax.SafeSAXReader;
import au.edu.usq.fascinator.common.storage.StorageUtils;

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

    /** Logging **/
    private Logger log = LoggerFactory.getLogger(Ice2Transformer.class);

    /** System config file **/
    private JsonConfig config;

    /** Item config file */
    private JsonConfigHelper itemConfig;

    /** ICE rendition output directory **/
    private File outputDir;

    /** ICE service url **/
    private String convertUrl;

    /** For html parsing **/
    private SafeSAXReader reader;

    /** For making sure the ICE thumbnail/preview is used **/
    private Boolean priority;

    /** Default zip mime type **/
    private static final String ZIP_MIME_TYPE = "application/zip";

    private static final String HTML_MIME_TYPE = "text/html";

    private static final String IMG_MIME_TYPE = "image/";

    /** Flag for first execution */
    private boolean firstRun = true;

    /** Exclude these file from renditions */
    private List<String> excludeList;

    /**
     * ICE transformer constructor
     */
    public Ice2Transformer() {
    }

    /**
     * Init method to initialise ICE transformer
     * 
     * @param jsonFile
     * @throws IOException
     * @throws PluginException
     */
    @Override
    public void init(File jsonFile) throws PluginException {
        try {
            config = new JsonConfig(jsonFile);
            reset();
        } catch (IOException ioe) {
            throw new PluginException(ioe);
        }
    }

    /**
     * Init method to initialise ICE transformer
     * 
     * @param jsonString
     * @throws IOException
     * @throws PluginException
     */
    @Override
    public void init(String jsonString) throws PluginException {
        try {
            config = new JsonConfig(jsonString);
            reset();
        } catch (IOException ioe) {
            throw new PluginException(ioe);
        }
    }

    /**
     * Reset the transformer in preparation for a new object
     */
    private void reset() throws TransformerException {
        if (firstRun) {
            firstRun = false;
            // Output directory
            String outputPath = config.get("outputPath");
            if (outputPath == null) {
                throw new TransformerException("Output path not specified!");
            }
            outputDir = new File(outputPath);
            outputDir.mkdirs();

            // Rendition exclusions
            excludeList = Arrays.asList(StringUtils.split(config
                    .get("excludeRenditionExt"), ','));

            // Conversion Service URL
            convertUrl = config.get("url");
            if (convertUrl == null) {
                throw new TransformerException("No ICE URL provided!");
            }
        }

        // Priority
        priority = Boolean.parseBoolean(get("priority", "true"));

        // Clear the old SAX reader
        reader = new SafeSAXReader();
    }

    /**
     * Transform method
     * 
     * @param object : DigitalObject to be transformed
     * @return transformed DigitalObject
     * @throws TransformerException
     * @throws StorageException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    @Override
    public DigitalObject transform(DigitalObject object, String jsonConfig)
            throws TransformerException {
        try {
            itemConfig = new JsonConfigHelper(jsonConfig);
        } catch (IOException ex) {
            throw new TransformerException("Invalid configuration! '{}'", ex);
        }

        // Purge old data - after itemConfig is set
        reset();

        String sourceId = object.getSourceId();
        String ext = FilenameUtils.getExtension(sourceId);
        String fileName = FilenameUtils.getBaseName(sourceId);

        // Cache the file out of storage
        File file;
        try {
            file = new File(outputDir, sourceId);
            FileOutputStream out = new FileOutputStream(file);
            // Payload from storage
            Payload payload = object.getPayload(sourceId);
            // Copy and close
            IOUtils.copy(payload.open(), out);
            payload.close();
            out.close();
        } catch (IOException ex) {
            log.error("Error writing temp file : ", ex);
            return object;
            // throw new TransformerException(ex);
        } catch (StorageException ex) {
            log.error("Error accessing storage data : ", ex);
            return object;
            // throw new TransformerException(ex);
        }

        if (file.exists() && !excludeList.contains(ext.toLowerCase())) {
            try {
                if (isSupported(file)) {
                    File outputFile = render(file);
                    outputFile.deleteOnExit();
                    object = createIcePayload(object, outputFile);
                    outputFile.delete();
                }
            } catch (TransformerException te) {
                log.debug("Adding error payload to {}", object.getId());
                try {
                    object = createErrorPayload(object, fileName, te
                            .getMessage());
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
        try {
            object.close();
        } catch (StorageException ex) {
            log.error("Failed writing object metadata", ex);
        }
        if (file.exists()) {
            file.delete();
        }
        return object;
    }

    /**
     * Create Payload method for ICE Error
     * 
     * @param object : DigitalObject that store the payload
     * @param file : File to be stored as payload
     * @param message : Error message
     * @return transformed DigitalObject
     * @throws StorageException
     * @throws UnsupportedEncodingException
     */
    public DigitalObject createErrorPayload(DigitalObject object, String file,
            String message) throws StorageException,
            UnsupportedEncodingException {
        String name = file + "_ice_error.htm";
        Payload errorPayload = StorageUtils.createOrUpdatePayload(object, name,
                new ByteArrayInputStream(message.getBytes("UTF-8")));
        errorPayload.setType(PayloadType.Error);
        errorPayload.setLabel("ICE conversion errors");
        errorPayload.setContentType("text/html");
        return object;
    }

    /**
     * Create Payload method for ICE rendition files
     * 
     * @param object : DigitalObject that store the payload
     * @param file : File to be stored as payload
     * @return transformed DigitalObject
     * @throws StorageException
     * @throws IOException
     */
    public DigitalObject createIcePayload(DigitalObject object, File file)
            throws StorageException, IOException {
        String previewPayloadId = "";
        if (ZIP_MIME_TYPE.equals(MimeTypeUtil.getMimeType(file))) {
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    String mimeType = MimeTypeUtil.getMimeType(name);
                    // log.info("(ZIP) Name : '" + name + "', MimeType : '" +
                    // mimeType + "'");
                    InputStream in = zipFile.getInputStream(entry);

                    // If this is a HTML document we need to strip it down
                    // to the 'body' tag and replace with a 'div'
                    if (mimeType.equals(HTML_MIME_TYPE)) {
                        try {
                            log.debug("Stripping unnecessary HTML");
                            // Parse the document
                            Document doc = reader.loadDocumentFromStream(in);
                            // Alter the body node
                            Node node = doc
                                    .selectSingleNode("//*[local-name()='body']");
                            node.setName("div");
                            // Write out the new 'document'
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            reader.docToStream(node, out);
                            // Prep our inputstream again
                            in = new ByteArrayInputStream(out.toByteArray());
                        } catch (DocumentException ex) {
                            createErrorPayload(object, name, ex.getMessage());
                            continue;
                        } catch (Exception ex) {
                            log.error("Error : ", ex);
                            continue;
                        }
                    }

                    Payload icePayload = StorageUtils.createOrUpdatePayload(
                            object, name, in);
                    icePayload.setLabel(name);
                    icePayload.setContentType(mimeType);
                    if (mimeType.equals(HTML_MIME_TYPE)
                            || (mimeType.contains(IMG_MIME_TYPE) && name
                                    .contains("_preview"))) {
                        previewPayloadId = icePayload.getId();
                        // icePayload.setType(PayloadType.Preview);
                    }
                    if (mimeType.contains(IMG_MIME_TYPE)
                            && name.contains("_thumbnail.jpg")) {
                        icePayload.setType(PayloadType.Thumbnail);
                    }
                    icePayload.close();
                }
            }
            zipFile.close();
        } else {
            String name = file.getName();
            String mimeType = MimeTypeUtil.getMimeType(name);
            // log.debug("Name : '" + name + "', MimeType : '" + mimeType +
            // "'");
            Payload icePayload = StorageUtils.createOrUpdatePayload(object,
                    name, new FileInputStream(file));
            icePayload.setLabel(name);
            icePayload.setContentType(mimeType);
            if (mimeType.equals(HTML_MIME_TYPE)
                    || (mimeType.contains(IMG_MIME_TYPE) && name
                            .contains("_preview"))) {
                previewPayloadId = icePayload.getId();
                // icePayload.setType(PayloadType.Preview);
            }
            if (mimeType.contains(IMG_MIME_TYPE)
                    && name.contains("_thumbnail.jpg")) {
                icePayload.setType(PayloadType.Thumbnail);
            }
            icePayload.close();
        }

        // Set preview Payload type to the specified payload
        // Check if preview Payload is exist
        Boolean hasPreview = false;
        String existingPreviewPayloadId = "";
        Set<String> payloadIds = object.getPayloadIdList();
        for (String payloadId : payloadIds) {
            Payload payload = object.getPayload(payloadId);
            if (payload.getType() == PayloadType.Preview) {
                hasPreview = true;
                existingPreviewPayloadId = payloadId;
            }
        }

        // If has preview payloads and payloads in ice transformer is
        // the priority, then set the existing payload to AltPreview
        Payload previewPayload = object.getPayload(previewPayloadId);
        if (hasPreview) {
            if (priority) {
                Payload existingPreviewPayload = object
                        .getPayload(existingPreviewPayloadId);
                existingPreviewPayload.setType(PayloadType.AltPreview);
                existingPreviewPayload.close();

                previewPayload.setType(PayloadType.Preview);
            } else {
                previewPayload.setType(PayloadType.AltPreview);
            }
        } else {
            previewPayload.setType(PayloadType.Preview);

        }
        previewPayload.close();

        return object;
    }

    /**
     * Main render method to send the file to ICE service
     * 
     * @param sourceFile : File to be rendered
     * @return file returned by ICE service
     * @throws TransformerException
     */
    private File render(File sourceFile) throws TransformerException {
        log.info("Converting {}...", sourceFile);
        String filename = sourceFile.getName();
        String basename = FilenameUtils.getBaseName(filename);
        String ext = FilenameUtils.getExtension(filename);
        int status = HttpStatus.SC_OK;

        // Grab our config
        Map<String, JsonConfigHelper> resizeConfig = itemConfig
                .getJsonMap("resize");
        if (resizeConfig == null || resizeConfig.size() == 0) {
            // Try system config instead
            resizeConfig = config.getJsonMap("resize");
            if (resizeConfig == null || resizeConfig.size() == 0) {
                throw new TransformerException(
                        "No resizing configuration found.");
            }
        }

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
                    new StringPart("addThumbnail", "on"),
                    new StringPart("pathext", ext),
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
            File outputFile = new File(outputDir, filename);
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

    /**
     * Check if the file extension is supported
     * 
     * @param sourceFile : File to be checked
     * @return True if it's supported, false otherwise
     * @throws TransformerException
     */
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

    /**
     * Get ICE template
     * 
     * @return ice template
     * @throws IOException
     */
    private String getTemplate() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(getClass().getResourceAsStream("/template.xhtml"), out);
        return out.toString("UTF-8");
    }

    /**
     * Get Transformer ID
     * 
     * @return id
     */
    @Override
    public String getId() {
        return "ice2";
    }

    /**
     * Get Transformer Name
     * 
     * @return name;
     */
    @Override
    public String getName() {
        return "ICE Transformer";
    }

    /**
     * Gets a PluginDescription object relating to this plugin.
     * 
     * @return a PluginDescription
     */
    @Override
    public PluginDescription getPluginDetails() {
        return new PluginDescription(this);
    }

    /**
     * Shut down the transformer plugin
     */
    @Override
    public void shutdown() throws PluginException {
    }

    /**
     * Get data from item JSON, falling back to system JSON if not found
     *
     * @param key path to the data in the config file
     * @return String containing the config data
     */
    private String get(String key) {
        return get(key, null);
    }

    /**
     * Get data from item JSON, falling back to system JSON, then to provided
     * default value if not found
     * 
     * @param json Config object containing the json data
     * @param key path to the data in the config file
     * @param value default to use if not found
     * @return String containing the config data
     */
    private String get(String key, String value) {
        String configEntry = null;
        if (itemConfig != null) {
            configEntry = itemConfig.get(key, null);
        }
        if (configEntry == null) {
            configEntry = config.get(key, value);
        }
        return configEntry;
    }
}
