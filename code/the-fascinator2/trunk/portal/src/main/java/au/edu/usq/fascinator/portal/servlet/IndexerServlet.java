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
package au.edu.usq.fascinator.portal.servlet;

import au.edu.usq.fascinator.GenericMessageListener;
import au.edu.usq.fascinator.common.FascinatorHome;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.python.core.PySystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts an embedded ActiveMQ message broker, harvest and render queue
 * consumers.
 * 
 * @author Oliver Lucido
 */
@SuppressWarnings("serial")
public class IndexerServlet extends HttpServlet {

    public static final String DEFAULT_MESSAGING_HOME = FascinatorHome
            .getPath("activemq-data");

    private Logger log = LoggerFactory.getLogger(IndexerServlet.class);

    private Timer timer;

    private List<GenericMessageListener> messageQueues;

    private JsonConfigHelper config;

    /**
     * activemq broker NOTE: Will use Fedora's broker if fedora is running,
     * otherwise use activemq broker
     */
    private BrokerService broker;

    @Override
    public void init() throws ServletException {
        // configure the broker
        try {
            config = new JsonConfigHelper(JsonConfig.getSystemFile());

            String dataDir = config.get("messaging/home",
                    DEFAULT_MESSAGING_HOME);
            broker = new BrokerService();
            broker.setDataDirectory(dataDir);
            broker.addConnector(config.get("messaging/url",
                    ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL));
            String stompUrl = config.get("messaging/stompUrl");
            if (stompUrl != null) {
                broker.addConnector(stompUrl);
            }
            broker.start();
        } catch (Exception e) {
            log.error("Failed to start broker: {}", e.getMessage());
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
        log.info("Starting The Fascinator indexer...");
        if (messageQueues == null) {
            messageQueues = new ArrayList();
        }
        List<JsonConfigHelper> threadConfig =
                config.getJsonList("messaging/threads");

        try {
            // TODO - This loop and timer should be more intelligent
            //  It should track what instantiations succeeded and not
            //  try those again on the next pass through.
            for (JsonConfigHelper thread : threadConfig) {
                String classId = thread.get("id");
                if (classId != null) {
                    GenericMessageListener queue = getListener(classId);
                    if (queue != null) {
                        queue.init(thread);
                        queue.start();
                        messageQueues.add(queue);
                    } else {
                        log.error("Failed to find Listener: '{}'", classId);
                        throw new Exception();
                    }
                } else {
                    log.error("No message classId provided: '{}'",
                            thread.toString());
                    throw new Exception();
                }
            }
            log.info("The Fascinator indexer was started successfully");
            timer.cancel();
            timer = null;
        } catch (Exception e) {
            log.warn("Will retry in 15 seconds.", e);
        }
    }

    /**
     * Get the access manager. Used in The indexer if the portal isn't running
     *
     * @param id plugin identifier
     * @return an access manager plugin, or null if not found
     */
    public GenericMessageListener getListener(String id) {
        ServiceLoader<GenericMessageListener> listeners =
                ServiceLoader.load(GenericMessageListener.class);
        for (GenericMessageListener listener : listeners) {
            if (id.equals(listener.getId())) {
                return listener;
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // do nothing
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // do nothing
    }

    @Override
    public void destroy() {
        try {
            broker.stop();
        } catch (Exception e) {
            log.error("Failed to stop message broker: {}", e.getMessage());
        }
        if (messageQueues != null) {
            for (GenericMessageListener queue : messageQueues) {
                try {
                    queue.stop();
                } catch (Exception e) {
                    log.error("Failed to stop listener '{}': {}",
                            queue.getId(), e.getMessage());
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
