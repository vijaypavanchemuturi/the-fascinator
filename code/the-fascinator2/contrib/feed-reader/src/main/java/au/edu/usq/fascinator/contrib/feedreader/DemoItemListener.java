/*
 * The Fascinator
 * Copyright (C) 2009 University of Southern Queensland
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
package au.edu.usq.fascinator.contrib.feedreader;

import java.util.List;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.sun.syndication.feed.module.DCModuleImpl;
import com.sun.syndication.feed.module.DCSubject;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.fetcher.FetcherEvent;

/**
 * A demonstration listener to illustrate the fields being returned.
 * 
 * You'll probably want to some more specific with your listener.
 * 
 * @author dickinso
 * 
 */
public class DemoItemListener extends ItemListener {
    /*
     * (non-Javadoc)
     * 
     * @see au.edu.usq.AtomReaderStateChangeListener#atomReaderStateChangeEvent
     * (au.edu.usq.AtomReaderStateChangeEvent)
     */
    public void feedReaderStateChangeEvent(FeedReaderStateChangeEvent event) {
        super.feedReaderStateChangeEvent(event);
        System.out.println("Event: " + event.getEventType() + "\n");

        if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(event.getEventType())) {
            System.out.println("URL: " + this.getFeedURL() + "\n");
            DemoItemListener.displayFeedItems(this.getFeed());
        } else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(event
                .getEventType())) {
            System.out.println("No change in feed\n");
        }

    }

    public static void displayFeedItems(SyndFeed feed) {
        List<SyndEntry> itemList = feed.getEntries();

        System.out.println("Items: " + itemList.size() + "\n");

        for (SyndEntry entry : itemList) {
            System.out.println("URI: " + entry.getUri() + "\n" + "Title: "
                    + entry.getTitle() + "\n" + "\n" + "Date: "
                    + entry.getPublishedDate() + "\n" + "Modified: "
                    + entry.getUpdatedDate() + "\n");

            System.out.println("Creators: \n");

            for (SyndPerson author : (List<SyndPerson>) FeedHelper
                    .getAuthors(entry)) {
                System.out.println("  - " + author.getName() + "\n");
            }

            System.out.println("Links: \n");
            for (SyndLink link : (List<SyndLink>) FeedHelper.getLinks(entry)) {
                System.out.println("  - " + link.getTitle() + ": "
                        + link.getHref() + "\n");
            }

            SyndContent description = entry.getDescription();
            if (description != null) {
                System.out.println("\nDescription(" + description.getType()
                        + "): " + description.getValue());
            }

            System.out.println("Contents: \n");
            for (SyndContent content : (List<SyndContent>) 
                    FeedHelper.getContents(entry)) {
                System.out.println(" Type: " + content.getType());
                System.out.println(" Body: " + content.getValue());
                System.out.println(" Body (Plain text): "
                        + PlainTextExtractor.getPlainText(content.getType(),
                                content.getValue()));
            }

            System.out.println("Categories: \n");
            for (SyndCategory category : (List<SyndCategory>) FeedHelper.getCategories(entry)) {
                System.out.println(category.getName() + "("
                        + category.getTaxonomyUri() + ")");
            }

            System.out.println(FeedHelper.toRDFXML(entry));
            try {
                System.out.println(FeedHelper.toXHTMLSegment(entry));
            } catch (ResourceNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseErrorException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("\n");
        }
    }
}
