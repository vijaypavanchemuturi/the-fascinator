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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import au.edu.usq.solr.fedora.DatastreamType;
import au.edu.usq.solr.fedora.FedoraRestClient;
import au.edu.usq.solr.harvest.Datastream;
import au.edu.usq.solr.util.StreamUtils;

public class OaiOreDatastream implements Datastream {

    private String pid;
    private FedoraRestClient client;
    private String datastreamId;
    private String datastreamLabel;
    private String rawDatastreamId;
    private String mimeType;
    private byte[] datastreamContentAsByteArray;

    public OaiOreDatastream(FedoraRestClient client, String pid,
        DatastreamType dsType) {
        this.client = client;
        this.pid = pid;

    }

    public OaiOreDatastream(String id, String pid, String mimeType) {
        this.rawDatastreamId = id;
        this.setDatastreamId(id);
        this.setdatastreamLabel();
        this.pid = pid;
        this.setMimeType(mimeType);
        setDataStreamContentAsByteArray();
    }

    private void setDataStreamContentAsByteArray() {
        InputStream in = null;

        try {
            URI uri = new URI(this.rawDatastreamId.trim());

            in = uri.toURL().openStream();
        } catch (MalformedURLException e) {
            System.out.println("The following url is malformed ");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Unable to contact url IO Exception");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            StreamUtils.copyStream(in, out);
        } catch (IOException e) {
            System.out.println("IO Exception");
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        this.datastreamContentAsByteArray = out.toByteArray();
    }

    private void setDatastreamId(String Id) {
        this.datastreamId = "DS";
        int lastSlash = Id.lastIndexOf('/');
        datastreamId += Id.substring(Id.indexOf("/", 8) + 1, lastSlash);
        datastreamId = datastreamId.replace('/', '_');
    }

    private void setdatastreamLabel() {
        this.datastreamLabel = this.rawDatastreamId.substring(
            this.rawDatastreamId.lastIndexOf("/")).substring(1);
        datastreamLabel = datastreamLabel.replace(',', '_');
        datastreamLabel = datastreamLabel.replace('/', '_');

    }

    private void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getId() {
        return this.datastreamId;
    }

    public String getLabel() {
        return this.datastreamLabel;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public byte[] getContent() throws IOException {
        return this.datastreamContentAsByteArray;
    }

    public String getContentAsString() throws IOException {
        return new String(getContent());
    }

    public InputStream getContentAsStream() throws IOException {
        return new ByteArrayInputStream(getContent());
    }

    public void getContent(File file) throws IOException {

        FileOutputStream out = new FileOutputStream(file);
        out.write(getContent());
        out.close();
    }

    @Override
    public String toString() {
        return pid + "/" + getId() + " [" + getLabel() + "] (" + getMimeType()
            + ")";
    }
}
