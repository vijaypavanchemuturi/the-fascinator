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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * A small interface into the java.net.HttpURLConnection class.
 * The QueueReader is based around time-based http requests.
 * 
 * Last modified timestamps are handled using milliseconds since
 * January 1, 1970
 * 
 * Proxies are dealt with by java.net.HttpURLConnection
 * 
 * An instance of this class will check a queue (checkQueue) to see if the queue 
 * has changed and, if so, fire off property change events. The instance
 * will keep a record of the last change timestamp for further checkQueue calls.
 * 
 * An app utilising this class needs to sort out persisting the timestamp if
 * lifespans greater than the instance's are needed. You should also rely on
 * the server's clock rather than the local one.
 * 
 * This class is bean(ish) and notifies PropertyChangeListeners of the following
 * property changes:
 * <ul>
 * <li>lastModified</li>
 * <li>content</li>
 * <li>contentType</li>
 * <li>responseCode: The latest HTTP response code</li>
 * </ul>
 * 
 */
public class QueueReader {

	private URL url = null;
	private StringBuilder content;
	private String contentType = "";
	private int responseCode = 0;
	private long lastModified = 0;

	private PropertyChangeSupport propChanges = new PropertyChangeSupport(this);

	/**
	 * Test app - just checks an http url but does nothing with
	 * timestamps
	 * 
	 * @param args
	 *            The url of the http server to query
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: QueueReader url");
			System.exit(1);
		}
		try {
			QueueReader qr = new QueueReader(args[0]);
			qr.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					System.out.println(evt.getPropertyName() + ": "
							+ evt.getNewValue());
				}
			});
			qr.checkQueue();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sets up a queue reader for the given url. The last modified timestamp is
	 * set to 0
	 * 
	 * @param url
	 */
	public QueueReader(URL url) {
		this.setUrl(url);
	}

	/**
	 * Sets up a queue reader for the given url. The last modified timestamp is
	 * set to 0
	 * 
	 * @param url
	 * @throws MalformedURLException
	 */
	public QueueReader(String url) throws MalformedURLException {
		this.setUrl(url);
	}

	/**
	 * Sets up a queue reader for the given url and last modified timestamp.
	 * 
	 * @param url
	 * @param lastModified
	 * @throws MalformedURLException
	 */
	public QueueReader(String url, long lastModified)
			throws MalformedURLException {
		this.setUrl(url);
		this.lastModified = lastModified;
	}

	/**
	 * Checks the queue via http for the instance's url. If a last modified
	 * timestamp has been provided, this is used in the HTTP request.
	 * 
	 * If the queue has been modified, the lastModified, contentType and content
	 * properties are change accordingly.
	 * 
	 * If you wish to be notified of these changes, use addPropertyChangeListener
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void checkQueue() throws IOException, URISyntaxException {

		HttpURLConnection conn = (HttpURLConnection) this.getUrl()
				.openConnection();
		if (this.lastModified != 0) {
			conn.setIfModifiedSince(this.lastModified);
		}
		conn.connect();

		this.setResponseCode(conn.getResponseCode());
		if (conn.getLastModified() != 0)
			this.setLastModified(conn.getLastModified());
		if (conn.getContentType() != null)
			this.setContentType(conn.getContentType());

		// Load the content
		StringBuilder str = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn
				.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			str.append(inputLine);
		in.close();
		this.setContent(str);
		conn.disconnect();
	}

	/**
	 * @return the url of the queue
	 */
	public URL getUrl() {
		return this.url;
	}

	/**
	 * @param url
	 *            the url for the queue
	 */
	private void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * @param url
	 *            the url to set
	 * @throws MalformedURLException
	 */
	private void setUrl(String url) throws MalformedURLException {
		this.url = new URL(url);
	}

	/**
	 * @return the content of the latest HTTP response
	 */
	public StringBuilder getContent() {
		return this.content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	private void setContent(StringBuilder content) {
		StringBuilder oldContent = this.content;
		this.content = content;
		propChanges.firePropertyChange("content", oldContent, this.content);
	}

	/**
	 * @return the contentType of the latest HTTP response
	 */
	public String getContentType() {
		return this.contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	private void setContentType(String contentType) {
		String oldContentType = this.contentType;
		this.contentType = contentType;
		propChanges.firePropertyChange("contentType", oldContentType,
				this.contentType);
	}

	/**
	 * @return the lastModified
	 */
	public long getLastModified() {
		return this.lastModified;
	}

	/**
	 * @param lastModified
	 *            the lastModified to set
	 */
	private void setLastModified(long lastModified) {
		long oldlastModified = this.lastModified;
		this.lastModified = lastModified;
		propChanges.firePropertyChange("lastModified", oldlastModified,
				this.lastModified);
	}

	/**
	 * @param responseCode
	 *            the responseCode to set
	 */
	private void setResponseCode(int responseCode) {
		int oldResponseCode = this.responseCode;
		this.responseCode = responseCode;
		propChanges.firePropertyChange("responseCode", oldResponseCode,
				this.responseCode);
	}

	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propChanges.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		propChanges.removePropertyChangeListener(l);
	}

}
