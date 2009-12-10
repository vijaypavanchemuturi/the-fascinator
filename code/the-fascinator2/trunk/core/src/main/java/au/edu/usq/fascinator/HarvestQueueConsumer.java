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
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.api.indexer.IndexerException;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * Harvest Queue Consumer class
 * 
 * @author Oliver Lucido
 * 
 */
public class HarvestQueueConsumer implements MessageListener {

    private Logger log = LoggerFactory.getLogger(HarvestQueueConsumer.class);

    private Properties props;

    private Indexer indexer;

    private Connection connection;

    private Session session;

    private MessageConsumer consumer;

    private Destination destination;

    private JsonConfig config;

    public HarvestQueueConsumer() throws IOException, JAXBException {
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
        destination = session.createQueue("harvest");
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
            String text = tm.getText();
            JsonConfig config = new JsonConfig(text);
            String oid = config.get("oid");
            indexer.index(oid);
            queueRender(oid, text);
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (IndexerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void queueRender(String oid, String jsonString) {
        try {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                    "tcp://localhost:61616");

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            Destination destination = session.createQueue("render");

            // Create a MessageProducer from the Session to the Topic or
            // Queue
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // Create a messages
            TextMessage message = session.createTextMessage(jsonString);

            // Tell the producer to send the message
            System.out.println("Sent message: " + message.hashCode() + " : "
                    + Thread.currentThread().getName());
            producer.send(message);

            // Clean up
            session.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }

    }
}
