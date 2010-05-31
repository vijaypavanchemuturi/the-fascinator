/*
 * The Fascinator - Core
 * Copyright (C) 2010 University of Southern Queensland
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
package au.edu.usq.fascinator.portal;

import au.edu.usq.fascinator.GenericMessageListener;
import au.edu.usq.fascinator.MessagingServices;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.storage.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * The House Keeper is a messaging object that periodically wakes itself
 * up to look for routine maintenance tasks requiring attention.
 *
 * @author Greg Pendlebury
 */
public class HouseKeeper implements GenericMessageListener {

    /** House Keeping queue */
    public static final String QUEUE_ID = "houseKeeping";

    /** Default timeout = 5 mins */
    public static final long DEFAULT_TIMEOUT = 300;

    /** Logging */
    private Logger log = LoggerFactory.getLogger(HouseKeeper.class);

    /** System configuration */
    private JsonConfig globalConfig;

    /** Desktop installation */
    private boolean desktop;

    /** Indexer object */
    private Indexer indexer;

    /** Storage */
    private Storage storage;

    /** Message Consumer instance */
    private MessageConsumer consumer;

    /** Messaging service instance */
    private MessagingServices services;

    /** Stack of actions needing attention */
    private Stack<UserAction> actionQueue;

    /** Current action needing attention */
    private UserAction currentAction;

    /** Timer for callbacl events */
    private Timer timer;

    /** Callback timeout for house keeping (in seconds) */
    private long timeout;

    /**
     * Switch log file
     *
     */
    private void openLog() {
        MDC.put("name", QUEUE_ID);
    }

    /**
     * Revert log file
     *
     */
    private void closeLog() {
        MDC.remove("name");
    }

    /**
     * An internal class for queueing actions that require
     * attention from the user interface.
     *
     */
    private class UserAction {
        /** Flag to set this action as 'blocking'. Used to ensure
         *  actions requiring a restart never disappear */
        public boolean block;

        /** Message to display */
        public String message;

        /** Template to use */
        public String template;
    }

    /**
     * Constructor required by ServiceLoader. Be sure to use init()
     *
     */
    public HouseKeeper() {}

    /**
     * Initialization method
     *
     * @param config Configuration to use
     * @throws Exception for any failure
     */
    @Override
    public void init(JsonConfigHelper config) throws Exception {
        openLog();
        try {
            log.info("=================");
            log.info("Starting House Keeping object");
            // Configuration
            globalConfig = new JsonConfig();
            desktop = Boolean.parseBoolean(
                    config.get("config/desktop", "true"));
            File sysFile = JsonConfig.getSystemFile();
            actionQueue = new Stack();

            // Initialise plugins
            indexer = PluginManager.getIndexer(
                    globalConfig.get("indexer/type", "solr"));
            indexer.init(sysFile);
            storage = PluginManager.getStorage(
                    globalConfig.get("storage/type", "file-system"));
            storage.init(sysFile);

            // Start our callback timer
            timeout = Long.valueOf(config.get("config/frequency",
                    String.valueOf(DEFAULT_TIMEOUT)));
            log.info("Starting callback timer. Timeout = {}s", timeout);
            timer = new Timer("HouseKeeping", true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    onTimeout();
                }
            }, 0, timeout * 1000);

        } catch (IOException ioe) {
            log.error("Failed to read configuration: {}", ioe.getMessage());
            throw ioe;
        } catch (PluginException pe) {
            log.error("Failed to initialise plugin: {}", pe.getMessage());
            throw pe;
        } finally {
            closeLog();
        }
    }

    /**
     * Return the ID string for this listener
     *
     */
    @Override
    public String getId() {
        return QUEUE_ID;
    }

    /**
     * Start the queue
     *
     * @throws Exception if an error occurred starting the JMS connections
     */
    @Override
    public void start() throws Exception {
        services = MessagingServices.getInstance();
        Session session = services.getSession();
        Destination destination = session.createQueue(QUEUE_ID);
        consumer = session.createConsumer(destination);
        consumer.setMessageListener(this);
    }

    /**
     * Stop the House Keeper. Including stopping the storage and
     * indexer
     */
    @Override
    public void stop() throws Exception {
        openLog();
        log.info("Stopping House Keeping object...");
        if (indexer != null) {
            try {
                indexer.shutdown();
            } catch (PluginException pe) {
                log.error("Failed to shutdown indexer: {}", pe.getMessage());
                closeLog();
                throw pe;
            }
        }
        if (storage != null) {
            try {
                storage.shutdown();
            } catch (PluginException pe) {
                log.error("Failed to shutdown storage: {}", pe.getMessage());
                closeLog();
                throw pe;
            }
        }
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException jmse) {
                log.warn("Failed to close consumer: {}", jmse.getMessage());
                closeLog();
                throw jmse;
            }
        }
        services.release();
        closeLog();
    }

    /**
     * Callback function for periodic house keeping.
     *
     */
    private void onTimeout() {
        openLog();

        log.info("House Keeping Timeout event firing...");

        // Perform our 'boot time' house keeping
        syncHarvestFiles();
        checkSystemConfig(); /* <<< Always last */

        closeLog();
    }

    /**
     * Callback function for incoming messages sent directly to housekeeping.
     *
     * @param message The incoming message
     */
    @Override
    public void onMessage(Message message) {
        openLog();
        try {
            // Doesn't really do anything yet
            String text = ((TextMessage) message).getText();
            JsonConfigHelper msgJson = new JsonConfigHelper(text);
            log.info("Message\n{}", msgJson.toString());

            String msgType = msgJson.get("type");
            if (msgType == null) {
                log.error("No message type set!");
                closeLog();
                return;
            }

            // Stop the system from working until a restart occurs
            if (msgType.equals("blocking-restart")) {
                UserAction ua = new UserAction();
                ua.block = true;
                ua.message = "Changes made to the system require a restart. " +
                        "Please restart the system before normal " +
                        "functionality can resume.";
                ua.template = "error";
                // For a blocking restart we can nuke all messages
                currentAction = null;
                actionQueue = new Stack();
                actionQueue.add(ua);
                progressQueue();
            }

            // Request a restart, not required though
            if (msgType.equals("basic-restart")) {
                UserAction ua = new UserAction();
                ua.block = false;
                ua.message = "Changes made to the system require a restart.";
                processAction(ua, true);
                progressQueue();
            }

            // Harvest file update
            if (msgType.equals("harvest-update")) {
                String oid = msgJson.get("oid");
                if (oid != null) {
                    UserAction ua = new UserAction();
                    ua.block = false;
                    ua.message = ("A harvest file has been updated: '"
                            + oid + "'");
                    processAction(ua);
                    progressQueue();
                } else {
                    log.error("Invalid message, no harvest file OID provided!");
                }
            }

            // User notications
            if (msgType.equals("user-notice")) {
                String messageText = msgJson.get("message");
                if (messageText != null) {
                    UserAction ua = new UserAction();
                    ua.block = false;
                    ua.message = messageText;
                    processAction(ua);
                    progressQueue();
                } else {
                    this.log.error("Invalid notice, no message text provided!");
                }
            }

            // 'Refresh' received, check config and rest timer
            if (msgType.equals("refresh")) {
                log.info("Refreshing House Keeping");
                globalConfig = new JsonConfig();
                timeout = Long.valueOf(globalConfig.get(
                        "portal/houseKeeping/config/frequency",
                        String.valueOf(DEFAULT_TIMEOUT)));
                log.info("Starting callback timer. Timeout = {}s", timeout);
                timer.cancel();
                timer = new Timer("HouseKeeping", true);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        onTimeout();
                    }
                }, 0, timeout * 1000);

                // Show a message for the user
                UserAction ua = new UserAction();
                ua.block = false;
                ua.message = ("House Keeping is restarting. Frequency = " +
                        timeout + "s");
                processAction(ua);
                progressQueue();
            }
        } catch (JMSException jmse) {
            log.error("Failed to receive message: {}", jmse.getMessage());
        } catch (IOException ioe) {
            log.error("Failed to parse message: {}", ioe.getMessage());
        }
        closeLog();
    }

    /**
     * Is there and action needing user attention?
     *
     * @returns boolean Flag if there is an action requiring attention
     */
    public boolean requiresUserAction() {
        if (currentAction == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get the message to display for the user
     *
     * @returns String The message to display
     */
    public String getUserMessage() {
        if (requiresUserAction()) {
            return currentAction.message;
        } else {
            return null;
        }
    }

    /**
     * Get the template to frame the message in
     *
     * @returns String The template to use
     */
    public String getDisplayTemplate() {
        if (requiresUserAction()) {
            return currentAction.template;
        } else {
            return null;
        }
    }

    /**
     * Confirm the last message was actioned.
     *
     */
    public void confirmMessage() {
        if (requiresUserAction()) {
            currentAction = null;
            progressQueue();
        }
    }

    /**
     * Progress the action queue.
     *
     */
    private void progressQueue() {
        if (currentAction == null &&
                actionQueue != null &&
                !actionQueue.empty()) {
            UserAction next = actionQueue.peek();
            if (!next.block) {
                currentAction = actionQueue.pop();
            } else {
                currentAction = next;
            }
        }
    }

    /**
     * During portal startup, make sure the system config file is up-to-date.
     *
     */
    private void checkSystemConfig() {
        log.info("Checking system config files ...");
        boolean fine = true;

        if (!globalConfig.isConfigured()) {
            fine = false;
            UserAction ua = new UserAction();
            ua.block = true;
            // The settings template is looking for this message
            ua.message = "configure";
            ua.template = "settings";
            processAction(ua, true);
        }

        // Higher priority, so go last
        if (globalConfig.isOutdated()) {
            fine = false;
            UserAction ua = new UserAction();
            ua.block = true;
            // The settings template is looking for this message
            ua.message = "out-of-date";
            ua.template = "settings";
            processAction(ua, true);
        }

        progressQueue();

        if (fine) {
            log.info("... system config files are OK.");
        } else {
            log.warn("... problems found in system config files.");
        }
    }

    /**
     * During portal startup, we should check to ensure our harvest files
     * are up-to-date with those in storage.
     *
     */
    private void syncHarvestFiles() {
        // Get the harvest files directory
        String harvestPath = globalConfig.get("portal/harvestFiles");
        if (harvestPath == null) {
            return;
        }

        // Make sure the directory exists
        File harvestDir = new File(harvestPath);
        if (!harvestDir.exists() || !harvestDir.isDirectory()) {
            return;
        }

        // Loop through the files from the directory
        for (File file : getFiles(harvestDir)) {
            DigitalObject object = null;
            try {
                // Check for the file in storage
                object = StorageUtils.checkHarvestFile(storage, file);
            } catch (StorageException ex) {
                log.error("Error during harvest file check: ", ex);
            }

            if (object != null) {
                // Generate a message to ourself. This merges with other places
                //   where the update occurs (like the HarvestClient).
                log.debug("Harvest file updated: '{}'", file.getAbsolutePath());
                JsonConfigHelper message = new JsonConfigHelper();
                message.set("type", "harvest-update");
                message.set("oid", object.getId());
                services.queueMessage(QUEUE_ID, message.toString());
            }
        }
    }

    /**
     * Recursively generate a list of file in directory and sub-directories.
     *
     * @param dir The directory to list
     * @return List<File> The list of files
     */
    private List<File> getFiles(File dir) {
        List files = new ArrayList();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                files.addAll(getFiles(file));
            } else {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * Process a normal action based on system configuration.
     *
     * @param userAction The action to process
     */
    private void processAction(UserAction ua) {
        processAction(ua, false);
    }

    /**
     * Process an action based on system configuration, allowing for priority.
     *
     * @param userAction The action to process
     * @param highPriority Flag for high priority, true adds actions to the
     * front of the queue
     */
    private void processAction(UserAction userAction, boolean highPriority) {
        log.info("Processing user action: '{}'", userAction.message);

        // Desktop installation - goes to the user interface
        if (desktop) {
            // Empty queue, priority doesn't matter
            if (actionQueue.empty()) {
                actionQueue.push(userAction);
                log.debug("First action added to queue");
                return;
            }

            if (highPriority) {
                // Duplicate check
                UserAction duplicate = null;
                for (UserAction ua : actionQueue) {
                    if (ua.message.equals(userAction.message)) {
                        duplicate = ua;
                    }
                }
                // Remove it from the queue, we'll push
                //   it onto the front below
                if (duplicate != null) {
                    actionQueue.remove(duplicate);
                    log.debug("Removing old action from queue");
                }

                // Do we have a current action?
                if (currentAction != null) {
                    // Check the current action
                    UserAction next = null;
                    if (!actionQueue.empty()) {
                        next = actionQueue.peek();
                    }
                    if (next != null &&
                            currentAction.message.equals(next.message)) {
                        // It's already a blocking action on the
                        // front of the queue (so it won't get lost)
                        log.debug("High priority action added to queue");
                        currentAction = null;
                        actionQueue.push(userAction);
                    } else {
                        // Put the current action back on the queue
                        actionQueue.push(currentAction);
                        log.debug("Pushing current action back into queue '{}'",
                                currentAction.message);
                        currentAction = null;
                        // And push our new action in front
                        actionQueue.push(userAction);
                        log.debug("High priority action added to queue");
                    }
                // NO: Simple push is all we need
                } else {
                    actionQueue.push(userAction);
                    log.debug("High priority action added to queue");
                }
            } else {
                // Low priority
                for (UserAction ua : actionQueue) {
                    if (ua.message.equals(userAction.message)) {
                        // We're done, it's already in the queue
                        return;
                    }
                }
                // Simple, add to the end
                actionQueue.add(userAction);
                log.debug("Low priority action added to queue");
            }

        // Server (or other) install
        } else {
            // TODO : Somefink needs doing for server installations
        }
    }
}
