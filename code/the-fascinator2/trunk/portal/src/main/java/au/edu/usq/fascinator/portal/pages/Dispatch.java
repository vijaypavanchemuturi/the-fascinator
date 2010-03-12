/* 
 * The Fascinator - Portal
 * Copyright (C) 2008-2009 University of Southern Queensland
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.util.TimeInterval;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.upload.services.MultipartDecoder;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

import au.edu.usq.fascinator.HarvestClient;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.MimeTypeUtil;
import au.edu.usq.fascinator.portal.FormData;
import au.edu.usq.fascinator.portal.JsonSessionState;
import au.edu.usq.fascinator.portal.services.DynamicPageService;
import au.edu.usq.fascinator.portal.services.GenericStreamResponse;
import au.edu.usq.fascinator.portal.services.HttpStatusCodeResponse;

public class Dispatch {

    private static final String AJAX_EXT = ".ajax";

    private static final String POST_EXT = ".post";

    private static final String DEFAULT_PORTAL_ID = "default";

    private static final String DEFAULT_RESOURCE = "home";

    @Inject
    private Logger log;

    @SessionState
    private JsonSessionState sessionState;

    @Inject
    private DynamicPageService pageService;

    @Inject
    private Request request;

    @Inject
    private Response response;

    @Persist
    private Map<String, FormData> formDataMap;

    @Inject
    private MultipartDecoder decoder;

    @Inject
    private RequestGlobals rg;

    // Resource Processing variable
    private String resourceName;
    private String portalId;
    private String requestUri;
    private String requestId;
    private String[] path;
    private HttpServletRequest hsr;
    private boolean isAjax;
    private boolean isFile;
    private boolean isPost;

    // Rendering variables
    private String mimeType;
    private InputStream stream;

    public StreamResponse onActivate(Object... params) {
        log.debug("Dispatch starting : {} {}",
                request.getMethod(), request.getPath());

        // Do all our parsing
        resourceName = resourceProcessing();

        // Make sure it's valid
        if (resourceName == null) {
            return new HttpStatusCodeResponse(404, "Page not found: "
                    + resourceName);
        }

        // Initialise storage for our form data
        // if there's no persistant data found.
        if (formDataMap == null) {
            formDataMap = Collections
                    .synchronizedMap(new HashMap<String, FormData>());
        }

        // make static resources cacheable
        if (resourceName.indexOf(".") != -1 && !isAjax) {
            response.setHeader("Cache-Control", "public");
            response.setDateHeader("Expires", System.currentTimeMillis()
                    + new TimeInterval("10y").milliseconds());
        }

        // Are we doing a file upload?
        hsr = rg.getHTTPServletRequest();
        isFile = ServletFileUpload.isMultipartContent(hsr);
        if (isFile) {
            fileProcessing();
        }

        // Redirection?
        if (redirectTest()) {
            return GenericStreamResponse.noResponse();
        }

        // Page render time
        renderProcessing();

        // clear formData
        formDataMap.remove(requestId);

        return new GenericStreamResponse(mimeType, stream);
    }

    private void fileProcessing() {
        // What we are looking for
        UploadedFile uploadedFile = null;
        String plugin = null;

        // Get all the parameters
        for (String param : request.getParameterNames()) {
            // A tmp file
            UploadedFile tmpFile = decoder.getFileUpload(param);
            if (tmpFile != null) {
                // Our file
                uploadedFile = tmpFile;
            } else {
                // Normalf form fields
                if (param.equals("upload-file-plugin")) {
                    plugin = request.getParameter(param);
                }
            }
        }
        if (uploadedFile == null) {
            log.error("No uploaded file found!");
            return;
        }
        if (plugin == null) {
            log.error("No plugin provided with form data.");
            return;
        }

        // Query the system config file
        JsonConfigHelper workflow_config;
        Map<String, JsonConfigHelper> available_plugins;
        try {
            JsonConfig config = new JsonConfig();
            config.getSystemFile();
            String workflow_config_path = config.get("portal/uploader");
            File workflow_config_file = new File(workflow_config_path);
            workflow_config = new JsonConfigHelper(workflow_config_file);
            available_plugins = workflow_config.getJsonMap("/");
        } catch (IOException e) {
            log.error("Failed to get the System config file.");
            return;
        }

        // Get the plugin's file directory
        String file_path = available_plugins.get(plugin).get("upload-path");

        // Write the file to that directory
        file_path = file_path + "/" + uploadedFile.getFileName();
        File file = new File(file_path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException ex) {
                log.error("Failed writing file", ex);
                return;
            }
        }
        log.debug("Writing file to disk : " + file_path);
        uploadedFile.write(file);

        // Make sure the new file gets harvested
        File harvest_config = new File(available_plugins.get(plugin).get(
                "json-config"));
        try {
            HarvestClient harvester = new HarvestClient(harvest_config, file);
            harvester.start();
            harvester.shutdown();
        } catch (Exception ex) {
            log.error("Failed harvest", ex);
            return;
        }

        // Now create some session data for use later
        Map<String, String> file_details = new LinkedHashMap<String, String>();
        file_details.put("name", uploadedFile.getFileName());
        file_details.put("location", file_path);
        file_details.put("size", String.valueOf(uploadedFile.getSize()));
        file_details.put("type", uploadedFile.getContentType());
        sessionState.set(uploadedFile.getFileName(), file_details);
    }

    private void renderProcessing() {
        // render the page or retrieve the resource
        if ((resourceName.indexOf(".") == -1) || isAjax) {
            FormData formData = formDataMap.get(requestId);
            if (formData == null) {
                formData = new FormData(request, hsr);
            }
            formDataMap.put(requestId, formData);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mimeType = pageService.render(portalId, resourceName, out,
                    formData, sessionState);
            stream = new ByteArrayInputStream(out.toByteArray());
        } else {
            mimeType = MimeTypeUtil.getMimeType(resourceName);
            stream = pageService.getResource(portalId, resourceName);
        }
    }

    private boolean redirectTest() {
        // save form data for POST requests, since we redirect after POSTs

        // 'isPost' in particular allows special cases to
        // override and prevent redirection.
        requestId = request.getAttribute("RequestID").toString();
        if ("POST".equalsIgnoreCase(request.getMethod()) && !isPost) {
            try {
                FormData formData = new FormData(request);
                formDataMap.put(requestId, formData);
                if (isAjax == false) {
                    String redirectUri = resourceName;
                    if (path.length > 2) {
                        redirectUri += StringUtils.join(path, "/", 2,
                                path.length);
                    }
                    log.info("Redirecting to {}...", redirectUri);
                    response.sendRedirect(redirectUri);
                    return true;
                }
            } catch (IOException ioe) {
                log.warn("Failed to redirect after POST", ioe);
            }
        }
        return false;
    }

    private String resourceProcessing() {
        portalId = (String) sessionState.get("portalId", DEFAULT_PORTAL_ID);
        requestUri = request.getAttribute("RequestURI").toString();
        path = requestUri.split("/");
        resourceName = DEFAULT_RESOURCE;

        if (path.length > 1) {
            portalId = path[0].toString();
            resourceName = StringUtils.join(path, "/", 1, path.length);
        }
        String match = getBestMatchResource(resourceName);
        log.trace("resourceName = {}, match = {}", resourceName, match);

        return match;
    }

    public String getBestMatchResource(String thisResourceName) {
        isPost = requestUri.endsWith(POST_EXT);
        isAjax = thisResourceName.endsWith(AJAX_EXT);
        if (isAjax) {
            thisResourceName = thisResourceName.substring(0, thisResourceName
                    .lastIndexOf(AJAX_EXT));
        }
        if (pageService.resourceExists(portalId, thisResourceName)) {
            return thisResourceName + (isAjax ? AJAX_EXT : "");
        }
        int slash = thisResourceName.lastIndexOf('/');
        if (slash != -1) {
            return getBestMatchResource(thisResourceName.substring(0, slash));
        }
        return null;
    }
}
