package au.edu.usq.solr.portal.pages;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.tapestry.Asset;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.ioc.annotations.Inject;

import au.edu.usq.solr.Configuration;
import au.edu.usq.solr.model.Facet;
import au.edu.usq.solr.model.FacetList;
import au.edu.usq.solr.model.Response;
import au.edu.usq.solr.model.SolrDoc;
import au.edu.usq.solr.portal.Page;
import au.edu.usq.solr.portal.Pagination;
import au.edu.usq.solr.portal.Portal;
import au.edu.usq.solr.portal.Searcher;

public class Search {

    private Logger log = Logger.getLogger(Search.class);

    private static final String PORTAL_ALL = "all";

    private static final String QUERY_ALL = "*:*";

    @Inject
    @Path(value = "context:css/default.css")
    private Asset stylesheet;

    @ApplicationState
    private Configuration config;

    private String[] context;

    private String portal;

    private String query;

    private Response response;

    private SolrDoc doc;

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

    String onActionFromConfigure() {
        return "config";
    }

    void onActivate(Object[] params) {
        if (params.length == 0) {
            portal = PORTAL_ALL;
        }
        if (params.length >= 1) {
            portal = params[0].toString();
        }
        if (params.length == 2) {
            query = params[1].toString();
        }
        if (query == null || params.length == 1) {
            query = QUERY_ALL;
        }

        log.info("onActivate: " + portal + "/" + query);
        pageNum = Math.max(pageNum, 1);

        if (facetLimits == null) {
            facetLimits = new HashSet<String>();
        }

        int recordsPerPage = config.getRecordsPerPage();

        Searcher searcher = new Searcher(config);
        searcher.setFacetCount(config.getFacetCount());
        searcher.setFacetFields(config.getFacetFieldList());

        Portal found = null;
        for (Portal p : config.getPortals()) {
            if (portal.equals(p.getName().toLowerCase())) {
                found = p;
                break;
            }
        }
        if (found == null) {
            log.debug("Portal '" + portal + "' not found, using ALL");
            found = Portal.getDefaultPortal();
        }
        searcher.setPortal(found);

        for (String facetLimit : facetLimits) {
            log.info("facetLimit: " + facetLimit);
            searcher.addFacetLimit(facetLimit);
        }

        searcher.setStart((pageNum - 1) * recordsPerPage);
        response = searcher.find(query);
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
        String p = portal == null ? PORTAL_ALL : portal;
        String q = query == null ? QUERY_ALL : query;
        if (query == null) {
            return new String[] { p };
        } else {
            return new String[] { p, q };
        }
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
        facetLimits.add(parseFacetLimits(limits));
    }

    @OnEvent(component = "removefacet")
    void removeFacetLimit(Object[] limits) {
        selectFirstPage();
        facetLimits.remove(parseFacetLimits(limits));
    }

    private String parseFacetLimits(Object[] limits) {
        StringBuilder s = new StringBuilder();
        for (Object limit : limits) {
            s.append("/");
            s.append(limit);
        }
        return s.toString().substring(1);
    }

    @OnEvent(component = "clearfacets")
    void clearFacetLimits() {
        selectFirstPage();
        facetLimits.clear();
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
        return stylesheet;
    }

    public void setStylesheet(Asset stylesheet) {
        this.stylesheet = stylesheet;
    }

    public String getPortal() {
        return portal;
    }

    public void setPortal(String portal) {
        this.portal = portal;
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

    public SolrDoc getDoc() {
        return doc;
    }

    public void setDoc(SolrDoc doc) {
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

    public String[] getContext() {
        return context;
    }

    public void setContext(String[] context) {
        this.context = context;
    }
}
