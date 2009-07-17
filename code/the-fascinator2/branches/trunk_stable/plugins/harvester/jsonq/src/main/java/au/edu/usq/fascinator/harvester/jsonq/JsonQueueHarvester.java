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
package au.edu.usq.fascinator.harvester.jsonq;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * A helper class to read The Watcher's queue.
 * 
 * @see http://fascinator.usq.edu.au/trac/wiki/Watcher/Server
 */
public class JsonQueueHarvester extends QueueReader {

	/**
	 * @param url
	 * @throws MalformedURLException
	 */
	public JsonQueueHarvester(String url) throws MalformedURLException {
		super(url);
	}

	/**
	 * @param url
	 * @param lastModified
	 * @throws MalformedURLException
	 */
	public JsonQueueHarvester(String url, long lastModified)
			throws MalformedURLException {
		super(url, lastModified);
	}

	/**
	 * Returns a hashmap representation of a JSON queue 
	 * 
	 * The watcher's queue is structured as follows: <code>
	 *   {
	 * 		"file://%2Fhome%2Fdickinso%2FMusic%2FJazz+Street+Trio%2FLicense.txt": {"state": "start", "time": "2009-06-23 16:41:06"},   
	 * 		"file://%2Fhome%2Fdickinso%2FMusic%2FRevolution+Void%2FRevolution+Void+-+Thread+Soul+--+Jamendo+-+MP3+VBR+192k+-+2006.08.21+%5Bwww.jamendo.com%5D%2FReadme+-+www.jamendo.com+.txt": {"state": "start", "time": "2009-06-23 16:41:06"
	 * 	}
	 * <code>
	 * 
	 * As you can see, it's a hashtag in a hashtag. The top-level hash has
	 *  - Key: (string) escaped url of the file 
	 *  - Value: A hashmap:
	 *     - State: the queue state
	 *     - Time: the queue's time stamp
	 * 
	 * @see http://www.ietf.org/rfc/rfc4627.txt
	 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
	 * @return A hashmap of the JSON
	 * @throws QueueReaderIncorrectMimeTypeException
	 *             We ensure that the server response is of mimetype
	 *             application/json
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public HashMap<String, HashMap<String, String>> getJson()
			throws QueueReaderIncorrectMimeTypeException, JsonParseException,
			JsonMappingException, IOException {
		// Check that we're getting a JSON file (application/json)
		if (!this.getContentType().equals("application/json")) {
			throw new QueueReaderIncorrectMimeTypeException();
		}

		HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();

		if (!this.getContent().equals("")) {
			// Transform JSON structure to hashmap
			ObjectMapper mapper = new ObjectMapper();
			map = mapper.readValue(this.getContent().toString(), HashMap.class);
		}
		return map;
	}

}
