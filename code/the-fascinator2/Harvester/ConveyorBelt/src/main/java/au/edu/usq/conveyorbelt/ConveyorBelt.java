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
package au.edu.usq.conveyorbelt;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import au.edu.usq.atomreader.AtomReader;
import au.edu.usq.atomreader.AtomReaderStateChangeEvent;
import au.edu.usq.atomreader.AtomReaderStateChangeListener;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FetcherEvent;

/**
 *
 */
public class ConveyorBelt {

	private String configFilePath = "";
	private String atomURL = "";
	private Integer atomUpdateFreq = 0;
	private String atomCacheDir = "";
	private AtomReader atomReader;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.exit(1);
		}

		ConveyorBelt cb = new ConveyorBelt(args[0]);

	}

	public ConveyorBelt(String configFilePath) {
		this.configFilePath = configFilePath;
		// STEP 1: Read the JSON config File
		readConfig();		
		//STEP 2: Prepare the Atom feed reader
		prepareAtomReader();
		atomReader.read();
	}
	
	private void readConfig() {
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, Object> config;
		try {
			config = mapper.readValue(new File(configFilePath), HashMap.class);
			this.atomURL = (String) ((HashMap) ((HashMap) config.get("watcher"))
					.get("atomFeed")).get("atomUrl");
			this.atomUpdateFreq = new Integer((String)(((HashMap) config.get("AtomReader"))
					.get("updateFreq")));
			this.atomCacheDir = (String)(((HashMap) config.get("AtomReader"))
					.get("cacheDir"));
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void prepareAtomReader(){
		atomReader = new AtomReader(atomURL, atomCacheDir);
		atomReader
				.addAtomReaderStateChangeListener(new AtomChangeListener());
		
	}
	
		private class AtomChangeListener implements
		AtomReaderStateChangeListener {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.edu.usq.AtomReaderStateChangeListener#atomReaderStateChangeEvent
	 * (au.edu.usq.AtomReaderStateChangeEvent)
	 */
	public void atomReaderStateChangeEvent(AtomReaderStateChangeEvent event) {
		System.out.println("Event: " + event.getEventType() + "\n");
		if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(event
				.getEventType())) {
			AtomReader ar = event.getReader();
			System.out.println("URL: " + ar.getFeedURLString() + "\n");
			SyndFeed feed = ar.getFeed();
			System.out.println("Items: " + ar.getFeed().getEntries().size()
					+ "\n");
			List<SyndEntryImpl> lst = feed.getEntries();
			for (SyndEntryImpl i : lst) {
				System.out.println("Title: " + i.getTitle() + "\n"
						+ "URI: " + i.getUri() + "\n"
						+ "Published Date: " + i.getPublishedDate() + "\n"
						+ "Updated Date: " + i.getUpdatedDate() + "\n"
						+ "Author: " + i.getAuthor() + "\n" + "Contents: "
						+ i.getContents().toString() + "\n"
						+ "Description: " + i.getDescription().getValue()
						+ "\n");
			}
		} else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(event
				.getEventType())) {
			System.out.println("No change in feed\n");
		}
	}

