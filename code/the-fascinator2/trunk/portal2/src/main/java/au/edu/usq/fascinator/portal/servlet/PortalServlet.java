package au.edu.usq.fascinator.portal.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.tools.generic.introspection.JythonUberspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.MimeTypeUtil;
import au.edu.usq.fascinator.portal.services.PortalManager;
import au.edu.usq.fascinator.portal.services.PortalManagerImpl;

public class PortalServlet extends HttpServlet {

    private static final String DEFAULT_PORTAL = "default";

    private static final String DEFAULT_TEMPLATE = "template.vm";

    private static final String DEFAULT_PORTAL_HOME = "/opt/the-fascinator/portal";

    private static final String DEFAULT_ENGINE_NAME = "python";

    private Logger log = LoggerFactory.getLogger(PortalServlet.class);

    private JsonConfig config;

    private String defaultPortal;

    private String templateName;

    private ScriptEngine scriptEngine;

    @Override
    public void init() throws ServletException {
        try {
            config = new JsonConfig();
            defaultPortal = config.get("portal/default", DEFAULT_PORTAL);
            templateName = config.get("portal/template", DEFAULT_TEMPLATE);
            Properties props = new Properties();
            props.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, config.get(
                    "portal/home", DEFAULT_PORTAL_HOME));
            props.setProperty("runtime.introspector.uberspect",
                    JythonUberspect.class.getName());
            Velocity.init(props);

            String engineName = config.get("portal/scriptEngine",
                    DEFAULT_ENGINE_NAME);
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            scriptEngine = scriptEngineManager.getEngineByName(engineName);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        log.trace("request={}", request);
        String portalName = getPortalName(request);
        String contentName = request.getPathInfo();
        if (contentName.indexOf('.') == -1) {
            contentName += ".vm";
        }
        log.debug("portalName={},contentName={}", portalName, contentName);
        try {
            if (contentName.endsWith(".vm")) {
                if (!Velocity.resourceExists(contentName)) {
                    contentName = portalName + "/" + contentName;
                }
                String scriptName = contentName + ".py";
                log.info("py={}", scriptName);
                InputStream in = RuntimeSingleton.getContent(scriptName)
                        .getResourceLoader().getResourceStream(scriptName);
                scriptEngine.put("Services", this);
                scriptEngine.eval(new InputStreamReader(in));
                Object page = scriptEngine.get("page");

                // set up the context
                VelocityContext vctx = new VelocityContext();
                vctx.put("request", request);
                vctx.put("response", response);
                vctx.put("contextPath", request.getContextPath());
                vctx.put("portalName", portalName);
                vctx.put("page", page);

                // render the page content
                StringWriter bodyWriter = new StringWriter();
                Template body = Velocity.getTemplate(contentName);
                body.merge(vctx, bodyWriter);
                vctx.put("pageContent", bodyWriter.toString());

                // render final page
                Template finalPage = Velocity.getTemplate(portalName + "/"
                        + templateName);
                finalPage.merge(vctx, response.getWriter());
            } else {
                // retrieve non-template resource
                InputStream in = RuntimeSingleton.getContent(contentName)
                        .getResourceLoader().getResourceStream(contentName);
                IOUtils.copy(in, response.getOutputStream());
                response.setContentType(MimeTypeUtil.getMimeType(contentName));
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String getPortalName(HttpServletRequest request) {
        String portalName = request.getParameter("portal");
        if (portalName == null || "".equals(portalName)
                || "$default".equals(portalName)) {
            portalName = defaultPortal;
        }
        return portalName;
    }

    // API accessor methods

    private PortalManager portalManager;

    public PortalManager getPortalManager() {
        if (portalManager == null) {
            portalManager = new PortalManagerImpl();
        }
        return portalManager;
    }

}
