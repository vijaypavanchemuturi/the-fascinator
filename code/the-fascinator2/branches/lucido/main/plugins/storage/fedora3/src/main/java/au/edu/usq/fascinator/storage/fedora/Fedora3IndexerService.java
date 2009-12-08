/* 
 * The Fascinator - Fedora Commons 3.x storage plugin
 * Copyright (C) 2009 University of Southern Queensland
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
package au.edu.usq.fascinator.storage.fedora;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.api.indexer.IndexerException;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fedora.messaging.AtomFedoraMessage;
import fedora.client.messaging.JmsMessagingClient;
import fedora.client.messaging.MessagingClient;
import fedora.client.messaging.MessagingListener;
import fedora.server.errors.MessagingException;

public class Fedora3IndexerService implements MessagingListener {

    private Logger log = LoggerFactory.getLogger(Fedora3IndexerService.class);

    private Properties props;

    private Indexer indexer;

    private MessagingClient messagingClient;

    private Unmarshaller parser;

    private List<String> methods;

    public Fedora3IndexerService() throws IOException, JAXBException {
        JsonConfig config = new JsonConfig();
        props = new Properties();
        props.putAll(config.getMap("storage/fedora3/messaging"));
        props.store(System.out, "");

        JAXBContext jc = JAXBContext.newInstance(AtomFedoraMessage.class);
        parser = jc.createUnmarshaller();

        try {
            indexer = PluginManager.getIndexer(config.get("indexer/type",
                    "solr"));
            log.info("indexer=" + indexer + ",file=" + config.getSystemFile());
            indexer.init(config.getSystemFile());
        } catch (PluginException pe) {
            log.error("Failed to initialise indexer: {}", pe.getMessage());
        }

        methods = new ArrayList<String>();
        methods.add("addDatastream");
        methods.add("modifyDatastreamByValue");
        methods.add("modifyDatastreamByReference");
        methods.add("modifyObject");
        methods.add("setDatastreamState");
    }

    public void start() throws Exception {
        try {
            messagingClient = new JmsMessagingClient("TheFascinatorIndexer",
                    this, props, false);
            // messagingClient.start();
        } catch (Throwable t) {
            throw new Exception("Failed to start messaging client");
        }
    }

    public void stop() throws MessagingException {
        messagingClient.stop(false);
    }

    public void onMessage(String clientId, Message message) {
        try {
            String pid = message.getStringProperty("pid");
            String method = message.getStringProperty("methodName");
            if ("ingest".equals(method)) {
                indexer.index(pid);
            } else if (methods.contains(method)) {
                AtomFedoraMessage msg = parseJmsMessage(message);
                String dsId = msg.getProperty("dsID");
                indexer.index(pid, dsId);
            } else if ("purgeObject".equals(method)) {
                indexer.remove(pid);
            } else {
                log.warn("Unsupported method: " + method);
            }
        } catch (JMSException jmse) {
            log.error("Failed to process message", jmse);
        } catch (JAXBException jaxbe) {
            log.error("Failed to parse message", jaxbe);
        } catch (IndexerException ie) {
            log.error("Failed to index: {}", ie.getMessage());
        }
    }

    private AtomFedoraMessage parseJmsMessage(Message message)
            throws JMSException, JAXBException {
        String messageText = ((TextMessage) message).getText();
        InputStream messageIn = new ByteArrayInputStream(messageText.getBytes());
        return (AtomFedoraMessage) parser.unmarshal(messageIn);
    }
}
