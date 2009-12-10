package au.edu.usq.fascinator.portal.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;
import org.python.core.PySystemState;
import org.slf4j.MDC;

import au.edu.usq.fascinator.HarvestQueueConsumer;
import au.edu.usq.fascinator.RenderQueueConsumer;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * Indexer Servlet class
 * 
 * @author Oliver Lucido
 * 
 */
public class IndexerServlet extends HttpServlet {

    private Logger log = Logger.getLogger(IndexerServlet.class);

    private Timer timer;

    private HarvestQueueConsumer service;

    private List<RenderQueueConsumer> renderers;

    private JsonConfig json;

    /**
     * activemq broker NOTE: Will use Fedora's broker if fedora is running,
     * otherwise use activemq broker
     */
    private BrokerService broker;

    @Override
    public void init() throws ServletException {
        try {
            json = new JsonConfig();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        broker = new BrokerService();

        // configure the broker
        try {
            broker.addConnector(json.get("messaging/url",
                    "tcp://localhost:61616"));
            broker.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // add jars for jython to scan for packages
        String realPath = getServletContext().getRealPath("/");
        if (!realPath.endsWith("/")) {
            realPath += "/";
        }

        String pythonHome = realPath + "WEB-INF/lib";
        Properties props = new Properties();
        props.setProperty("python.home", pythonHome);

        PySystemState.initialize(PySystemState.getBaseProperties(), props,
                new String[] { "" });
        PySystemState.add_classdir(realPath + "WEB-INF/classes");
        PySystemState.add_classdir(realPath + "../../../target/classes");

        PySystemState.add_extdir(pythonHome, true);

        // use a timer to start the indexer because tomcat's deployment order
        // is not reliable, fedora may not have started yet thus the indexer
        // is not able to subscribe to fedora messages. the timer will retry
        // every 15 secs if it was unable to start initially

        timer = new Timer("StartIndexer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                startIndexer();
            }
        }, 0, 15000);
    }

    private void startIndexer() {
        MDC.put("name", "Main");
        log.info("Starting The Fascinator indexer...");
        try {
            if (service == null) {
                service = new HarvestQueueConsumer();
            }
            service.start();
            log.info("The Fascinator indexer was started successfully");
            timer.cancel();
            timer = null;
        } catch (Exception e) {
            log.warn("Will retry in 15 seconds.", e);
        }

        // Start the queue renderer
        try {
            if (renderers == null) {
                renderers = new ArrayList<RenderQueueConsumer>();
                for (int i = 0; i < Integer.parseInt(json.get(
                        "messaging/render-thread", "3")); i++) {
                    renderers.add(new RenderQueueConsumer("render" + i));
                }
            }
            for (RenderQueueConsumer rqc : renderers) {
                rqc.start();
            }
        } catch (Exception e) {
            log.warn("Will retry in 15 seconds.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        /*
         * String terms = request.getParameter("terms"); String query =
         * request.getParameter("query");
         * 
         * if (isValidParam(terms) && isValidParam(query)) {
         * response.sendError(500,
         * "Must specify only TERMS or QUERY, not both"); } else {
         * FindObjectsType searchType = null; String searchQuery = null;
         * 
         * if (isValidParam(terms)) { searchType = FindObjectsType.TERMS;
         * searchQuery = terms; } else if (isValidParam(query)) { searchType =
         * FindObjectsType.QUERY; searchQuery = query; } else {
         * response.sendError(500,
         * "Must specify items to reindex using TERMS or QUERY"); return; }
         * 
         * FedoraRestClient fedora = service.getFedoraClient(); boolean done =
         * false; boolean first = true; ResultType results; String token = null;
         * while (!done) { if (first) { first = false; results =
         * fedora.findObjects(searchType, searchQuery, 25); } else { results =
         * fedora.resumeFindObjects(token);
         * log.info("Resuming search using token: " + token); } for
         * (ObjectFieldType object : results.getObjectFields()) {
         * service.index(object.getPid()); } ListSessionType session =
         * results.getListSession(); if (session != null) { token =
         * results.getListSession().getToken(); } else { token = null; } done =
         * token == null; } }
         */
    }

    private boolean isValidParam(String param) {
        return (param != null) && (!"".equals(param.trim()));
    }

    @Override
    public void destroy() {
        try {
            broker.stop();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (service != null) {
            try {
                service.stop();
                log.info("The Fascinator indexer stopped");
            } catch (Exception me) {
                log.error("Failed to stop the indexer", me);
            }
        }
        if (renderers != null) {
            for (RenderQueueConsumer rqc : renderers) {
                try {
                    rqc.stop();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (timer != null) {
            timer.purge();
            timer = null;
        }
        super.destroy();
    }
}
