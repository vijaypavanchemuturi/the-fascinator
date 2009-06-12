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
package au.edu.usq.atomreader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.FetcherListener;
import com.sun.syndication.fetcher.impl.DiskFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;

/**
 * 
 *
 */
public class AtomReader implements FetcherListener {

	private URL feedURL;
	private SyndFeed feed;
	private FeedFetcher fetcher;
	private DiskFeedInfoCache cache;
	private String cacheDir;
	private HashSet<AtomReaderStateChangeListener> stateChangeListeners = new HashSet<AtomReaderStateChangeListener>();
	private FetcherEvent lastFetcherEvent;

	/**
	 * @param args
	 *            Command line arguments
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			if (args[0].equals("-h")) {
				displayUsageMessage();
			}
		} else if (args.length == 2) {
			AtomReader reader;
			try {
				reader = new AtomReader(args[0], args[1]);
				reader
						.addAtomReaderStateChangeListener(new SampleAtomReaderChangeListener());
				reader.read();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.out.println("Failed to read the feed.\n");
				System.exit(1);
			}
		} else {
			System.out.println("Incorrect arguments\n\n");
			displayUsageMessage();
			System.exit(1);
		}
	}

	/**
	 * @param feedURL
	 */
	public AtomReader(String feedURL, String cacheDir)
			throws MalformedURLException {
		super();
		setFeedURL(feedURL);
		setCacheDir(cacheDir);

		cache = new DiskFeedInfoCache(getCacheDir());
		fetcher = new HttpURLFeedFetcher(cache);
		fetcher.addFetcherEventListener(this);
	}

	public SyndFeed read() {
		try {
			this.feed = fetcher.retrieveFeed(this.getFeedURL());
			AtomReaderStateChangeEvent event = new AtomReaderStateChangeEvent(
					this, this.lastFetcherEvent);
			for (AtomReaderStateChangeListener listener : stateChangeListeners) {
				listener.atomReaderStateChangeEvent(event);
			}
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FeedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FetcherException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return this.feed;
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
	public void setCacheDir(String cacheDir) {
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
	public void setFeedURL(String feedURL) throws MalformedURLException {
		this.feedURL = new URL(feedURL);
	}

	/**
	 * @param feedURL
	 *            the feedURL to set
	 */
	public void setFeedURL(URL feedURL) {
		this.feedURL = feedURL;
	}

	private static final void displayUsageMessage() {
		System.out
				.println("Usage: AtomReader -h|<url> <cache dir>\n\n"
						+ "AtomReader displays the latest contents of a syndication feed\n\n"
						+ "-h\tView usage message (this one)\n"
						+ "<url>\tThe url of the feed\n"
						+ "<cache dir>\tThe location to cache feed entries (this will be created)\n");
	}

	public void fetcherEvent(FetcherEvent fevent) {
		this.lastFetcherEvent = fevent;
	}

	public void addAtomReaderStateChangeListener(
			AtomReaderStateChangeListener listener) {
		stateChangeListeners.add(listener);
	}

	public void removeAtomReaderStateChangeListener(
			AtomReaderStateChangeListener listener) {
		stateChangeListeners.remove(listener);
	}

}
