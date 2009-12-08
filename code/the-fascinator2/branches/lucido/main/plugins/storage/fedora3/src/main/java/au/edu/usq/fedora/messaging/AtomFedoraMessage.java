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
package au.edu.usq.fedora.messaging;

import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "entry", namespace = AtomFedoraMessage.ATOM_URI)
@XmlAccessorType(XmlAccessType.NONE)
public class AtomFedoraMessage {

    public static final String ATOM_URI = "http://www.w3.org/2005/Atom";

    @XmlElement(name = "id", namespace = ATOM_URI)
    private String id;

    @XmlElement(name = "updated", namespace = ATOM_URI)
    private String updated;

    @XmlElement(name = "author", namespace = ATOM_URI)
    private AtomAuthor author;

    @XmlElement(name = "title", namespace = ATOM_URI)
    private String title;

    @XmlElement(name = "summary", namespace = ATOM_URI)
    private String summary;

    @XmlElement(name = "category", namespace = ATOM_URI)
    private List<AtomCategory> categories;

    private Properties props;

    public String getId() {
        return id;
    }

    public String getUpdated() {
        return updated;
    }

    public AtomAuthor getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public Properties getProperties() {
        if (props == null) {
            props = new Properties();
        }
        for (AtomCategory cat : categories) {
            String value = cat.getTerm();
            if (!"null".equals(value)) {
                props.setProperty(cat.getScheme(), value);
            }
        }
        return props;
    }

    public String getProperty(String name) {
        String value = getProperties().getProperty(name);
        if (value == null) {
            // try with 'fedora-types:'
            value = getProperties().getProperty("fedora-types:" + name);
        }
        return value;
    }
}
