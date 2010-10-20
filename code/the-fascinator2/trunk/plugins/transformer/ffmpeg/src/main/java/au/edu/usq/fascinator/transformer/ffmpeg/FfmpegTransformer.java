/*
 * The Fascinator - Plugin - Transformer - FFMPEG
 * Copyright (C) 2010 University of Southern Queensland
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
package au.edu.usq.fascinator.transformer.ffmpeg;

import au.edu.usq.fascinator.api.PluginDescription;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.api.transformer.Transformer;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.MimeTypeUtil;
import au.edu.usq.fascinator.common.storage.StorageUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts audio and video media to web friendly versions using the FFMPEG
 * library.
 * 
 * @author Oliver Lucido, Linda Octalina
 */
public class FfmpegTransformer implements Transformer {

    /** Error Payload */
    private static String ERROR_PAYLOAD = "ffmpegErrors.json";

    /** Metadata Payload */
    private static String METADATA_PAYLOAD = "ffmpeg.info";

    /** Logger */
    private Logger log = LoggerFactory.getLogger(FfmpegTransformer.class);

    /** System config file */
    private JsonConfigHelper config;

    /** Item config file */
    private JsonConfigHelper itemConfig;

    /** FFMPEG output directory */
    private File outputDir;

    /** Ffmpeg class for conversion */
    private Ffmpeg ffmpeg;

    /** Flag for first execution */
    private boolean firstRun = true;

    /** Object format */
    private String format;

    /** Parsed media info */
    private FfmpegInfo info;

    /** Metadata storage */
    private Map<String, JsonConfigHelper> metadata;

    /** Error messages */
    private Map<String, JsonConfigHelper> errors;

    /**
     * Basic constructor
     * 
     */
    public FfmpegTransformer() {
        // Need a default constructor for ServiceLoader
    }

    /**
     * Instantiate the transformer with an existing instantiation of Ffmpeg
     * 
     * @param ffmpeg already instaniated ffmpeg installation
     */
    public FfmpegTransformer(Ffmpeg ffmpeg) {
        this.ffmpeg = ffmpeg;
    }

    /**
     * Init method to initialise Ffmpeg transformer
     * 
     * @param jsonFile
     * @throws IOException
     * @throws PluginException
     */
    @Override
    public void init(File jsonFile) throws PluginException {
        try {
            config = new JsonConfigHelper(jsonFile);
            reset();
        } catch (IOException ioe) {
            throw new PluginException(ioe);
        }
    }

    /**
     * Init method to initialise Ffmpeg transformer
     * 
     * @param jsonString
     * @throws IOException
     * @throws PluginException
     */
    @Override
    public void init(String jsonString) throws PluginException {
        try {
            config = new JsonConfigHelper(jsonString);
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
            testExecLevel();

            // Prep output area
            String outputPath = config.get("outputPath");
            outputDir = new File(outputPath);
            outputDir.mkdirs();

            // Set system variable for presets location
            String presetsPath = config.get("presetsPath");
            if (presetsPath != null) {
                File presetDir = new File(presetsPath);
                // Make sure it's valid
                if (presetDir.exists() && presetDir.isDirectory()) {
                    // And let FFmpeg know about it
                    ffmpeg.setEnvironmentVariable("FFMPEG_DATADIR",
                            presetDir.getAbsolutePath());
                } else {
                    log.error("Invalid FFmpeg presets path provided: '{}'",
                            presetsPath);
                }
            }
        }

        itemConfig = null;
        info = null;
        format = null;
        errors = new LinkedHashMap();
        metadata = new LinkedHashMap();
    }

    /**
     * Add an error to the list of errors for this pass through the transformer
     *
     * @param index: The index to use in the Map
     * @param message: The error message to record
     */
    private void addError(String index, String message) {
        // Sanity check
        if (message == null || index == null) {
            return;
        }
        // Drop to the log files too
        log.error(message);
        // Create JSON version of the message
        JsonConfigHelper msg = new JsonConfigHelper();
        msg.set("/message", message);
        addError(index, msg);
    }

    /**
     * Add an error to the list of errors for this pass through the transformer.
     * This method also accepts an exception object.
     *
     * @param index: The index to use in the Map
     * @param message: The error message to record
     * @param ex: The
     */
    private void addError(String index, String message, Exception ex) {
        // Sanity check
        if (message == null || index == null) {
            return;
        }
        // Drop to the log files too
        log.error(message, ex);
        // Create JSON version of the message
        JsonConfigHelper msg = new JsonConfigHelper();
        msg.set("/message", message);
        if (ex != null) {
            msg.set("/exception", ex.getMessage());
            // Turn the stacktrace into a string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            msg.set("/stacktrace", sw.toString());
        }
        addError(index, msg);
    }

    /**
     * Add an error to the list of errors for this pass through the transformer
     *
     * @param index: The index to use in the Map
     * @param message: The error message to record
     */
    private void addError(String index, JsonConfigHelper message) {
        // Sanity check
        if (message == null || index == null) {
            return;
        }
        // Avoid overwriting the index if re-used
        if (errors.containsKey(index)) {
            int inc = 2;
            while (errors.containsKey(index + "_" + inc)) {
                inc++;
            }
            index = index + "_" + inc;
        }
        // Store the error
        errors.put(index, message);
    }

    /**
     * Test the level of functionality available on this system
     * 
     * @return String indicating the level of available functionality
     */
    private String testExecLevel() {
        // Make sure we can start
        if (ffmpeg == null) {
            ffmpeg = new FfmpegImpl(get(config, "binaries/transcoding"),
                    get(config, "binaries/metadata"));
        }
        return ffmpeg.testAvailability();
    }

    /**
     * Transforming digital object method
     * 
     * @params object: DigitalObject to be transformed
     * @return transformed DigitalObject after transformation
     * @throws TransformerException if the transformation fails
     */
    @Override
    public DigitalObject transform(DigitalObject object, String jsonConfig)
            throws TransformerException {
        if (testExecLevel() == null) {
            log.error("FFmpeg is either not installed, or not executing!");
            return object;
        }
        // Purge old data
        reset();

        try {
            itemConfig = new JsonConfigHelper(jsonConfig);
        } catch (IOException ex) {
            throw new TransformerException("Invalid configuration! '{}'", ex);
        }

        // Find the format 'group' this file is in
        String sourceId = object.getSourceId();
        String ext = FilenameUtils.getExtension(sourceId);
        format = getFormat(ext);

        // Return now if this isn't a format we care about
        if (format == null) {
            return object;
        }
        //log.debug("Supported format found: '{}' => '{}'", ext, format);

        // Cache the file from storage
        File file;
        try {
            file = new File(outputDir, sourceId);
            FileOutputStream tempFileOut = new FileOutputStream(file);
            // Payload from storage
            Payload payload = object.getPayload(sourceId);
            // Copy and close
            IOUtils.copy(payload.open(), tempFileOut);
            payload.close();
            tempFileOut.close();
        } catch (IOException ex) {
            addError(sourceId, "Error writing temp file", ex);
            errorAndClose(object);
            return object;
        } catch (StorageException ex) {
            addError(sourceId, "Error accessing storage data", ex);
            errorAndClose(object);
            return object;
        }
        if (!file.exists()) {
            addError(sourceId, "Unknown error writing cache: does not exist");
            errorAndClose(object);
            return object;
        }

        // **************************************************************
        // From here on we know (assume) that we SHOULD be able to support
        //  this object, so errors can't just throw exceptions. We should
        //  only return under certain circumstances (ie. not just because
        //  one rendition fails), and the object must get closed.
        // **************************************************************

        // Check for a custom display type
        String display = get(itemConfig, "displayTypes/" + format);
        if (display != null) {
            try {
                Properties prop = object.getMetadata();
                prop.setProperty("displayType", display);
            } catch (StorageException ex) {
                addError("display", "Could not access object metadata", ex);
            }
        }

        // Gather metadata
        try {
            info = ffmpeg.getInfo(file);
        } catch (IOException ex) {
            addError("metadata", "Error accessing metadata", ex);
            errorAndClose(object);
            return object;
        }

        // Can we even process this file?
        if (!info.isSupported()) {
            closeObject(object);
            return object;
        }

        // What conversions are required for this format?
        List<JsonConfigHelper> conversions = getJsonList(itemConfig,
                "transcodings/" + format);
        for (JsonConfigHelper conversion : conversions) {
            String name = conversion.get("alias");
            // And what/how many renditions does it have?
            List<JsonConfigHelper> renditions =
                    conversion.getJsonList("renditions");
            if (renditions == null || renditions.isEmpty()) {
                addError("transcodings", "Invalid or missing transcoding data:"
                        + " '/transcodings/" + format + "'");
            } else {
                // Config look valid, lets give it a try
                //log.debug("Starting renditions for '{}'", name);
                for (JsonConfigHelper render : renditions) {
                    File converted = null;
                    // Render the output
                    try {
                        converted = convert(file, render, info);
                    } catch (Exception ex) {
                        String outputFile = render.get("name");
                        if (outputFile != null) {
                            addError(jsonKey(outputFile),
                                    "Error converting file", ex);
                        } else {
                            // Couldn't read the config for a name
                            addError("unknown", "Error converting file", ex);
                        }
                    }

                    // Now store the output if valid
                    if (converted != null) {
                        try {
                            Payload payload = createFfmpegPayload(object,
                                    converted);
                            // TODO: Type checking needs more work
                            // Indexing fails silently if you add two thumbnails
                            // or two previews
                            payload.setType(resolveType(render.get("type")));
                            payload.close();
                        } catch (Exception ex) {
                            addError(jsonKey(converted.getName()),
                                    "Error storing output", ex);
                        } finally {
                            converted.delete();
                        }
                    }

                }
            }
        }

        // Write metadata to storage
        if (compileMetadata(object)) {
            // Close normally
            closeObject(object);
        } else {
            // Close with some errors
            errorAndClose(object);
        }
        // Cleanup
        if (file.exists()) {
            file.delete();
        }
        return object;
    }

    /**
     * Find and return the payload type to use from the input string
     *
     * @param type The input string to resolve to a proper type
     * @return PayloadType The properly enumerated type to use
     */
    private PayloadType resolveType(String type) {
        // Invalid data
        if (type == null) {
            return PayloadType.Enrichment;
        }

        try {
            PayloadType pt = PayloadType.valueOf(type);
            // Valid data
            return pt;
        } catch (Exception ex) {
            // Unmatched data
            return PayloadType.Enrichment;
        }
    }

    /**
     * Determine the format group use in transcoding
     *
     * @param extension The file extension
     * @param String The format group, NULL if not found
     */
    private String getFormat(String extension) {
        List<JsonConfigHelper> formatList =
                config.getJsonList("supportedFormats");
        for (JsonConfigHelper json : formatList) {
            String group = json.get("group");
            List<String> extensions = getList(json, "extensions");
            for (String ext : extensions) {
                if (extension.equalsIgnoreCase(ext)) {
                    return group;
                }
            }
        }
        return null;
    }

    /**
     * Try to create an error payload on the object and close it
     * 
     * @param object to close
     */
    private void errorAndClose(DigitalObject object) {
        try {
            createFfmpegErrorPayload(object);
        } catch (Exception ex) {
            JsonConfigHelper content = new JsonConfigHelper();
            content.setJsonMap("/", errors);
            log.error("Unable to write error payload, {}", ex);
            log.error("Errors:\n{}", content.toString());
        } finally {
            closeObject(object);
        }
    }

    /**
     * Try to close the object
     * 
     * @param object to close
     */
    private void closeObject(DigitalObject object) {
        try {
            object.close();
        } catch (StorageException ex) {
            log.error("Failed writing object metadata", ex);
        }
    }

    /**
     * Compile all available metadata and add to object.
     *
     * Note that a False response indicates data has been written into the
     * 'errors' map and should be followed by an 'errorAndClose()' call.
     *
     * @return: boolean: False if any errors occurred, True otherwise
     */
    private boolean compileMetadata(DigitalObject object) {
        // Nothing to do
        if (info == null) {
            return true;
        }
        if (!info.isSupported()) {
            return true;
        }

        // Get base metadata of source
        JsonConfigHelper fullMetadata = null;
        try {
            fullMetadata = new JsonConfigHelper(info.toString());
        } catch (IOException ex) {
            addError("metadata", "Error parsing metadata output", ex);
            return false;
        }

        // Add individual conversion(s) metadata
        if (!metadata.isEmpty()) {
            fullMetadata.setJsonMap("outputs", metadata);
        }

        // Write the file to disk
        File metaFile;
        try {
            //log.debug("\nMetadata:\n{}", fullMetadata.toString());
            metaFile = writeMetadata(fullMetadata.toString());
            if (metaFile == null) {
                addError("metadata", "Unknown error extracting metadata");
                return false;
            }
        } catch (TransformerException ex) {
            addError("metadata", "Error writing metadata to disk", ex);
            return false;
        }

        // Store metadata
        try {
            Payload payload = createFfmpegPayload(object, metaFile);
            payload.setType(PayloadType.Enrichment);
            payload.close();
        } catch (Exception ex) {
            addError("metadata", "Error storing metadata payload", ex);
            return false;
        } finally {
            metaFile.delete();
        }

        // Everything should be fine if we got here
        return true;
    }

    /**
     * Create ffmpeg error payload
     * 
     * @param object : DigitalObject to store the payload
     * @return Payload the error payload
     * @throws FileNotFoundException if the file provided does not exist
     * @throws UnsupportedEncodingException for encoding errors in the message
     */
    public Payload createFfmpegErrorPayload(DigitalObject object)
            throws StorageException, FileNotFoundException,
            UnsupportedEncodingException {
        // Compile our error data
        JsonConfigHelper content = new JsonConfigHelper();
        content.setJsonMap("/", errors);
        log.debug("\nErrors:\n{}", content.toString());
        InputStream  data = new ByteArrayInputStream(
                content.toString().getBytes("UTF-8"));
        // Write to the object
        Payload payload = StorageUtils.createOrUpdatePayload(object,
                ERROR_PAYLOAD, data);
        payload.setType(PayloadType.Error);
        payload.setContentType("application/json");
        payload.setLabel("FFMPEG conversion errors");
        return payload;
    }

    /**
     * Create converted ffmpeg payload
     * 
     * @param object DigitalObject to store the payload
     * @param file File to be stored as payload
     * @return Payload new payload
     * @throws StorageException if there is a problem trying to store
     * @throws FileNotFoundException if the file provided does not exist
     */
    public Payload createFfmpegPayload(DigitalObject object, File file)
            throws StorageException, FileNotFoundException {
        String name = file.getName();
        Payload payload = StorageUtils.createOrUpdatePayload(object, name,
                new FileInputStream(file));
        payload.setContentType(MimeTypeUtil.getMimeType(name));
        payload.setLabel(name);
        return payload;
    }

    /**
     * Write FFMPEG metadata to disk
     * 
     * @param data : Extracted metadata
     * @return File : Containing metadata
     * @throws TransformerException if the write failed
     */
    private File writeMetadata(String data) throws TransformerException {
        File outputFile = new File(outputDir, METADATA_PAYLOAD);
        if (outputFile.exists()) {
            FileUtils.deleteQuietly(outputFile);
        }
        try {
            outputFile.createNewFile();
            FileUtils.writeStringToFile(outputFile, data, "utf-8");
        } catch (IOException ioe) {
            throw new TransformerException(ioe);
        }
        return outputFile;
    }

    /**
     * Convert audio/video to required output(s)
     * 
     * @param sourceFile : The file to be converted
     * @param render : Configuration to use during the render
     * @param info : Parsed metadata about the source
     * @return File containing converted media
     * @throws TransformerException if the conversion failed
     */
    private File convert(File sourceFile, JsonConfigHelper render,
            FfmpegInfo info) throws TransformerException {

        // Prepare the output location
        String outputName = render.get("name");
        if (outputName == null) return null;
        File outputFile = new File(outputDir, outputName);
        if (outputFile.exists()) {
            FileUtils.deleteQuietly(outputFile);
        }
        log.info("Converting '{}': '{}'",
                sourceFile.getName(), outputFile.getName());

        // Get metadata ready
        JsonConfigHelper renderMetadata = new JsonConfigHelper();
        String key = jsonKey(outputName);
        String formatString = render.get("formatMetadata");
        if (formatString != null) {
            renderMetadata.set("format", formatString);
        }

        try {
            List<String> params = new ArrayList<String>();
            // *************
            // 1) Input file
            // *************
            params.add("-i");
            // Quote to preserve spaces in filenames
            params.add(preserveSpaces(sourceFile.getAbsolutePath()));
            // Overwrite output file if it exists
            params.add("-y");

            // *************
            // 2) Configurable options
            // *************
            String optionStr = render.get("options", "");
            List<String> options = split(optionStr, " ");
            // Replace the offset placeholder now that we know the duration
            long start = 0;
            for (int i = 0; i < options.size(); i++) {
                String option = options.get(i);
                // If it even exists that is...
                if (option.equalsIgnoreCase("[[OFFSET]]")) {
                    start = (long) (Math.random() * info.getDuration() * 0.25);
                    options.set(i, Long.toString(start));
                }
            }
            // Merge option parameters into standard parameters
            if (!options.isEmpty()) {
                params.addAll(options);
            }

            // *************
            // 3) Video resolution / padding
            // *************
            String audioStr = render.get("audioOnly");
            boolean audio = Boolean.parseBoolean(audioStr);

            // Non-audio files need some resolution work
            if (!audio) {
                List<String> dimensions = getPaddedParams(render, info,
                        renderMetadata);
                if (dimensions == null || dimensions.isEmpty()) {
                    addError(key, "Error calculating dimensions");
                    return null;
                }
                // Merge resultion parameters into standard parameters
                params.addAll(dimensions);
            }

            // *************
            // 4) Output options
            // *************
            optionStr = render.get("output", "");
            options = split(optionStr, " ");
            // Merge option parameters into standard parameters
            if (!options.isEmpty()) {
                params.addAll(options);
            }
            params.add(preserveSpaces(outputFile.getAbsolutePath()));

            // *************
            // 5) All done. Perform the transcoding
            // *************
            String stderr = ffmpeg.transform(params);
            renderMetadata.set("debugOutput", stderr);
            if (outputFile.exists()) {
                long fileSize = outputFile.length();
                if (fileSize == 0) {
                    throw new TransformerException(
                            "File conversion failed!\n=====\n" + stderr);
                } else {
                    renderMetadata.set("size", String.valueOf(fileSize));
                }
            } else {
                throw new TransformerException(
                        "File conversion failed!\n=====\n" + stderr);
            }

            //log.debug("FFMPEG Output:\n=====\n\\/\\/\\/\\/\n{}/\\/\\/\\/\\\n=====\n", stderr);
        } catch (IOException ioe) {
            addError(key, "Failed to convert!", ioe);
            throw new TransformerException(ioe);
        }

        // On a multi-pass encoding we may be asked to
        //  throw away the video from some passes.
        if (outputFile.getName().contains("nullFile")) {
            return null;
        } else {
            // For anything else, record metadata
            metadata.put(key, renderMetadata);
        }
        return outputFile;
    }

    /**
     * Preserve spaces in command line parameters in an OS dependent fashion.
     *
     * @param input : The String to preserve
     * @return String : The resultant string
     */
    private String preserveSpaces(String input) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            // Windows, quote the string
            return "\"" + input + "\"";
        } else {
            // Everyone else, escape the spaces
            return input.replace(" ", "\\ ");
        }
    }

    /**
     * Build the list of configuration strings to use for the resolution and
     * padding required to match the desired output whilst maintaining the
     * aspect ratio.
     *
     * @param render : Configuration to use during the render
     * @param info : Parsed metadata about the source
     * @return List<String> : A list of parameters
     */
    private List<String> getPaddedParams(JsonConfigHelper renderConfig,
            FfmpegInfo info, JsonConfigHelper renderMetadata) {
        List<String> response = new ArrayList();

        // Get the output dimensions to use for the actual video
        int maxX = Integer.valueOf(renderConfig.get("maxWidth", "-1"));
        int maxY = Integer.valueOf(renderConfig.get("maxHeight", "-1"));
        String size = getSize(info.getWidth(), info.getHeight(), maxX, maxY);
        if (size == null) return null;

        // Validate the response before we calculate padding
        int i = size.indexOf("x");
        int x = makeEven(Integer.parseInt(size.substring(0, i)));
        int y = makeEven(Integer.parseInt(size.substring(i + 1)));
        String paddingConfig = renderConfig.get("padding", "none");
        String paddingColor = renderConfig.get("paddingColor", "black");

        // No padding is requested or we don't have both X and Y constraints...
        if (paddingConfig.equalsIgnoreCase("none")
                || maxX == -1 || maxY == -1) {
            // We're done
            response.add("-s");
            response.add(size);
            // Don't forget metadata
            renderMetadata.set("width", String.valueOf(x));
            renderMetadata.set("height", String.valueOf(y));
            return response;
        }

        // Anything else, we need to modify the response
        int padXleft = makeEven((maxX - x) / 2);
        int padXright = makeEven(maxX - x - padXleft);
        int padYtop = makeEven((maxY - y) / 2);
        int padYbottom = makeEven(maxY - y - padYtop);

        // Record overall dimensions
        String width = String.valueOf(padXleft + x + padXright);
        String height = String.valueOf(padYtop + y + padYbottom);
        renderMetadata.set("width", width);
        renderMetadata.set("height", height);
        // Debugging
        //log.debug("WIDTH: " + padXleft + " + " + x + " + " + padXright + " = " + width);
        //log.debug("HEIGHT: " + padYtop + " + " + y + " + " + padYbottom + " = " + height);

        // Older 'deprecated' builds use individual padding
        if (paddingConfig.equalsIgnoreCase("individual")) {
            response.add("-s");
            response.add(size);
            response.add("-padtop");
            response.add(String.valueOf(padYtop));
            response.add("-padbottom");
            response.add(String.valueOf(padYbottom));
            response.add("-padleft");
            response.add(String.valueOf(padXleft));
            response.add("-padright");
            response.add(String.valueOf(padXright));
            response.add("-padcolor");
            response.add(paddingColor);
            return response;
        }

        // Newer builds use a filter
        if (paddingConfig.equalsIgnoreCase("filter")) {
            String filter = "pad=" + // Type of filter
                    width + ":" +    // WIDTH
                    height + ":" +   // HEIGHT
                    padXleft + ":" + // X PAD : Right hand is calculated
                    padYtop + ":" +  // Y PAD : Bottom is calculated
                    paddingColor;    // Color
            response.add("-s");
            response.add(size);
            response.add("-vf");
            response.add(filter);
            return response;
        }

        // Fallback, assume no padding
        log.error("Invalid padding config found: '{}'", paddingConfig);
        response.add("-s");
        response.add(size);
        return response;
    }

    /**
     * Make sure the provided number is even, reducing if required. FFmpeg
     * only allows even numbers for frame sizes and padding. This is a simple
     * function to make that easier, since it is called a few times above.
     *
     * @param input : The input integer to verify
     * @return int : The verified integer
     */
    private int makeEven(int input) {
        // An odd number
        if (input % 2 == 1) {
            return input - 1;
        }
        return input;
    }

    /**
     * Compute and return the size string that allows the provided image to
     * fit within dimension constraints whilst respecting the aspect ratio.
     *
     * @param width : The width of the original
     * @param height : The height of the original
     * @param maxWidth : Width constraint of the output
     * @param maxHeight : Height constraint of the output
     * @return String : The computed size string
     */
    private String getSize(int width, int height, int maxWidth, int maxHeight) {
        // Calculate scaling for dimensions independently
        float scale = 0;
        float dX = maxWidth != -1 ? (float) width / (float) maxWidth : -1;
        float dY = maxWidth != -1 ? (float) height / (float) maxHeight : -1;

        // A) Scaling is constrained by height
        if (dY > dX) {
            scale = dY;
        } else {
            // B) Scaling is constrained by Width (or equal)
            if (dX != 0 && dX != -1) {
                scale = dX;
            // C) No scaling required
            } else {
                scale = 1;
            }
        }

        // Scale and round the numbers to return
        int newWidth = Math.round((float) width / scale);
        int newHeight = Math.round((float) height / scale);
        return newWidth + "x" + newHeight;
    }

    /**
     * Make a string safe to use as a JSON key
     *
     * @param input: The string to make safe
     * @return String: The modified string, safe to use in JSON
     */
    private String jsonKey(String input) {
        return input.replace(" ", "_");
    }

    /**
     * Get Transformer id
     * 
     * @return id
     */
    @Override
    public String getId() {
        return "ffmpeg";
    }

    /**
     * Get Transformer name
     * 
     * @return name
     */
    @Override
    public String getName() {
        return "FFMPEG Transformer";
    }

    /**
     * Gets a PluginDescription object relating to this plugin.
     * 
     * @return a PluginDescription
     */
    @Override
    public PluginDescription getPluginDetails() {
        PluginDescription pd = new PluginDescription(this);
        JsonConfigHelper details = new JsonConfigHelper();
        details.set("debug/availability", testExecLevel());

        pd.setMetadata(details.toString());
        return pd;
    }

    /**
     * Get a list of JSON configs from item JSON, falling back to system JSON
     * if not found
     *
     * @param json Config object containing the json data
     * @param key path to the data in the config file
     * @return List<JsonConfigHelper> containing the config data
     */
    private List<JsonConfigHelper> getJsonList(JsonConfigHelper json,
            String key) {
        // We can't tell the difference between NULL and empty from
        //  getJsonList(), so get the entry as a basic string first.
        String test = json.get(key);
        if (test == null) {
            // It doesn't exist in item config
            return config.getJsonList(key);
        }

        // We found SOMETHING in the item config, now let's get it properly
        return json.getJsonList(key);
    }

    /**
     * Get a list from item JSON, falling back to system JSON if not found
     *
     * @param json Config object containing the json data
     * @param key path to the data in the config file
     * @return List<String> containing the config data
     */
    private List<String> getList(JsonConfigHelper json, String key) {
        return getList(json, key, ",");
    }

    /**
     * Get a list from item JSON, falling back to system JSON if not found
     * 
     * @param json Config object containing the json data
     * @param key path to the data in the config file
     * @param separator The separator to use in splitting the string to a list
     * @return List<String> containing the config data
     */
    private List<String> getList(JsonConfigHelper json, String key,
            String separator) {
        String configEntry = get(json, key);
        if (configEntry == null) {
            return new ArrayList();
        } else {
            return split(configEntry, separator);
        }
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
    private String get(JsonConfigHelper json, String key, String value) {
        String configEntry = json.get(key, null);
        if (configEntry == null) {
            configEntry = config.get(key, value);
        }
        return configEntry;
    }

    /**
     * Get data from item JSON, falling back to system JSON if not found
     * 
     * @param json Config object containing the json data
     * @param key path to the data in the config file
     * @return String containing the config data
     */
    private String get(JsonConfigHelper json, String key) {
        String configEntry = json.get(key, null);
        if (configEntry == null) {
            configEntry = config.get(key, null);
        }
        return configEntry;
    }

    /**
     * Simple wrapper for commonly used function, use StrinUtils to split a
     * string in an array and then transform it into a list.
     *
     * @param original : The original string to split
     * @param separator : The separator to split on
     * @return List<String> : The resulting list of split strings
     */
    private List<String> split(String original, String separator) {
        return Arrays.asList(StringUtils.split(original, separator));
    }

    /**
     * Shut down the transformer plugin
     */
    @Override
    public void shutdown() throws PluginException {
    }
}
