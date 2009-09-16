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
package au.edu.usq.fascinator.contrib.feedreader;

import java.util.Date;
import java.util.HashSet;

/**
 * @author dickinso
 * 
 */
public class FeedItem {

	private String id;

	private String title;

	private HashSet<String> creator = new HashSet<String>();

	private TypeValueItem description = new TypeValueItem();

	private HashSet<String> format = new HashSet<String>();

	private String link;

	private Date date;

	private Date modified;

	private HashSet<TypeValueItem> fulltext = new HashSet<TypeValueItem>();

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void addCreator(String creator) {
		this.creator.add(creator);
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String type, String description) {
		this.description.setType(type);
		this.description.setValue(description);
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @param modified
	 *            the modified to set
	 */
	public void setModified(Date modified) {
		this.modified = modified;
	}

	/**
	 * @param fulltext
	 *            the fulltext to set
	 */
	public void addFulltext(String type, String fulltext) {
		this.fulltext.add(new TypeValueItem(type, fulltext));
	}

	/**
	 * @return the fulltext
	 */
	public HashSet<TypeValueItem> getFulltext() {
		return this.fulltext;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * @return the creator list
	 */
	public HashSet<String> getCreators() {
		return this.creator;
	}

	/**
	 * @return the description object
	 */
	public TypeValueItem getDescriptionObject() {
		return this.description;
	}

	/**
	 * @return the description's MIME type
	 */
	public String getDescriptionType() {
		return this.description.getType();
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description.getValue();
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return this.date;
	}

	/**
	 * @return the modified
	 */
	public Date getModified() {
		return this.modified;
	}

	/**
	 * @return the relation
	 */
	public String getLink() {
		return this.link;
	}

	/**
	 * @param relation
	 *            the relation to set
	 */
	public void setLink(String link) {
		this.link=link;
	}

	/**
	 * @param format
	 *            the format to set
	 */
	public void addFormat(String format) {
		this.format.add(format);
	}

	public class TypeValueItem {
		String type;
		String value;

		public TypeValueItem() {
			this.type = "";
			this.value = "";
		}

		public TypeValueItem(String type, String value) {
			this.type = type;
			this.value = value;
		}

		protected void setType(String type) {
			this.type = type;
		}

		protected void setValue(String value) {
			this.value = value;
		}

		public String getType() {
			return this.type;
		}

		public String getValue() {
			return this.value;
		}
	}

}
