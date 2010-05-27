/*
 * The Fascinator - Plugin - Harvester - HCA Data
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
package au.edu.usq.fascinator.harvester.arts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.FascinatorHome;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.harvester.impl.GenericHarvester;
import au.edu.usq.fascinator.common.storage.StorageUtils;

/**
 * Harvests groups of files from a directory. Special attention is given to a
 * file named "PI.xml". This is the metadata file that acts as a manifest and a
 * Fascinator package object will be created for it. Each file referenced in
 * PI.xml will be harvested as a separate digital object.
 * <p>
 * <h3>Configuration</h3>
 * </p>
 * <table border="1">
 * <tr>
 * <th>Option</th>
 * <th>Description</th>
 * <th>Required</th>
 * <th>Default</th>
 * </tr>
 * <tr>
 * <td>baseDir</td>
 * <td>The directory harvest from. A single file can also be specified. Note
 * that the harvester looks specifically for a file named "PI.xml", specifying a
 * file with a different name will not work.</td>
 * <td><b>Yes</b></td>
 * <td><i>None</i></td>
 * </tr>
 * <tr>
 * <td>recursive</td>
 * <td>Set true to recursively harvest sub-directories. By default only the
 * specified directory will be harvested. If a single file is specified for
 * <b>baseDir</b> then this option has no effect.</td>
 * <td>No</td>
 * <td>false</td>
 * </tr>
 * <tr>
 * <td>ignoreFilter</td>
 * <td>Wildcard (i.e. '*' matches any characters) patterns of names to ignore
 * separated by '|'. For example, <code>".svn|Thumbs.db"</code> will ignore .svn
 * directories and Thumbs.db files.</td>
 * <td>No</td>
 * <td>.svn</td>
 * </tr>
 * <tr>
 * <td>link</td>
 * <td>Set true to link the to the original files. By default the harvest files
 * will be copied.</td>
 * <td>No</td>
 * <td>false</td>
 * </tr>
 * </table>
 * 
 * <h3>Examples</h3>
 * <ol>
 * <li>
 * Get a single group of files from the <code>/home/hca/data/2006/ex1</code>
 * directory
 * 
 * <pre>
 * "harvester": {
 *     "type": "hca-data",
 *     "hca-data": {
 *         "baseDir": "/home/hca/data/2006/ex1"
 *     }
 * }
 * </pre>
 * 
 * </li>
 * <li>
 * Recursively get all files from the <code>/home/hca/data</code> directory
 * 
 * <pre>
 * "harvester": {
 *     "type": "hca-data",
 *     "hca-data": {
 *         "baseDir": "/home/hca/data",
 *         "recursive": true
 *     }
 * }
 * </pre>
 * 
 * </li>
 * <li>
 * Recursively get all files from the <code>/home/hca/data</code> directory, but
 * ignoring files or directories whose name starts with 199
 * 
 * <pre>
 * "harvester": {
 *     "type": "hca-data",
 *     "hca-data": {
 *         "baseDir": "/home/hca/data",
 *         "recursive": true,
 *         "ignoreFilter": "199*"
 *     }
 * }
 * </pre>
 * 
 * </li>
 * </ol>
 * 
 * @author Oliver Lucido
 */
public class HcaDataHarvester extends GenericHarvester {

    public static final String USQ_NS_URI = "http://usq.edu.au/research/";

    public static final String SEER_NS_URI = "http://seer.arc.gov.au/2009/seer/1";

    public static final String OAI_DC_NS_URI = "http://www.openarchives.org/OAI/2.0/oai_dc/";

    public static final String DC_NS_URI = "http://purl.org/dc/elements/1.1/";

    private static final String PI_XML = "PI.xml";

    /** logging */
    private Logger log = LoggerFactory.getLogger(HcaDataHarvester.class);

    /** default ignore list */
    private static final String DEFAULT_IGNORE_PATTERNS = ".svn";

    /** directory to harvest */
    private File baseDir;

    /** whether or not to recursively harvest */
    private boolean recursive;

    /** current directory while harvesting */
    private File currentDir;

    /** stack of sub-directories while harvesting */
    private Stack<File> subDirs;

    /** whether or not there are more files to harvest */
    private boolean hasMore;

    /** filter used to ignore files matching specified patterns */
    private IgnoreFilter ignoreFilter;

    /** use links instead of copying */
    private boolean link;

    private DocumentFactory docFactory;

    private SAXReader saxReader;

    private Transformer hcaDcFull;

    private Transformer hcaDcAttachment;

    private File packageDir;

    /**
     * File filter used to ignore specified files
     */
    private class IgnoreFilter implements FileFilter {

        /** wildcard patterns of files to ignore */
        private String[] patterns;

        public IgnoreFilter(String[] patterns) {
            this.patterns = patterns;
        }

        public boolean accept(File path) {
            for (String pattern : patterns) {
                if (FilenameUtils.wildcardMatch(path.getName(), pattern)) {
                    return false;
                }
            }
            return path.isDirectory() || PI_XML.equals(path.getName());
        }
    }

    public HcaDataHarvester() {
        super("hca-data", "HCA Data Harvester");
    }

    @Override
    public void init() throws HarvesterException {
        JsonConfig config = getJsonConfig();
        baseDir = new File(config.get("harvester/hca-data/baseDir", "."));
        log.info("Harvesting directory: {}", baseDir);
        recursive = Boolean.parseBoolean(config.get(
                "harvester/hca-data/recursive", "false"));
        ignoreFilter = new IgnoreFilter(config.get(
                "harvester/hca-data/ignoreFilter", DEFAULT_IGNORE_PATTERNS)
                .split("\\|"));
        link = Boolean.parseBoolean(config.get("harvester/hca-data/link",
                "false"));
        currentDir = baseDir;
        subDirs = new Stack<File>();
        hasMore = true;

        Map<String, String> namespaceURIs = new HashMap<String, String>();
        namespaceURIs.put("seer", SEER_NS_URI);
        namespaceURIs.put("usq", USQ_NS_URI);
        namespaceURIs.put("oai_dc", OAI_DC_NS_URI);
        namespaceURIs.put("dc", DC_NS_URI);
        docFactory = new DocumentFactory();
        docFactory.setXPathNamespaceURIs(namespaceURIs);
        saxReader = new SAXReader(docFactory);

        hcaDcFull = createTransformer("/hca-dc-full.xsl");
        hcaDcAttachment = createTransformer("/hca-dc-attachment.xsl");

        packageDir = FascinatorHome.getPathFile("packages/hca-data");
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }
    }

    private Transformer createTransformer(String xslPath)
            throws HarvesterException {
        try {
            return TransformerFactory.newInstance().newTransformer(
                    new StreamSource(getClass().getResourceAsStream(xslPath)));
        } catch (Exception e) {
            throw new HarvesterException(e);
        }
    }

    public Set<String> getObjectIdList() throws HarvesterException {
        Set<String> fileObjectIdList = new LinkedHashSet<String>();
        if (currentDir.isDirectory()) {
            File[] files = currentDir.listFiles(ignoreFilter);
            for (File file : files) {
                if (file.isDirectory()) {
                    if (recursive) {
                        subDirs.push(file);
                    }
                } else {
                    try {
                        Set<String> oidList = processPiXml(file);
                        fileObjectIdList.addAll(oidList);
                    } catch (StorageException se) {
                        log.warn("File not harvested {}: {}", file, se
                                .getMessage());
                    }
                }
            }
            hasMore = !subDirs.isEmpty();
            if (hasMore) {
                currentDir = subDirs.pop();
            }
        } else {
            try {
                Set<String> oidList = processPiXml(currentDir);
                fileObjectIdList.addAll(oidList);
            } catch (StorageException se) {
                log.warn("File not harvested {}: {}", currentDir, se
                        .getMessage());
            }
            hasMore = false;
        }
        return fileObjectIdList;
    }

    public boolean hasMoreObjects() {
        return hasMore;
    }

    private Set<String> processPiXml(File file) throws HarvesterException,
            StorageException {
        Set<String> idList = new LinkedHashSet<String>();
        try {
            Document piDoc = saxReader.read(file);
            Element usqTitle = (Element) piDoc.selectSingleNode("//usq:title");

            // create manifest
            JsonConfigHelper json = new JsonConfigHelper();
            json.set("title", usqTitle.attributeValue("nativeScript"));

            List<String> creators = new ArrayList<String>();
            List creatorNodes = piDoc.selectNodes("//creator");
            Iterator itc = creatorNodes.iterator();
            while (itc.hasNext()) {
                Element elem = (Element) itc.next();
                creators.add(elem.attributeValue("lastName") + ", "
                        + elem.attributeValue("firstName"));
            }

            List nodes = piDoc.selectNodes("//usq:electronicLocation");
            Iterator it = nodes.iterator();
            while (it.hasNext()) {
                Element elem = (Element) it.next();
                String repositoryLink = elem
                        .attributeValue(getAttrName("usq:repositoryLink"));
                String repositoryLinkDescription = elem
                        .attributeValue(getAttrName("usq:repositoryLinkDescription"));

                // create an object for each attachment
                File subFile = new File(file.getParentFile(), repositoryLink);
                if (subFile.exists()) {
                    log.debug("Creating object for {}", subFile);
                    DigitalObject object = createDigitalObject(subFile);
                    String oid = object.getId();

                    // add manifest entry
                    json.set("manifest/node-" + oid + "/id", oid);
                    json.set("manifest/node-" + oid + "/title",
                            repositoryLinkDescription);

                    // add basic DC metadata
                    createDcXmlPayload(file, object, repositoryLink);

                    idList.add(oid);
                    object.close();
                } else {
                    log.debug("Attachment file not found: {}", subFile);
                }
            }

            if (idList.isEmpty()) {
                throw new StorageException("No attachments created");
            }

            // create a package object for this PI.xml
            String hash = DigestUtils.md5Hex(file.getAbsolutePath());
            File packageFile = new File(packageDir, hash + ".tfpackage");
            Writer fw = new FileWriter(packageFile);
            json.store(fw);
            fw.close();

            log.debug("Created object for {}", file);
            DigitalObject object = createDigitalObject(packageFile);
            StorageUtils.createOrUpdatePayload(object, PI_XML,
                    new FileInputStream(file));

            // add full DC metadata
            createDcXmlPayload(file, object);

            idList.add(object.getId());
            object.close();

            // for any errors, throw StorageException to skip harvesting these
            // group of files
        } catch (DocumentException de) {
            log.error("Failed parsing XML: {}", de.getMessage());
            throw new StorageException(de);
        } catch (FileNotFoundException fnfe) {
            log.error("Failed to add payload: {}", fnfe.getMessage());
            throw new StorageException(fnfe);
        } catch (TransformerException te) {
            log.error("Failed to create dc.xml: {}", te.getMessage());
            throw new StorageException(te);
        } catch (IOException ioe) {
            log.error("Failed to create manifest: {}", ioe.getMessage());
            throw new StorageException(ioe);
        }
        return idList;
    }

    private DigitalObject createDigitalObject(File file)
            throws HarvesterException, StorageException {
        DigitalObject object = StorageUtils.storeFile(getStorage(), file, link);

        // update object metadata
        Properties props = object.getMetadata();
        props.setProperty("render-pending", "true");
        props.setProperty("file.path", FilenameUtils.separatorsToUnix(file
                .getAbsolutePath()));

        return object;
    }

    private QName getAttrName(String qName) {
        if (qName.startsWith("usq:")) {
            return QName.get(qName, USQ_NS_URI);
        } else if (qName.startsWith("seer:")) {
            return QName.get(qName, SEER_NS_URI);
        }
        return QName.get(qName);
    }

    private void createDcXmlPayload(File piXmlFile, DigitalObject object)
            throws TransformerException {
        createDcXmlPayload(piXmlFile, object, null);
    }

    private void createDcXmlPayload(File piXmlFile, DigitalObject object,
            String repositoryLink) throws TransformerException {
        try {
            Transformer t = null;
            if (repositoryLink == null) {
                t = hcaDcFull;
            } else {
                t = hcaDcAttachment;
                t.setParameter("repositoryLink", repositoryLink);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.transform(new StreamSource(new FileInputStream(piXmlFile)),
                    new StreamResult(out));
            StorageUtils.createOrUpdatePayload(object, "dc.xml",
                    new ByteArrayInputStream(out.toByteArray()));
        } catch (Exception e) {
            throw new TransformerException(e);
        }
    }
}
