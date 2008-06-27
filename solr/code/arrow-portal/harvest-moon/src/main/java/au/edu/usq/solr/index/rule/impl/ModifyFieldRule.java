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

import java.io.InputStream;
import java.io.OutputStream;
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

public class ModifyFieldRule extends AbstractRule {

    private Logger log = Logger.getLogger(ModifyFieldRule.class);

    private String fieldName;

    private String regex;

    private String replacement;

    public ModifyFieldRule(String fieldName, String regex, String replacement) {
        super("ModifyField", false);
        this.fieldName = fieldName;
        this.regex = regex;
        this.replacement = replacement;
    }

    @Override
    public void run(InputStream in, OutputStream out) throws RuleException {
        log.info("Modifying '" + fieldName + "' matching '" + regex
            + "' with replacement '" + replacement + "'");
        try {
            JAXBContext jc = JAXBContext.newInstance(AddDocType.class);
            Unmarshaller u = jc.createUnmarshaller();
            AddDocType addDoc = (AddDocType) u.unmarshal(in);
            List<FieldType> fields = addDoc.getFields(fieldName);
            for (FieldType field : fields) {
                String value = field.getValue();
                String newValue = value.replaceAll(regex, replacement);
                if (value.equals(newValue)) {
                    log.info("Value '" + value + "' was unmodified");
                } else {
                    field.setValue(newValue);
                    log.info("Modified value '" + value + "' to '" + newValue
                        + "'");
                }
            }
            Marshaller m = jc.createMarshaller();
            m.marshal(addDoc, out);
        } catch (JAXBException jaxbe) {
            throw new RuleException("Failed to modify field: " + fieldName,
                jaxbe);
        }
    }
}
