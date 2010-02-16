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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.transformer.Transformer;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;

/**
 * Converts audio and video media to web friendly versions using the FFMPEG
 * library.
 * 
 * @author Oliver Lucido
 */
public class FfmpegTransformer implements Transformer {

    private static final String DEFAULT_EXECUTABLE = "ffmpeg";

    private static final String DEFAULT_OUTPUT_PATH = System
            .getProperty("user.home")
            + File.separator + ".fascinator" + File.separator + "ffmpeg-output";

    private static final List<String> DEFAULT_PARAMS = Arrays
            .asList(new String[] { "-f", "flv", "-b", "128", "-ab", "16",
                    "-ar", "11025", "-ac", "1", "-s", "384x288", "-aspect",
                    "4:3" });

    private Logger log = LoggerFactory.getLogger(FfmpegTransformer.class);

    private JsonConfig config;

    private String outputPath;

    private String executable;

    @Override
    public DigitalObject transform(DigitalObject object)
            throws TransformerException {
        File file = new File(object.getId());
        executable = get("executable", DEFAULT_EXECUTABLE);
        if (file.exists() && isAvailable()) {
            outputPath = get("outputPath", DEFAULT_OUTPUT_PATH);
            File outputDir = new File(outputPath);
            outputDir.mkdirs();
            GenericDigitalObject ffmpegObject = new GenericDigitalObject(object);
            try {
                File thumbnailFile = getThumbnail(file);
                if (thumbnailFile != null) {
                    ffmpegObject.addPayload(new FfmpegPayload(thumbnailFile));
                    File convertedFile = convert(file);
                    ffmpegObject.addPayload(new FfmpegPayload(convertedFile));
                } else {
                    String errorId = FilenameUtils.getBaseName(file.getName())
                            + "_error.htm";
                    if (ffmpegObject.getPayload(errorId) == null) {
                        ffmpegObject.addPayload(new FfmpegErrorPayload(file,
                                "Preview currently not available"));
                    }
                }
                return ffmpegObject;
            } catch (Exception e) {
                e.printStackTrace();
                log.debug("Adding error payload to {}", object.getId());
                ffmpegObject.addPayload(new FfmpegErrorPayload(file, e
                        .getMessage()));
                return ffmpegObject;
            }
        }
        return object;
    }

    private File getThumbnail(File sourceFile) throws TransformerException {
        log.info("Creating thumbnail...");
        String basename = FilenameUtils.getBaseName(sourceFile.getName());
        File outputFile = new File(outputPath, basename + "_thumbnail.jpg");
        try {
            List<String> params = new ArrayList<String>();
            params.add("-i");
            params.add(sourceFile.getAbsolutePath()); // input file
            // get random frame from first quarter of video
            Process proc = execute(params);
            proc.waitFor();
            String stderr = IOUtils.toString(proc.getErrorStream());
            if (stderr.indexOf(": Unknown format") > -1) {
                log.info("Unknown source format");
                return null;
            } else {
                Pattern p = Pattern.compile("Duration: ((\\d+):(\\d+):(\\d+))");
                Matcher m = p.matcher(stderr);
                long start = 0;
                if (m.find()) {
                    long hrs = Long.parseLong(m.group(2)) * 3600;
                    long min = Long.parseLong(m.group(3)) * 60;
                    long sec = Long.parseLong(m.group(4));
                    long total = hrs + min + sec;
                    start = (long) (Math.random() * total * 0.25);
                }
                // create the thumbnail
                params.add("-deinterlace");
                params.add("-an"); // disable audio
                params.add("-ss");
                params.add(Long.toString(start)); // start time offset
                params.add("-t");
                params.add("00:00:01"); // duration
                params.add("-r");
                params.add("1"); // frame rate
                params.add("-y"); // overwrite output file
                params.add("-s");
                params.add(get("thumbnailSize", "160x120")); // frame size
                params.add("-vcodec");
                params.add("mjpeg");
                params.add("-f");
                params.add("mjpeg"); // mjpeg output format
                params.add(outputFile.getAbsolutePath()); // output file
                proc = execute(params);
                proc.waitFor();
                log.debug("stdout: " + IOUtils.toString(proc.getInputStream()));
                log.debug("stderr: " + IOUtils.toString(proc.getErrorStream()));
                log.info("Thumbnail created: outputFile={}", outputFile);
            }
        } catch (InterruptedException ie) {
            log.error("ffmpeg was interrupted!", ie);
            throw new TransformerException(ie);
        } catch (IOException ioe) {
            log.error("Failed to create thumbnail!", ioe);
            throw new TransformerException(ioe);
        }
        return outputFile;
    }

    private File convert(File sourceFile) throws TransformerException {
        log.info("Converting to FLV: {}", sourceFile);
        String filename = sourceFile.getName();
        String basename = FilenameUtils.getBaseName(filename);
        File outputFile = new File(outputPath, basename + ".flv");
        try {
            List<String> params = new ArrayList<String>();
            params.add("-i");
            params.add(sourceFile.getAbsolutePath()); // input file
            params.add("-y"); // overwrite output file
            // load extension specific parameters or use defaults if not found
            List<String> configParams = getList("params/default",
                    DEFAULT_PARAMS);
            String ext = FilenameUtils.getExtension(filename);
            if (!"".equals(ext)) {
                log.debug("Loading params for {}...", ext);
                configParams = getList("params/" + ext, configParams);
            }
            params.addAll(configParams);
            params.add(outputFile.getAbsolutePath()); // output file
            Process proc = execute(params);
            proc.waitFor();
            log.debug("stdout: " + IOUtils.toString(proc.getInputStream()));
            log.debug("stderr: " + IOUtils.toString(proc.getErrorStream()));
            log.info("Conversion successful: outputFile={}", outputFile);
        } catch (InterruptedException ie) {
            log.error("ffmpeg was interrupted!", ie);
            throw new TransformerException(ie);
        } catch (IOException ioe) {
            log.error("Failed to convert!", ioe);
            throw new TransformerException(ioe);
        }
        return outputFile;
    }

    private Process execute() throws IOException {
        List<String> noParams = Collections.emptyList();
        return execute(noParams);
    }

    private Process execute(List<String> params) throws IOException {
        List<String> cmd = new ArrayList<String>();
        cmd.add(executable);
        cmd.addAll(params);
        log.debug("Executing: {}", params);
        ProcessBuilder builder = new ProcessBuilder(cmd);
        return builder.start();
    }

    private boolean isAvailable() {
        boolean available = false;
        try {
            execute();
            available = true;
        } catch (IOException ioe) {
            log.warn("ffmpeg execute failed! searching system path...");
            File found = searchPathForExecutable(executable);
            if (found != null) {
                executable = found.getAbsolutePath();
                log.info("ffmpeg found at {}", executable);
                try {
                    execute();
                    available = true;
                } catch (IOException ioe2) {
                }
            }
            log.error("ffmpeg not available!");
        }
        return available;
    }

    private File searchPathForExecutable(String name) {
        if (System.getProperty("os.name").startsWith("Windows")
                && !name.endsWith(".exe")) {
            name += ".exe";
        }
        String[] dirs = System.getenv("PATH").split(File.pathSeparator);
        for (String dir : dirs) {
            File file = new File(dir, name);
            if (file.isFile()) {
                return file;
            }
        }
        return null;
    }

    @Override
    public String getId() {
        return "ffmpeg";
    }

    @Override
    public String getName() {
        return "FFMPEG Transformer";
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
        return config.get("transformer/ffmpeg/" + key, defaultValue);
    }

    private List<String> getList(String key, List<String> defaultValueList) {
        List<Object> objectList = config.getList("transformer/ffmpeg/" + key);
        if (!objectList.isEmpty()) {
            List<String> valueList = new ArrayList<String>();
            for (Object object : objectList) {
                valueList.add(object.toString());
            }
            return valueList;
        }
        return defaultValueList;
    }

    @Override
    public void shutdown() throws PluginException {
    }
}
