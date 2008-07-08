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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.util.TextStreamResponse;

import au.edu.usq.solr.fedora.DatastreamType;
import au.edu.usq.solr.model.DublinCore;
import au.edu.usq.solr.model.Response;
import au.edu.usq.solr.model.types.DocumentType;
import au.edu.usq.solr.model.types.ResultType;
import au.edu.usq.solr.portal.Searcher;
import au.edu.usq.solr.portal.State;
import au.edu.usq.solr.portal.services.RegistryManager;
import au.edu.usq.solr.portal.services.VelocityResourceLocator;
import au.edu.usq.solr.util.BinaryStreamResponse;

@IncludeStylesheet("context:css/default.css")
public class Detail {

    public static final String DC_ID = "DC0";

    private Logger log = Logger.getLogger(Detail.class);

    @ApplicationState
    private State state;

    @Inject
    private VelocityResourceLocator locator;

    @Inject
    private RegistryManager registryManager;

    private String uuid;

    private DublinCore item;

    private String portalName;

    private List<DatastreamType> dsList;

    private boolean viewable;

    @Inject
    private Request request;

    @InjectPage
    private Search searchPage;

    @Persist
    private URL refererUrl;

    void onActivate(Object[] params) {
        String referer = request.getHeader("Referer");
        if (!referer.endsWith("/login") && !referer.endsWith("/logout")) {
            try {
                refererUrl = new URL(referer);
            } catch (MalformedURLException mue) {
                refererUrl = null;
                log.warn("Bad referer: " + referer + " (" + mue.getMessage()
                    + ")");
            }
        }
        log.info("Referer: " + referer + " (" + refererUrl + ")");
        if (params.length > 0) {
            try {
                String idParam = params[0].toString();
                if (idParam.startsWith("uuid")) {
                    uuid = URLDecoder.decode(params[0].toString(), "UTF-8");
                    viewable = checkRole();
                }
            } catch (UnsupportedEncodingException e) {
            }
        }
    }

    String onPassivate() {
        return uuid;
    }

    public String getReferer() {
        String referer = request.getHeader("Referer");
        log.info("Referer: " + referer);
        return refererUrl == null ? null : refererUrl.toString();
    }

    @OnEvent(value = "download")
    Object download(String dsId) {
        viewable = DC_ID.equals(dsId) || checkRole(dsId);
        if (viewable) {
            DatastreamType ds = registryManager.getDatastream(uuid, dsId);
            String contentType = ds.getMimeType();
            if (contentType.startsWith("text/")) {
                return new TextStreamResponse(contentType,
                    registryManager.getDatastreamAsString(uuid, dsId));
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                registryManager.getDatastreamAsStream(uuid, dsId, out);
                return new BinaryStreamResponse(contentType,
                    new ByteArrayInputStream(out.toByteArray()));
            }
        }
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

    public DublinCore getMetadata() {
        if (item == null) {
            item = registryManager.getMetadata(uuid);
        }
        return item;
    }

    public List<DatastreamType> getDatastreams() {
        if (dsList == null) {
            dsList = new ArrayList<DatastreamType>();
        }
        Searcher searcher = searchPage.getSecureSearcher();
        searcher.addFacetQuery("pid:\"" + uuid + "\"");
        try {
            Response response = searcher.findAll();
            ResultType result = response.getResult();
            if (result.getNumFound() > 0) {
                for (DocumentType doc : result.getItems()) {
                    String id = doc.field("id");
                    int slash = id.lastIndexOf('/') + 1;
                    DatastreamType ds = new DatastreamType();
                    if (slash == 0) {
                        ds.setDsid(DC_ID);
                        ds.setLabel("Dublin Core Metadata");
                        ds.setMimeType("text/xml");
                    } else {
                        ds.setDsid(doc.field("identifier"));
                        ds.setLabel(doc.field("title"));
                        ds.setMimeType(doc.field("format"));
                    }
                    dsList.add(ds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsList;
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
        return checkRole(null);
    }

    boolean checkRole(String dsId) {
        log.info("checkRole: " + uuid + " : " + dsId);
        Searcher searcher = searchPage.getSecureSearcher();
        searcher.addFacetQuery("pid:\"" + uuid + "\"");
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
}
