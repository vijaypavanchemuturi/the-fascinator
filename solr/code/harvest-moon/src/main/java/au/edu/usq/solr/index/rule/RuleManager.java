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
package au.edu.usq.solr.index.rule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import au.edu.usq.solr.util.StreamUtils;

public class RuleManager {

    private Logger log = Logger.getLogger(RuleManager.class);

    private List<Rule> rules;

    private File workDir;

    public RuleManager() {
        rules = new ArrayList<Rule>();
        workDir = new File(System.getProperty("java.io.tmpdir"));
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public void add(Rule rule) {
        rules.add(rule);
    }

    public void remove(Rule rule) {
        rules.remove(rule);
    }

    public void run(InputStream in, OutputStream out) throws IOException {
        File tmpFile = null;
        File lastTmpFile = null;
        InputStream tmpIn = in;
        for (Rule rule : rules) {
            try {
                lastTmpFile = tmpFile;
                tmpFile = File.createTempFile("rule", ".xml", workDir);
                OutputStream tmpOut = new FileOutputStream(tmpFile);
                rule.run(tmpIn, tmpOut);
                tmpOut.close();
                tmpIn.close();
                if (lastTmpFile != null) {
                    lastTmpFile.delete();
                }
                tmpIn = new FileInputStream(tmpFile);
            } catch (Exception e) {
                if (rule.getStopOnFailure()) {
                    log.error("Stopping since " + rule + " failed", e);
                    break;
                } else {
                    log.warn("Rule " + rule + " failed.", e);
                }
            }
        }
        StreamUtils.copyStream(tmpIn, out);
        tmpFile.delete();
    }
}
