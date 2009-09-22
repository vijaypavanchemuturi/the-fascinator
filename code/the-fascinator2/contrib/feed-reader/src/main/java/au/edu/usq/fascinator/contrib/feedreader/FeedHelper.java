package au.edu.usq.fascinator.contrib.feedreader;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ontoware.rdf2go.ModelFactory;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.impl.PlainLiteralImpl;

import au.edu.usq.fascinator.common.nco.Contact;
import au.edu.usq.fascinator.common.nco.EmailAddress;
import au.edu.usq.fascinator.common.nie.InformationElement;
import au.edu.usq.fascinator.common.nie.Thing;

import com.sun.syndication.feed.module.DCModuleImpl;
import com.sun.syndication.feed.module.DCSubject;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndLinkImpl;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl;

public class FeedHelper {

	public static List<SyndPerson> getAuthors(SyndEntry entry) {
		if (entry.getAuthors().size() > 0) {
			return entry.getAuthors();

		}
		ArrayList<SyndPerson> authors = new ArrayList<SyndPerson>();
		SyndPerson author = new SyndPersonImpl();
		author.setName(entry.getAuthor());
		authors.add(author);
		return authors;

	}

	public static List<SyndLink> getLinks(SyndEntry entry) {
		if (entry.getLinks().size() > 0) {
			return entry.getLinks();
		}
		ArrayList<SyndLink> links = new ArrayList<SyndLink>();
		SyndLinkImpl link = new SyndLinkImpl();
		link.setHref(entry.getLink());
		links.add(link);
		return links;
	}

	public static List<SyndContent> getContents(SyndEntry entry) {
		return entry.getContents();
	}

	public static List<SyndCategory> getCategories(SyndEntry entry) {
		ArrayList<SyndCategory> categories = new ArrayList<SyndCategory>();
		categories.addAll(entry.getCategories());

		/*
		 * Services such as Slashdot seem to have their tags/categories within
		 * the Dublin Core
		 */
		DCModuleImpl dc = (DCModuleImpl) entry
				.getModule(com.sun.syndication.feed.module.DCModule.URI);
		if (dc != null) {
			for (DCSubject dcSubject : (List<DCSubject>) dc.getSubjects()) {
				SyndCategoryImpl category = new SyndCategoryImpl();
				category.setName(dcSubject.getValue());
				category.setTaxonomyUri(dcSubject.getTaxonomyUri());
				categories.add(category);
			}
		}
		return categories;
	}

	public static String dateToXMLDate(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
		return df.format(date);
	}

	public static String toRDFXML(SyndEntry entry) {
		ModelFactory modelFactory = RDF2Go.getModelFactory();
		Model model = modelFactory.createModel();
		model.open();
		InformationElement nie = new InformationElement(model, entry.getUri(),
				true);
		nie.setTitle(entry.getTitle());

		/*
		 * model.addStatement(nie.getResource(),
		 * InformationElement.CONTENTCREATED, dateToXMLDate(entry
		 * .getPublishedDate()), new URIImpl(
		 * "http://www.w3.org/2001/XMLSchema#date"));
		 */

		// Add Description
		if (entry.getDescription() != null) {
			String description = entry.getDescription().getValue();
			PlainLiteralImpl node = new PlainLiteralImpl(description);
			nie.addDescription(node);
			
			//model.addStatement(nie.getResource(), Thing.DESCRIPTION, model
			//		.createPlainLiteral(description));
		}

		// Add links
		/*
		for (SyndLink link : (List<SyndLink>) FeedHelper.getLinks(entry)) {
			System.out.println("  - " + link.getTitle() + ": " + link.getHref()
					+ "\n");
			Bookmark bookmark = new Bookmark(model, true);
			InformationElement bookmarkNie = new InformationElement(model, true);
			bookmarkNie.setTitle(entry.getTitle());

			model.addStatement(bookmark.getResource(),
					InformationElement.TITLE, bookmarkNie.getResource());
			model.addStatement(nie.getResource(),
			 au.usq.edu.fascinator.common.nfo.InformationElement., bookmark
			 .getResource());
		}
		*/

		// Add categories
		for (SyndCategory category : (List<SyndCategory>) FeedHelper
				.getCategories(entry)) {
			nie.addKeyword(category.getName());
		}

		for (SyndPerson author : (List<SyndPerson>) FeedHelper
				.getAuthors(entry)) {
			Contact contact = null;
			if (author.getUri() == null) {
				contact = new Contact(model, true);
			} else {
				contact = new Contact(model, author.getUri(), true);
			}
			contact.setFullname(author.getName());
			if (author.getEmail() != null) {
				EmailAddress email = new EmailAddress(model, author.getUri(),
						false);
				email.setEmailAddress(author.getEmail());
			}

			contact.getAllIdentifier_asNode();
			model
					.addStatement(
							nie.getResource(),
							au.edu.usq.fascinator.common.nco.InformationElement.CREATOR,
							contact.getResource());
		}

		StringBuilder plainText = new StringBuilder();
		for (SyndContent content : (List<SyndContent>) FeedHelper
				.getContents(entry)) {
			plainText.append(PlainTextExtractor.getPlainText(content.getType(),
					content.getValue()));
		}
		nie.addPlainTextContent(plainText.toString());

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
}
