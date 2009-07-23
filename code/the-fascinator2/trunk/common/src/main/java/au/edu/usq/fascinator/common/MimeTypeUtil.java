/* 
 * The Fascinator - Common Library
 * Copyright (C) 2008-2009 University of Southern Queensland
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
import java.io.InputStream;

import eu.medsea.mimeutil.MimeUtil;

/**
 * Utility class to determine MIME type
 * 
 * @author Oliver Lucido
 */
public class MimeTypeUtil {

    /**
     * Register default MIME detectors
     */
    static {
        registerDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
        registerDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        if (System.getProperty("os.name").startsWith("Windows")) {
            registerDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
        }
    }

    /**
     * Gets the MIME type for the specified file name
     * 
     * @param fileName a file name
     * @return MIME type
     */
    public static String getMimeType(String fileName) {
        return MimeUtil.getMimeTypes(fileName).iterator().next().toString();
    }

    /**
     * Gets the MIME type for the specified file
     * 
     * @param file a file
     * @return MIME type
     */
    public static String getMimeType(File file) {
        return MimeUtil.getMimeTypes(file).iterator().next().toString();
    }

    /**
     * Gets the MIME type for the specified input stream
     * 
     * @param in an input stream
     * @return MIME type
     */
    public static String getMimeType(InputStream in) {
        return MimeUtil.getMimeTypes(in).iterator().next().toString();
    }

    /**
     * Registers a MIME type detector.
     * 
     * @param detector MIME detector class name
     */
    private static void registerDetector(String detector) {
        MimeUtil.registerMimeDetector(detector);
    }
}
