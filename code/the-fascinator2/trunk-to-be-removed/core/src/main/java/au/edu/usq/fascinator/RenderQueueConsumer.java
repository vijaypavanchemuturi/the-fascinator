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
package au.edu.usq.fascinator;

import java.io.IOException;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.api.indexer.IndexerException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * Consumer Class to handle the Queue
 * 
 * @author Oliver Lucido & Linda Octalina
 * 
 */
public class RenderQueueConsumer implements MessageListener {

    private Logger log = LoggerFactory.getLogger(RenderQueueConsumer.class);

    private Properties props;

    private Indexer indexer;

    private Connection connection;

    private Session session;

    private MessageConsumer consumer;

    private Destination destination;

    private String name;

    private JsonConfig config;

    public RenderQueueConsumer(String name) throws IOException, JAXBException {
        // Name will be shown in the portal log to show which queue is currently
        // running
        this.name = name;

        config = new JsonConfig();
        try {
            indexer = PluginManager.getIndexer(config.get("indexer/type",
                    "solr"));
            indexer.init(config.getSystemFile());
        } catch (PluginException pe) {
            log.error("Failed to initialise indexer: {}", pe.getMessage());
        }
    }

    public void start() throws Exception {
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                config.get("messaging/url", "tcp://localhost:61616"));

        // Create a Connection
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // connection.setExceptionListener(this);

        // Create a Session
        Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        destination = session.createQueue("render");
        consumer = session.createConsumer(destination);
        consumer.setMessageListener(this);
    }

    public void stop() throws Exception {
        consumer.close();
        session.close();
        connection.close();
    }

    @Override
    public void onMessage(Message message) {
        TextMessage tm = (TextMessage) message;
        try {
            MDC.put("name", name);
            String text = tm.getText();
            JsonConfig config = new JsonConfig(text);
            Storage storage = PluginManager.getStorage(config.get(
                    "storage/type", "file-system"));
            storage.init(config.getSystemFile());
            DigitalObject object = storage.getObject(config.get("oid"));

            // Transform using ICE
            ConveyerBelt cb = new ConveyerBelt(text, "render");
            object = cb.transform(object);
            storage.addObject(object);
            indexer.index(object.getId());
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IndexerException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (PluginException e) {
            e.printStackTrace();
        }

    }
}
