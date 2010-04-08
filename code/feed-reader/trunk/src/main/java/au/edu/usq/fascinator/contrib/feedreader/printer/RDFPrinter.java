package au.edu.usq.fascinator.contrib.feedreader.printer;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

public class RDFPrinter {
    private static Logger log = LoggerFactory.getLogger(RDFPrinter.class);

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

        // Last modified date
        if (entry.getUpdatedDate() != null) {
            Calendar calMod = Calendar.getInstance();
            calMod.setTime(entry.getUpdatedDate());
            // nie.setNepoContentLastModified(calMod);
        }

        // Creation/Publish date
        if (entry.getUpdatedDate() != null) {
            dc.setDcDateModified(new PlainLiteralImpl(formatDate(entry
                    .getPublishedDate())));
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
        SyndContent desc = entry.getDescription();
        if (desc != null) {
            dc.setDcDescription(new PlainLiteralImpl(PlainTextExtractor
                    .getPlainText(desc.getType(), desc.getValue())));
        }

        // Plain text

        InformationElement nie = new InformationElement(model, uri, true);

        StringBuilder plainText = new StringBuilder();
        for (SyndContent content : FeedHelper.getContents(entry)) {
            plainText.append(PlainTextExtractor.getPlainText(content.getType(),
                    content.getValue()));
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

    public static String formatDate(Date d) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return df.format(d);
    }

}
