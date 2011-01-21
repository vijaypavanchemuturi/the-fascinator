/*
 * The Fascinator - JSON Simple Config
 * Copyright (C) 2011 University of Southern Queensland
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

package au.edu.usq.fascinator.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of the JsonSimple class specifically to access configuration.
 *
 * Will replace String placeholder values with system properties if found.
 *
 * @author Greg Pendlebury
 */
public class JsonSimpleConfig extends JsonSimple {

    /** Logging */
    private static Logger log = LoggerFactory.getLogger(JsonSimpleConfig.class);

    /** Default configuration directory */
    private static final String CONFIG_DIR = FascinatorHome.getPath();

    /** Default system configuration file name */
    private static final String SYSTEM_CONFIG_FILE = "system-config.json";

    /**
     * Creates an empty JSON object
     *
     * @throws IOException if there was an error during creation
     */
    public JsonSimpleConfig() throws IOException {
        super(JsonSimpleConfig.getSystemFile());
    }

    /**
     * Retrieve the String value on the given path.
     *
     * @param defaultValue : The fallback value to use if the path is
     * invalid or not found
     * @return String : The String value found on the given path, or null if
     * no default provided
     */
    @Override
    public String getString(String defaultValue, Object... path) {
        String output = super.getString(defaultValue, path);
        return StrSubstitutor.replaceSystemProperties(output);
    }

    /**
     * Performs a backup on the system-wide configuration file from the default
     * config dir if it exists. Returns a reference to the backed up file.
     *
     * @return the backed up system JSON file
     * @throws IOException if there was an error reading or writing either file
     */
    public static File backupSystemFile() throws IOException {
        File configFile = new File(CONFIG_DIR, SYSTEM_CONFIG_FILE);
        File backupFile = new File(CONFIG_DIR, SYSTEM_CONFIG_FILE + ".old");
        if (!configFile.exists()) {
            throw new IOException("System file does not exist! '"
                    + configFile.getAbsolutePath() + "'");
        } else {
            if (backupFile.exists()) {
                backupFile.delete();
            }
            OutputStream out = new FileOutputStream(backupFile);
            InputStream in = new FileInputStream(configFile);
            IOUtils.copy(in, out);
            in.close();
            out.close();
            log.info("Configuration copied to '{}'", backupFile);
        }
        return backupFile;
    }

    /**
     * Gets the system-wide configuration file from the default config dir. If
     * the file doesn't exist, a default is copied to the config dir.
     *
     * @return the system JSON file
     * @throws IOException if there was an error reading or writing the system
     *             configuration file
     */
    public static File getSystemFile() throws IOException {
        File configFile = new File(CONFIG_DIR, SYSTEM_CONFIG_FILE);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            OutputStream out = new FileOutputStream(configFile);
            IOUtils.copy(JsonSimpleConfig.class.getResourceAsStream("/"
                    + SYSTEM_CONFIG_FILE), out);
            out.close();
            log.info("Default configuration copied to '{}'", configFile);
        }
        return configFile;
    }

    /**
     * Tests whether or not the system-config has been properly configured.
     *
     * @return <code>true</code> if configured, <code>false</code> if still
     *         using defaults
     */
    public boolean isConfigured() {
        return this.getBoolean(false, "configured");
    }

    /**
     * To check if configuration file is outdated
     *
     * @return <code>true</code> if outdated, <code>false</code> otherwise
     */
    public boolean isOutdated() {
        boolean outdated = false;
        String systemVersion = this.getString(null, "version");
        if (systemVersion == null) {
            return true;
        }

        try {
            JsonSimple compiledConfig = new JsonSimple(getClass()
                    .getResourceAsStream("/" + SYSTEM_CONFIG_FILE));
            String compiledVersion = compiledConfig.getString(null, "version");
            outdated = !systemVersion.equals(compiledVersion);
            if (compiledVersion == null) {
                return false;
            }
            if (outdated) {
                log.debug("Configuration versions do not match! '{}' != '{}'",
                        systemVersion, compiledVersion);
            }
        } catch (IOException ioe) {
            log.error("Failed to parse compiled configuration!", ioe);
        }
        return outdated;
    }
}
