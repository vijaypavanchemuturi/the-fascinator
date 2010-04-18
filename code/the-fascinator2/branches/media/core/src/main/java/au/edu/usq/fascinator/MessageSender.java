/* 
 * The Fascinator - Core
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

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.JsonConfig;

/**
 * Sends notification messages.
 * 
 * @author Oliver Lucido
 * @author Linda Octalina
 */
public class MessageSender {

    public static final String MESSAGE_QUEUE = "message";

    /** Logging */
    private Logger log = LoggerFactory.getLogger(MessageSender.class);

    private JsonConfig config;

    private Connection connection;

    private Session session;

    private MessageProducer producer;

    public MessageSender(String topic) throws JMSException {
        try {
            log.debug("Starting message sender...");
            config = new JsonConfig();
            String brokerUrl = config.get("messaging/url",
                    ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                    brokerUrl);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false,
                    Session.CLIENT_ACKNOWLEDGE);
            Destination messageDest = session.createTopic(topic);
            producer = session.createProducer(messageDest);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        } catch (IOException ioe) {
            log.error("Failed to read configuration: {}", ioe.getMessage());
        }
    }

    public void close() {
        log.info("Closing message sender...");
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close producer: {}", jmse.getMessage());
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

    public void sendMessage(String msg) {
        try {
            TextMessage message = session.createTextMessage(msg);
            producer.send(message);
        } catch (JMSException jmse) {
            log.error("Failed to send message: {}", jmse.getMessage());
        }
    }
}
