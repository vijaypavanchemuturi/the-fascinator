/*
 * The Fascinator - Dummy File Lock
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

package au.edu.usq.fascinator.common;

import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A basic file lock against a local file. For practical purposes it's
 * often best to do logic checking against a seperate '.lock' file so
 * that other code (such as a properties parser) doesn't get mixed up
 * with a lock on your 'real' file.
 *
 * @author Greg Pendlebury
 */
public class DummyFileLock {
    private String file_name;
    private FileOutputStream file;
    private final FileChannel fileChannel;
    private FileLock lock;
    private final Logger log = LoggerFactory.getLogger(DummyFileLock.class);

    /**
     * Creates a new lock against the file provided.
     *
     */
    public DummyFileLock(String new_file) throws IOException {
        file_name = new_file;
        file  = new FileOutputStream(new_file);
        fileChannel = file.getChannel();
    }

    public void getLock() throws IOException {
        synchronized (fileChannel) {
            try {
                // Try to get the lock
                lock = fileChannel.lock();

            // If it's already locked
            } catch (OverlappingFileLockException e) {
                try {
                    // Go to sleep briefly
                    Thread.sleep(100);
                    // Try again
                    getLock();
                } catch (InterruptedException ex) {
                    // We've been woken up early
                    throw new IOException("Interrupted waiting for lock! Terminating.");
                }
            }
        }
    }

    public void release() throws IOException {
        lock.release();
    }
}
