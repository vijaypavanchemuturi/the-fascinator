/*
 * The Fascinator - Python Utils
 * Copyright (C) 2008-2010 University of Southern Queensland
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

package au.edu.usq.fascinator.common;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.access.AccessControlException;
import au.edu.usq.fascinator.api.access.AccessControlManager;
import au.edu.usq.fascinator.api.access.AccessControlSchema;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.StorageException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXReader;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The purpose of this class is to expose common Java classes and methods we use
 * to Python scripts.
 * 
 * Messaging is a duplicate of au.edu.usq.fascinator.MessagingServices
 *  since Common library cannot acces it.
 * 
 * @author Greg Pendlebury
 */
public class PythonUtils {
    private static Logger log = LoggerFactory.getLogger(PythonUtils.class);

    /** Security */
    private AccessControlManager access;

    /** XML Parsing */
    private Map<String, String> namespaces;
    private SAXReader saxReader;

    /** Message Queues */
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private Map<String, Destination> destinations;

    public PythonUtils(JsonConfig config) throws PluginException {
        // Security
        String accessControlType = "accessmanager";
        access = PluginManager.getAccessManager(accessControlType);
        access.init(config.toString());

        // XML parsing
        namespaces = new HashMap<String, String>();
        DocumentFactory docFactory = new DocumentFactory();
        docFactory.setXPathNamespaceURIs(namespaces);
        saxReader = new SAXReader(docFactory);

        // Message Queues
        String brokerUrl = config.get("messaging/url",
                ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);
        connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // create single producer for multiple destinations
            producer = session.createProducer(null);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // cache destinations
            destinations = new HashMap<String, Destination>();
        } catch (JMSException ex) {
            throw new PluginException(ex);
        }
    }


    /*****
     * Try to closing any objects that require closure
     *
     */
    public void shutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close connection: {}", jmse.getMessage());
            }
        }
        if (session != null) {
            try {
                session.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close session: {}", jmse.getMessage());
            }
        }
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close producer: {}", jmse.getMessage());
            }
        }
    }

    /*****
     * Send a message to the given message queue
     *
     * @param messageQueue to connect to
     * @param message to send
     * @return boolean flag for success
     */
    public boolean sendMessage(String messageQueue, String message) {
        try {
            //log.debug("Queuing '{}' to '{}'", msg, name);
            sendMessage(getDestination(messageQueue, true), message);
            return true;
        } catch (JMSException jmse) {
            log.error("Failed to queue message", jmse);
            return false;
        }
    }

    /**
     * Sends a message to a JMS destination.
     *
     * @param name destination name
     * @param msg message to send
     */
    private void sendMessage(Destination destination, String msg)
            throws JMSException {
        TextMessage message = session.createTextMessage(msg);
        producer.send(destination, message);
    }

    /**
     * Gets a JMS destination with the given name. If the destination doesn't
     * exist it is created and cached for reuse.
     *
     * @param name name of the destination
     * @param queue true if the destination is a queue, false for topic
     * @return a JMS destination
     * @throws JMSException if an error occurred creating the destination
     */
    private Destination getDestination(String name, boolean queue)
            throws JMSException {
        Destination destination = destinations.get(name);
        if (destination == null) {
            destination = queue ? session.createQueue(name) : session
                    .createTopic(name);
            destinations.put(name, destination);
        }
        return destination;
    }

    /*****
     * Get a resource from one of the compiled classes on the classpath
     * 
     * @param path To the requested resource
     * @return InputStream to the resource
     */
    public InputStream getResource(String path) {
        return getClass().getResourceAsStream(path);
    }

    /*****
     * Parse an XML document stored in a payload
     * 
     * @param payload holding the document
     * @return Document object after parsing
     */
    public Document getXmlDocument(Payload payload) {
        try {
            Document doc = getXmlDocument(payload.open());
            payload.close();
            return doc;
        } catch (StorageException ex) {
            log.error("Failed to access payload", ex);
        }
        return null;
    }

    /*****
     * Parse an XML document from a string
     * 
     * @param xmlData to parse
     * @return Document object after parsing
     */
    public Document getXmlDocument(String xmlData) {
        Reader reader = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(xmlData
                    .getBytes("utf-8"));
            return saxReader.read(in);
        } catch (UnsupportedEncodingException uee) {
        } catch (DocumentException de) {
            log.error("Failed to parse XML", de);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                }
            }
        }
        return null;
    }

    /*****
     * Parse an XML document from an inputstream
     * 
     * @param xmlIn, the inputstream to read and parse
     * @return Document object after parsing
     */
    public Document getXmlDocument(InputStream xmlIn) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(xmlIn, "UTF-8");
            return saxReader.read(reader);
        } catch (UnsupportedEncodingException uee) {
        } catch (DocumentException de) {
            log.error("Failed to parse XML", de);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                }
            }
        }
        return null;
    }

    /*****
     * Register a namespace for our XML parser
     * 
     * @param prefix of the namespace
     * @param uri of the namespace
     */
    public void registerNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
    }

    /*****
     * UN-register a namespace for our XML parser
     * 
     * @param prefix of the namespace
     */
    public void unregisterNamespace(String prefix) {
        namespaces.remove(prefix);
    }

    /*****
     * Parse a JSON object from an inputstream
     * 
     * @param in, the inputstream to read and parse
     * @return JsonConfigHelper object after parsing
     */
    public JsonConfigHelper getJsonObject(InputStream in) {
        try {
            return new JsonConfigHelper(in);
        } catch (IOException ex) {
            log.error("Failure during stream access", ex);
            return null;
        }
    }

    /*****
     * Parse RDF data stored in a payload
     * 
     * @param payload containing the data
     * @return Model object after parsing
     */
    public Model getRdfModel(Payload payload) {
        try {
            Model model = getRdfModel(payload.open());
            payload.close();
            return model;
        } catch (StorageException ioe) {
            log.info("Failed to read payload stream", ioe);
        }
        return null;
    }

    /*****
     * Parse RDF data from an inputstream
     * 
     * @param rdfIn, the inputstream to read and parse
     * @return Model object after parsing
     */
    public Model getRdfModel(InputStream rdfIn) {
        Model model = null;
        Reader reader = null;
        try {
            reader = new InputStreamReader(rdfIn, "UTF-8");
            model = RDF2Go.getModelFactory().createModel();
            model.open();
            model.readFrom(reader);
        } catch (ModelRuntimeException mre) {
            log.error("Failed to create RDF model", mre);
        } catch (IOException ioe) {
            log.error("Failed to read RDF input", ioe);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                }
            }
        }
        return model;
    }

    /*****
     * Return an empty access control schema from the given plugin
     * 
     * @param plugin to request the schema from
     * @return AccessControlSchema returned by the plugin
     */
    public AccessControlSchema getAccessSchema(String plugin) {
        if (access == null) {
            return null;
        }
        access.setActivePlugin(plugin);
        return access.getEmptySchema();
    }

    /*****
     * Submit a new access control schema to a security plugin
     * 
     * @param schema to submit
     * @param plugin to submit to
     */
    public void setAccessSchema(AccessControlSchema schema, String plugin) {
        if (access == null) {
            return;
        }

        try {
            access.setActivePlugin(plugin);
            access.applySchema(schema);
        } catch (AccessControlException ex) {
            log.error("Failed to add new access schema", ex);
        }
    }

    /*****
     * Remove an access control schema from a security plugin
     * 
     * @param schema to remove
     * @param plugin to remove to
     */
    public void removeAccessSchema(AccessControlSchema schema, String plugin) {
        if (access == null) {
            return;
        }

        try {
            access.setActivePlugin(plugin);
            access.removeSchema(schema);
        } catch (AccessControlException ex) {
            log.error("Failed to revoke existing access schema", ex);
        }
    }

    /*****
     * Find the list of roles with access to the given object
     * 
     * @param recordId the object to query
     * @return List<String> of roles with access to the object
     */
    public List<String> getRolesWithAccess(String recordId) {
        if (access == null) {
            return null;
        }
        try {
            return access.getRoles(recordId);
        } catch (AccessControlException ex) {
            log.error("Failed to query security plugin for roles", ex);
            return null;
        }
    }
}
