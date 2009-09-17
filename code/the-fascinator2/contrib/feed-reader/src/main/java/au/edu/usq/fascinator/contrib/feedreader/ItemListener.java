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

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.fetcher.FetcherEvent;

/**
 * @author dickinso
 * 
 */
public class ItemListener implements FeedReaderStateChangeListener {

	private String feedURL = "";
	private SyndFeed feed;

	/**
	 * @return the feedURL
	 */
	public String getFeedURL() {
		return this.feedURL;
	}

	/**
	 * @return the feedItems
	 */
	public SyndFeed getFeed() {
		return this.feed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.edu.usq.fascinator.contrib.feedreader.FeedReaderStateChangeListener
	 * #feedReaderStateChangeEvent
	 * (au.edu.usq.fascinator.contrib.feedreader.FeedReaderStateChangeEvent)
	 */
	@Override
	public void feedReaderStateChangeEvent(FeedReaderStateChangeEvent event) {
		if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(event.getEventType())) {
			FeedReader ar = event.getReader();
			this.feedURL = ar.getFeedURLString();
			feed = ar.getFeed();
		}
	}
}
