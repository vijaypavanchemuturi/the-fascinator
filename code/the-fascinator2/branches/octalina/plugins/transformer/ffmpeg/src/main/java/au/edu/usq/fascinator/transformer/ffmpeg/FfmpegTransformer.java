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
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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

    private static final String DEFAULT_OUTPUT_EXT = "flv";

    private static final String DEFAULT_OUTPUT_PATH = System
            .getProperty("user.home")
            + File.separator + ".fascinator" + File.separator + "ffmpeg-output";

    private static final String DEFAULT_THUMBNAIL_SIZE = "160x120";

    private static final String DEFAULT_EXCLUDE_EXT = "flv,m4a,mp3";

    private static final String DEFAULT_PARAMS = "-f flv -b 400 -ab 48"
            + " -ar 22050 -ac 1 -s 400x224 -aspect 16:9";

    private Logger log = LoggerFactory.getLogger(FfmpegTransformer.class);

    private JsonConfig config;

    private String outputPath;

    private Ffmpeg ffmpeg;

    @Override
    public DigitalObject transform(DigitalObject object)
            throws TransformerException {
        File file = new File(object.getId());
        ffmpeg = new Ffmpeg(get("executable", Ffmpeg.DEFAULT_EXECUTABLE));
        if (file.exists() && ffmpeg.isAvailable()) {
            outputPath = get("outputPath", DEFAULT_OUTPUT_PATH);
            File outputDir = new File(outputPath);
            outputDir.mkdirs();
            GenericDigitalObject ffmpegObject = new GenericDigitalObject(object);
            try {
                FfmpegInfo info = ffmpeg.getInfo(file);
                log.debug("{}: {}", file.getName(), info);
                if (info.isSupported()) {
                    if (info.hasVideo()) {
                        File thumbnail = getThumbnail(file, info.getDuration());
                        ffmpegObject.addPayload(new FfmpegPayload(thumbnail));
                    }
                    String ext = FilenameUtils.getExtension(file.getName());
                    List<String> excludeList = Arrays.asList(StringUtils.split(
                            get("excludeExt", DEFAULT_EXCLUDE_EXT), ','));
                    if (!excludeList.contains(ext.toLowerCase())
                            && (info.hasVideo() || info.hasAudio())) {
                        File converted = convert(file);
                        ffmpegObject.addPayload(new FfmpegPayload(converted));
                    }
                    return ffmpegObject;
                }
            } catch (Exception e) {
                log.error("Conversion failed for " + file, e);
                ffmpegObject.addPayload(new FfmpegErrorPayload(file, e));
                return ffmpegObject;
            }
        }
        return object;
    }

    private File getThumbnail(File sourceFile, long duration)
            throws TransformerException {
        log.info("Creating thumbnail...");
        String basename = FilenameUtils.getBaseName(sourceFile.getName());
        File outputFile = new File(outputPath, basename + "_thumbnail.jpg");
        if (outputFile.exists()) {
            FileUtils.deleteQuietly(outputFile);
        }
        try {
            List<String> params = new ArrayList<String>();
            params.add("-i");
            params.add(sourceFile.getAbsolutePath()); // input file
            // get random frame from first quarter of video
            params.add("-y"); // overwrite output file
            params.add("-deinterlace");
            params.add("-an"); // disable audio
            params.add("-ss");
            long start = (long) (Math.random() * duration * 0.25);
            params.add(Long.toString(start)); // start time offset
            params.add("-t");
            params.add("00:00:01"); // duration
            params.add("-r");
            params.add("1"); // frame rate
            params.add("-s");
            params.add(get("thumbnailSize", DEFAULT_THUMBNAIL_SIZE)); // size
            params.add("-vcodec");
            params.add("mjpeg");
            params.add("-f");
            params.add("mjpeg"); // mjpeg output format
            params.add(outputFile.getAbsolutePath()); // output file
            String stderr = ffmpeg.executeAndWait(params);
            log.debug(stderr);
            log.info("Thumbnail created: outputFile={}", outputFile);
        } catch (IOException ioe) {
            log.error("Failed to create thumbnail!", ioe);
            throw new TransformerException(ioe);
        }
        return outputFile;
    }

    private File convert(File sourceFile) throws TransformerException {
        String outputExt = get("outputExt", DEFAULT_OUTPUT_EXT);
        log.info("Converting to {}: {}", outputExt, sourceFile);
        String filename = sourceFile.getName();
        String basename = FilenameUtils.getBaseName(filename);
        File outputFile = new File(outputPath, basename + "." + outputExt);
        if (outputFile.exists()) {
            FileUtils.deleteQuietly(outputFile);
        }
        try {
            List<String> params = new ArrayList<String>();
            params.add("-i");
            params.add(sourceFile.getAbsolutePath()); // input file
            params.add("-y"); // overwrite output file
            // load extension specific parameters or use defaults if not found
            String configParams = get("params/default", DEFAULT_PARAMS);
            String ext = FilenameUtils.getExtension(filename);
            if (!"".equals(ext)) {
                log.debug("Loading params for {}...", ext);
                configParams = get("params/" + ext, configParams);
            }
            params.addAll(Arrays.asList(StringUtils.split(configParams, ' ')));
            params.add(outputFile.getAbsolutePath()); // output file
            String stderr = ffmpeg.executeAndWait(params);
            log.debug(stderr);
            log.info("Conversion successful: outputFile={}", outputFile);
        } catch (IOException ioe) {
            log.error("Failed to convert!", ioe);
            throw new TransformerException(ioe);
        }
        return outputFile;
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

    @Override
    public void shutdown() throws PluginException {
    }
}
