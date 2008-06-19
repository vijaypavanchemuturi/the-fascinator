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
package au.edu.usq.solr.harvest.fedora;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;

import au.edu.usq.solr.util.StreamUtils;

public class FedoraRestClient {

    private Logger log = Logger.getLogger(FedoraRestClient.class);

    private String baseUrl;

    private String username;

    private String password;

    public FedoraRestClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void authenticate(String username, String password) {
        this.username = username;
        this.password = password;
    }

    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        client.getState().setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(username, password));
        return client;
    }

    // Access methods (Fedora 2.2+ compatible)

    public ResultType findObjects(String terms, int maxResults)
        throws IOException {
        ResultType result = null;
        try {
            StringBuilder uri = new StringBuilder(baseUrl);
            uri.append("/search?terms=");
            uri.append(URLEncoder.encode(terms, "UTF-8"));
            uri.append("&maxResults=");
            uri.append(maxResults);
            uri.append("&xml=true");
            uri.append("&pid=true");
            GetMethod method = new GetMethod(uri.toString());
            HttpClient client = getHttpClient();
            int status = client.executeMethod(method);
            if (status == 200) {
                JAXBContext jc = JAXBContext.newInstance(ResultType.class);
                Unmarshaller um = jc.createUnmarshaller();
                result = (ResultType) um.unmarshal(method.getResponseBodyAsStream());
            }
            method.releaseConnection();
        } catch (JAXBException jaxbe) {
            log.error(jaxbe);
        }
        return result;
    }

    public ResultType resumeFindObjects(String token) throws IOException {
        ResultType result = null;
        try {
            StringBuilder uri = new StringBuilder(baseUrl);
            uri.append("/search?sessionToken=");
            uri.append(token);
            uri.append("&xml=true");
            GetMethod method = new GetMethod(uri.toString());
            int status = getHttpClient().executeMethod(method);
            if (status == 200) {
                JAXBContext jc = JAXBContext.newInstance(ResultType.class);
                Unmarshaller um = jc.createUnmarshaller();
                result = (ResultType) um.unmarshal(method.getResponseBodyAsStream());
            }
            method.releaseConnection();
        } catch (JAXBException jaxbe) {
            log.error(jaxbe);
        }
        return result;
    }

    public ObjectDatastreamsType listDatastreams(String pid) throws IOException {
        ObjectDatastreamsType result = null;
        try {
            StringBuilder uri = new StringBuilder(baseUrl);
            uri.append("/listDatastreams/");
            uri.append(pid);
            uri.append("?xml=true");
            GetMethod method = new GetMethod(uri.toString());
            int status = getHttpClient().executeMethod(method);
            if (status == 200) {
                JAXBContext jc = JAXBContext.newInstance(ObjectDatastreamsType.class);
                Unmarshaller um = jc.createUnmarshaller();
                result = (ObjectDatastreamsType) um.unmarshal(method.getResponseBodyAsStream());
            }
            method.releaseConnection();
        } catch (JAXBException jaxbe) {
            log.error(jaxbe);
        }
        return result;
    }

    public void get(String pid, OutputStream out) throws IOException {
        get(pid, null, out);
    }

    public void get(String pid, String dsId, OutputStream out)
        throws IOException {
        StringBuilder uri = new StringBuilder(baseUrl);
        uri.append("/get/");
        uri.append(pid);
        if (dsId != null) {
            uri.append("/");
            uri.append(dsId);
        }
        GetMethod method = new GetMethod(uri.toString());
        int status = getHttpClient().executeMethod(method);
        if (status == 200) {
            StreamUtils.copyStream(method.getResponseBodyAsStream(), out);
        }
        method.releaseConnection();
    }

    // Management methods (Fedora 3.0+ compatible)

    public String getNextPid() throws IOException {
        return getNextPid(1, null).getPids().get(0);
    }

    public PidListType getNextPid(int numPids, String namespace)
        throws IOException {
        PidListType result = null;
        try {
            StringBuilder uri = new StringBuilder(baseUrl);
            uri.append("/objects/nextPID?numPIDs=");
            uri.append(numPids);
            if (namespace != null) {
                uri.append("&namespace=");
                uri.append(namespace);
            }
            uri.append("&format=xml");
            GetMethod method = new GetMethod(uri.toString());
            int status = getHttpClient().executeMethod(method);
            if (status == 200) {
                JAXBContext jc = JAXBContext.newInstance(PidListType.class);
                Unmarshaller um = jc.createUnmarshaller();
                result = (PidListType) um.unmarshal(method.getResponseBodyAsStream());
            }
        } catch (JAXBException jaxbe) {
            log.error(jaxbe);
        }
        return result;
    }

    public String createObject(String label, String namespace)
        throws IOException {
        String pid = namespace + ":" + UUID.randomUUID().toString();
        Map<String, String> options = new HashMap<String, String>();
        options.put("label", label);
        options.put("namespace", namespace);
        ingest(pid, options, null);
        return pid;
    }

    public void ingest(String pid, Map<String, String> options, File content)
        throws IOException {
        StringBuilder uri = new StringBuilder(baseUrl);
        uri.append("/objects/");
        uri.append(pid);
        addParam(uri, options, "label");
        addParam(uri, options, "format");
        addParam(uri, options, "encoding");
        addParam(uri, options, "namespace");
        addParam(uri, options, "ownerId");
        addParam(uri, options, "logMessage");
        PostMethod method = new PostMethod(uri.toString());
        RequestEntity request = null;
        if (content == null) {
            request = new StringRequestEntity("", "text/xml", "UTF-8");
        } else {
            request = new FileRequestEntity(content, getMimeType(content));
        }
        method.setRequestEntity(request);
        getHttpClient().executeMethod(method);
    }

    public void addDatastream(String pid, String dsId, String dsLabel,
        String content, String contentType) throws IOException {
        Map<String, String> options = new HashMap<String, String>();
        options.put("dsLabel", dsLabel);
        addDatastream(pid, dsId, options, new ByteArrayInputStream(
            content.getBytes("UTF-8")), contentType);
    }

    public void addDatastream(String pid, String dsId,
        Map<String, String> options, InputStream content, String contentType)
        throws IOException {
        StringBuilder uri = new StringBuilder(baseUrl);
        uri.append("/objects/");
        uri.append(pid);
        uri.append("/datastreams/");
        uri.append(dsId);
        addParam(uri, options, "controlGroup");
        addParam(uri, options, "dsLocation");
        addParam(uri, options, "altIDs");
        addParam(uri, options, "dsLabel");
        addParam(uri, options, "versionable");
        addParam(uri, options, "dsState");
        addParam(uri, options, "formatURI");
        addParam(uri, options, "checksumType");
        addParam(uri, options, "checksum");
        addParam(uri, options, "logMessage");
        log.info("uri: " + uri);
        PostMethod method = new PostMethod(uri.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copyStream(content, out);
        RequestEntity request = new StringRequestEntity(out.toString(),
            contentType, "UTF-8");
        method.setRequestEntity(request);
        int status = getHttpClient().executeMethod(method);
        log.info("status: " + status);
        log.info("response: " + method.getResponseBodyAsString());
    }

    private void addParam(StringBuilder uri, Map<String, String> options,
        String name) throws IOException {
        String value = options.get(name);
        if (value != null) {
            char sep = '?';
            if (uri.lastIndexOf("?") > -1) {
                sep = '&';
            }
            uri.append(sep);
            uri.append(URLEncoder.encode(name, "UTF-8"));
            uri.append("=");
            uri.append(URLEncoder.encode(value, "UTF-8"));
        }
    }

    private String getMimeType(File file) {
        return MimetypesFileTypeMap.getDefaultFileTypeMap()
            .getContentType(file);
    }
}
