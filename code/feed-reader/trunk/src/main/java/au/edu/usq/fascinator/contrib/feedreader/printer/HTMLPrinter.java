package au.edu.usq.fascinator.contrib.feedreader.printer;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.contrib.feedreader.util.FeedHelper;

import com.sun.syndication.feed.synd.SyndEntry;

public class HTMLPrinter {
    private static VelocityEngine ve = null;
    private static Logger log = LoggerFactory.getLogger(HTMLPrinter.class);

    public static String toXHTMLSegment(SyndEntry entry, String templateFile)
            throws ResourceNotFoundException, ParseErrorException,
            MethodInvocationException, IOException, Exception {

        if (ve == null) {
            ve = new VelocityEngine();
            ve.init("velocity.properties");
        }
        VelocityContext context = new VelocityContext();

        Template template = Velocity.getTemplate(templateFile);

        // Title
        context.put("title", entry.getTitle());

        // Published date
        context.put("date", entry.getPublishedDate());

        // Last Modified
        context.put("modified", entry.getUpdatedDate());

        // Subjects
        context.put("subject", FeedHelper.getCategories(entry));

        // Description
        if (entry.getDescription() != null) {
            context.put("description", entry.getDescription());
        }

        // Contents
        context.put("content", FeedHelper.getContents(entry));

        // Links
        context.put("relation", FeedHelper.getLinks(entry));

        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString();

    }
}
