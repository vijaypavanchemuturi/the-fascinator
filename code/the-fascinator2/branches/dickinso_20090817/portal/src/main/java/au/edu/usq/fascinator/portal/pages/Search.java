/* 
 * The Fascinator - Solr Portal
 * Copyright (C) 2008  University of Southern Queensland
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
package au.edu.usq.fascinator.portal.pages;

import static au.edu.usq.fascinator.portal.services.PortalManager.DEFAULT_PORTAL_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.model.Facet;
import au.edu.usq.fascinator.model.FacetList;
import au.edu.usq.fascinator.model.Response;
import au.edu.usq.fascinator.model.taxonomy.Taxonomy;
import au.edu.usq.fascinator.model.types.DocumentType;
import au.edu.usq.fascinator.pages.portal.Create;
import au.edu.usq.fascinator.portal.Page;
import au.edu.usq.fascinator.portal.Pagination;
import au.edu.usq.fascinator.portal.Portal;
import au.edu.usq.fascinator.portal.Role;
import au.edu.usq.fascinator.portal.Searcher;
import au.edu.usq.fascinator.portal.State;
import au.edu.usq.fascinator.portal.services.PortalManager;
import au.edu.usq.fascinator.portal.services.VelocityResourceLocator;

@IncludeStylesheet("context:css/default.css")
@IncludeJavaScriptLibrary("context:js/default.js")
public class Search {

    private Logger log = Logger.getLogger(Search.class);

    @Inject
    private ComponentResources resources;

    @Inject
    private PortalManager portalManager;

    @SessionState
    private State state;

    private String[] context;

    private String portalName;

    private String query;

    private Response response;

    private DocumentType doc;

    private String identifier;

    private FacetList facetList;

    private Facet facet;

    private Page page;

    private String facetLimit;

    private List<DocumentType> clusteredItems;

    @Persist
    private Set<String> facetLimits;

    @Persist
    private int pageNum;

    @Persist
    private Pagination pagination;

    @InjectPage(value = "portal/create")
    private Create createPortalPage;

    @InjectPage(value = "detail")
    private Detail detailPage;

    @Inject
    private Request httpRequest;

    @Inject
    private org.apache.tapestry5.services.Response httpResponse;

    @Inject
    private VelocityResourceLocator locator;

    @Persist
    private String sortField;

    // @Inject
    // private RegistryManager registryManager;

    private boolean searchEscape = true;

    void onActivate(Object[] params) {
        String newFacet = null;
        // handle forms from velocity templates
        List<String> paramNames = httpRequest.getParameterNames();
        for (String name : paramNames) {
            log.info("PARAM: " + name + "=" + httpRequest.getParameter(name));
        }
        if (!paramNames.isEmpty() && !paramNames.contains("t:ac")) {
            if (paramNames.contains("add")) {
                newFacet = httpRequest.getParameter("newFacet");
                log.info("newFacet: " + newFacet);
            } else if (paramNames.contains("savetags")) {
                String uuid = httpRequest.getParameter("uuid");
                String tags = httpRequest.getParameter("tags");
                log.info("saveTags: " + uuid + "," + tags);
                // saveTags(uuid, tags);
            } else {
                String portalValue = httpRequest.getParameter("portal");
                String queryValue = httpRequest.getParameter("query");
                String redirectPath = httpRequest.getContextPath() + "/search/"
                    + portalValue;
                if (queryValue != null && !"".equals(queryValue)) {
                    redirectPath += "/" + queryValue;
                }
                try {
                    selectFirstPage();
                    httpResponse.sendRedirect(redirectPath);
                    return;
                } catch (IOException e) {
                }
            }
        }

        log.info("Current portal: " + state.getPortalName());
        // normal tapestry processing
        if (params.length == 0) {
            portalName = DEFAULT_PORTAL_NAME;
        }
        if (params.length >= 1) {
            portalName = params[0].toString();
        }
        if (params.length == 2) {
            query = params[1].toString();
        }
        if (query == null || params.length == 1) {
            query = Searcher.QUERY_ALL;
        }

        Portal found = portalManager.get(portalName);
        if (found == null) {
            log.debug("Portal '" + portalName + "' not found, using DEFAULT");
            portalName = DEFAULT_PORTAL_NAME;
            found = portalManager.getDefault();
        }
        if (!portalName.equals(state.getPortalName())) {
            // clear facets if changing portals
            sortField = null;
            getFacetLimits().clear();
        }
        if (newFacet != null) {
            // simple facet name translation
            String name = newFacet;
            if (name.startsWith("f_")) {
                name = name.substring(2);
            }
            name = name.replace('_', ' ');
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            found.getFacetFields().put(newFacet, name);
        }
        state.setPortal(found);

        int recordsPerPage = found.getRecordsPerPage();
        pageNum = Math.max(pageNum, 1);

        Searcher searcher = getSecureSearcher();
        searcher.setBaseFacetQuery(found.getQuery());
        searcher.setRows(recordsPerPage);
        searcher.setFacetMinCount(1);
        searcher.setFacetSort(found.getFacetSort());
        searcher.setFacetLimit(found.getFacetCount());
        searcher.setFacetFields(found.getFacetFieldList());
        searcher.setStart((pageNum - 1) * recordsPerPage);
        searcher.addFacetQuery("item_type:object");

        // item class
        String itemClass = found.getItemClass();
        if (itemClass != null && !"".equals(itemClass)) {
            searcher.addFacetQuery("item_class:" + itemClass);
        }

        // sorting
        log.info("sortField: " + sortField);
        if (sortField != null) {
            searcher.addSortField(sortField);
        }

        for (String facetLimit : getFacetLimits()) {
            log.info("facetLimit: " + facetLimit);
            searcher.addFacetQuery(facetLimit);
        }

        try {
            response = searcher.find(query, searchEscape);
        } catch (IOException e) {
            log.error("Error reading response", e);
        } catch (JAXBException e) {
            log.error("Error parsing response", e);
        }

        if (response != null) {
            int numFound = response.getResult().getNumFound();
            pagination = new Pagination(pageNum, numFound, recordsPerPage);
            // cluster results based on a facet
            String clusterFacet = found.getClusterFacet();
            if (clusterFacet != null) {
                Map<String, DocumentType> map = new HashMap<String, DocumentType>();
                for (DocumentType doc : response.getResult().getItems()) {
                    String workId = doc.field(clusterFacet);
                    DocumentType workDoc = map.get(workId);
                    if (workDoc == null) {
                        map.put(workId, doc);
                    }
                }
                clusteredItems = new ArrayList<DocumentType>(map.values());
            }
        } else {
            selectFirstPage();
        }

        if (Searcher.QUERY_ALL.equals(query)) {
            query = null;
        }
    }

    String[] onPassivate() {
        String p = portalName == null ? DEFAULT_PORTAL_NAME : portalName;
        String q = query == null ? Searcher.QUERY_ALL : query;
        if (query == null) {
            return new String[] { p };
        } else {
            return new String[] { p, q };
        }
    }

    public String[] getContext() {
        return context;
    }

    public void setContext(String[] context) {
        this.context = context;
    }

    public boolean getHasResults() {
        if (response != null) {
            return response.getResult().getNumFound() > 0;
        }
        return false;
    }

    public boolean getShowFirst() {
        return pagination.getPage() > 5;
    }

    public boolean getShowLast() {
        return (pagination.getLastPage() > 5)
            && (pageNum != pagination.getLastPage());
    }

    @OnEvent(value = "sort")
    void sort(String field) {
        log.info("sorting by field: " + field);
        sortField = field;
    }

    @OnEvent(value = "showdetail")
    Object showDetail(String uuid) {
        detailPage.setUuid(uuid);
        detailPage.setPortalName(portalName);
        return detailPage;
    }

    @OnEvent(value = "addfacet")
    void addFacetLimit(Object[] limits) {
        selectFirstPage();
        facetLimits.add(parseFacetLimits(limits, '/'));
    }

    @OnEvent(value = "removefacet")
    void removeFacetLimit(Object[] limits) {
        selectFirstPage();
        facetLimits.remove(parseFacetLimits(limits, '/'));
    }

    @OnEvent(value = "deletefacet")
    void deleteFacet(String facetName) {
        clearFacetLimits();
        Portal portal = portalManager.get(portalName);
        if (portal != null) {
            portal.getFacetFields().remove(facetName);
        }
    }

    // @OnEvent(value = "deletePackage")
    // void deletePackage(String uuid) {
    // log.info("In delete package");
    // try {
    // FedoraRestClient client = registryManager.getClient();
    // log.info("trying to purgeObject" + uuid);
    // client.purgeObject(uuid);
    // } catch (IOException ioe) {
    // log.error(ioe);
    // }
    // }

    // private void saveTags(String uuid, String tags) {
    // try {
    // FedoraRestClient client = registryManager.getClient();
    // File tmpFile = File.createTempFile("tf-", ".xml");
    // FileOutputStream fos = new FileOutputStream(tmpFile);
    // client.get(uuid, "DC0", fos);
    // fos.close();
    // SAXReader reader = new SAXReader();
    // Document doc = reader.read(tmpFile);
    // Element root = doc.getRootElement();
    // // remove existing tags
    // List tagNodes =
    // root.selectNodes("//dc:relation[starts-with(., 'tag::')]");
    // Iterator it = tagNodes.iterator();
    // while (it.hasNext()) {
    // Element elem = (Element) it.next();
    // elem.getParent().remove(elem);
    // }
    // // add tags
    // StringTokenizer st = new StringTokenizer(tags, " ");
    // while (st.hasMoreTokens()) {
    // String tag = st.nextToken();
    // Element tagElem = (Element)
    // root.selectSingleNode("//dc:relation[.='tag::"
    // + tag + "']");
    // if (tagElem == null) {
    // tagElem = root.addElement("dc:relation");
    // }
    // tagElem.setText("tag::" + tag);
    // }
    // StringWriter sw = new StringWriter();
    // doc.write(sw);
    // sw.close();
    // client.modifyDatastream(uuid, "DC0", sw.toString());
    // // tmpFile.delete();
    // } catch (IOException ioe) {
    // log.error(ioe);
    // } catch (DocumentException de) {
    // log.error(de);
    // }
    // }

    private String parseFacetLimits(Object[] limits, char sep) {
        StringBuilder s = new StringBuilder();
        for (Object limit : limits) {
            s.append(sep);
            s.append(limit);
        }
        String t = s.toString();
        if (t.length() > 0) {
            t = t.substring(1);
        }
        return t;
    }

    @OnEvent(value = "clearfacets")
    void clearFacetLimits() {
        selectFirstPage();
        facetLimits.clear();
    }

    @OnEvent(value = "createportal")
    Object createFromSelected() {
        String[] facetArray = facetLimits.toArray(new String[] {});
        String limits = parseFacetLimits(facetArray, '+');
        String newPortalName = facetArray[facetArray.length - 1];
        newPortalName = newPortalName.substring(newPortalName.indexOf(':') + 2,
            newPortalName.length() - 1);
        newPortalName = newPortalName.toLowerCase().replaceAll(" ", "");
        Portal current = state.getPortal();
        if (current != null && !"".equals(current.getQuery())) {
            limits = current.getQuery() + "+" + limits;
        }
        Portal portal = new Portal(newPortalName, limits);
        createPortalPage.setPortal(portal);
        return createPortalPage;
    }

    @OnEvent(value = "selectpage")
    void selectPage(int pageNum) {
        this.pageNum = pageNum;
    }

    @OnEvent(value = "firstpage")
    void selectFirstPage() {
        pageNum = 1;
    }

    @OnEvent(value = "lastpage")
    void selectLastPage() {
        pageNum = pagination.getLastPage();
    }

    @OnEvent(value = "download")
    Object download(String uuid, String dsId) {
        detailPage.setUuid(uuid);
        detailPage.setState(state);
        detailPage.setPortalName(portalName);
        return detailPage.download(dsId);
    }

    @OnEvent(value = "tag")
    void tag(String uuid) {
        log.info("tagging " + uuid);
    }

    public FacetList getFacetList() {
        return facetList;
    }

    public void setFacetList(FacetList facetList) {
        this.facetList = facetList;
    }

    public Facet getFacet() {
        return facet;
    }

    public void setFacet(Facet facet) {
        this.facet = facet;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public String getStylesheet() {
        return locator.getLocation(portalName, "style.css");
    }

    public String getPortalName() {
        return portalName;
    }

    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

    public String getQuery() {
        return Searcher.QUERY_ALL.equals(query) ? "" : query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public DocumentType getDoc() {
        return doc;
    }

    public void setDoc(DocumentType doc) {
        this.doc = doc;
    }

    public boolean isIdentifierLink() {
        return getIdentifier().startsWith("http://");
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFacetLimit() {
        return facetLimit;
    }

    public String getFacetLimitClean() {
        return facetLimit.replaceAll("\"", "").trim();
    }

    public void setFacetLimit(String facetLimit) {
        this.facetLimit = facetLimit;
    }

    public Set<String> getFacetLimits() {
        if (facetLimits == null) {
            facetLimits = new HashSet<String>();
        }
        return facetLimits;
    }

    public void setFacetLimits(Set<String> facetLimits) {
        this.facetLimits = facetLimits;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public String getFacetListDisplayName() {
        String name = getFacetList().getName();
        return state.getPortal().getFacetFields().get(name);
    }

    public State getState() {
        return state;
    }

    public boolean isSelectedFacet(String facet) {
        for (String selected : facetLimits) {
            int pos = selected.indexOf(':');
            String value = selected.substring(pos + 1).replaceAll("\"", "");
            if (facet.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public Searcher getSearcher(boolean secure) {
        Searcher searcher = new Searcher(state.getSolrBaseUrl());
        searcher.setBaseFacetQuery(state.getPortal().getQuery());

        // String solrUser = state.getProperty("solr.user");
        // String solrPass = state.getProperty("solr.pass");
        JsonConfig config;
        String solrUser = null;
        String solrPass = null;
        try {
            config = new JsonConfig();
            File systemFile = config.getSystemFile();
            config = new JsonConfig(systemFile);

            solrUser = config.get("indexer/solr/username"); // "solrAdmin";
            solrPass = config.get("indexer/solr/password");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.warn("Unable to load system file");
            e.printStackTrace();
        }

        if (solrUser != null && solrPass != null) {
            searcher.authenticate(solrUser, solrPass);
        }
        if (secure) {
            String accessQuery = "";
            boolean firstRole = true;
            for (Role role : state.getUserRoles()) {
                if (firstRole) {
                    firstRole = false;
                    accessQuery = role.getQuery();
                } else {
                    accessQuery += " OR " + role.getQuery();
                }
            }
            accessQuery = accessQuery.trim();
            log.info("accessQuery: " + accessQuery);
            if (accessQuery != null) {
                searcher.addFacetQuery(accessQuery);
            }
        }
        return searcher;
    }

    public Searcher getSecureSearcher() {
        return getSearcher(true);
    }

    public String getFeedUrl() {
        Link link = resources.createPageLink("feed", false, portalName);
        return link.toAbsoluteURI();
    }

    // public String getTags(String uuid) {
    // String tags = "";
    // try {
    // FedoraRestClient client = registryManager.getClient();
    // File tmpFile = File.createTempFile("tf-", ".xml");
    // FileOutputStream fos = new FileOutputStream(tmpFile);
    // client.get(uuid, "DC0", fos);
    // fos.close();
    // SAXReader reader = new SAXReader();
    // Document doc = reader.read(tmpFile);
    // Element root = doc.getRootElement();
    // // remove existing tags
    // List tagNodes =
    // root.selectNodes("//dc:relation[starts-with(., 'tag::')]");
    // Iterator it = tagNodes.iterator();
    // while (it.hasNext()) {
    // Element elem = (Element) it.next();
    // tags += elem.getTextTrim().substring(5);
    // tags += " ";
    // }
    // tmpFile.delete();
    // } catch (IOException ioe) {
    // log.error(ioe);
    // } catch (DocumentException de) {
    // log.error(de);
    // }
    // return tags.trim();
    // }

    public Taxonomy getTaxonomy() {
        Taxonomy t = new Taxonomy();
        t.load(new InputStreamReader(getClass().getResourceAsStream(
            "/anzsrc_for")));
        return t;
    }

    public List<DocumentType> getClusteredItems() {
        if (clusteredItems == null) {
            clusteredItems = new ArrayList<DocumentType>();
        }
        return clusteredItems;
    }

    public boolean getSearchEscape() {
        return searchEscape;
    }

    public void setSearchEscape(boolean value) {
        searchEscape = value;
    }
}
