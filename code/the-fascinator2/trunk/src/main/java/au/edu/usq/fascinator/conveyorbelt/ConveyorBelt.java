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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ontoware.rdf2go.model.Syntax;
import org.semanticdesktop.aperture.extractor.ExtractorException;
import org.semanticdesktop.aperture.rdf.RDFContainer;

import au.edu.usq.fascinator.harvester.extractor.Extractor;
import au.edu.usq.fascinator.harvester.queuereader.JsonQueueReader;
import au.edu.usq.fascinator.harvester.queuereader.QueueReaderIncorrectMimeTypeException;

/**
 *
 */
public class ConveyorBelt implements PropertyChangeListener {

	public final static String DEFAULT_SYSTEM_PROPERTIES_FILE = "SystemProperties";
	public final static String DEFAULT_USER_PROPERTIES_FILE = "config.json";
	private String systemPropertiesFile;
	private String userPropertiesFile;
	private String queueURL = "";
	private Integer queueUpdateFreq = 0;
	private JsonQueueReader queueHandler;
	private Properties systemState = new Properties();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String props = "";
		if (args.length == 1) {
			props = args[0];
		}
		try {
			ConveyorBelt cb = new ConveyorBelt(props);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ConveyorBelt(String systemProperties) throws IOException,
			URISyntaxException {
		// STEP 0: load my system properties file
		if (systemProperties.equals("")) {
			this.systemPropertiesFile = ConveyorBelt.DEFAULT_SYSTEM_PROPERTIES_FILE;
			this.writeSystemPropertiesFile();
		} else {
			this.systemPropertiesFile = systemProperties;
		}
		System.out.println("System Properties File: "
				+ this.systemPropertiesFile);
		this.readSystemPropertiesFile();

		if (systemState.containsKey("user_properties_file")) {
			this.userPropertiesFile = (String) systemState
					.get("user_properties_file");
		} else {
			this.userPropertiesFile = ConveyorBelt.DEFAULT_USER_PROPERTIES_FILE;
		}
		System.out.println("User Properties File:" + this.userPropertiesFile);
		// STEP 1: Read the JSON config File
		readConfig();

		// STEP 2: Prepare the queue feed reader
		// Try to read the queue properties file to get the last checked
		// timestamp
		if (!systemState.containsKey("lastQueueCheck")) {
			systemState.setProperty("lastQueueCheck", "0");
		}
		queueHandler = new JsonQueueReader(this.queueURL, Long
				.valueOf(systemState.getProperty("lastQueueCheck")));
		queueHandler.addPropertyChangeListener(this);
		this.checkQueue();
	}

	private void checkQueue() throws IOException, URISyntaxException {
		System.out.println("Checking the queue");
		this.queueHandler.checkQueue();
	}

	private void readConfig() {
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, Object> config;
		try {
			config = mapper.readValue(new File(this.userPropertiesFile),
					HashMap.class);
			String host = (String) ((HashMap) ((HashMap) config.get("watcher"))
					.get("feedservice")).get("host");
			String port = (String) ((HashMap) ((HashMap) config.get("watcher"))
					.get("feedservice")).get("port");
			this.queueURL = "http://" + host + ":" + port;
			System.out.println("Queue URL:" + this.queueURL);
			this.queueUpdateFreq = new Integer((String) (((HashMap) config
					.get("QueueReader")).get("updateFreq")));
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

	private void readSystemPropertiesFile() throws IOException {
		FileInputStream in = new FileInputStream(systemPropertiesFile);
		systemState.load(in);
		in.close();
	}

	private void writeSystemPropertiesFile() throws IOException {
		FileOutputStream out = new FileOutputStream(systemPropertiesFile);
		systemState.store(out, "Updated");
		out.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println("An event was received for property: "
				+ evt.getPropertyName());
		System.out.println("An event from class type: "
				+ evt.getClass().getName());

		// If the last modified date is changed, update the system
		// properties
		if (evt.getPropertyName().equals("lastModified")) {
			systemState.setProperty("lastQueueCheck", String.valueOf(evt
					.getNewValue()));
			try {
				this.writeSystemPropertiesFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// If the contents have changed, proccess the queue
		if (evt.getPropertyName().equals("content")) {
			try {
				extract(((JsonQueueReader) evt.getSource()).getJson());
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (QueueReaderIncorrectMimeTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExtractorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void extract(HashMap<String, HashMap<String,String>> queue) throws IOException, ExtractorException, URISyntaxException {
		System.out.println("Processing new queue items");
		for (String key : queue.keySet()) {
			HashMap<String, String> item = queue.get(key);
			String file = key;
			String state = item.get("state");
			String time = item.get("time");
			
			System.out.println("Queue item: " + file);
			
			System.out.println("  State: " + state);
			System.out.println("  Time: " + time);

			// Use extractor to get the RDF
			RDFContainer rdf = Extractor.extractRDF(file);
			System.out.println(rdf.getModel().serialize(Syntax.RdfXml));
		}
	}

}
