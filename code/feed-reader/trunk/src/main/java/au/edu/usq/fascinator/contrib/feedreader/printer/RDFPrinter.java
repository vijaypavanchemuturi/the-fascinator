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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.htmlparser.util.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.contrib.feedreader.util.FeedHelper;
import au.edu.usq.fascinator.contrib.feedreader.util.PlainTextExtractor;
import au.edu.usq.fascinator.contrib.feedreader.vocabulary.NIE;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
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
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("nie", NIE.NS);
        model.setNsPrefix("dc", DCTerms.NS);

        String identifier = FeedHelper.getID(entry);
        // create the resource
        Resource post = model.createResource(identifier);
        post.addProperty(DCTerms.identifier, identifier);
        post.addProperty(DCTerms.title, entry.getTitle());

        // Set authors
        for (SyndPerson author : FeedHelper.getAuthors(entry)) {
            post.addProperty(DCTerms.creator, author.getName());
        }

        // Creation/Publish date

        if (entry.getPublishedDate() != null) {
            post.addProperty(DCTerms.created, formatDate(entry
                    .getPublishedDate()));
        }

        // Modified date
        if (entry.getUpdatedDate() != null) {
            post.addProperty(DCTerms.modified, formatDate(entry
                    .getUpdatedDate()));
        }

        // Add categories
        for (SyndCategory category : FeedHelper.getCategories(entry)) {
            post.addProperty(DCTerms.subject, category.getName());
        }

        // Add any referenced links
        for (SyndLink link : FeedHelper.getLinks(entry)) {
            post.addProperty(DCTerms.references, link.getHref());
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
            if (text != null) post.addProperty(DCTerms.description, text);
        }

        // Plain text
        StringBuilder plainText = new StringBuilder();
        for (SyndContent content : FeedHelper.getContents(entry)) {
            try {
                plainText.append(PlainTextExtractor.getPlainText(content
                        .getType(), content.getValue()));
            } catch (ParserException e) {
                plainText.append("Failed to parse content");
            }
        }
        post.addProperty(NIE.plainTextContent, plainText.toString());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        model.write(os);
        return os.toString();
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
