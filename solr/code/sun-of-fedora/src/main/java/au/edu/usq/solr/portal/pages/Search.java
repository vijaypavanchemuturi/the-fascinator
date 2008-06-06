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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.tapestry.Asset;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.AssetSource;

import au.edu.usq.solr.model.Document;
import au.edu.usq.solr.model.Facet;
import au.edu.usq.solr.model.FacetList;
import au.edu.usq.solr.model.Response;
import au.edu.usq.solr.portal.Page;
import au.edu.usq.solr.portal.Pagination;
import au.edu.usq.solr.portal.Portal;
import au.edu.usq.solr.portal.Searcher;
import au.edu.usq.solr.portal.State;
import au.edu.usq.solr.portal.pages.portal.Create;

@IncludeStylesheet("context:css/default.css")
public class Search {

    private Logger log = Logger.getLogger(Search.class);

    private static final String PORTAL_ALL = "all";

    private static final String QUERY_ALL = "*:*";

    @Inject
    private AssetSource assetSource;

    @ApplicationState
    private State state;

    private String[] context;

    private String portalName;

    private String query;

    private Response response;

    private Document doc;

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

    void onActivate(Object[] params) {
        if (params.length == 0) {
            portalName = PORTAL_ALL;
        }
        if (params.length >= 1) {
            portalName = params[0].toString();
        }
        if (params.length == 2) {
            query = params[1].toString();
        }
        if (query == null || params.length == 1) {
            query = QUERY_ALL;
        }

        log.info("onActivate: " + getPortalName() + "/" + query);
        pageNum = Math.max(pageNum, 1);

        if (facetLimits == null) {
            facetLimits = new HashSet<String>();
        }

        Portal currentPortal = state.getCurrentPortal();
        int recordsPerPage = currentPortal.getRecordsPerPage();

        Searcher searcher = new Searcher(state.getSolrBaseUrl());
        searcher.setRows(recordsPerPage);
        searcher.setFacetMinCount(2);
        searcher.setFacetLimit(currentPortal.getFacetCount());
        searcher.setFacetFields(currentPortal.getFacetFieldList());

        Portal found = null;
        for (Portal p : state.getPortals()) {
            if (portalName.equals(p.getName())) {
                found = p;
                break;
            }
        }
        if (found == null) {
            log.debug("Portal '" + portalName + "' not found, using ALL");
            portalName = PORTAL_ALL;
            found = state.getPortalManager().getDefaultPortal();
        }
        state.setCurrentPortal(found);
        searcher.setBaseFacetQuery(found.getQuery());
        log.debug("portal=" + portalName);
        for (String facetLimit : facetLimits) {
            log.info("facetLimit: " + facetLimit);
            searcher.addFacetQuery(facetLimit);
        }

        searcher.setStart((pageNum - 1) * recordsPerPage);
        try {
            response = searcher.find(query);
        } catch (IOException e) {
            // TODO exception page
            e.printStackTrace();
        }
        if (response != null) {
            int numFound = response.getResult().getNumFound();
            pagination = new Pagination(pageNum, numFound, recordsPerPage);
        } else {
            selectFirstPage();
        }

        if (QUERY_ALL.equals(query)) {
            query = null;
        }
    }

    String[] onPassivate() {
        String p = portalName == null ? PORTAL_ALL : portalName;
        String q = query == null ? QUERY_ALL : query;
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

    public boolean getShowResults() {
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

    @OnEvent(component = "addfacet")
    void addFacetLimit(Object[] limits) {
        selectFirstPage();
        facetLimits.add(parseFacetLimits(limits, '/'));
    }

    @OnEvent(component = "removefacet")
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

    @OnEvent(component = "clearfacets")
    void clearFacetLimits() {
        selectFirstPage();
        facetLimits.clear();
    }

    @OnEvent(component = "createportal")
    Object createFromSelected() {
        String[] facetArray = facetLimits.toArray(new String[] {});
        String limits = parseFacetLimits(facetArray, '+');
        String portalName = facetArray[facetArray.length - 1];
        portalName = portalName.substring(portalName.indexOf(':') + 2,
            portalName.length() - 1);
        Portal current = state.getCurrentPortal();
        if (current != null && !"".equals(current.getQuery())) {
            limits = current.getQuery() + "+" + limits;
        }
        Portal portal = new Portal(portalName, limits);
        createPortalPage.setPortal(portal);
        return createPortalPage;
    }

    @OnEvent(component = "selectpage")
    void selectPage(int pageNum) {
        this.pageNum = pageNum;
    }

    @OnEvent(component = "firstpage")
    void selectFirstPage() {
        this.pageNum = 1;
    }

    @OnEvent(component = "lastpage")
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

    public Asset getStylesheet() {
        String cssPath = portalName + "/style.css";
        String assetPath = "velocity:" + cssPath;
        Asset stylesheet = assetSource.getClasspathAsset(assetPath);
        try {
            log.info("stylesheeturl:" + stylesheet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return stylesheet;
    }

    public void setStylesheet(Asset stylesheet) {
    }

    public String getPortalName() {
        return portalName;
    }

    public void setPortalName(String portal) {
        this.portalName = portal;
    }

    public String getQuery() {
        return QUERY_ALL.equals(query) ? "" : query;
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

    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
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
        return state.getCurrentPortal().getFacetFields().get(name);
    }

    public State getState() {
        return state;
    }
}
