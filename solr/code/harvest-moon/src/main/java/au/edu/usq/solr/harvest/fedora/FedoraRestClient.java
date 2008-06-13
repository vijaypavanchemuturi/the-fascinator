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
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import au.edu.usq.solr.harvest.fedora.types.ObjectDatastreamsType;
import au.edu.usq.solr.harvest.fedora.types.ResultType;
import fedora.server.utilities.StreamUtility;

public class FedoraRestClient {

    private String baseUrl;

    public FedoraRestClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

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
            HttpClient client = new HttpClient();
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
            HttpClient client = new HttpClient();
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
}
