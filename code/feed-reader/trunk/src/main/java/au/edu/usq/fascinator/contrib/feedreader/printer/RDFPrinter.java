/*
 * Copyright (C) 2010 University of Southern Queensland
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
package au.edu.usq.fascinator.contrib.feedreader.printer;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.htmlparser.util.ParserException;
import org.ontoware.rdf2go.ModelFactory;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.impl.PlainLiteralImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.contrib.feedreader.util.FeedHelper;
import au.edu.usq.fascinator.contrib.feedreader.util.PlainTextExtractor;
import au.edu.usq.fascinator.contrib.feedreader.vocabulary.dc.Thing;
import au.edu.usq.fascinator.contrib.feedreader.vocabulary.nie.InformationElement;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndPerson;

/**
 * Provides functionality to convert a feed entry into an RDF representation
 * 
 * @author Duncan Dickinson
 * 
 */
public class RDFPrinter {
    private static Logger log = LoggerFactory.getLogger(RDFPrinter.class);

    /**
     * Uses DC and NIE to create an RDF representation of the feed
     * 
     * @param entry
     * @return RDF/XML representation of the feed.
     */
    public static String toRDFXML(SyndEntry entry) {
        ModelFactory modelFactory = RDF2Go.getModelFactory();
        Model model = modelFactory.createModel();
        model.open();

        String uri = FeedHelper.getID(entry);

        // Create a new DC RDF model
        Thing dc = new Thing(model, uri, true);

        // Set the dc.identifier
        dc.setDcIdentifier(new PlainLiteralImpl(uri));

        // Set title
        dc.setDcTitle(new PlainLiteralImpl(entry.getTitle()));

        // Add Description
        if (entry.getDescription() != null) {
            dc.addDcDescription(new PlainLiteralImpl(entry.getDescription()
                    .getValue()));
        }

        // Set authors
        for (SyndPerson author : FeedHelper.getAuthors(entry)) {
            dc.addDcCreator(new PlainLiteralImpl(author.getName()));
        }

        // Creation/Publish date
        if (entry.getPublishedDate() != null) {
            dc.setDcDateCreated(new PlainLiteralImpl(formatDate(entry
                    .getPublishedDate())));
        }

        // Modified date
        if (entry.getUpdatedDate() != null) {
            String d = formatDate(entry.getUpdatedDate());
            dc.setDcDateModified(new PlainLiteralImpl(d));
        }

        // Add categories
        for (SyndCategory category : FeedHelper.getCategories(entry)) {
            dc.addDcSubject(new PlainLiteralImpl(category.getName()));
        }

        // Add any referenced links
        for (SyndLink link : FeedHelper.getLinks(entry)) {
            dc.addDcReferences(new PlainLiteralImpl(link.getHref()));
        }

        // Abstract
        if (entry.getDescription() != null) {
            SyndContent desc = entry.getDescription();
            String text;
            try {
                text = PlainTextExtractor.getPlainText(desc.getType(), desc
                        .getValue());
            } catch (ParserException e) {
                text = "Failed to parse abstract";
            }
            if (text != null) dc.setDcDescription(new PlainLiteralImpl(text));
        }

        // Plain text

        InformationElement nie = new InformationElement(model, uri, true);

        StringBuilder plainText = new StringBuilder();
        for (SyndContent content : FeedHelper.getContents(entry)) {
            try {
                plainText.append(PlainTextExtractor.getPlainText(content
                        .getType(), content.getValue()));
            } catch (ParserException e) {
                plainText.append("Failed to parse content");
            }
        }
        nie.setNepoPlainTextContent(plainText.toString());

        StringWriter rdfXML = new StringWriter();

        try {
            model.writeTo(rdfXML);
        } catch (ModelRuntimeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        model.close();
        return rdfXML.toString();
    }

    /**
     * (Hopefully) formats a date to W3C standard
     * 
     * @param d
     * @return
     */
    public static String formatDate(Date d) {
        if (d == null) return null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return df.format(d);
    }

}
