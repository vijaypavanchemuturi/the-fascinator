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
 * Class to read The Watcher's queue Basically, a small interface into the
 * Apache HTTPClient component
 * 
 * @see http://hc.apache.org/httpcomponents-client/index.html
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
	 * @param args
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
					//if (!evt.getPropertyName().equals("content"))
						System.out.println(evt.getPropertyName() + ": "
								+ evt.getNewValue());
				}
			});
			qr.checkQueue();
			qr.checkQueue();
			qr.checkQueue();
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

	public QueueReader(URL url) {
		this.setUrl(url);
	}

	public QueueReader(String url) throws MalformedURLException {
		this.setUrl(url);
	}

	public QueueReader(String url, long lastModified)
			throws MalformedURLException {
		this.setUrl(url);
		this.lastModified = lastModified;
	}

	/**
	 * @return the lastModified
	 */
	public long getLastModified() {
		return this.lastModified;
	}


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
	 * @return the url
	 */
	public URL getUrl() {
		return this.url;
	}

	/**
	 * @param url
	 *            the url to set
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
	 * @return the content
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
	 * @return the contentType
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
