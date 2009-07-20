package au.edu.usq.fascinator.portal;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.MimeTypeUtil;

public class PortalServlet extends HttpServlet {

    private static final String DEFAULT_PORTAL = "default";

    private static final String DEFAULT_TEMPLATE = "template.vm";

    private static final String DEFAULT_PORTAL_HOME = "/opt/the-fascinator/portal";

    private Logger log = LoggerFactory.getLogger(PortalServlet.class);

    private JsonConfig config;

    private String defaultPortal;

    private String templateName;

    @Override
    public void init() throws ServletException {
        try {
            config = new JsonConfig();
            defaultPortal = config.get("portal/default", DEFAULT_PORTAL);
            templateName = config.get("portal/template", DEFAULT_TEMPLATE);
            Properties props = new Properties();
            props.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, config.get(
                    "portal/home", DEFAULT_PORTAL_HOME));
            Velocity.init(props);
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

        log.debug("request={}", request);
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
                // set up the context
                VelocityContext context = new VelocityContext();
                context.put("request", request);
                context.put("response", response);
                context.put("contextPath", request.getContextPath());
                context.put("portalName", portalName);

                // render the page content
                StringWriter bodyWriter = new StringWriter();
                Template body = Velocity.getTemplate(contentName);
                body.merge(context, bodyWriter);
                context.put("pageContent", bodyWriter.toString());

                // render final page
                Template page = Velocity.getTemplate(portalName + "/"
                        + templateName);
                page.merge(context, response.getWriter());
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
}
