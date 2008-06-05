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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Result {

    private String name;

    private int numFound;

    private int start;

    private List<Document> docs;

    public Result(Element elem) {
        this(elem.getAttribute("name"),
            Integer.parseInt(elem.getAttribute("numFound")),
            Integer.parseInt(elem.getAttribute("start")));
        NodeList docNodes = elem.getElementsByTagName("doc");
        for (int i = 0; i < docNodes.getLength(); i++) {
            Element docElem = (Element) docNodes.item(i);
            docs.add(new Document(docElem));
        }
    }

    public Result(String name, int numFound, int start) {
        this.name = name;
        this.numFound = numFound;
        this.start = start;
        docs = new ArrayList<Document>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumFound() {
        return numFound;
    }

    public int getStart() {
        return start;
    }

    public List<Document> getDocs() {
        return docs;
    }
}
