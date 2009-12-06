/* 
 * The Fascinator - Fedora Commons 3.x storage plugin
 * Copyright (C) 2009 University of Southern Queensland
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
package au.edu.usq.fascinator.storage.fedora;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;
import au.edu.usq.fedora.RestClient;
import au.edu.usq.fedora.types.DatastreamProfile;
import au.edu.usq.fedora.types.DatastreamType;
import au.edu.usq.fedora.types.ObjectDatastreamsType;

/**
 * 
 * @author Oliver Lucido & Linda Octalina
 * 
 */
public class Fedora3DigitalObject extends GenericDigitalObject {

    private Logger log = LoggerFactory.getLogger(Fedora3DigitalObject.class);

    private RestClient client;

    private String fedoraId;

    public Fedora3DigitalObject(RestClient client, String fedoraId, String oid) {
        super(oid);
        this.fedoraId = fedoraId;
        this.client = client;
    }

    @Override
    public List<Payload> getPayloadList() {
        List<Payload> dsList = new ArrayList<Payload>();
        try {
            ObjectDatastreamsType odt = client.listDatastreams(fedoraId);
            for (DatastreamType dst : odt.getDatastreams()) {
                Fedora3Payload ds = new Fedora3Payload(client, fedoraId, dst
                        .getDsid());

                DatastreamProfile dsProfile = client.getDatastream(fedoraId,
                        dst.getDsid());
                if (dsProfile.getDsAltID() != null) {
                    // Use the PayloadType and dsId from AltID
                    // The dsId will be used to search the data straem in fedora
                    String[] altId = dsProfile.getDsAltID().split(":");
                    ds.setId(altId[1]);
                    ds.setLabel(dst.getLabel());
                    ds.setType(PayloadType.valueOf(altId[0]));
                    if (ds.getType().equals(PayloadType.Data)) {
                        // If it's a source, set the source there
                        setSourceId(dst.getDsid());
                    }
                    ds.setContentType(dst.getMimeType());
                    dsList.add(ds);
                }
            }
        } catch (IOException ioe) {
            log.error("Failed to list datastreams for {}", fedoraId);
        }
        return dsList;
    }
}
