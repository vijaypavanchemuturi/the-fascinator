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
package au.edu.usq.solr.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.solr.util.SimplePostTool;

public class Optimize {

    private URL solrUpdateUrl;

    public Optimize(String solrUpdateUrl) throws MalformedURLException {
        this.solrUpdateUrl = new URL(solrUpdateUrl);
    }

    public void optimize() throws IOException {
        SimplePostTool postTool = new SimplePostTool(solrUpdateUrl);
        Writer result = new OutputStreamWriter(System.out);
        postTool.postData(new StringReader("<optimize/>"), result);
        postTool.commit(result);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: " + Optimize.class.getCanonicalName()
                + " <solrUpdateUrl>");
        } else {
            try {
                String solrUpdateUrl = args[0];
                Optimize optimize = new Optimize(solrUpdateUrl);
                optimize.optimize();
            } catch (MalformedURLException e) {
                System.err.println("Invalid Solr URL: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Failed to commit" + e.getMessage());
            }
        }
    }
}
