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
package au.edu.usq.fascinator.portal.services.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.tools.generic.introspection.JythonUberspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.portal.FormData;
import au.edu.usq.fascinator.portal.services.DynamicPageService;
import au.edu.usq.fascinator.portal.services.ScriptingServices;

public class DynamicPageServiceImpl implements DynamicPageService {

    private static final String DEFAULT_PORTAL_HOME_DIR = "/opt/the-fascinator/config";

    private static final String DEFAULT_TEMPLATE_NAME = "template";

    private static final String DEFAULT_SCRIPT_ENGINE = "python";

    private Logger log = LoggerFactory.getLogger(DynamicPageServiceImpl.class);

    @Inject
    private Request request;

    @Inject
    private Response response;

    @Inject
    private ScriptingServices scriptingServices;

    private Object templateObject;

    private Object pageObject;

    private String templateName;

    private ScriptEngine scriptEngine;

    public DynamicPageServiceImpl() {
        try {
            JsonConfig config = new JsonConfig();
            templateName = config.get("portal/templateName",
                    DEFAULT_TEMPLATE_NAME);

            // setup scripting engine
            String engineName = config.get("portal/scriptEngine",
                    DEFAULT_SCRIPT_ENGINE);
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            scriptEngine = scriptEngineManager.getEngineByName(engineName);

            // setup velocity engine
            String portalDir = config.get("portal/home",
                    DEFAULT_PORTAL_HOME_DIR);
            Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, portalDir);
            // Velocity.setProperty(Velocity.VM_LIBRARY, "portal-library.vm");
            Velocity.setProperty(Velocity.UBERSPECT_CLASSNAME,
                    JythonUberspect.class.getName());
            Velocity.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getResource(String portalId, String resourceName) {
        String path = portalId + "/" + resourceName;
        try {
            return RuntimeSingleton.getContent(path).getResourceLoader()
                    .getResourceStream(path);
        } catch (Exception e) {
            log.error("Failed to get resource: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void render(String portalId, String pageName, OutputStream out,
            FormData formData) {
        // run page and template scripts
        templateObject = new Object();
        try {
            templateObject = evalScript(portalId, templateName, formData);
        } catch (ScriptException se) {
            log.warn("Failed to run page script!", se);
        }

        pageObject = new Object();
        try {
            pageObject = evalScript(portalId, pageName, formData);
        } catch (ScriptException se) {
            log.warn("Failed to run page script!\n==========\n{}\n==========",
                    se.getMessage());
        }

        try {
            // set up the velocity context
            VelocityContext vc = new VelocityContext();
            vc.put("systemProperties", System.getProperties());
            vc.put("request", request);
            vc.put("response", response);
            vc.put("formData", formData);
            vc.put("contextPath", request.getContextPath());
            vc.put("portalId", portalId);
            vc.put("page", templateObject);
            vc.put("self", pageObject);

            // render the page content
            StringWriter pageContentWriter = new StringWriter();
            Template pageContent = getTemplate(portalId, pageName);
            pageContent.merge(vc, pageContentWriter);
            vc.put("pageContent", pageContentWriter.toString());

            // render the page using the template
            Template page = getTemplate(portalId, templateName);
            Writer pageWriter = new OutputStreamWriter(out, "UTF-8");
            page.merge(vc, pageWriter);
            pageWriter.close();
        } catch (Exception e) {
            log.error("Failed rendering: {}", e.getMessage());
        }
    }

    private Object evalScript(String portalId, String scriptName,
            FormData formData) throws ScriptException {
        Object scriptObject = new Object();
        scriptName = "scripts/" + scriptName + ".py";
        log.debug("Running page script {}...", scriptName);
        InputStream in = getResource(portalId, scriptName);
        if (in != null) {
            scriptEngine.put("formData", formData);
            scriptEngine.put("request", request);
            scriptEngine.put("response", response);
            scriptEngine.put("Services", scriptingServices);
            scriptEngine.eval(new InputStreamReader(in));
            scriptObject = scriptEngine.get("scriptObject");
        } else {
            log.info("No script found for {}", scriptName);
        }
        return scriptObject;
    }

    private Template getTemplate(String portalId, String templateName)
            throws Exception {
        return Velocity.getTemplate(portalId + "/" + templateName + ".vm");
    }

    public Object getTemplateObject() {
        return templateObject;
    }

    public Object getPageObject() {
        return pageObject;
    }
}
