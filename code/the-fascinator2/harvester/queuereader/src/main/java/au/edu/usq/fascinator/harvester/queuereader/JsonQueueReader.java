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
package au.edu.usq.fascinator.harvester.queuereader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author dickinso
 * 
 */
public class JsonQueueReader extends QueueReader {

	/**
	 * @param url
	 * @throws MalformedURLException
	 */
	public JsonQueueReader(String url) throws MalformedURLException {
		super(url);
	}

	/**
	 * @param url
	 * @param lastModified
	 * @throws MalformedURLException
	 */
	public JsonQueueReader(String url, long lastModified)
			throws MalformedURLException {
		super(url, lastModified);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	/*
	 * We ensure that the server response is of mimetype application/json
	 * 
	 * @see http://www.ietf.org/rfc/rfc4627.txt
	 * 
	 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
	 */
	public HashMap<String, HashMap> getJson()
			throws QueueReaderIncorrectMimeTypeException, JsonParseException,
			JsonMappingException, IOException {
		// Check that we're getting a JSON file (application/json)
		if (!this.getContentType().equals("application/json")) {
			throw new QueueReaderIncorrectMimeTypeException();
		}

		HashMap<String, HashMap> map = new HashMap<String, HashMap>();

		if (!this.getContent().equals("")) {
			// Transform JSON structure to hashmap
			ObjectMapper mapper = new ObjectMapper(); 
			map = mapper.readValue(this.getContent()
					.toString(), HashMap.class);
		}
		return map;
	}

}
