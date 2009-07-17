/*
 * The Fascinator
 * Copyright (C) 2009  University of Southern Queensland
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

package au.edu.usq.fascinator.extractor.aperture;

import org.semanticdesktop.aperture.rdf.RDFContainer;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.impl.GenericDigitalObject;

/**
 * Provides RdfDigitalObject for extracted RDF metadata from a given file.
 * 
 * @author Linda Octalina & Oliver Lucido
 * 
 */
public class RdfDigitalObject extends GenericDigitalObject {

    private RdfPayload rdfPayload;

    /**
     * RdfDigitalObject constructor
     * 
     * @param object, DigitalObject type
     * @param rdf, RdfContainer type
     */
    public RdfDigitalObject(DigitalObject object, RDFContainer rdf) {
        super(object.getId());
        rdfPayload = new RdfPayload(rdf);
        addPayload(rdfPayload);
        for (Payload payload : object.getPayloadList()) {
            addPayload(payload);
        }
    }

    /**
     * Getting the Payload object
     */
    @Override
    public Payload getMetadata() {
        return rdfPayload;
    }

}