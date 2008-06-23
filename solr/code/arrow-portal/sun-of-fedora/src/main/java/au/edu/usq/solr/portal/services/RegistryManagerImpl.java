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
import java.io.File;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tapestry.ioc.Resource;

import au.edu.usq.solr.fedora.FedoraRestClient;
import au.edu.usq.solr.util.StreamUtils;

public class RegistryManagerImpl implements RegistryManager {

    private FedoraRestClient client;

    private File portalsDir;

    public RegistryManagerImpl(Resource configuration) {
        Properties props = new Properties();
        try {
            props.load(configuration.toURL().openStream());
            String registryUrl = props.getProperty(AppModule.REGISTRY_URL_KEY);
            portalsDir = new File(props.getProperty(AppModule.PORTALS_DIR_KEY));
            client = new FedoraRestClient(registryUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getMetadata(String uuid, String portalName) {
        OutputStream dcOut = new ByteArrayOutputStream();
        OutputStream solrOut = new ByteArrayOutputStream();
        try {
            client.get(uuid + "/DC0", dcOut);
            StreamUtils.copyStream(new ByteArrayInputStream(dcOut.toString()
                .getBytes("UTF-8")), System.out);
            File detailXsl = new File(portalsDir, portalName + "/detail.xsl");
            if (!detailXsl.exists()) {
                detailXsl = new File(portalsDir, "default/detail.xsl");
            }
            TransformerFactory tf = TransformerFactory.newInstance();
            Templates t = tf.newTemplates(new StreamSource(detailXsl));
            Transformer dc = t.newTransformer();
            dc.transform(new StreamSource(new ByteArrayInputStream(
                dcOut.toString().getBytes("UTF-8"))), new StreamResult(solrOut));
            return solrOut.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
