/* 
 * The Fascinator - Internal Authentication plugin
 * Copyright (C) 2008-2010 University of Southern Queensland
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
package au.edu.usq.fascinator.authentication.internal;

import au.edu.usq.fascinator.api.authentication.Authentication;
import au.edu.usq.fascinator.api.authentication.AuthenticationException;
import au.edu.usq.fascinator.api.authentication.User;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.JsonConfig;

/**
 * A plugin to implement the Fascinators default
 * internal userbase.
 *
 * Mostly a direct port from Fascinator IR code.
 * 
 * @author Greg Pendlebury
 */
public class InternalAuthentication implements Authentication {

    private static String DEFAULT_FILE_NAME = "users.properties";
    private final Logger log = LoggerFactory.getLogger(InternalAuthentication.class);
    private InternalUser user_object;
    private String file_path;
    private Properties file_store;

    @Override
    public String getId() {
        return "internal";
    }

    @Override
    public String getName() {
        return "Internal Authentication";
    }

    @Override
    public void init(String jsonString) throws AuthenticationException {
        try {
            JsonConfig config = new JsonConfig(new ByteArrayInputStream(
                    jsonString.getBytes("UTF-8")));
            setConfig(config);
        } catch (UnsupportedEncodingException e) {
            throw new AuthenticationException(e);
        } catch (IOException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public void init(File jsonFile) throws AuthenticationException {
        try {
            JsonConfig config = new JsonConfig(jsonFile);
            setConfig(config);
        } catch (IOException ioe) {
            throw new AuthenticationException(ioe);
        }
    }

    private void setConfig(JsonConfig config) throws IOException {
        // Get the basics
        user_object = new InternalUser();
        file_path   = config.get("authentication/internal/path", null);
        loadUsers();
    }

    private void loadUsers() throws IOException {
        file_store  = new Properties();

        // Load our userbase from disk
        try {
            File user_file = new File(file_path);
            if (!user_file.exists()) {
                user_file.getParentFile().mkdirs();
                OutputStream out = new FileOutputStream(user_file);
                IOUtils.copy(getClass().getResourceAsStream("/" + DEFAULT_FILE_NAME), out);
                out.close();
            }

            file_store.load(new FileInputStream(file_path));
        } catch (Exception e) {
            throw new IOException (e);
        }
    }

    private void saveUsers() throws IOException {
        if (file_store != null) {
            try {
                file_store.store(new FileOutputStream(file_path), "");
            } catch (Exception e) {
                throw new IOException (e);
            }
        }
    }

    private String encryptPassword(String password) throws AuthenticationException {
        byte[] passwordBytes = password.getBytes();

        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(passwordBytes);

            byte messageDigest[] = algorithm.digest();
            BigInteger number = new BigInteger(1, messageDigest);

            password = number.toString(16);
            if (password.length() == 31) {
                password = "0" + password;
            }
        } catch (Exception e) {
            throw new AuthenticationException("Internal password encryption failure: " + e.getMessage());
        }
        return password;
    }

    @Override
    public void shutdown() throws AuthenticationException {
        // Don't need to do anything
    }

    /**
     * Tests the user's username/password validity.
     *
     * @param username The username of the user logging in.
     * @param password The password of the user logging in.
     * @return A user object for the newly logged in user.
     * @throws AuthenticationException if there was an error logging in.
     */
    @Override
    public User logIn(String username, String password)
            throws AuthenticationException {
        // Find our user
        String uPwd = file_store.getProperty(username);
        if (uPwd == null) {
            throw new AuthenticationException("User '" + username + "' not found.");
        }
        // Encrypt the password given by the user
        String ePwd = encryptPassword(password);
        // Compare them
        if (ePwd.equals(uPwd)) {
            return getUser(username);
        } else {
            throw new AuthenticationException("Invalid password.");
        }
    }

    /**
     * Optional logout method if the implementing class wants
     * to do any post-processing.
     *
     * @param username The username of the logging out user.
     * @throws AuthenticationException if there was an error logging out.
     */
    @Override
    public void logOut(User user) throws AuthenticationException {
        // Do nothing
    }

    /**
     * Method for testing if the implementing plugin allows
     * the creation, deletion and modification of users.
     *
     * @return true/false reponse.
     */
    @Override
    public boolean supportsUserManagement() {
        return true;
    }

    /**
     * Describe the metadata the implementing class
     * needs/allows for a user.
     *
     * TODO: This is a placeholder of possible later SQUIRE integration.
     *
     * @return TODO: possibly a JSON string.
     */
    @Override
    public String describeUser() {
        return user_object.describeMetadata();
    }

    /**
     * Create a user.
     *
     * @param username The username of the new user.
     * @param password The password of the new user.
     * @return A user object for the newly created in user.
     * @throws AuthenticationException if there was an error creating the user.
     */
    @Override
    public User createUser(String username, String password)
            throws AuthenticationException {
        String user = file_store.getProperty(username);
        if (user != null) {
            throw new AuthenticationException("User '" + username + "' already exists.");
        }
        // Encrypt the new password
        String ePwd = encryptPassword(password);
        file_store.put(username, ePwd);
        try {
            saveUsers();
        } catch(IOException e) {
            throw new AuthenticationException("Error changing password: ", e);
        }

        return getUser(username);
    }

    /**
     * Delete a user.
     *
     * @param username The username of the user to delete.
     * @throws AuthenticationException if there was an error during deletion.
     */
    @Override
    public void deleteUser(String username) throws AuthenticationException {
        String user = file_store.getProperty(username);
        if (user == null) {
            throw new AuthenticationException("User '" + username + "' not found.");
        }
        file_store.remove(username);
        try {
            saveUsers();
        } catch(IOException e) {
            throw new AuthenticationException("Error deleting user: ", e);
        }
    }

    /**
     * Change a user's password.
     *
     * @param username The user changing their password.
     * @param password The new password for the user.
     * @throws AuthenticationException if there was an error changing the password.
     */
    @Override
    public void changePassword(String username, String password)
            throws AuthenticationException {
        String user = file_store.getProperty(username);
        if (user == null) {
            throw new AuthenticationException("User '" + username + "' not found.");
        }
        // Encrypt the new password
        String ePwd = encryptPassword(password);
        file_store.put(username, ePwd);
        try {
            saveUsers();
        } catch(IOException e) {
            throw new AuthenticationException("Error changing password: ", e);
        }
    }

    /**
     * Modify one of the user's properties. Available properties should match
     * up with the return value of describeUser().
     *
     * @param username The user being modified.
     * @param property The user property being modified.
     * @param newValue The new value to be assigned to the property.
     * @return An updated user object for the modifed user.
     * @throws AuthenticationException if there was an error during modification.
     */
    @Override
    public User modifyUser(String username, String property, String newValue)
            throws AuthenticationException {
        throw new AuthenticationException("This class does not support user modification.");
    }
    @Override
    public User modifyUser(String username, String property, int newValue)
            throws AuthenticationException {
        throw new AuthenticationException("This class does not support user modification.");
    }
    @Override
    public User modifyUser(String username, String property, boolean newValue)
            throws AuthenticationException {
        throw new AuthenticationException("This class does not support user modification.");
    }

    /**
     * Returns a User object if the implementing class supports
     * user queries without authentication.
     *
     * @param username The username of the user required.
     * @return An user object of the requested user.
     * @throws AuthenticationException if there was an error retrieving the object.
     */
    @Override
    public User getUser(String username) throws AuthenticationException {
        // Find our user
        String user = file_store.getProperty(username);
        if (user == null) {
            throw new AuthenticationException("User '" + username + "' not found.");
        }
        // Purge any old data and init()
        user_object = new InternalUser();
        user_object.init(username);
        // before returning
        return user_object;
    }

    /**
     * Returns a list of users matching the search.
     *
     * @param search The search string to execute.
     * @return A list of usernames (String) that match the search.
     * @throws AuthenticationException if there was an error searching.
     */
    @Override
    public List<User> searchUsers(String search)
            throws AuthenticationException {
        // Complete list of users
        String[] users = file_store.keySet().toArray(new String[file_store.size()]);
        List<User> found = new ArrayList();

        // Look through the list for anyone who matches
        for (int i = 0; i < users.length; i++) {
            if (users[i].toLowerCase().contains(search.toLowerCase())) {
                found.add(getUser(users[i]));
            }
        }

        // Return the list
        return found;
    }

}
