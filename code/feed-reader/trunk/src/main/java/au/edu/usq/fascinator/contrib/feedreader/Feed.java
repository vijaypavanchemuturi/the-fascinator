package au.edu.usq.fascinator.contrib.feedreader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.FetcherListener;
import com.sun.syndication.fetcher.impl.DiskFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;

/**
 * Provides a basic interface to the ROME Fetcher library. To use this class:
 * 1.Instantiate it with the feed url and cache location 2. Add a state change
 * listener (via addFeedReaderStateChangeListener) 3. Call read() one or more
 * times The NewItemListener class provides a sample listener that dumps out
 * data as the feed is read.
 * 
 * @author Duncan Dickinson
 * @see http://wiki.java.net/bin/view/Javawsxml/RomeFetcher
 */
public class Feed implements FetcherListener, Runnable {
	/**
	 * Generic logging
	 */
	private static Logger log = LoggerFactory.getLogger(Feed.class);

	/**
	 * The URLs of the RSS/ATOM feeds
	 */
	private URL feedURL;

	/**
	 * The actual feed
	 */
	private SyndFeed feed;

	/**
	 * Syndication fetcher
	 */
	private HttpURLFeedFetcher fetcher;

	/**
	 * The location of the fetcher cache
	 */
	private String cacheDir;

	/**
	 * Registered listeners are informed when the feed is read
	 */
	private HashSet<FeedReaderStateChangeListener> stateChangeListeners = new HashSet<FeedReaderStateChangeListener>();

	private FetcherEvent lastFetcherEvent;

	public Feed(URL feedURL) throws MalformedURLException {
		super();
		setFeedURL(feedURL);
		fetcher = new HttpURLFeedFetcher();
		fetcher.addFetcherEventListener(this);

	}

	/**
	 * Sets up the basic configuration for a feed read() is called to actually
	 * fetch the feed
	 * 
	 * @param feedURL
	 *            The URL of the feed
	 * @param cacheDir
	 *            The location for storing the feed caches
	 * @throws MalformedURLException
	 */
	public Feed(URL feedURL, String cacheDir) throws MalformedURLException {
		this(feedURL);
		if (cacheDir != null) {
			setCacheDir(cacheDir);
			fetcher.setFeedInfoCache(new DiskFeedInfoCache(getCacheDir()));
		}

	}

	/**
	 * Reads the feed. Note that the whole
	 * 
	 * @return
	 */
	public SyndFeed read() {
		try {
			this.feed = fetcher.retrieveFeed(this.getFeedURL());

		} catch (IllegalArgumentException e1) {
			log.error(e1.getLocalizedMessage());
		} catch (IOException e1) {
			log.error(e1.getLocalizedMessage());
		} catch (FeedException e1) {
			log.error(e1.getLocalizedMessage());
		} catch (FetcherException e1) {
			log.error(e1.getLocalizedMessage());
		}
		FeedReaderStateChangeEvent event = new FeedReaderStateChangeEvent(this,
				this.lastFetcherEvent);
		for (FeedReaderStateChangeListener listener : stateChangeListeners) {
			listener.feedReaderStateChangeEvent(event);
		}
		return this.feed;
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
	 */
	protected void setFeedURL(URL feedURL) {
		this.feedURL = feedURL;
	}

	/**
	 * Returns all items in the cache
	 * 
	 * @return list of FeedItems
	 */
	public SyndFeed getAll() {
		return this.fetcher.getFeedInfoCache().getFeedInfo(this.feedURL)
				.getSyndFeed();
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

	@Override
	public void run() {
		log.debug("Thread started for : " + this.feedURL.toExternalForm());
		this.read();
		log.debug("Read completed for : " + this.feedURL.toExternalForm());
	}

}
