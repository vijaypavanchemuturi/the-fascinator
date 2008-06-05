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
package au.edu.usq.solr.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Document {

    private Logger log = Logger.getLogger(Document.class);

    private String id;

    private String title;

    private List<String> identifiers;

    private String description;

    public Document(Element elem) {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        try {
            id = xpath.evaluate("./*[@name='id']", elem);
            title = xpath.evaluate("./*[@name='title']", elem);
            identifiers = new ArrayList<String>();
            NodeList identifierNodes = (NodeList) xpath.evaluate(
                "./*[@name='identifier']/str", elem, XPathConstants.NODESET);
            for (int i = 0; i < identifierNodes.getLength(); i++) {
                Element idenifierElem = (Element) identifierNodes.item(i);
                identifiers.add(idenifierElem.getTextContent());
            }
            description = xpath.evaluate("./*[@name='description']", elem);
        } catch (XPathExpressionException e) {
            log.error("Invalid XPath expression", e);
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public String getDescription() {
        return description;
    }
}
