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

import java.util.HashSet;
import java.util.List;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
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
			DemoItemListener.displayFeedItems(this.getFeedItems());
		} else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(event
				.getEventType())) {
			System.out.println("No change in feed\n");
		}

	}
	
	public static void displayFeedItems(HashSet<FeedItem> itemList) {
		System.out.println("Items: " + itemList.size() + "\n");

		for (FeedItem i : itemList) {
			System.out.println("URI: " + i.getId() + "\n" +  "Title: "
					+ i.getTitle() + "\n" + "Link: " + i.getLink() + "\n"+ "Date: " + i.getDate() + "\n"
					+ "Modified: " + i.getModified() + "\n");

			for (String creator : i.getCreators()) {
				System.out.println("Creator: " + creator + "\n");
			}

			System.out.println("\nDescription(" + i.getDescriptionType()
					+ "): " + i.getDescription());

			System.out.println("Contents: \n");
			for (FeedItem.TypeValueItem fulltext : i.getFulltext()) {
				System.out.println(" Type: " + fulltext.getType());
				System.out.println(" Body: " + fulltext.getValue());
			}

			System.out.println("\n");
		}
	}

}
