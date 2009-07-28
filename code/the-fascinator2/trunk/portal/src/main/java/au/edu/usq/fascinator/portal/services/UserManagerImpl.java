/* 
 * The Fascinator - Solr Portal
 * Copyright (C) 2008  University of Southern Queensland
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
package au.edu.usq.fascinator.portal.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.tapestry5.ioc.Resource;

import au.edu.usq.fascinator.portal.User;

public class UserManagerImpl implements UserManager {

    private Logger log = Logger.getLogger(UserManagerImpl.class);

    private Properties prop = null;

    private String userFile;

    private String portalDir;

    private final String DEFAULT_ADMIN = "tfadmin";

    public UserManagerImpl(Resource configuration) {
        try {
            Properties props = new Properties();
            props.load(configuration.toURL().openStream());
            portalDir = props.getProperty(AppModule.PORTALS_DIR_KEY);
            loadUsers();
            log.info("users.properties has been loaded");
        } catch (Exception e) {
            log.error("cannot load the properties file");
            log.error(e);
        }
    }

    public String encryptPassword(String password) {
        byte[] passwordBytes = password.getBytes();

        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(passwordBytes);
            byte messageDigest[] = algorithm.digest();

            StringBuffer hexString = new StringBuffer();

            BigInteger number = new BigInteger(1, messageDigest);

            password = number.toString(16);

            if (password.length() == 31) {
                password = "0" + password;
            }

            log.info("password created");
        } catch (Exception e) {
            log.error("error encrypting password");
        }

        return password;
    }

    private void loadUsers() throws FileNotFoundException, IOException {
        userFile = portalDir + "/users.properties";

        prop = new Properties();
        prop.load(new FileInputStream(userFile));
    }

    private void saveUsers() {
        if (prop != null) {
            try {
                prop.store(new FileOutputStream(userFile), "");
                log.info("users.properties file has been updated");
            } catch (Exception e) {
                log.error("cannot save changed to the properties file");
                log.error(e);
            }
        }
    }

    public void add(String username, String password) {
        prop.put(username, password);
        saveUsers();
    }

    public String[] getUsers() {
        String[] users = prop.keySet().toArray(new String[prop.size()]);
        return users;
    }

    public void remove(String username) {
        prop.remove(username);
        saveUsers();
    }

    public void save(String username, String password) {
        prop.put(username, password);
        saveUsers();
    }

    public User get(String username) {
        User user = null;

        if (prop.containsKey(username)) {
            user = new User(username);
        }

        return user;
    }

    public boolean isValidUser(String username, String password) {
        return (encryptPassword(password).equals(prop.getProperty(username))) ? true
                : false;
    }

    public String getDefault() {
        return DEFAULT_ADMIN;
    }
}
