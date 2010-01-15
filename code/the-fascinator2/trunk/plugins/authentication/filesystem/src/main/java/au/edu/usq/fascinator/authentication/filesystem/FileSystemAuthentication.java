/*
 * The Fascinator - File System Authentication Plugin
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
package au.edu.usq.fascinator.authentication.filesystem;

import au.edu.usq.fascinator.api.authentication.Authentication;
import au.edu.usq.fascinator.api.authentication.AuthenticationException;
import au.edu.usq.fascinator.api.authentication.User;
import au.edu.usq.fascinator.common.JsonConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example plugin on how to manage authentication
 * against basic files on the filesystem.
 *
 * @author Greg Pendlebury
 */
public class FileSystemAuthentication implements Authentication {

    private final Logger log = LoggerFactory.getLogger(FileSystemAuthentication.class);
    private FileSystemUser user_object;
    private String file_path;

    @Override
    public String getId() {
        return "file-system";
    }

    @Override
    public String getName() {
        return "Filesystem Authentication";
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

    private void setConfig(JsonConfig config) {
        user_object = new FileSystemUser();
        file_path = config.get("authentication/file-system/path", null);
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
        return user_object;
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
        return false;
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
        throw new AuthenticationException("This class does not support user creation.");
    }

    /**
     * Delete a user.
     *
     * @param username The username of the user to delete.
     * @throws AuthenticationException if there was an error during deletion.
     */
    @Override
    public void deleteUser(String username) throws AuthenticationException {
        throw new AuthenticationException("This class does not support user deletion.");
    }

    /**
     * A simplified method alternative to modifyUser() if the implementing
     * class wants to just allow password changes.
     *
     * @param username The user changing their password.
     * @param password The new password for the user.
     * @throws AuthenticationException if there was an error changing the password.
     */
    @Override
    public void changePassword(String username, String password)
            throws AuthenticationException {
        throw new AuthenticationException("This class does not support password changes.");
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
        throw new AuthenticationException("This class does not support user retrieval.");
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
        throw new AuthenticationException("This class does not support user searching.");
    }

}
