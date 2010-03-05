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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;

/**
 * NOTE: Could not perform the test to talk to ffmpeg directly as ffmpeg might
 * not be installed locally
 * 
 * @author Linda Octalina
 * 
 */

public class FfmpegTransformerTest {
    private GenericDigitalObject testObject;
    private DigitalObject testObjectOutput;
    private FfmpegTransformer ffmpeg = new FfmpegTransformer();

    @Test
    public void testSingleFile() throws URISyntaxException, StorageException,
            IOException {
        File audioFile = new File(getClass().getResource("/african_drum.aif")
                .toURI());
        testObject = new GenericDigitalObject(audioFile.getAbsolutePath());
        testObjectOutput = ffmpeg.createFfmpegPayload(testObject, audioFile);
        Set<String> payloads = testObjectOutput.getPayloadIdList();
        Assert.assertEquals(1, payloads.size());
        Payload ffmpegPayload = testObjectOutput.getPayload("african_drum.aif");
        Assert.assertEquals(ffmpegPayload.getId(), "african_drum.aif");
        Assert.assertEquals(ffmpegPayload.getLabel(), "african_drum.aif");
        Assert.assertEquals(ffmpegPayload.getType(), PayloadType.Enrichment);
        Assert.assertEquals(ffmpegPayload.getContentType(),
                "application/octet-stream");
    }

    @Test
    public void testErrorFile() throws URISyntaxException, StorageException,
            UnsupportedEncodingException, FileNotFoundException {
        File audioFile = new File(getClass().getResource("/african_drum.aif")
                .toURI());
        testObject = new GenericDigitalObject(audioFile.getAbsolutePath());
        testObjectOutput = ffmpeg.createFfmpegErrorPayload(testObject,
                audioFile, "This is error messasge");
        Set<String> payloads = testObjectOutput.getPayloadIdList();
        Assert.assertEquals(1, payloads.size());
        Payload ffmpegPayload = testObjectOutput
                .getPayload("african_drum_ffmpeg_error.htm");
        Assert.assertEquals(ffmpegPayload.getId(),
                "african_drum_ffmpeg_error.htm");
        Assert.assertEquals(ffmpegPayload.getLabel(),
                "FFMPEG conversion errors");
        Assert.assertEquals(ffmpegPayload.getType(), PayloadType.Error);
        Assert.assertEquals(ffmpegPayload.getContentType(), "text/html");
    }

}
