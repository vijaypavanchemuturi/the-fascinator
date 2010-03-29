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
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
        Assert.assertTrue(newUser!= null);
        newUser = internalAuthentication.modifyUser("NewUser", "displayName", "new user displayName");
        Assert.assertEquals(newUser.get("displayName"), "new user displayName");
        newUser = internalAuthentication.modifyUser("NewUser", "uri", "http://newuser.com");
        Assert.assertEquals(newUser.get("uri"), "http://newuser.com");
    }
    
    @Test
    public void getUser() throws AuthenticationException {
        User user = internalAuthentication.getUser("admin");
        Assert.assertEquals(user.realName(), "Administrator");
        Assert.assertEquals(user.get("uri"), "http://admin.uri");
    }
    
    @Test
    public void searchUser() throws AuthenticationException {
        List<User> userList = internalAuthentication.searchUsers("admin");
        Assert.assertEquals(userList.size(), 1);
        
        userList = internalAuthentication.searchUsers("Registered");
        Assert.assertEquals(userList.size(), 1);
    }
    
    @Test
    public void listAllUser() throws AuthenticationException {
        List<User> userList = internalAuthentication.searchUsers("");
        Assert.assertEquals(userList.size(), 4);
    }
    
    @Test
    public void testLogin() throws AuthenticationException {
        User user = internalAuthentication.logIn("admin ", "admin");
        Assert.assertEquals(user.realName(), "Administrator");
        Assert.assertEquals(user.get("uri"), "http://admin.uri");
    }
    
}
