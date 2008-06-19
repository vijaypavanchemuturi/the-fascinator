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
package au.edu.usq.solr.util;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class OaiDcNsContext implements NamespaceContext {

    public static final String DC_PREFIX = "dc";

    public static final String DC_NS = "http://purl.org/dc/elements/1.1/";

    public static final String OAI_DC_PREFIX = "oai_dc";

    public static final String OAI_DC_NS = "http://www.openarchives.org/OAI/2.0/oai_dc/";

    public String getNamespaceURI(String prefix) {
        if (DC_PREFIX.equals(prefix)) {
            return DC_NS;
        } else if (OAI_DC_PREFIX.equals(prefix)) {
            return OAI_DC_NS;
        }
        return null;
    }

    public String getPrefix(String ns) {
        if (DC_NS.equals(ns)) {
            return DC_PREFIX;
        } else if (OAI_DC_NS.equals(ns)) {
            return OAI_DC_PREFIX;
        }
        return null;
    }

    public Iterator getPrefixes(String ns) {
        return null;
    }
}
