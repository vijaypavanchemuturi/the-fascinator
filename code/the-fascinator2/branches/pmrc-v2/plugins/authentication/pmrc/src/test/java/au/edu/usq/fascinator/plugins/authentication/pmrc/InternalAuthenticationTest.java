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
package au.edu.usq.fascinator.plugins.authentication.pmrc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.authentication.AuthenticationException;
import au.edu.usq.fascinator.api.authentication.User;
import au.edu.usq.fascinator.authentication.pmrc.InternalAuthentication;
import au.edu.usq.fascinator.authentication.pmrc.InternalUser;
import au.edu.usq.fascinator.common.JsonConfigHelper;

/**
 * Prmc Internal Authentication Test
 * 
 * @author Linda Octalina
 */
public class InternalAuthenticationTest {
    private final Logger log = LoggerFactory
            .getLogger(InternalAuthenticationTest.class);

    private InternalAuthentication internalAuthentication;
    private JsonConfigHelper config;

    @Before
    public void setup() throws Exception {
        File f = new File(System.getProperty("java.io.tmpdir")
                + "/_pmrc_test/users.properties.json");
        f.getParentFile().mkdirs();
        OutputStream out = new FileOutputStream(System
                .getProperty("java.io.tmpdir")
                + "/_pmrc_test/users.properties.json");

        IOUtils.copy(getClass().getResourceAsStream("/users.test.json"),
                out);
        out.close();

        internalAuthentication = new InternalAuthentication();
        internalAuthentication.init(new File(getClass().getResource(
                "/test-config.json").toURI()));
    }

    
    @Test
    public void createUser() throws AuthenticationException {
        User newUser = internalAuthentication.createUser("NewUser", "NewPassword");
    }
    
    @Test
    public void getUser() throws AuthenticationException {
        User user = internalAuthentication.getUser("NewUser");
        System.out.println(user.realName());
//        Assert.assertEquals(user.realName(), "Administrator");
//        Assert.assertEquals(user.get("uri"), "http://admin.uri");
    }
    
    //    @Test
    //    public void testInit() throws URISyntaxException, AuthenticationException {
    //        File userprops = new File(getClass().getResource(
    //                "/users.properties.json").toURI());
    //        System.out.println(config.toString(true));
    //    }

    //    private static Server server;
    //
    //    private DigitalObject sourceObject, outputObject;
    //
    //    @BeforeClass
    //    public static void setup() throws Exception {
    //        server = new Server(10002);
    //        server.setHandler(new Ice2Handler());
    //        server.start();
    //    }
    //
    //    @AfterClass
    //    public static void shutdown() throws Exception {
    //        if (server != null) {
    //            server.stop();
    //        }
    //    }
    //
    //    @Test
    //    public void testSingleFile() throws URISyntaxException, IOException,
    //            PluginException {
    //        File file = new File(getClass().getResource("/first-post.odt").toURI());
    //        sourceObject = new GenericDigitalObject(file.getAbsolutePath());
    //        Transformer iceTransformer = PluginManager.getTransformer("ice2");
    //        iceTransformer.init(new File(getClass().getResource(
    //                "/ice-transformer.json").toURI()));
    //        outputObject = iceTransformer.transform(sourceObject);
    //        Set<String> payloads = outputObject.getPayloadIdList();
    //
    //        Assert.assertEquals(3, payloads.size());
    //
    //        // check for the Preview payload
    //        boolean foundPreview = false;
    //        for (String pid : payloads) {
    //            Payload payload = outputObject.getPayload(pid);
    //            if (PayloadType.Preview.equals(payload.getType())) {
    //                foundPreview = true;
    //            }
    //        }
    //        Assert.assertTrue("There should be a Preview payload!", foundPreview);
    //    }
    //
    //    @Test
    //    public void testErrorFile() throws URISyntaxException,
    //            UnsupportedEncodingException, PluginException {
    //        File file = new File(getClass().getResource("/somefile.doc").toURI());
    //        sourceObject = new GenericDigitalObject(file.getAbsolutePath());
    //
    //        Transformer iceTransformer = PluginManager.getTransformer("ice2");
    //        iceTransformer.init(new File(getClass().getResource(
    //                "/ice-transformer.json").toURI()));
    //
    //        outputObject = iceTransformer.transform(sourceObject);
    //        Set<String> payloads = outputObject.getPayloadIdList();
    //
    //        Payload icePayload = outputObject.getPayload("somefile_ice_error.htm");
    //        Assert.assertEquals(1, payloads.size());
    //        Assert.assertEquals(icePayload.getId(), "somefile_ice_error.htm");
    //        Assert.assertEquals(icePayload.getLabel(), "ICE conversion errors");
    //        Assert.assertEquals(icePayload.getType(), PayloadType.Error);
    //        Assert.assertEquals(icePayload.getContentType(), "text/html");
    //    }
}
