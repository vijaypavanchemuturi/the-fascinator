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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.FetcherListener;
import com.sun.syndication.fetcher.impl.DiskFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;

/**
 * Provides a basic interface to the ROME Fetcher library.
 * 
 * To use this class: 1. Instantiate it with the feed url and cache location 2.
 * Add a state change listener (via addFeedReaderStateChangeListener) 3. Call
 * read() one or more times
 * 
 * The NewItemListener class provides a sample listener that dumps out data as
 * the feed is read.
 * 
 * @author dickinso
 * @see http://wiki.java.net/bin/view/Javawsxml/RomeFetcher
 */
public class FeedReader implements FetcherListener {

	/**
	 * The URL of the RSS/ATOM feed
	 */
	private URL feedURL;

	/**
	 * The actual feed
	 */
	private SyndFeed feed;

	/**
	 * Syndication fetcher
	 */
	private FeedFetcher fetcher;

	/**
	 * Provides a simple disk-based cache
	 */
	private DiskFeedInfoCache cache;

	/**
	 * The location of the fetcher cache
	 */
	private String cacheDir;

	/**
	 * Registered listeners are informed when the feed is read
	 */
	private HashSet<FeedReaderStateChangeListener> stateChangeListeners = new HashSet<FeedReaderStateChangeListener>();

	private FetcherEvent lastFetcherEvent;

	/**
	 * Generic logging
	 */
	private static Logger log = LoggerFactory.getLogger(FeedReader.class);

	/**
	 * Sets up the basic configuration for a feed
	 * 
	 * read() is called to actually fetch the feed
	 * 
	 * @param feedURL
	 *            The URL of the feed
	 * @param cacheDir
	 *            The location for storing the feed caches
	 * @throws MalformedURLException
	 */
	public FeedReader(String feedURL, String cacheDir)
			throws MalformedURLException {
		super();
		setFeedURL(feedURL);
		setCacheDir(cacheDir);

		cache = new DiskFeedInfoCache(getCacheDir());
		fetcher = new HttpURLFeedFetcher(cache);
		fetcher.addFetcherEventListener(this);
	}

	/**
	 * Reads the feed
	 * 
	 * Note that the whole
	 * 
	 * @return
	 */
	public SyndFeed read() {
		try {
			this.feed = fetcher.retrieveFeed(this.getFeedURL());
			FeedReaderStateChangeEvent event = new FeedReaderStateChangeEvent(
					this, this.lastFetcherEvent);
			for (FeedReaderStateChangeListener listener : stateChangeListeners) {
				listener.feedReaderStateChangeEvent(event);
			}
		} catch (IllegalArgumentException e1) {
			log.error(e1.getLocalizedMessage());
		} catch (IOException e1) {
			log.error(e1.getLocalizedMessage());
		} catch (FeedException e1) {
			log.error(e1.getLocalizedMessage());
		} catch (FetcherException e1) {
			log.error(e1.getLocalizedMessage());
		}
		return this.feed;
	}

	/**
	 * Removes the cache for this feed
	 */
	public void remove() {
		this.cache.remove(this.feedURL);
	}

	/**
	 * Returns all items in the cache
	 * @return list of FeedItems
	 */
	public HashSet<FeedItem> getAll(){
		SyndFeed feed = this.cache.getFeedInfo(this.feedURL).getSyndFeed();
		HashSet<FeedItem> feedItems = FeedModel.packageFeedItems(feed);
		return feedItems;
	}

	/**
	 * @return the cacheDir
	 */
	public String getCacheDir() {
		return this.cacheDir;
	}

	/**
	 * @param cacheDir
	 *            the cacheDir to set
	 */
	private void setCacheDir(String cacheDir) {
		this.cacheDir = cacheDir;
		new File(cacheDir).mkdirs();
	}

	public SyndFeed getFeed() {
		return this.feed;
	}

	/**
	 * @return the URL being read
	 */
	public URL getFeedURL() {
		return this.feedURL;
	}

	/**
	 * @return the URL being read
	 */
	public String getFeedURLString() {
		return this.feedURL.toString();
	}

	/**
	 * @param feedURL
	 *            the feedURL to set
	 * @throws MalformedURLException
	 */
	private void setFeedURL(String feedURL) throws MalformedURLException {
		this.feedURL = new URL(feedURL);
	}

	/**
	 * @param feedURL
	 *            the feedURL to set
	 */
	protected void setFeedURL(URL feedURL) {
		this.feedURL = feedURL;
	}

	public void fetcherEvent(FetcherEvent fevent) {
		this.lastFetcherEvent = fevent;
	}

	public void addFeedReaderStateChangeListener(
			FeedReaderStateChangeListener listener) {
		stateChangeListeners.add(listener);
	}

	public void removeFeedReaderStateChangeListener(
			FeedReaderStateChangeListener listener) {
		stateChangeListeners.remove(listener);
	}

	/**
	 * @param args
	 *            Command line arguments
	 * 
	 */
	public static void main(String[] args) {
		HashMap<String, String> argumentMap = null;

		if (args.length == 0) {
			displayUsageMessage();
			System.exit(1);
		}
		if (args.length == 1) {
			if (args[0].equals("-h")) {
				displayUsageMessage();
				System.exit(0);
			}
		}

		try {
			argumentMap = parseArgs(args);
			checkArgs(argumentMap);
		} catch (IllegalArgumentException e) {
			System.out.println("Incorrect arguments");
			displayUsageMessage();
			System.exit(1);
		}
		setArgDefaults(argumentMap);

		FeedReader reader = null;
		try {
			reader = new FeedReader(argumentMap.get("url"), argumentMap
					.get("cache"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out
					.println("Failed to read the feed - the URL appears incorrect.\n");
			System.exit(1);
		}
		reader.addFeedReaderStateChangeListener(new DemoItemListener());
		reader.read();
	}

	public static HashMap<String, String> parseArgs(String[] args)
			throws IllegalArgumentException {
		HashMap<String, String> argMap = new HashMap<String, String>();
		for (String param : args) {
			String[] kv = param.split("=");
			if (kv.length == 1) {
				throw new IllegalArgumentException();
			}
			argMap.put(kv[0], kv[1]);
		}
		return argMap;
	}

	private static void checkArgs(HashMap<String, String> argumentMap) {
		// we need url at least
		if (!argumentMap.containsKey("url")) {
			throw new IllegalArgumentException();
		}
	}

	private static void setArgDefaults(HashMap<String, String> argumentMap) {
		if (!argumentMap.containsKey("cache")) {
			argumentMap.put("cache", System.getProperty("user.home")
					+ "/.feed-reader");
		}
	}

	private static final void displayUsageMessage() {
		System.out
				.println("Usage: FeedReader -h|url=<url> [cache=<cache dir>]\n\n"
						+ "FeedReader displays the latest contents of a syndication feed\n\n"
						+ "-h\tView usage message (this one)\n"
						+ "url=<url>\tThe url of the feed\n"
						+ "cache=<cache dir>\tThe location to cache feed entries (this will be created)\n");
	}
}
