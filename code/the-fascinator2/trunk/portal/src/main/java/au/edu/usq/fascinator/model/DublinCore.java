/* 
 * The Fascinator - Solr Portal
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
package au.edu.usq.fascinator.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dc", namespace = "http://www.openarchives.org/OAI/2.0/oai_dc/")
@XmlAccessorType(XmlAccessType.NONE)
public class DublinCore {

    @XmlElement(name = "title", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> title;

    @XmlElement(name = "creator", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> creator;

    @XmlElement(name = "subject", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> subject;

    @XmlElement(name = "description", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> description;

    @XmlElement(name = "publisher", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> publisher;

    @XmlElement(name = "contributor", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> contributor;

    @XmlElement(name = "date", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> date;

    @XmlElement(name = "type", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> type;

    @XmlElement(name = "format", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> format;

    @XmlElement(name = "identifier", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> identifier;

    @XmlElement(name = "source", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> source;

    @XmlElement(name = "language", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> language;

    @XmlElement(name = "relation", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> relation;

    @XmlElement(name = "coverage", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> coverage;

    @XmlElement(name = "rights", namespace = "http://purl.org/dc/elements/1.1/")
    private List<String> rights;

    private Map<String, List<String>> fields;

    public List<String> getTitle() {
        return title;
    }

    public List<String> getCreator() {
        return creator;
    }

    public List<String> getSubject() {
        return subject;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<String> getPublisher() {
        return publisher;
    }

    public List<String> getContributor() {
        return contributor;
    }

    public List<String> getDate() {
        return date;
    }

    public List<String> getType() {
        return type;
    }

    public List<String> getFormat() {
        return format;
    }

    public List<String> getIdentifier() {
        return identifier;
    }

    public List<String> getSource() {
        return source;
    }

    public List<String> getLanguage() {
        return language;
    }

    public List<String> getRelation() {
        return relation;
    }

    public List<String> getCoverage() {
        return coverage;
    }

    public List<String> getRights() {
        return rights;
    }

    public Map<String, List<String>> getAllFields() {
        if (fields == null) {
            fields = new HashMap<String, List<String>>();
            addFields("Title", title);
            addFields("Creator", creator);
            addFields("Subject", subject);
            addFields("Description", description);
            addFields("Contributor", contributor);
            addFields("Date", date);
            addFields("Type", type);
            addFields("Format", format);
            addFields("Identifier", identifier);
            addFields("Source", source);
            addFields("Language", language);
            addFields("Relation", relation);
            addFields("Coverage", coverage);
            addFields("Rights", rights);
        }
        return fields;
    }

    public Map<String, List<String>> getFields() {
        Map<String, List<String>> map = getAllFields();
        map.remove("Title");
        map.remove("Description");
        return map;
    }

    private void addFields(String name, List<String> value) {
        if (value != null && !value.isEmpty()) {
            fields.put(name, value);
        }
    }
}
