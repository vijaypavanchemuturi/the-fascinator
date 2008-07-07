/* 
 * Sun of Fedora - Solr Portal
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
package au.edu.usq.solr.portal.pages;

import static au.edu.usq.solr.portal.services.PortalManager.DEFAULT_PORTAL_NAME;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.IncludeJavaScriptLibrary;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.Request;

import au.edu.usq.solr.model.Facet;
import au.edu.usq.solr.model.FacetList;
import au.edu.usq.solr.model.Response;
import au.edu.usq.solr.model.types.DocumentType;
import au.edu.usq.solr.portal.Page;
import au.edu.usq.solr.portal.Pagination;
import au.edu.usq.solr.portal.Portal;
import au.edu.usq.solr.portal.Role;
import au.edu.usq.solr.portal.Searcher;
import au.edu.usq.solr.portal.State;
import au.edu.usq.solr.portal.pages.portal.Create;
import au.edu.usq.solr.portal.services.PortalManager;
import au.edu.usq.solr.portal.services.VelocityResourceLocator;

@IncludeStylesheet("context:css/default.css")
@IncludeJavaScriptLibrary("context:js/default.js")
public class Search {

    private Logger log = Logger.getLogger(Search.class);

    @Inject
    private PortalManager portalManager;

    @ApplicationState
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
    private HttpServletRequest httpServletRequest;

    @Inject
    private Request httpRequest;

    @Inject
    private org.apache.tapestry.services.Response httpResponse;

    @Inject
    private VelocityResourceLocator locator;

    @Persist
    private String sortField;

    void onActivate(Object[] params) {
        // handle forms from velocity templates
        List<String> paramNames = httpRequest.getParameterNames();
        if (!paramNames.isEmpty() && !paramNames.contains("t:ac")) {
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
        state.setPortal(found);

        int recordsPerPage = found.getRecordsPerPage();
        pageNum = Math.max(pageNum, 1);

        Searcher searcher = getSecureSearcher();
        searcher.setBaseFacetQuery(found.getQuery());
        searcher.setRows(recordsPerPage);
        searcher.setFacetMinCount(1);
        searcher.setFacetLimit(found.getFacetCount());
        searcher.setFacetFields(found.getFacetFieldList());
        searcher.setStart((pageNum - 1) * recordsPerPage);
        searcher.addFacetQuery("item_type:object");

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
            response = searcher.find(query);
        } catch (IOException e) {
            log.error("Error reading response", e);
        } catch (JAXBException e) {
            log.error("Error parsing response", e);
        }

        if (response != null) {
            int numFound = response.getResult().getNumFound();
            pagination = new Pagination(pageNum, numFound, recordsPerPage);
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
        this.pageNum = 1;
    }

    @OnEvent(value = "lastpage")
    void selectLastPage() {
        this.pageNum = pagination.getLastPage();
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

    public Searcher getSecureSearcher() {
        Searcher searcher = new Searcher(state.getSolrBaseUrl());
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
        return searcher;
    }
}
