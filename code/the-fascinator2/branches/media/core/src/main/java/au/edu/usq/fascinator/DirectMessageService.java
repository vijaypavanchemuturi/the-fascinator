/*
 * The Fascinator - Core - DiReCt Messaging Service
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

import au.edu.usq.fascinator.common.JsonConfig;

import java.io.File;
import java.io.IOException;

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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


/**
 * A message processing class for both incoming and outgoing messages to the
 * DiReCt server. Messaging framework adapted from the RenderQueueConsumer.
 *
 * @author Greg Pendlebury
 */
public class DirectMessageService implements MessageListener {

    public static final String MESSAGE_QUEUE = "direct";
    public static final String DIRECT_QUEUE  = "toDirect";

    /** Logging */
    private Logger log = LoggerFactory.getLogger(DirectMessageService.class);

    private JsonConfig config;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private MessageProducer producer;

    public DirectMessageService() throws IOException {
        try {
            File sysFile = JsonConfig.getSystemFile();
            config = new JsonConfig(sysFile);
        } catch (IOException ioe) {
            log.error("Failed to read configuration: {}", ioe.getMessage());
        }
    }

    public void start() throws JMSException {
        log.info("Starting DiReCt message service...");
        String brokerUrl = config.get("messaging/url",
                ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                brokerUrl);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Message coming from either direction
        Destination destination = session.createQueue(MESSAGE_QUEUE);
        consumer = session.createConsumer(destination);
        consumer.setMessageListener(this);

        // Message heading out to our DiReCt API
        Destination destNewDirectItems = session.createQueue(DIRECT_QUEUE);
        producer = session.createProducer(destNewDirectItems);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
    }

    public void stop() {
        log.info("Stopping DiReCt message service...");
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close consumer: {}", jmse.getMessage());
            }
        }
        if (session != null) {
            try {
                session.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close session: {}", jmse.getMessage());
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close connection: {}", jmse.getMessage());
            }
        }
    }

    @Override
    public void onMessage(Message message) {
        MDC.put("name", MESSAGE_QUEUE);
        try {
            String text = ((TextMessage) message).getText();
            // Verify the json is valid
            JsonConfig msgJson = new JsonConfig(text);
            String messageType = msgJson.get("messageType");
            if (messageType != null && messageType.equals(
                    DirectIncomingApi.EXTERNAL_QUEUE)) {
                // Messages coming back from DiReCt
                log.debug("Incoming message from DiReCt : '{}'", text);
            } else {
                // Messages coming from the workflow system
                log.debug("Sending message to DiReCt : '{}'", text);
                // Send it to DiReCt
                TextMessage newMessage = session.createTextMessage(text);
                producer.send(newMessage);
            }
        } catch (JMSException jmse) {
            log.error("Failed to receive message: {}", jmse.getMessage());
        } catch (IOException ioe) {
            log.error("Failed to parse message: {}", ioe.getMessage());
        }
    }
}
