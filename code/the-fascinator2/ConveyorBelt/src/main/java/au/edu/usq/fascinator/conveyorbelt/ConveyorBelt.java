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
package au.edu.usq.fascinator.conveyorbelt;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import au.edu.usq.atomreader.AtomReader;
import au.edu.usq.atomreader.SampleAtomReaderChangeListener;

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
		// STEP 2: Prepare the Atom feed reader
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
			this.atomUpdateFreq = new Integer((String) (((HashMap) config
					.get("AtomReader")).get("updateFreq")));
			this.atomCacheDir = (String) (((HashMap) config.get("AtomReader"))
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

	private void prepareAtomReader() {
		try {
			atomReader = new AtomReader(atomURL, atomCacheDir);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		atomReader
				.addAtomReaderStateChangeListener(new SampleAtomReaderChangeListener());

	}

}
