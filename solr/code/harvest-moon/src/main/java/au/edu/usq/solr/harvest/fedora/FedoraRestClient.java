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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import au.edu.usq.solr.harvest.fedora.types.ObjectDatastreamsType;
import au.edu.usq.solr.harvest.fedora.types.PidListType;
import au.edu.usq.solr.harvest.fedora.types.ResultType;
import fedora.server.utilities.StreamUtility;

public class FedoraRestClient {

    private String baseUrl;

    private String username;

    private String password;

    private HttpClient client;

    public void authenticate(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public FedoraRestClient(String baseUrl) {
        this.baseUrl = baseUrl;
        client = new HttpClient();
    }

    // Access methods (Fedora 2.2 compatible)

    public ResultType findObjects(String terms, int maxResults) {
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
            int status = client.executeMethod(method);
            if (status == 200) {
                JAXBContext jc = JAXBContext.newInstance(ResultType.class);
                Unmarshaller um = jc.createUnmarshaller();
                result = (ResultType) um.unmarshal(method.getResponseBodyAsStream());
            }
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ResultType resumeFindObjects(String token) {
        ResultType result = null;
        try {
            StringBuilder uri = new StringBuilder(baseUrl);
            uri.append("/search?sessionToken=");
            uri.append(token);
            uri.append("&xml=true");
            GetMethod method = new GetMethod(uri.toString());
            int status = client.executeMethod(method);
            if (status == 200) {
                JAXBContext jc = JAXBContext.newInstance(ResultType.class);
                Unmarshaller um = jc.createUnmarshaller();
                result = (ResultType) um.unmarshal(method.getResponseBodyAsStream());
            }
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ObjectDatastreamsType listDatastreams(String pid) {
        ObjectDatastreamsType result = null;
        try {
            StringBuilder uri = new StringBuilder(baseUrl);
            uri.append("/listDatastreams/");
            uri.append(pid);
            uri.append("?xml=true");
            GetMethod method = new GetMethod(uri.toString());
            int status = client.executeMethod(method);
            if (status == 200) {
                JAXBContext jc = JAXBContext.newInstance(ObjectDatastreamsType.class);
                Unmarshaller um = jc.createUnmarshaller();
                result = (ObjectDatastreamsType) um.unmarshal(method.getResponseBodyAsStream());
            }
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void get(String pid, OutputStream out) {
        get(pid, null, out);
    }

    public void get(String pid, String dsId, OutputStream out) {
        try {
            StringBuilder uri = new StringBuilder(baseUrl);
            uri.append("/get/");
            uri.append(pid);
            if (dsId != null) {
                uri.append("/");
                uri.append(dsId);
            }
            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(uri.toString());
            int status = client.executeMethod(method);
            if (status == 200) {
                StreamUtility.pipeStream(method.getResponseBodyAsStream(), out,
                    4096);
            }
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Management methods (Fedora 3.0+ compatible)

    public String getNextPid() {
        PidListType result = getNextPid(1, null);
        return result.getPids().get(0);
    }

    public PidListType getNextPid(int numPids, String namespace) {
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
            int status = client.executeMethod(method);
            if (status == 200) {
                JAXBContext jc = JAXBContext.newInstance(PidListType.class);
                Unmarshaller um = jc.createUnmarshaller();
                result = (PidListType) um.unmarshal(method.getResponseBodyAsStream());
            }
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void ingest(String pid, String label, InputStream content) {
        Map<String, String> options = new HashMap<String, String>();
        options.put("label", label);
        ingest(pid, content, options);
    }

    public void ingest(String pid, InputStream content,
        Map<String, String> options) {
        try {
            StringBuilder uri = new StringBuilder(baseUrl);
            uri.append("/objects/");
            uri.append(pid);
            addParam(uri, options, "label", '?');
            addParam(uri, options, "format");
            addParam(uri, options, "encoding");
            addParam(uri, options, "namespace");
            addParam(uri, options, "ownerId");
            addParam(uri, options, "logMessage");
            PostMethod method = new PostMethod(uri.toString());
            RequestEntity request = new InputStreamRequestEntity(content);
            method.setRequestEntity(request);
            int status = client.executeMethod(method);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addDatastream(String pid, String dsId, String dsLabel,
        InputStream content) {
        Map<String, String> options = new HashMap<String, String>();
        options.put("dsLabel", dsLabel);
        addDatastream(pid, dsId, content, options);
    }

    public void addDatastream(String pid, String dsId, InputStream content,
        Map<String, String> options) {
        try {
            StringBuilder uri = new StringBuilder(baseUrl);
            uri.append("/objects/");
            uri.append(pid);
            uri.append("/datastreams/");
            uri.append(dsId);
            addParam(uri, options, "controlGroup", '?');
            addParam(uri, options, "dsLocation");
            addParam(uri, options, "altIDs");
            addParam(uri, options, "dsLabel");
            addParam(uri, options, "versionable");
            addParam(uri, options, "dsState");
            addParam(uri, options, "formatURI");
            addParam(uri, options, "checksumType");
            addParam(uri, options, "checksum");
            addParam(uri, options, "logMessage");
            PostMethod method = new PostMethod(uri.toString());
            RequestEntity request = new InputStreamRequestEntity(content);
            method.setRequestEntity(request);
            int status = client.executeMethod(method);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addParam(StringBuilder uri, Map<String, String> options,
        String name) {
        addParam(uri, options, name, '&');
    }

    private void addParam(StringBuilder uri, Map<String, String> options,
        String name, char sep) {
        String value = options.get(name);
        if (value != null) {
            uri.append(sep);
            uri.append(name);
            uri.append("=");
            uri.append(value);
        }
    }
}
