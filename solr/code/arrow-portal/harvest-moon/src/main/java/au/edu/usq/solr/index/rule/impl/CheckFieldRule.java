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
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import au.edu.usq.solr.index.AddDocType;
import au.edu.usq.solr.index.FieldType;
import au.edu.usq.solr.index.rule.AbstractRule;
import au.edu.usq.solr.index.rule.RuleException;

public class CheckFieldRule extends AbstractRule {

    private Logger log = Logger.getLogger(CheckFieldRule.class);

    private String fieldName;

    private String regex;

    private boolean matchAll;

    public CheckFieldRule(String fieldName) {
        this(fieldName, ".+", false);
    }

    public CheckFieldRule(String fieldName, String regex) {
        this(fieldName, regex, false);
    }

    public CheckFieldRule(String fieldName, boolean matchAll) {
        this(fieldName, ".+", matchAll);
    }

    public CheckFieldRule(String fieldName, String regex, boolean matchAll) {
        super("CheckField", true);
        this.fieldName = fieldName;
        this.regex = regex;
        this.matchAll = matchAll;
    }

    @Override
    public void run(Reader in, Writer out) throws RuleException {
        log.info("Checking " + (matchAll ? "ALL '" : "AT LEAST ONE '")
            + fieldName + "' match '" + regex + "'");
        try {
            JAXBContext jc = JAXBContext.newInstance(AddDocType.class);
            Unmarshaller u = jc.createUnmarshaller();
            AddDocType addDoc = (AddDocType) u.unmarshal(in);
            List<FieldType> fields = addDoc.getFields(fieldName);
            int valid = 0;
            for (FieldType field : fields) {
                String value = field.getValue();
                boolean match = Pattern.matches(regex, field.getValue());
                if (match) {
                    valid++;
                    log.info("'" + value + "' matches");
                } else {
                    log.info("'" + value + "' does not match");
                }
            }
            int diff = fields.size() - valid;
            if (matchAll && diff > 0) {
                throw new RuleException("All " + fieldName
                    + " values must match " + regex);
            } else if (!matchAll && valid == 0) {
                throw new RuleException("At least one " + fieldName
                    + " value must match " + regex);
            } else {
                log.info("OK");
                Marshaller m = jc.createMarshaller();
                m.marshal(addDoc, out);
            }
        } catch (JAXBException jaxbe) {
            throw new RuleException(jaxbe.getLinkedException());
        }
    }
}
