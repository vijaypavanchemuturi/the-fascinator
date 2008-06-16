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
package au.edu.usq.solr.harvest.filter.impl;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import au.edu.usq.solr.harvest.filter.BaseSolrFilter;
import au.edu.usq.solr.harvest.filter.FilterException;
import au.edu.usq.solr.index.AddDocType;
import au.edu.usq.solr.index.FieldType;

public class AddFieldFilter extends BaseSolrFilter {

    private FieldType field;

    public AddFieldFilter(String fieldName) {
        this(fieldName, "");
    }

    public AddFieldFilter(String fieldName, String value) {
        super("AddField");
        field = new FieldType(fieldName, value);
    }

    public void setValue(String value) {
        field.setValue(value);
    }

    @Override
    public void filter(InputStream in, OutputStream out) throws FilterException {
        try {
            JAXBContext jc = JAXBContext.newInstance(AddDocType.class);
            Unmarshaller u = jc.createUnmarshaller();

            AddDocType addDoc = (AddDocType) u.unmarshal(in);
            addDoc.getFields().add(field);

            Marshaller m = jc.createMarshaller();
            m.marshal(addDoc, out);

        } catch (JAXBException jaxbe) {
            throw new FilterException("Failed to add field: " + field, jaxbe);
        }
    }
}
