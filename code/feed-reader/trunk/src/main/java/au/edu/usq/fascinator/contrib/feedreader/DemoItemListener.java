/*
 * The Fascinator Copyright (C) 2009 University of Southern Queensland This
 * program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package au.edu.usq.fascinator.contrib.feedreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.contrib.feedreader.printer.HTMLPrinter;
import au.edu.usq.fascinator.contrib.feedreader.printer.RDFPrinter;
import au.edu.usq.fascinator.contrib.feedreader.util.FeedHelper;
import au.edu.usq.fascinator.contrib.feedreader.util.PlainTextExtractor;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.impl.DiskFeedInfoCache;

/**
 * A demonstration listener to illustrate the fields being returned. You'll
 * probably want to some more specific with your listener.
 * 
 * @author Duncan Dickinson
 */
public class DemoItemListener extends ItemListener {

	private boolean html = true;
	private File htmlFile = null;
	private boolean rdf = true;
	private File rdfFile = null;
	private String baseFolder = null;
	private String outputFolder;

	/**
	 * Generic logging
	 */
	private static Logger log = LoggerFactory.getLogger(DemoItemListener.class);

	public DemoItemListener(boolean html, boolean rdf, String outputFolder,
			URL url) throws FileNotFoundException {
		super();
		this.html = html;
		this.rdf = rdf;
		setOutputFolder(outputFolder);
		this.baseFolder = this.outputFolder
				+ DiskFeedInfoCache.replaceNonAlphanumeric(
						url.toExternalForm(), '_') + File.separator;
	}

	/**
	 * @param outputFolder
	 *            the outputFolder to set
	 * @throws FileNotFoundException
	 */
	public void setOutputFolder(String outputFolder)
			throws FileNotFoundException {
		if (!outputFolder.endsWith(File.separator)) {
			this.outputFolder = outputFolder + File.separator;
		} else {
			this.outputFolder = outputFolder;
		}
		new File(this.outputFolder).mkdirs();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.edu.usq.AtomReaderStateChangeListener#atomReaderStateChangeEvent
	 * (au.edu.usq.AtomReaderStateChangeEvent)
	 */
	@Override
	public void feedReaderStateChangeEvent(FeedReaderStateChangeEvent event) {
		super.feedReaderStateChangeEvent(event);

		log.debug("Event (" + this.getFeedURL() + "): " + event.getEventType());

		if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(event.getEventType())) {
			saveFeedItems();
		} else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(event
				.getEventType())) {
			log.info("No change in feed: " + this.getFeedURL());
		}

	}

	public void saveFeedItems() {
		String rdfFileName = null;
		String htmlFileName = null;
		FileWriter rdfFile = null;
		FileWriter htmlFile = null;

		String propertiesFileName = "feed-reader.properties";

		List<SyndEntry> itemList = this.getFeed().getEntries();

		for (SyndEntry entry : itemList) {
			printEntry(entry);
			if (!rdf && !html) {
				continue;
			}
			String itemFileBase = baseFolder
					+ DiskFeedInfoCache.replaceNonAlphanumeric(FeedHelper
							.getID(entry), '_');
			new File(baseFolder).mkdirs();

			if (rdf) {
				try {
					rdfFileName = itemFileBase + ".rdf";
					rdfFile = new FileWriter(rdfFileName);
					rdfFile.write(RDFPrinter.toRDFXML(entry));
					rdfFile.flush();
				} catch (IOException e) {
					log.error("Failed writing to " + rdfFileName + ": "
							+ e.getMessage());
					break;
				} finally {
					if (rdfFile != null) {
						try {
							rdfFile.close();
						} catch (IOException e) {
							log.error("Error closing " + rdfFileName + ": "
									+ e.getMessage());
						}
					}
				}

			}
			if (html) {
				Properties props = new Properties();
				FileInputStream in;
				String templateFile = null;
				try {
					try {
						htmlFileName = itemFileBase + ".html";
						htmlFile = new FileWriter(htmlFileName);
						in = new FileInputStream(propertiesFileName);
						props.load(in);
						in.close();
					} catch (FileNotFoundException e) {
						log.error("Could not load " + propertiesFileName
								+ " file: " + e.getMessage());
						break;
					} catch (IOException e) {
						log.error("Couldn't read " + propertiesFileName
								+ " file: " + e.getMessage());
						break;
					}
					templateFile = props.getProperty("html.template");

					try {
						htmlFile.write(HTMLPrinter.toXHTMLSegment(entry,
								templateFile));
						htmlFile.flush();
					} catch (ResourceNotFoundException e) {
						log.error("Could not find template " + templateFile
								+ ": " + e.getMessage());
						break;
					} catch (ParseErrorException e) {
						log.error("Could not parse template " + templateFile
								+ ": " + e.getMessage());
						break;
					} catch (IOException e) {
						log.error("Failed writing to " + htmlFileName + ": "
								+ e.getMessage());
						break;
					} catch (Exception e) {
						log.error(e.getMessage());
						break;
					}
				} finally {
					if (htmlFile != null) {
						try {
							htmlFile.close();
						} catch (IOException e) {
							log.error("Error closing " + htmlFileName + ": "
									+ e.getMessage());
						}
					}
				}

			}
		}

	}

	public static void printEntry(SyndEntry entry) {
		if (!log.isTraceEnabled()) {
			return;
		}

		StringBuffer pr = new StringBuffer("URI: " + entry.getUri() + "\n"
				+ "Title: " + entry.getTitle() + "\n" + "\n" + "Date: "
				+ entry.getPublishedDate() + "\n" + "Modified: "
				+ entry.getUpdatedDate() + "\n");

		pr.append("Creators: \n");

		for (SyndPerson author : FeedHelper.getAuthors(entry)) {
			pr.append("  - " + author.getName() + "\n");
		}

		pr.append("Links: \n");
		for (SyndLink link : FeedHelper.getLinks(entry)) {
			pr.append("  - " + link.getTitle() + ": " + link.getHref() + "\n");
		}

		SyndContent description = entry.getDescription();
		if (description != null) {
			pr.append("\nDescription(" + description.getType() + "): "
					+ description.getValue());
		}

		pr.append("Contents: \n");
		for (SyndContent content : FeedHelper.getContents(entry)) {
			pr.append(" Type: " + content.getType());
			pr.append(" Body: " + content.getValue());
			pr.append(" Body (Plain text): "
					+ PlainTextExtractor.getPlainText(content.getType(),
							content.getValue()));
		}

		pr.append("Categories: \n");
		for (SyndCategory category : FeedHelper.getCategories(entry)) {
			pr.append(category.getName() + "(" + category.getTaxonomyUri()
					+ ")");
		}
		log.trace(pr.toString());
	}
}
