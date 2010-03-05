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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.testing.HttpTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.transformer.Transformer;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;

/**
 * NOTE: Could not perform the test to talk to ICE as ICE service might not be
 * set locally
 * 
 * @author Linda Octalina
 * 
 */

public class Ice2TransformerTest {
    /** Logging **/
    private static Logger log = LoggerFactory
            .getLogger(Ice2TransformerTest.class);
    private GenericDigitalObject testObject;
    private DigitalObject testObjectOutput;

    // private static Storage ram;
    private static Server server;
    private static HttpTester httpTester;

    @BeforeClass
    public static void setup() throws Exception {
        // init mock ice-service server
        httpTester = new HttpTester();
        Handler handler = new AbstractHandler() {
            @Override
            public void handle(String target, HttpServletRequest request,
                    HttpServletResponse response, int dispatch)
                    throws IOException, ServletException {
                String pathInfo = request.getPathInfo();
                InputStream in;

                log.info("pathInfo: {}", pathInfo);
                if (pathInfo.endsWith("query")) {
                    httpTester.parse(getRequestString(request));
                    String pathext = request.getParameter("pathext");
                    if ("odp".equals(pathext) || "doc".equals(pathext)) {
                        // Check for extension
                        response.setStatus(HttpServletResponse.SC_OK);
                        in = new ByteArrayInputStream("OK".getBytes("UTF-8"));
                    } else {
                        response.setStatus(HttpServletResponse.SC_OK);
                        in = new ByteArrayInputStream("Not supported"
                                .getBytes("UTF-8"));
                    }
                } else {
                    try {
                        boolean flag = false;
                        FileItemFactory factory = new DiskFileItemFactory();
                        ServletFileUpload upload = new ServletFileUpload(
                                factory);
                        List items = upload.parseRequest(request);
                        Iterator iter = items.iterator();
                        while (iter.hasNext()) {
                            FileItem item = (FileItem) iter.next();
                            if ("somefile.doc".equals(item.getName())) {
                                flag = true;
                                break;
                            }
                        }
                        if (flag) {
                            response
                                    .setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            in = new ByteArrayInputStream(
                                    "INTERNAL SERVER ERROR".getBytes("UTF-8"));
                        } else {
                            // Return zip
                            response.setContentType("application/zip");
                            response.setStatus(HttpServletResponse.SC_OK);
                            in = getClass().getResourceAsStream(
                                    "/resources.zip");
                        }
                    } catch (FileUploadException fue) {
                        response
                                .setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        in = new ByteArrayInputStream("INTERNAL SERVER ERROR"
                                .getBytes("UTF-8"));
                    }
                }
                OutputStream out = response.getOutputStream();
                IOUtils.copy(in, out);
                out.close();
                in.close();
                ((Request) request).setHandled(true);
            }
        };
        server = new Server(10002);
        server.setHandler(handler);
        server.start();
    }

    private static String getRequestString(HttpServletRequest request)
            throws IOException {
        ServletInputStream reqz = request.getInputStream();
        int contentLen = request.getContentLength();
        if (contentLen == -1) {
            return request.toString();
        }
        byte[] buff = new byte[contentLen];
        int realLen = reqz.read(buff);
        // Assert.assertEquals(realLen, contentLen);
        return request.toString() + new String(buff);
    }

    @AfterClass
    public static void shutdown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testSingleFile() throws URISyntaxException, IOException,
            PluginException {
        File fileNameodp = new File(getClass().getResource("/presentation.odp")
                .toURI());
        testObject = new GenericDigitalObject(fileNameodp.getAbsolutePath());
        Transformer iceTransformer = PluginManager.getTransformer("ice2");
        iceTransformer.init(new File(getClass().getResource(
                "/ice-transformer.json").toURI()));
        testObjectOutput = iceTransformer.transform(testObject);
        Set<String> payloads = testObjectOutput.getPayloadIdList();
        Assert.assertEquals(2, payloads.size());
        Payload icePayload = testObjectOutput.getPayload("AboutStacks.pdf");
        Assert.assertEquals(icePayload.getId(), "AboutStacks.pdf");
        Assert.assertEquals(icePayload.getLabel(), "AboutStacks.pdf");
        Assert.assertEquals(icePayload.getType(), PayloadType.Enrichment);
        Assert.assertEquals(icePayload.getContentType(), "application/pdf");
    }

    @Test
    public void testErrorFile() throws URISyntaxException,
            UnsupportedEncodingException, PluginException {
        File fileNameodp = new File(getClass().getResource("/somefile.doc")
                .toURI());
        testObject = new GenericDigitalObject(fileNameodp.getAbsolutePath());

        Transformer iceTransformer = PluginManager.getTransformer("ice2");
        iceTransformer.init(new File(getClass().getResource(
                "/ice-transformer.json").toURI()));

        testObjectOutput = iceTransformer.transform(testObject);
        Set<String> payloads = testObjectOutput.getPayloadIdList();

        // Assert.assertEquals(1, payloads.size());
        Payload icePayload = testObjectOutput
                .getPayload("somefile_ice_error.htm");
        Assert.assertEquals(1, payloads.size());
        Assert.assertEquals(icePayload.getId(), "somefile_ice_error.htm");
        Assert.assertEquals(icePayload.getLabel(), "ICE conversion errors");
        Assert.assertEquals(icePayload.getType(), PayloadType.Error);
        Assert.assertEquals(icePayload.getContentType(), "text/html");
    }

}
