/* 
 * Sun of Fedora - Solr Portal
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
package au.edu.usq.solr.harvest.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;

import au.edu.usq.solr.harvest.Registry;
import au.edu.usq.solr.harvest.RegistryException;
import fedora.client.FedoraClient;
import fedora.server.management.FedoraAPIM;
import fedora.server.utilities.StreamUtility;

/**
 * Registry implementation for Fedora 3.0b1.
 * 
 * @author Oliver Lucido
 */
public class Fedora30Registry implements Registry {

    private static final String FOXML_TEMPLATE = "/fedora/foxml11_template.xml";

    /** Logging */
    private final Logger log = Logger.getLogger(Fedora30Registry.class);

    /** Fedora client */
    private FedoraClient client;

    /** Fedora management API */
    private FedoraAPIM manage;

    /** Base Fedora URL */
    private final String baseUrl;

    /** Username */
    private final String username;

    /** Password */
    private final String password;

    /** Whether or not a connection to the registry is established. */
    private boolean connected;

    /**
     * Creates a new Fedora 3.0 registry for the specified repository.
     * 
     * @param baseUrl The repository base URL.
     * @param username Username for a valid user.
     * @param password Password for the specified user.
     */
    public Fedora30Registry(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.connected = false;
    }

    public void connect() throws RegistryException {
        if (connected) {
            log.warn("Connect request already connected...");
        } else {
            try {
                client = new FedoraClient(baseUrl, username, password);
                manage = client.getAPIM();
                connected = true;
            } catch (Exception e) {
                throw new RegistryException(e);
            }
        }
    }

    public String createObject(Map<String, String> options)
        throws RegistryException {

        if (!connected) {
            throw new RegistryException("Not connected");
        }

        String pid = null;
        String comment = "";
        if (options != null && options.containsValue("comment")) {
            comment = options.get("comment");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            StreamUtility.pipeStream(getClass().getResourceAsStream(
                FOXML_TEMPLATE), out, 4096);
            pid = manage.ingest(out.toByteArray(),
                FedoraClient.FOXML1_1.toString(), comment);
            log.debug("Created new object: PID = " + pid);
        } catch (Exception e) {
            throw new RegistryException(e);
        }
        return pid;
    }

    public void addDatastream(String pid, String dsId, InputStream data,
        Map<String, String> options) throws RegistryException {

        if (!connected) {
            throw new RegistryException("Not connected");
        }

        try {
            String[] altIds = new String[] {};
            String dsLabel = options.get("dsLabel");
            boolean versionable = true;
            String mimeType = options.get("mimeType");
            String formatUri = "";
            String controlGroup = options.get("controlGroup");
            String dsState = "A"; // Active;
            String logMessage = options.get("logMessage");

            File dcFile = File.createTempFile("f3r_tmp", ".xml");
            FileOutputStream fos = new FileOutputStream(dcFile);
            StreamUtility.pipeStream(data, fos, 4096);
            fos.close();
            String dsLocation = client.uploadFile(dcFile);
            manage.addDatastream(pid, dsId, altIds, dsLabel, versionable,
                mimeType, formatUri, dsLocation, controlGroup, dsState,
                "DISABLED", "none", logMessage);

            log.info(String.format("Added datastream; ID = %s, Label = %s",
                dsId, dsLabel));
        } catch (Exception e) {
            throw new RegistryException(e);
        }
    }
}
