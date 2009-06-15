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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Class to read The Watcher's queue Basically, a small interface into the
 * Apache HTTPClient component
 * 
 * @see http://hc.apache.org/httpcomponents-client/index.html
 * 
 */
public class QueueReader {

	private URI uri;
	private HttpClient httpclient = new DefaultHttpClient();
	String responseBody;

	/**
	 * @return the responseBody
	 */
	public String getResponseBody() {
		return this.responseBody;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: QueueReader url");
			System.exit(1);
		}
		try {
			QueueReader qr = new QueueReader(args[0]);
			qr.getQueue();
			System.out.println(args[0] + " responded with:\n"
					+ qr.getResponseBody());
			qr.close();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public QueueReader(String uri) throws URISyntaxException {
		this.setUri(uri);
	}

	/**
	 * 
	 * 
	 * We ensure that the server response is of mimetype application/json
	 * 
	 * @see http://www.ietf.org/rfc/rfc4627.txt
	 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public HashMap getQueue() throws ClientProtocolException, IOException {
		HashMap map = new HashMap();

		// GET the URL
		HttpGet httpget = new HttpGet(this.getUri());
		// org.apache.http.impl.cookie
		// httpget.addHeader("If-Modified-Since", "");

		// TODO: Deal with proxies
		
		 // Create a response handler
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        responseBody = httpclient.execute(httpget, responseHandler);

		// Check that we're getting a JSON file (application/json)

		// Transform JSON structure to hashmap

		return map;
	}

	public void close() {
		// When HttpClient instance is no longer needed,
		// shut down the connection manager to ensure
		// immediate deallocation of all system resources
		httpclient.getConnectionManager().shutdown();
	}

	public void QueueReader(URI uri) {
		this.setUri(uri);
	}

	/**
	 * @return the uri
	 */
	public URI getUri() {
		return this.uri;
	}

	/**
	 * @param uri
	 *            the uri to set
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}

	/**
	 * @param uri
	 *            the uri to set
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public void setUri(String uri) throws URISyntaxException {
		this.uri = new URI(uri);
	}

}
