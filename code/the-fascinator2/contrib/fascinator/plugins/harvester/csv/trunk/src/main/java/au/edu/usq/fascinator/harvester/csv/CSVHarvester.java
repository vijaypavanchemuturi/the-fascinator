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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
 * Harvester for CSV files.
 * <p>
 * Configuration options:
 * <ul>
 * <li>fileLocation: The location of the csv file (required)</li>
 * <li>idColumn: the column holding the primary key. 
 * 	If not provided, the row number will be used.</li>
 * <li>recordIDPrefix: Adds a prefix to the value found in the ID column.
 *	For example, setting this as "http://id.example.com/" with an ID value of "453"
 *	will result in http://id.example.com/453 as the ID. 
 * <li>delimiter: The csv delimiter. Comma (,) is the default</li>
 * <li>ignoredFields: An array of fields (columns) ignored by the harvest.</li>
 * <li>includedFields: An array of fields (columns) included by the harvest</li>
 * </ul>
 * <p>
 * You can select to harvest all columns (fields) or be selective:
 * <ul>
 * <li>If you don't set ignoredFields or includedFields: all fields are harvested.</li>
 * <li>ignoredFields has precedence over includedFields: 
 * 	If you have the same field in both lists, it will get ignored.</li>
 * <li>If you only provide fields in ignoredFields (and leave includedFields blank), all other fields will be harvested</li>
 * <li>If you only provide fields in includedFields (and leave ignoredFields blank), only these fields will be harvested</li>
 * </ul>
 * <p>
 * Note: due to limitations in our JSON parser, any fields with a space in the name
 * (e.g. "First Name") will have the space replaced by an underscore (e.g. "First_Name").
 * If you use spaces in items in the ignoredFields or includedFields arrays they'll be converted as well.
 * <p>
 * Fields with repeated names result in only the first value being stored.
 * <p>
 * Based on Greg Pendlebury's CallistaHarvester.
 * 
 * @author Duncan Dickinson
 */
/**
 * @author dickinso
 * 
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

	/** Ignored field names (column) */
	private ArrayList<String> ignoredFields = null;

	/** Included field names (column) */
	private ArrayList<String> includedFields = null;

	/** The name of the column holding the ID */
	String idColumn = "";

	/** A prefix for generating the object's ID */
	String recordIDPrefix = "";

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
			throw new HarvesterException("No data file provided!");
		}

		csvData = new File(filePath);
		if (csvData == null || !csvData.exists()) {
			throw new HarvesterException("Could not find CSV data file: '"
					+ filePath + "'");
		}

		delimiter = config.get("harvester/csv/delimiter", ",").charAt(0);

		idColumn = convertFieldName(config.get("harvester/csv/idColumn", null));

		recordIDPrefix = config.get("harvester/csv/recordIDPrefix", "");

		String limitString = config.get("harvester/csv/limit", "-1");
		limit = Integer.parseInt(limitString);

		ignoredFields = new ArrayList<String>();
		List<Object> ignore = config.getList("harvester/csv/ignoreFields");
		for (Object item : ignore) {
			String s = convertFieldName((String) item);
			ignoredFields.add(s);
			log.debug("Ignoring field: " + s);
		}

		includedFields = new ArrayList<String>();
		List<Object> include = config.getList("harvester/csv/includedFields");
		for (Object item : include) {
			String s = convertFieldName((String) item);
			includedFields.add(s);
			log.debug("Including field: " + s);
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
					throw new HarvesterException("Error parsing CSV file: "
							+ ex.getMessage(), ex);
				}

				for (String[] columns : values) {
					if (!titleFlag) {
						// Read the field names from the first row
						for (String column : columns) {
							String col = convertFieldName(column);
							dataFields.add(col);
							log.debug("CSV field name: {}", col);
						}
						// j = 0;
						if (idColumn != null) {
							if (!dataFields.contains(idColumn)) {
								throw new HarvesterException("The ID Column ("
										+ idColumn
										+ ") does not appear in the first row");
							}
						}

						titleFlag = true;
						continue;
					} else {
						// Store normal data rows
						if (i % 500 == 0) {
							log.info("Parsing row {}", i);
						}
						fileObjectIdList.add(createRecord(columns, i));
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

	/**
	 * Replaces spaces with an underscore. Workaround as the JSON library balks
	 * at spaces in the key
	 * 
	 * @param str
	 *            The space to be converted
	 * @return A string with all spaces converted into underscores
	 */
	public static String convertFieldName(String str) {
		// TODO: workaround as the JSON library balks at spaces in the key
		if (str == null)
			return null;
		return str.replaceAll("\\s", "_");
	}

	private String createRecord(String[] columns, int rowNumber) {
		int j = -1;

		JsonConfigHelper json = new JsonConfigHelper();

		HashMap<String, Object> data = new HashMap<String, Object>();
		HashMap<String, Object> metadata = new HashMap<String, Object>();

		if (idColumn == null) {
			// Use the row number as the ID
			metadata.put("dc.identifier", recordIDPrefix
					+ Integer.toString(rowNumber));
		}

		for (String column : columns) {
			j++;
			String colName = dataFields.get(j);

			if (idColumn != null) {
				if (idColumn.equals(colName)) {
					metadata.put("dc.identifier", recordIDPrefix + column);
				}
			}

			if (ignoredFields.contains(colName)) {
				continue;
			}
			if (includedFields.size() > 0 && !includedFields.contains(colName)) {
				continue;
			}

			// log.debug("Storing field: " + colName);
			data.put(colName, column);
		}

		try {
			json.setMap("data", data);
			json.setMap("metadata", metadata);
		} catch (Exception ex) {
			log.error("Error parsing record '{}': " + ex.getMessage(), metadata
					.get("dc.identifier"), ex);
		}

		if (json != null) {
			try {
				return storeJson(json, (String) metadata.get("dc.identifier"));
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
