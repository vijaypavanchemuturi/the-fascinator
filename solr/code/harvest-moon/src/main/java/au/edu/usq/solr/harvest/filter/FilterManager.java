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
package au.edu.usq.solr.harvest.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class FilterManager {

    private Logger log = Logger.getLogger(FilterManager.class);

    private List<Filter> filters;

    private File workDir;

    public FilterManager() {
        filters = new ArrayList<Filter>();
        workDir = new File(System.getProperty("java.io.tmpdir"));
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public void run(String id, InputStream in, OutputStream out)
        throws IOException {
        File tmpFile = null;
        File lastTmpFile = null;
        InputStream tmpIn = in;
        log.info("Processing " + id + "...");
        for (Filter filter : filters) {
            log.debug("Running " + filter + "...");
            try {
                lastTmpFile = tmpFile;
                tmpFile = File.createTempFile("filter", ".xml", workDir);
                OutputStream tmpOut = new FileOutputStream(tmpFile);
                filter.filter(id, tmpIn, tmpOut);
                tmpOut.close();
                tmpIn.close();
                if (lastTmpFile != null) {
                    lastTmpFile.delete();
                }
                tmpIn = new FileInputStream(tmpFile);
            } catch (Exception e) {
                if (filter.getStopOnFailure()) {
                    log.error("Stopping since " + filter + " failed", e);
                    break;
                } else {
                    log.warn("Filter " + filter + " failed.", e);
                }
            }
        }
        copyStream(tmpIn, out);
        tmpFile.delete();
    }

    private void copyStream(InputStream in, OutputStream out)
        throws IOException {
        ReadableByteChannel src = Channels.newChannel(in);
        WritableByteChannel dest = Channels.newChannel(out);
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (src.read(buffer) != -1) {
            buffer.flip();
            dest.write(buffer);
            buffer.compact();
        }
        buffer.flip();
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
    }
}
