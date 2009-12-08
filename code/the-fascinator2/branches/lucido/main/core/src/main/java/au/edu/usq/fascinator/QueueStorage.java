package au.edu.usq.fascinator;

import java.io.File;
import java.util.List;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.JsonConfigHelper;

/**
 * Wraps a storage plugin to provide indexing functions
 * 
 * @author Oliver Lucido
 */
public class QueueStorage implements Storage {

    public void queueHarvest(String oid, File jsonFile) {
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
            Destination destination = session.createQueue("harvest");

            // Create a MessageProducer from the Session to the Topic or
            // Queue
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // Create a messages
            JsonConfigHelper json = new JsonConfigHelper(jsonFile);
            json.set("oid", oid);
            TextMessage message = session.createTextMessage(json.toString());

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

    private Logger log = LoggerFactory.getLogger(QueueStorage.class);

    private Storage storage;

    private File jsonFile;

    public QueueStorage(Storage storage, File jsonFile) {
        this.storage = storage;
        this.jsonFile = jsonFile;
    }

    public String addObject(DigitalObject object) throws StorageException {
        String sid = storage.addObject(object);
        queueHarvest(object.getId(), jsonFile);
        return sid;
    }

    public void addPayload(String oid, Payload payload) {
        storage.addPayload(oid, payload);
    }

    public DigitalObject getObject(String oid) {
        return storage.getObject(oid);
    }

    public Payload getPayload(String oid, String pid) {
        return storage.getPayload(oid, pid);
    }

    public void removeObject(String oid) {
        storage.removeObject(oid);
        // queue delete
    }

    public void removePayload(String oid, String pid) {
        storage.removePayload(oid, pid);
    }

    public String getId() {
        return storage.getId();
    }

    public String getName() {
        return storage.getName();
    }

    public void init(File jsonFile) throws PluginException {
        storage.init(jsonFile);
    }

    public void shutdown() throws PluginException {
        storage.shutdown();
    }

    @Override
    public void init(String jsonString) throws PluginException {
        storage.init(jsonString);
    }

    @Override
    public List<DigitalObject> getObjectList() {
        return storage.getObjectList();
    }
}
