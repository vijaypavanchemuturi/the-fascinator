/*
 * The Fascinator - CSV Harvester Plugin
 * Copyright (C) 2010 University of Southern Queensland
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
package au.edu.usq.fascinator.harvester.csv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.harvester.impl.GenericHarvester;
import au.edu.usq.fascinator.common.storage.StorageUtils;

import com.Ostermiller.util.CSVParser;

/**
 * 
 * <p>
 * Based on Greg Pendlebury's CallistaHarvester.
 * 
 * @author Duncan Dickinson
 * @author Greg Pendlebury
 */
public class CSVHarvester extends GenericHarvester {

	/** logging */
	private Logger log = LoggerFactory.getLogger(CSVHarvester.class);

	/** whether or not there are more files to harvest */
	private boolean hasMore;

	/** Field names (column) */
	private ArrayList<String> dataFields = null;

	/** Data file */
	private File csvData;

	/** Delimiter in the CSV */
	private char delimiter = ',';

	/** The name of the column holding the ID */
	String idColumn = null;

	/** Debugging limit */
	private int limit;

	/**
	 * File System Harvester Constructor
	 */
	public CSVHarvester() {
		super("csv", "CSV Harvester");
	}

	/**
	 * Initialisation of harvester plugin
	 * 
	 * @throws HarvesterException
	 *             if fails to initialise
	 */
	@Override
	public void init() throws HarvesterException {
		dataFields = new ArrayList<String>();
		JsonConfigHelper config;

		// Read config
		try {
			config = new JsonConfigHelper(getJsonConfig().toString());
		} catch (IOException ex) {
			throw new HarvesterException("Failed reading configuration", ex);
		}

		String filePath = config.get("harvester/csv/fileLocation");
		if (filePath == null) {
			throw new HarvesterException("No Callista data file provided!");
		}

		delimiter = config.get("harvester/csv/delimiter", ",").charAt(0);

		idColumn = convertFieldName(config.get("harvester/csv/idColumn"));
		if (idColumn == null) {
			throw new HarvesterException("No ID Column defined.");
		}

		String limitString = config.get("harvester/callista/limit", "-1");
		limit = Integer.parseInt(limitString);

		csvData = new File(filePath);
		if (csvData == null || !csvData.exists()) {
			throw new HarvesterException("Could not find CSV data file: '"
					+ filePath + "'");
		}

		hasMore = false;
	}

	/**
	 * Shutdown the plugin
	 * 
	 * @throws HarvesterException
	 *             is there are errors
	 */
	@Override
	public void shutdown() throws HarvesterException {
	}

	/**
	 * Harvest the next set of files, and return their Object IDs
	 * 
	 * @return Set<String> The set of object IDs just harvested
	 * @throws HarvesterException
	 *             is there are errors
	 */
	@Override
	public Set<String> getObjectIdList() throws HarvesterException {
		Set<String> fileObjectIdList = new HashSet<String>();

		// Data streams - get CSV data
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(csvData);
		} catch (FileNotFoundException ex) {
			// We tested for this earlier
			throw new HarvesterException("Could not find file", ex);
		}
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		int i = 0; // Row counter
		boolean titleFlag = false;
		boolean stop = false;
		// Line by line from buffered reader
		// It is assumed that the first line provides the field names
		String line;
		try {
			while ((line = br.readLine()) != null && !stop) {
				// Parse the CSV for this line
				String[][] values;
				try {
					values = CSVParser.parse(new StringReader(line), delimiter);
				} catch (IOException ex) {
					log.error("Error parsing CSV file", ex);
					throw new HarvesterException("Error parsing CSV file: " + ex.getMessage(), ex);
				}

				for (String[] columns : values) {
					if (!titleFlag) {
						// Read the field names from the first row
						for (String column : columns) {
							// Print if debugging
							// log.debug("HEADING {}: '{}'", j, column);
							// j++;
							String col = convertFieldName(column);
							dataFields.add(col);
							log.debug("CSV field name: {}", col);
						}
						// j = 0;
						if (!dataFields.contains(idColumn)) {
							throw new HarvesterException("The ID Column ("
									+ idColumn
									+ ") does not appear in the first row");
						}
						
						titleFlag = true;
						continue;
					} else {
						// Store normal data rows
						if (i % 500 == 0) {
							log.info("Parsing row {}", i);
						}
						fileObjectIdList.add(createRecord(columns));
						i++;
					}
				}

				// Check our record limit if debugging
				if (limit != -1 && i >= limit) {
					stop = true;
					log.debug("Stopping at debugging limit");
				}
			}
			in.close();
		} catch (IOException ex) {
			log.error("Error reading from CSV file", ex);
			throw new HarvesterException("Error reading from CSV file", ex);
		}
		log.info("Object creation complete: {} objects", i);

		return fileObjectIdList;

	}

	public static String convertFieldName(String str) {
		//TODO: workaround as the JSON library balks at spaces in the key
		return str.replaceAll("\\s", "_");
	}
	
	private String createRecord(String[] columns) {
		int j = 0;
		String id = null;

		JsonConfigHelper json = new JsonConfigHelper();

		for (String column : columns) {
			if (idColumn.equals(dataFields.get(j))) {
				id = column;
			}
			try {
				store(dataFields.get(j), column, json);
			} catch (Exception ex) {
				log.error("Error parsing record '{}': " + ex.getMessage(), id, ex);
			}
			j++;
		}

		json.set("id", id);
		json.set("step", "pending");

		if (json != null) {
			try {
				return storeJson(json, csvData.getName() + "?" + id);
			} catch (StorageException ex) {
				log.error("Error during storage: ", ex);
			} catch (HarvesterException ex) {
				log.error("Harvest error: ", ex);
			}
		}
		return null;
	}

	private String storeJson(JsonConfigHelper jsonData, String recordId)
			throws HarvesterException, StorageException {
		Storage storage = getStorage();
		String oid = DigestUtils.md5Hex(recordId);
		DigitalObject object = StorageUtils.getDigitalObject(storage, oid);
		String pid = "metadata.json";

		InputStream stream = getStream(jsonData.toString());

		if (stream == null) {
			log.error("Failed to create a stream from the JSON Data");
		}

		Payload payload = StorageUtils.createOrUpdatePayload(object, pid,
				stream);
		payload.setContentType("application/json");
		payload.close();

		// update object metadata
		Properties props = object.getMetadata();
		props.setProperty("render-pending", "true");

		object.close();
		return object.getId();
	}

	/**
	 * Turn the String into an inputstream
	 * 
	 * @param data
	 *            The data to read
	 * @return InputStream of the data
	 */
	public static InputStream getStream(String data) {
		try {
			return new ByteArrayInputStream(data.getBytes("utf-8"));
		} catch (UnsupportedEncodingException ex) {
			return null;
		}
	}

	/**
	 * Simple wrapper for storing strings in json to avoid excessive null
	 * testing. Duplicate testing is performed to ensure only like values exist
	 * in the data stream and no mismatches exist.
	 * 
	 * @param field
	 *            : Field name to use
	 * @param value
	 *            : Data to store, possibly null
	 * @param json
	 *            : The JSON object to store in
	 * @return boolean : <code>true</code> the value is appropriate for storage,
	 *         <code>false</code> if there is a clash
	 */
	private void store(String field, String value, JsonConfigHelper json)
			throws Exception {
		if (value != null) {
			String existing = json.get(field);
			if (existing != null) {
				if (existing.equals(value)) {
					// Match is good
				} else {
					// Data mismatch, we have two or more different values
					// in a field that should be the same.
					throw new Exception("Duplicate in field '" + field + "'!");
				}
			} else {
				// First time we've seen this field
				json.set(field, value);
			}
		}
	}

	/**
	 * Check if there are more objects to harvest
	 * 
	 * @return <code>true</code> if there are more, <code>false</code> otherwise
	 */
	@Override
	public boolean hasMoreObjects() {
		return hasMore;
	}

	/**
	 * Delete cached references to files which no longer exist and return the
	 * set of IDs to delete from the system.
	 * 
	 * @return Set<String> The set of object IDs deleted
	 * @throws HarvesterException
	 *             is there are errors
	 */
	@Override
	public Set<String> getDeletedObjectIdList() throws HarvesterException {
		return new HashSet<String>();
	}

	/**
	 * Check if there are more objects to delete
	 * 
	 * @return <code>true</code> if there are more, <code>false</code> otherwise
	 */
	@Override
	public boolean hasMoreDeletedObjects() {
		return false;
	}
}
