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

import javax.script.Bindings;
import javax.script.ScriptContext;
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
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.apache.velocity.tools.generic.introspection.JythonUberspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.portal.FormData;
import au.edu.usq.fascinator.portal.JsonSessionState;
import au.edu.usq.fascinator.portal.services.DynamicPageService;
import au.edu.usq.fascinator.portal.services.ScriptingServices;

public class DynamicPageServiceImpl implements DynamicPageService {

    private static final String DEFAULT_PORTAL_HOME_DIR = "/opt/the-fascinator/config";

    private static final String DEFAULT_LAYOUT_TEMPLATE = "layout";

    private static final String DEFAULT_SCRIPT_ENGINE = "python";

    private Logger log = LoggerFactory.getLogger(DynamicPageServiceImpl.class);

    @Inject
    private Request request;

    @Inject
    private Response response;

    @Inject
    private ScriptingServices scriptingServices;

    private String layoutName;

    private ScriptEngine scriptEngine;

    private Bindings bindings;

    public DynamicPageServiceImpl() {
        try {
            JsonConfig config = new JsonConfig();
            layoutName = config.get("portal/layout", DEFAULT_LAYOUT_TEMPLATE);

            // setup scripting engine
            String engineName = config.get("portal/scriptEngine",
                    DEFAULT_SCRIPT_ENGINE);
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            scriptEngine = scriptEngineManager.getEngineByName(engineName);
            bindings = scriptEngine.createBindings();

            // setup velocity engine
            String portalDir = config.get("portal/homeDir",
                    DEFAULT_PORTAL_HOME_DIR);
            Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, portalDir);
            Velocity.setProperty(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER,
                    Velocity.class.getName());
            Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS,
                    Log4JLogChute.class.getName());
            Velocity.setProperty(Velocity.VM_LIBRARY, "portal-library.vm");
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
            FormData formData, JsonSessionState sessionState) {

        boolean isAjax = pageName.endsWith(".ajax");
        if (isAjax) {
            pageName = pageName.substring(0, pageName.lastIndexOf(".ajax"));
        }
        StringBuilder renderMessages = new StringBuilder();

        // setup script and velocity context
        bindings.put("Services", scriptingServices);
        bindings.put("systemProperties", System.getProperties());
        bindings.put("request", request);
        bindings.put("response", response);
        bindings.put("formData", formData);
        bindings.put("sessionState", sessionState);
        bindings.put("contextPath", request.getContextPath());
        bindings.put("portalId", portalId);
        bindings.put("pageName", pageName);

        // run page and template scripts
        Object layoutObject = new Object();
        try {
            layoutObject = evalScript(portalId, layoutName, formData);
        } catch (ScriptException se) {
            renderMessages.append("Layout script error:\n");
            renderMessages.append(se.getMessage());
            log.warn("Failed to run layout script!\n=====\n{}\n=====", se
                    .getMessage());
        }
        bindings.put("page", layoutObject);

        Object pageObject = new Object();
        try {
            pageObject = evalScript(portalId, pageName, formData);
        } catch (ScriptException se) {
            renderMessages.append("Page script error:\n");
            renderMessages.append(se.getMessage());
            log.warn("Failed to run page script!\n=====\n{}\n=====", se
                    .getMessage());
        }
        bindings.put("self", pageObject);

        // set up the velocity context
        VelocityContext vc = new VelocityContext();
        for (String key : bindings.keySet()) {
            vc.put(key, bindings.get(key));
        }
        if (renderMessages.length() > 0) {
            vc.put("renderMessages", renderMessages.toString());
        }

        try {
            // render the page content
            StringWriter pageContentWriter = new StringWriter();
            Template pageContent = getTemplate(portalId, pageName);
            pageContent.merge(vc, pageContentWriter);
            if (isAjax) {
                out.write(pageContentWriter.toString().getBytes());
            } else {
                vc.put("pageContent", pageContentWriter.toString());
            }
        } catch (Exception e) {
            renderMessages.append("Page content template error:\n");
            renderMessages.append(e.getMessage());
            vc.put("renderMessages", renderMessages.toString());
            log.error("Failed rendering page template: {} ({})",
                    e.getMessage(), isAjax ? "ajax" : "html");
        }

        if (!isAjax) {
            try {
                // render the page using the layout template
                Template page = getTemplate(portalId, layoutName);
                Writer pageWriter = new OutputStreamWriter(out, "UTF-8");
                page.merge(vc, pageWriter);
                pageWriter.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Object evalScript(String portalId, String scriptName,
            FormData formData) throws ScriptException {
        Object scriptObject = new Object();
        scriptName = "scripts/" + scriptName + ".py";
        log.debug("Running page script {}...", scriptName);
        InputStream in = getResource(portalId, scriptName);
        if (in != null) {
            scriptEngine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
            scriptEngine.eval(new InputStreamReader(in));
            scriptObject = scriptEngine.get("scriptObject");
        } else {
            log.warn("No script found for {}", scriptName);
        }
        return scriptObject;
    }

    private Template getTemplate(String portalId, String templateName)
            throws Exception {
        return Velocity.getTemplate(portalId + "/" + templateName + ".vm");
    }
}
