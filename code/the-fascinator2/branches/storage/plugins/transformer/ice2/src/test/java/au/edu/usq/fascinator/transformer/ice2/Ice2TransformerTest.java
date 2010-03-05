/*
 * The Fascinator - Plugin - Transformer - ICE 2
 * Copyright (C) 2009 University of Southern Queensland
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
package au.edu.usq.fascinator.transformer.ice2;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
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
 * NOTE: Count not perform the test to talk to ICE as ICE service might not be
 * set locally
 * 
 * @author Linda Octalina
 * 
 */

public class Ice2TransformerTest {
    private GenericDigitalObject testObject;
    private DigitalObject testObjectOutput;
    // private Ice2Transformer tf = new Ice2Transformer("", "/tmp");
    private Ice2Transformer tf = new Ice2Transformer();

    @Test
    public void testZipFile() throws URISyntaxException, StorageException,
            IOException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {

        File zipFile = new File(getClass().getResource("/resources.zip")
                .toURI());

        testObject = new GenericDigitalObject(zipFile.getAbsolutePath());
        testObjectOutput = tf.createIcePayload(testObject, zipFile);
        // Commented line below is used if the createIcePayload method is set as
        // private
        // Class[] argClasses = { DigitalObject.class, File.class };
        // Object[] objectList = { testObject, zipFile };
        // Method method = tf.getClass().getDeclaredMethod("createIcePayload",
        // argClasses);
        // method.setAccessible(true);
        // method.invoke(null, objectList);
        Set<String> payloads = testObjectOutput.getPayloadIdList();
        Assert.assertEquals(2, payloads.size());
    }

    @Test
    public void testSingleFile() throws URISyntaxException, StorageException,
            IOException {
        File fileNameodp = new File(getClass().getResource("/presentation.odp")
                .toURI());
        testObject = new GenericDigitalObject(fileNameodp.getAbsolutePath());
        testObjectOutput = tf.createIcePayload(testObject, fileNameodp);
        Set<String> payloads = testObjectOutput.getPayloadIdList();
        Assert.assertEquals(1, payloads.size());
        Payload icePayload = testObjectOutput.getPayload("presentation.odp");
        Assert.assertEquals(icePayload.getId(), "presentation.odp");
        Assert.assertEquals(icePayload.getLabel(), "presentation.odp");
        Assert.assertEquals(icePayload.getType(), PayloadType.Enrichment);
        Assert.assertEquals(icePayload.getContentType(),
                "application/octet-stream");
    }

    @Test
    public void testErrorFile() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        File fileNameodp = new File(getClass().getResource("/presentation.odp")
                .toURI());
        testObject = new GenericDigitalObject(fileNameodp.getAbsolutePath());
        testObjectOutput = tf.createErrorPayload(testObject, fileNameodp,
                "This is error messasge");
        Set<String> payloads = testObjectOutput.getPayloadIdList();
        Assert.assertEquals(1, payloads.size());
        Payload icePayload = testObjectOutput
                .getPayload("presentation_error.htm");
        Assert.assertEquals(icePayload.getId(), "presentation_error.htm");
        Assert.assertEquals(icePayload.getLabel(), "ICE conversion errors");
        Assert.assertEquals(icePayload.getType(), PayloadType.Enrichment);
        Assert.assertEquals(icePayload.getContentType(), "text/html");
    }

}
