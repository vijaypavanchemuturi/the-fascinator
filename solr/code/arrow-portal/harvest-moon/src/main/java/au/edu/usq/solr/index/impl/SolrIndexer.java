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
package au.edu.usq.solr.index.impl;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

import org.apache.solr.util.SimplePostTool;

import au.edu.usq.solr.index.Indexer;
import au.edu.usq.solr.index.IndexerException;

public class SolrIndexer implements Indexer {

    private SimplePostTool postTool;

    private Writer outputLog;

    public SolrIndexer(String baseUrl) throws IndexerException {
        try {
            postTool = new SimplePostTool(new URL(baseUrl + "/update"));
            outputLog = new OutputStreamWriter(System.out);
        } catch (Exception e) {
            throw new IndexerException(e);
        }
    }

    public void index(File file) throws IndexerException {
        try {
            postTool.postFile(file, outputLog);
        } catch (Exception e) {
            throw new IndexerException(e);
        }
    }

    public void index(InputStream doc) throws IndexerException {
        try {
            postTool.postData(new InputStreamReader(doc, "UTF-8"), outputLog);
        } catch (Exception e) {
            throw new IndexerException(e);
        }
    }

    public void commit() throws IndexerException {
        try {
            postTool.commit(outputLog);
        } catch (Exception e) {
            throw new IndexerException(e);
        }
    }
}
