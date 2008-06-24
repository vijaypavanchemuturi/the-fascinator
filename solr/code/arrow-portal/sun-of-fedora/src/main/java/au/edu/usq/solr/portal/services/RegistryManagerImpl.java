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
package au.edu.usq.solr.portal.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.apache.tapestry.ioc.Resource;

import au.edu.usq.solr.fedora.DatastreamType;
import au.edu.usq.solr.fedora.FedoraRestClient;
import au.edu.usq.solr.fedora.ObjectDatastreamsType;
import au.edu.usq.solr.model.DublinCore;

public class RegistryManagerImpl implements RegistryManager {

    private Logger log = Logger.getLogger(RegistryManagerImpl.class);

    private FedoraRestClient client;

    public RegistryManagerImpl(Resource configuration) {
        Properties props = new Properties();
        try {
            props.load(configuration.toURL().openStream());
            String registryUrl = props.getProperty(AppModule.REGISTRY_URL_KEY);
            client = new FedoraRestClient(registryUrl);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    public DublinCore getMetadata(String uuid) {
        ByteArrayOutputStream dcOut = new ByteArrayOutputStream();
        try {
            log.info("id:" + uuid);
            client.get(uuid + "/DC0", dcOut);
            JAXBContext jc = JAXBContext.newInstance(DublinCore.class);
            Unmarshaller um = jc.createUnmarshaller();
            return (DublinCore) um.unmarshal(new ByteArrayInputStream(
                dcOut.toByteArray()));
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    public DatastreamType getDatastream(String uuid, String dsId) {
        for (DatastreamType ds : getDatastreams(uuid)) {
            if (dsId.equals(ds.getDsid())) {
                return ds;
            }
        }
        return null;
    }

    public List<DatastreamType> getDatastreams(String uuid) {
        try {
            ObjectDatastreamsType dsList = client.listDatastreams(uuid);
            return dsList.getDatastreams();
        } catch (IOException ioe) {
            log.error(ioe);
            throw new RuntimeException(ioe);
        }
    }

    public void getDatastreamAsStream(String uuid, String dsId, OutputStream out) {
        try {
            client.get(uuid, dsId, out);
        } catch (IOException ioe) {
            log.error(ioe);
            throw new RuntimeException(ioe);
        }
    }

    public String getDatastreamAsString(String uuid, String dsId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getDatastreamAsStream(uuid, dsId, out);
        try {
            return out.toString("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            log.error(uee);
            throw new RuntimeException(uee);
        }
    }
}
