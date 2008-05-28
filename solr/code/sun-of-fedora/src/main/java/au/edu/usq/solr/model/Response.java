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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Response {

    private Logger log = Logger.getLogger(Response.class);

    private long queryTime;

    private List<FacetList> facetLists;

    private Result result;

    public Response(InputStream in) {
        facetLists = new ArrayList<FacetList>();
        process(in);
    }

    public double getQueryTime() {
        return queryTime / 1000.0;
    }

    public List<FacetList> getFacetLists() {
        return facetLists;
    }

    public Result getResult() {
        return result;
    }

    private void process(InputStream in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document domDoc = db.parse(in);

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();
            StringBuilder xpe;

            xpe = new StringBuilder("/response");
            xpe.append("/lst[@name='responseHeader']");
            xpe.append("/int[@name='QTime']");
            queryTime = Long.parseLong(xpath.evaluate(xpe.toString(), domDoc));

            // process facets
            xpe = new StringBuilder("/response");
            xpe.append("/lst[@name='facet_counts']");
            xpe.append("/lst[@name='facet_fields']");
            xpe.append("/lst");
            NodeList facetFieldNodes = (NodeList) xpath.evaluate(
                xpe.toString(), domDoc, XPathConstants.NODESET);

            for (int i = 0; i < facetFieldNodes.getLength(); i++) {
                Element facetFieldElem = (Element) facetFieldNodes.item(i);
                FacetList facetList = new FacetList(facetFieldElem);
                facetLists.add(facetList);
            }

            // process results
            Element resultElem = (Element) xpath.evaluate("/response/result",
                domDoc, XPathConstants.NODE);
            result = new Result(resultElem);

        } catch (ParserConfigurationException e) {
            log.error("Failed to create DocumentBuilder", e);
        } catch (XPathExpressionException e) {
            log.error("Invalid XPath expression", e);
        } catch (SAXException e) {
            log.error("Failed to parse source document", e);
        } catch (IOException e) {
            log.error("Failed to read source documen", e);
        }
    }
}
