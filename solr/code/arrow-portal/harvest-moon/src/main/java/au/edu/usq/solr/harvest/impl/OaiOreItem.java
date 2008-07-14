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
package au.edu.usq.solr.harvest.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dspace.foresite.AggregatedResource;
import org.dspace.foresite.OREException;
import org.dspace.foresite.ResourceMap;
import org.dspace.foresite.Triple;

import au.edu.usq.solr.harvest.Datastream;
import au.edu.usq.solr.harvest.Item;
import au.edu.usq.solr.util.StreamUtils;

public class OaiOreItem implements Item {

    private Logger log = Logger.getLogger(OaiOreItem.class);
    private ResourceMap rem;
    private String id;
    private Element metadata;
    private String stringMetadata;
    private ArrayList<Trippy> ArrayListOfTripleObjects = new ArrayList<Trippy>();
    private List<Datastream> dataStreamList = new ArrayList<Datastream>();

    public OaiOreItem(ResourceMap rem) {
        this.setResourceMap(rem);
        this.setId();
        this.populateTrippyArrayList();
        setMetadataAsString();
        this.populateDatastreamList();
    }

    private void populateDatastreamList() {
        ArrayList<String> itemsAdded = new ArrayList<String>();
        int datastreamPid = 1;
        for (Trippy iterator : this.ArrayListOfTripleObjects) {
            boolean alreadyAdded = false;
            if ("application/pdf".equalsIgnoreCase(iterator.getObjectLiteral())) {

                for (String singleItem : itemsAdded) {
                    if (singleItem.equalsIgnoreCase(iterator.getSubject()
                        .toString()))
                        ;
                    alreadyAdded = true;
                }
                if (alreadyAdded == false) {
                    String datastreamId = iterator.getSubject().toString();
                    String mimeType = iterator.getObjectLiteral();
                    OaiOreDatastream dStream = new OaiOreDatastream(
                        datastreamId, Integer.toString(datastreamPid), mimeType);
                    dataStreamList.add(dStream);
                    itemsAdded.add(datastreamId);
                    datastreamPid = datastreamPid + 1;

                }

            }

        }
    }

    private void setResourceMap(ResourceMap rem) {
        this.rem = rem;
    }

    private void setId() {
        try {
            this.id = this.trimString(this.rem.getURI().toString());

        } catch (OREException e) {
            System.out.println("Unable to fetch URI from Resource Map");
            e.printStackTrace();
        }
    }

    private void setMetadataAsString() {
        URI u = null;
        for (Trippy t : this.ArrayListOfTripleObjects)
            if ("OAI_DC".equals(t.objectLiteral)) {
                u = t.getSubject();
                String s = u.toString();
                InputStream in = null;
                try {
                    in = u.toURL().openStream();
                } catch (MalformedURLException e) {
                    System.out.println("The following url is malformed ");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Unable to contact url IO Exception");
                    e.printStackTrace();
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    StreamUtils.copyStream(in, out);
                } catch (IOException e) {
                    System.out.println("IO Exception");
                    e.printStackTrace();
                }
                try {
                    in.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
                try {
                    this.stringMetadata = out.toString("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    System.out.println("Unable to convert URL's contents to UTF-8");
                    e.printStackTrace();
                }

            }

    }

    private String trimString(String longId) {
        String shortString = null;
        int start = 0;
        int end = longId.length();
        if (longId.contains("/")) {
            start = longId.lastIndexOf("/") + 1;
        }
        if (longId.contains(".xml")) {
            end = longId.indexOf(".xml");
        }
        shortString = longId.substring(start, end);
        return shortString;

    }

    public String getId() {
        return this.id;
    }

    private void setMetadata() {
    }

    public Element getMetadata() {
        return this.metadata;
    }

    public String getMetadataAsString() {
        return this.stringMetadata;

    }

    public boolean hasDatastreams() {
        return false;
    }

    public List<Datastream> getDatastreams() {
        return this.dataStreamList;
    }

    public Datastream getDatastream(String dsId) {
        return null;
    }

    private void populateTrippyArrayList() {
        URI objectUri = null;
        String objectLiteral = null;
        try {
            for (AggregatedResource iterator : this.rem.getAggregatedResources()) {
                for (Triple t : iterator.listAllTriples()) {
                    URI subjectUri = t.getSubjectURI();
                    URI predicateUri = t.getPredicate().getURI();
                    try {
                        objectUri = t.getObjectURI();
                    } catch (Exception e) {
                        System.out.println("No Object URI!");
                    } finally {
                    }

                    try {
                        objectLiteral = t.getObjectLiteral();
                    } catch (Exception e) {
                        System.out.println("No Object Literal!");
                    } finally {
                    }
                    Trippy trip = new Trippy(subjectUri, predicateUri,
                        objectUri, objectLiteral);
                    this.ArrayListOfTripleObjects.add(trip);
                }
            }
        } catch (OREException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private class Trippy {
        private URI subject;
        private URI predicate;
        private URI objectUri;
        private String objectLiteral;

        public Trippy(URI subject, URI predicate, URI objectUri,
            String objectLiteral) {
            this.setSubject(subject);
            this.setPredicate(predicate);
            this.setObjectUri(objectUri);
            this.setObjectLiteral(objectLiteral);
        }

        private void setSubject(URI subject) {
            this.subject = subject;
        }

        private void setPredicate(URI predicate) {
            this.predicate = predicate;
        }

        private void setObjectUri(URI objectUri) {
            this.objectUri = objectUri;
        }

        private void setObjectLiteral(String objectLiteral) {
            this.objectLiteral = objectLiteral;
        }

        private URI getSubject() {
            return this.subject;
        }

        private URI getPredicate() {
            return this.predicate;
        }

        private URI getObjectUri() {
            return this.objectUri;
        }

        private String getObjectLiteral() {
            return this.objectLiteral;
        }

    }

}