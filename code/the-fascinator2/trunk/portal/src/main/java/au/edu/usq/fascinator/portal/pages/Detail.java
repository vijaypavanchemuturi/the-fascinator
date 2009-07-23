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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.model.Facet;
import au.edu.usq.fascinator.model.FacetList;
import au.edu.usq.fascinator.model.Rdf;
import au.edu.usq.fascinator.model.Response;
import au.edu.usq.fascinator.model.types.DocumentType;
import au.edu.usq.fascinator.model.types.ResultType;
import au.edu.usq.fascinator.portal.Portal;
import au.edu.usq.fascinator.portal.Searcher;
import au.edu.usq.fascinator.portal.State;
import au.edu.usq.fascinator.portal.services.PortalManager;
import au.edu.usq.fascinator.portal.services.RegistryManager;
import au.edu.usq.fascinator.portal.services.VelocityResourceLocator;
import au.edu.usq.fascinator.util.BinaryStreamResponse;

@IncludeStylesheet("context:css/default.css")
public class Detail {

    public static final String DC_ID = "DC0";

    private Logger log = Logger.getLogger(Detail.class);

    @SessionState
    private State state;

    @Inject
    private VelocityResourceLocator locator;

    @Inject
    private RegistryManager registryManager;

    @Inject
    private PortalManager portalManager;

    private String uuid;

    private Rdf item;

    private String portalName;

    // private List<DatastreamType> dsList;

    private boolean viewable;

    @Inject
    private Request request;

    @InjectPage
    private Search searchPage;

    @Inject
    private Request httpRequest;

    @Persist
    private URL refererUrl;

    @Persist
    private String portalValue;

    private String clusterValue = "";

    Object onActivate(Object[] params) {
        this.portalValue = params[0].toString();
        log.info("portalValue: " + this.portalValue);
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.endsWith("/login")
            && !referer.endsWith("/logout")) {
            try {
                refererUrl = new URL(referer);
            } catch (MalformedURLException mue) {
                refererUrl = null;
                log.warn("Bad referer: " + referer + " (" + mue.getMessage()
                    + ")");
            }
        }
        log.info("Referer: " + referer + " (" + refererUrl + ")");
        if (params.length >= 2) {
            String portal = params[0].toString();
            setPortalName(portal);
            state.setPortal(portalManager.get(portal));
            try {
                String idParam = params[1].toString();
                log.info("idParam: " + idParam);
                // if (idParam.startsWith("uuid") ||
                // idParam.startsWith("sword")) {
                uuid = URLDecoder.decode(idParam, "UTF-8");
                log.info("uuid in onActivate: " + uuid);
                viewable = checkRole();
                // }
                if (params.length > 2) {
                    String dsIdParam = "";
                    for (int i = 2; i < params.length; i++) {
                        dsIdParam += params[i].toString();
                        if (i < params.length - 1) {
                            dsIdParam += "/";
                        }
                    }
                    String dsId = URLDecoder.decode(dsIdParam, "UTF-8");
                    return download(dsId);
                }
            } catch (UnsupportedEncodingException e) {
            }
        }
        return null;
    }

    String[] onPassivate() {
        return new String[] { portalName, uuid };
    }

    public String getReferer() {
        String referer = request.getHeader("Referer");
        log.info("Referer: " + referer);
        return refererUrl == null ? null : refererUrl.toString();
    }

    @OnEvent(value = "deletePackage")
    Search deletePackage(String uuid) {
        // try {
        //
        // FedoraRestClient client = registryManager.getClient();
        // client.purgeObject(uuid);
        //
        // } catch (IOException ioe) {
        // log.error(ioe);
        // }
        // try {
        // Thread.currentThread();
        // Thread.sleep(1000);// sleep for 1000 ms
        // } catch (InterruptedException ie) {
        //
        // }
        // searchPage.setPortalName(this.portalValue);
        return searchPage;
    }

    @OnEvent(value = "download")
    Object download(String dsId) {
        log.info("DOWNLOAD: " + uuid + "," + dsId);
        viewable = DC_ID.equals(dsId) || checkRole(dsId, true);
        log.info("viewable??: " + viewable);
        // NEED TO FIX this so i can get the download path
        if (viewable) {
            Payload content = registryManager.getPayload(uuid, dsId);
            try {
                return new BinaryStreamResponse(content.getContentType(),
                    content.getInputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            log.info("Is it indexed: " + checkRole(dsId, false));
        }

        // if (viewable) {
        // DatastreamType ds = registryManager.getDatastream(uuid, dsId);
        // String contentType = ds.getMimeType();
        // InputStream content = registryManager.getDatastreamAsStream(uuid,
        // dsId);
        // log.info("DOWNLOAD: " + content + ", TYPE: " + contentType);
        // return new BinaryStreamResponse(contentType, content);
        // } else {
        // boolean isIndexed = checkRole(dsId, false);
        // if (isIndexed) {
        // return new HttpStatusCodeResponse(403, "Forbidden");
        // } else {
        // return new HttpStatusCodeResponse(404, "Not found");
        // }
        // }
        return null;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getStylesheet() {
        return locator.getLocation(state.getPortalName(), "style.css");
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEncodedUuid() {
        try {
            log.info("getEncodedUuid: " + URLEncoder.encode(getUuid(), "UTF-8"));
            return URLEncoder.encode(getUuid(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return getUuid();
    }

    public Rdf getMetadata(String dsId) {
        setUuid(dsId);
        JsonConfig config;
        try {
            config = new JsonConfig();
            File systemFile = config.getSystemFile();
            registryManager.getClient(systemFile);

            item = registryManager.getRdf(dsId);
            // return registryManager.getXmlDatastream(dsId);
            return item;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
        // registryManager.getClient()
        // return null;

        // Add registry manager to call the Storage
        // Storage --> getXmlDatastream
        // http://fascinator.usq.edu.au/trac/browser/code/the-fascinator/portal/src/main/java/au/edu/usq/solr/portal/services/RegistryManagerImpl.java
        // return registryManager.getXmlDatastream(uuid, dsId);
    }

    // public DublinCore getMetadata() {
    // // if (item == null) {
    // // item = registryManager.getMetadata(uuid);
    // // }
    // return item;
    // }

    public List<Payload> getDatastreams() {
        return registryManager.getPayloadList(uuid);

        // if (dsList == null) {
        // dsList = new ArrayList<DatastreamType>();
        // }
        // dsList.clear();
        // DatastreamType dcDatastream = new DatastreamType();
        // dcDatastream.setDsid(DC_ID);
        // dcDatastream.setLabel("Dublin Core Metadata");
        // dcDatastream.setMimeType("text/xml");
        // dsList.add(dcDatastream);
        // Searcher searcher = searchPage.getSecureSearcher();
        // searcher.addFacetQuery("pid:\"" + uuid + "\"");
        // searcher.addFacetQuery("item_type:\"datastream\"");
        // searcher.setRows(1000);
        // try {
        // Response response = searcher.findAll();
        // ResultType result = response.getResult();
        // if (result.getNumFound() > 0) {
        // for (DocumentType doc : result.getItems()) {
        // DatastreamType ds = new DatastreamType();
        // String dsId = doc.field("identifier");
        // if (dsId == null) {
        // dsId = doc.field("id");
        // }
        // dsId = URLEncoder.encode(dsId, "UTF-8");
        // ds.setDsid(dsId);
        // ds.setLabel(doc.field("title"));
        // ds.setMimeType(doc.field("format"));
        // dsList.add(ds);
        // }
        // }
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // return dsList;
    }

    public String getPortalName() {
        return portalName;
    }

    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

    public boolean isViewable() {
        return viewable;
    }

    boolean checkRole() {
        return checkRole(null, true);
    }

    boolean checkRole(String dsId, boolean secure) {
        log.info("checkRole: " + uuid + " : " + dsId);
        Searcher searcher = searchPage.getSearcher(secure);
        // searcher.addFacetQuery("pid:\"" + uuid + "\"");
        searcher.addFacetQuery("storageId:\"" + uuid + "\"");
        if (dsId != null) {
            searcher.addFacetQuery("identifier:\"" + dsId + "\"");
        }
        try {
            Response response = searcher.findAll();
            ResultType result = response.getResult();
            return result.getNumFound() > 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return false;
    }

    public FacetList getRelatedItems() {
        FacetList facetList = null;
        Portal portal = state.getPortal();
        Searcher searcher = searchPage.getSecureSearcher();
        searcher.addFacetQuery("pid:\"" + uuid + "\"");
        searcher.addFacetQuery("item_type:\"object\"");
        searcher.setRows(1000);
        log.info("pid=" + uuid);
        try {
            Response response = searcher.findAll();
            ResultType result = response.getResult();
            String clusterFacet = portal.getClusterFacet();
            String clusterFacetLabel = portal.getClusterFacetLabel();
            clusterValue = result.getItems().get(0).field(clusterFacet);
            searcher = searchPage.getSecureSearcher();
            searcher.addFacetQuery("item_type:\"object\"");
            searcher.addFacetQuery(clusterFacet + ":\"" + clusterValue + "\"");
            searcher.addFacetField(clusterFacetLabel);
            response = searcher.findAll();
            List<DocumentType> items = response.getResult().getItems();
            facetList = response.getFacetLists().iterator().next();
            for (DocumentType doc : items) {
                // log.info(doc.field("pid"));
                for (String key : doc.fields(clusterFacetLabel)) {
                    Facet facet = facetList.findFacet(key);
                    facet.setUserData(doc.field(portal.getClusterFacetData()));
                }
            }
            return facetList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return facetList;
    }

    public String getClusterValue() {
        return clusterValue;
    }

    public String getClustedILOX() {
        String results = "clustedILOX";
        try {
            Searcher searcher = searchPage.getSecureSearcher();
            Response response = searcher.findAll();
            List<DocumentType> items = response.getResult().getItems();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}
