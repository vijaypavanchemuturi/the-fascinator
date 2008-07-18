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
package au.edu.usq.solr.index.rule.impl;

import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import au.edu.usq.solr.index.AddDocType;
import au.edu.usq.solr.index.FieldType;
import au.edu.usq.solr.index.rule.AbstractRule;
import au.edu.usq.solr.index.rule.RuleException;

public class LowercaseFieldRule extends AbstractRule {

    private Logger log = Logger.getLogger(LowercaseFieldRule.class);

    private String fieldName;

    public LowercaseFieldRule(String fieldName) {
        super("LowercaseFieldRule", false);
        this.fieldName = fieldName;
    }

    @Override
    public void run(Reader in, Writer out) throws RuleException {
        log.info("Changing '" + fieldName + "' value to lower case");
        try {
            JAXBContext jc = JAXBContext.newInstance(AddDocType.class);
            Unmarshaller u = jc.createUnmarshaller();
            AddDocType addDoc = (AddDocType) u.unmarshal(in);
            List<FieldType> fields = addDoc.getFields(fieldName);
            for (FieldType field : fields) {
                field.setValue(field.getValue().toLowerCase());
            }
            Marshaller m = jc.createMarshaller();
            m.marshal(addDoc, out);
        } catch (JAXBException jaxbe) {
            throw new RuleException(jaxbe.getLinkedException());
        }
    }
}
