package au.edu.usq.solr.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FacetList {

	private String name;

	private List<Facet> facets;

	public FacetList(Element elem) {
		this(elem.getAttribute("name"));
		NodeList facetNodes = elem.getElementsByTagName("int");
		for (int i = 0; i < facetNodes.getLength(); i++) {
			Element facetElem = (Element) facetNodes.item(i);
			facets.add(new Facet(this, facetElem));
		}
	}

	public FacetList(String name) {
		this.name = name;
		facets = new ArrayList<Facet>();
	}

	public String getName() {
		return name;
	}

	public List<Facet> getFacets() {
		return facets;
	}
}
