package au.edu.usq.solr.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Result {

	private String name;

	private int numFound;

	private int start;

	private List<SolrDoc> docs;

	public Result(Element elem) {
		this(elem.getAttribute("name"), Integer.parseInt(elem
				.getAttribute("numFound")), Integer.parseInt(elem
				.getAttribute("start")));
		NodeList docNodes = elem.getElementsByTagName("doc");
		for (int i = 0; i < docNodes.getLength(); i++) {
			Element docElem = (Element) docNodes.item(i);
			docs.add(new SolrDoc(docElem));
		}
	}

	public Result(String name, int numFound, int start) {
		this.name = name;
		this.numFound = numFound;
		this.start = start;
		docs = new ArrayList<SolrDoc>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumFound() {
		return numFound;
	}

	public int getStart() {
		return start;
	}

	public List<SolrDoc> getDocs() {
		return docs;
	}
}
