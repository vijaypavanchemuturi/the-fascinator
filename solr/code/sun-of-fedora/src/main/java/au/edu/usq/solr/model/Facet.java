package au.edu.usq.solr.model;

import org.w3c.dom.Element;

public class Facet {

	private FacetList facetList;

	private String value;

	private int count;

	public Facet(FacetList facetList, Element elem) {
		this(facetList, elem.getAttribute("name"), Integer.parseInt(elem
				.getTextContent()));
	}

	public Facet(FacetList facetList, String value, int count) {
		this.facetList = facetList;
		this.value = value;
		this.count = count;
	}

	public String getName() {
		return facetList.getName();
	}

	public String getValue() {
		return value;
	}

	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return getName() + ":\"" + getValue() + "\"";
	}
}
