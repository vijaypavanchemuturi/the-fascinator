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

/**
 * @author dickinso
 * 
 */
public class FeedModel {
	public static HashSet<FeedItem> packageFeedItems(SyndFeed feed) {
		HashSet<FeedItem> feedItems = new HashSet<FeedItem>();
		List<SyndEntryImpl> entryList = feed.getEntries();

		for (SyndEntryImpl entry : entryList) {
			FeedItem item = new FeedItem();
			item.setId(entry.getUri());
			item.setTitle(entry.getTitle());
			item.setDate(entry.getPublishedDate());
			item.setModified(entry.getUpdatedDate());
			item.setLink(entry.getLink());

			if (entry.getAuthors().size() > 0) {
				for (Object author : entry.getAuthors()) {
					if (author.getClass().getName().equals(
							"com.sun.syndication.feed.synd.SyndPersonImpl")) {
						item.addCreator(((SyndPerson) author).getName());
					}
				}
			} else {
				item.addCreator(entry.getAuthor());
			}

			if (entry.getDescription() != null) {
				item.setDescription(entry.getDescription().getType(), entry
						.getDescription().getValue());
			}

			List<SyndContentImpl> contents = entry.getContents();
			for (SyndContentImpl contentItem : contents) {
				item.addFulltext(contentItem.getType(), contentItem.getValue());

			}
			feedItems.add(item);
		}
		return feedItems;
	}
}
