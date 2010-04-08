/*
 * The Fascinator - Plugin - Harvester - ICE2
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
package au.edu.usq.fascinator.harvester.ice2;

import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.harvester.impl.GenericHarvester;
import au.edu.usq.fascinator.common.storage.StorageUtils;
import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

import net.htmlparser.jericho.*;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A harvester for ingesting pre-rendered ICE2 packages.
 *
 * Initial work based on File System harvester.
 *
 * @author Greg Pendlebury
 */
public class Ice2Harvester extends GenericHarvester {

    /** logging */
    private Logger log = LoggerFactory.getLogger(Ice2Harvester.class);

    /** default ignore list */
    private static final String DEFAULT_IGNORE_PATTERNS = ".svn";

    /** directory to house temp data */
    private File tempDir;

    /** directory to harvest */
    private File baseDir;

    /** cache directory */
    private File cacheDir;

    /** current directory while harvesting */
    private File currentDir;

    /** stack of sub-directories while harvesting */
    private Stack<File> subDirs;

    /** stack of sub-directories found with ICE manifests */
    private Stack<File> iceDirs;

    /** stack of ICE manifest files found */
    private Stack<File> iceMetadata;

    /** whether or not there are more files to harvest */
    private boolean hasMore;

    /** use links instead of copying */
    private boolean link;

    /** filter used to ignore files matching specified patterns */
    private IgnoreFilter ignoreFilter;

    /** our python rendering engine */
    private PythonInterpreter python;

    /** A copy of the ICE manifest parsing code */
    private File iceManifestLib;
    private String iceManifestPath;
    private String iceManifestName;
    private String jsonName;

    /**
     * File filter used to ignore specified files
     */
    private class IgnoreFilter implements FileFilter {

        /** wildcard patterns of files to ignore */
        private String[] patterns;

        public IgnoreFilter(String[] patterns) {
            this.patterns = patterns;
        }

        @Override
        public boolean accept(File path) {
            for (String pattern : patterns) {
                if (FilenameUtils.wildcardMatch(path.getName(), pattern)) {
                    return false;
                }
            }
            return true;
        }
    }

    public Ice2Harvester() {
        super("ice2-harvester", "ICE2 Harvester");
    }

    @Override
    public void init() throws HarvesterException {
        JsonConfig config = getJsonConfig();
        String tempPath = System.getProperty("java.io.tmpdir");
        tempDir = new File(tempPath, "ice2Harvest");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        baseDir = new File(config.get("harvester/ice2-harvester/baseDir", "."));
        log.info("Harvesting directory: {}", baseDir);
        ignoreFilter = new IgnoreFilter(config.get(
                "harvester/ice2-harvester/ignoreFilter",
                DEFAULT_IGNORE_PATTERNS).split("\\|"));
        cacheDir = null;
        String cacheDirValue = config.get("harvester/ice2-harvester/cacheDir");
        if (cacheDirValue != null) {
            cacheDir = new File(cacheDirValue);
            cacheDir.mkdirs();
            log.info("File system state will be cached in {}", cacheDir);
        }
        link = Boolean.parseBoolean(config.get("harvester/ice2-harvester/link",
                "false"));
        currentDir = baseDir;
        subDirs = new Stack<File>();
        iceDirs = new Stack<File>();
        iceMetadata = new Stack<File>();
        hasMore = true;

        python = new PythonInterpreter();
        iceManifestLib = null;
    }

    @Override
    public Set<String> getObjectIdList() throws HarvesterException {
        if (currentDir.isDirectory()) {
            // Traverse our directory
            for (File file : currentDir.listFiles(ignoreFilter)) {
                if (file.isDirectory()) {
                    // Store it for further traversal
                    subDirs.push(file);
                    // Have we found an ICE directory?
                    if (file.getName().equals(".ice")) {
                        iceDirs.push(file);
                    }
                }
            }
            // Test for more sub-directories
            hasMore = !subDirs.isEmpty();
            if (hasMore) {
                currentDir = subDirs.pop();
            }
        // We're finally done
        } else {
            hasMore = false;
        }
        if (iceDirs.size() > 0) {
            return findIceMetadata();
        } else {
            return new HashSet<String>();
        }
    }

    private Set<String> findIceMetadata()
            throws HarvesterException {
        File file, fileDir, metaFile = null;

        while (!iceDirs.empty()) {
            file = iceDirs.pop();
            fileDir = new File(file, "__dir__");
            if (fileDir.exists() && fileDir.isDirectory()) {
                metaFile = new File(fileDir, "meta");
                if (metaFile.exists()) {
                    iceMetadata.push(metaFile);
                } else {
                    log.error("Expected ICE manifest not found : '" + metaFile + "'");
                }
            } else {
                log.error("Expected ICE directory not found : '" + fileDir + "'");
            }

        }
        return parseIceMetadata();
    }

    private Set<String> parseIceMetadata()
            throws HarvesterException {
        // Some basic initialisation
        File file = null;
        InputStream iceParser = null;
        JsonConfigHelper response = null;

        // Cache our Python libraries for parsing manifests
        if (iceManifestLib == null) {
            // For ICE manifest
            iceParser = getClass().getResourceAsStream("/plugin_manifest.py");
            try {
                iceManifestLib = File.createTempFile("iceParser", ".py");
                iceManifestLib.deleteOnExit();
                FileOutputStream out = new FileOutputStream(iceManifestLib);
                IOUtils.copy(iceParser, out);
                out.close();
                iceParser.close();
                iceManifestPath = iceManifestLib.getParent();
                iceManifestName = FilenameUtils.getBaseName(iceManifestLib.getName());
            } catch (IOException ex) {
                log.error("Error caching ICE parser : ", ex);
                return new HashSet<String>();
            }
            // JSON generation
            iceParser = getClass().getResourceAsStream("/json.py");
            try {
                File json = File.createTempFile("json", ".py");
                json.deleteOnExit();
                FileOutputStream out = new FileOutputStream(json);
                IOUtils.copy(iceParser, out);
                out.close();
                iceParser.close();
                jsonName = FilenameUtils.getBaseName(json.getName());
            } catch (IOException ex) {
                log.error("Error caching JSON library : ", ex);
                return new HashSet<String>();
            }
        }

        // Loop through all the ICE metadata files we found
        while (!iceMetadata.empty()) {
            // Prepare the data
            file = iceMetadata.pop();
            iceParser = getClass().getResourceAsStream("/ice_item.py");
            response = new JsonConfigHelper();

            // Run the ICE parser
            python.set("filePath",  file.getAbsoluteFile());
            python.set("response",  response);
            python.set("parsePath", iceManifestPath);
            python.set("parseLib",  iceManifestName);
            python.set("jsonLib",   jsonName);
            python.execfile(iceParser);
            response = python.get("response", JsonConfigHelper.class);
            python.cleanup();

            // Check response is valid
            String guid = response.get("guid");
            if (guid != null) {
                // We've found our manifest
                hasMore = false;
                return processIceManifest(response);
            }
        }
        return new HashSet<String>();
    }

    private Set<String> processIceManifest(JsonConfigHelper response)
            throws HarvesterException {
        Set<String> fileObjectIdList = new HashSet<String>();

        String manifest = response.get("json");
        JsonConfigHelper jsonManifest;
        try {
            jsonManifest = new JsonConfigHelper(manifest);
        } catch (IOException ex) {
            log.error("Error in manifest JSON : ", ex);
            return new HashSet<String>();
        }

        String title = jsonManifest.get("title");
        String home  = jsonManifest.get("homePage");
        List<JsonConfigHelper> children = new ArrayList<JsonConfigHelper>();
        List<JsonConfigHelper> toc = jsonManifest.getJsonList("toc");

        for (JsonConfigHelper entry : toc) {
            boolean visible = Boolean.parseBoolean(entry.get("visible"));
            if (visible) {
                children.add(entry);
            }
        }

        Map<String, String> responseMap = prepareObject(title, home, children);

        for (String key : responseMap.keySet()) {
            if (responseMap.get(key) != null)  {
                fileObjectIdList.add(responseMap.get(key));
            }
        }
        return fileObjectIdList;
    }

    private Map<String, String> prepareObject(String title, String rootDoc,
            List<JsonConfigHelper> children) throws HarvesterException {

        Map<String, String> globalObjectMap = new HashMap<String, String>();

        return prepareObject(title, rootDoc, children, globalObjectMap, 0);
    }

    private Map<String, String> prepareObject(String title, String rootDoc,
            List<JsonConfigHelper> children, Map<String, String> objectIdMap, int lvl)
            throws HarvesterException {
        lvl++;

        // HTML processing
        List<Element> links, images, params = null;

        // Child processing
        String childTitle, childHome = null;
        List<JsonConfigHelper> grandChildren = null;

        log.debug(" *** ICE2 : Title (" + lvl + ") '" + title + "' => '" + rootDoc + "'");

        // Process the manifest children first
        for (JsonConfigHelper child : children) {
            //log.debug(" *** ICE2 : Child : '" + child.toString() + "'");
            childTitle = child.get("title");
            childHome  = child.get("relPath");
            grandChildren = child.getJsonList("children");
            prepareObject(childTitle, childHome, grandChildren, objectIdMap, lvl);
        }

        // Now find the html rendition of this file
        File rootFile = getOriginalDoc(rootDoc);
        File htmlDir = null;
        try {
            htmlDir = getHtmlRendition(rootFile);
        } catch (IOException ex) {
            // Nothing, leave it to the test below
        }
        if (htmlDir == null || !htmlDir.exists()) {
            log.warn(" *** ICE2 : Root document not found, skipping");

        } else {
            // Is this an object we've previsouly harvested?
            if (!objectIdMap.keySet().contains(title)) {
                try {
                    File htmlFile = new File(htmlDir, htmlDir.getName() + ".html");
                    String content = FileUtils.readFileToString(htmlFile);
                    Source source = new Source(content);
                    OutputDocument htmlOut = new OutputDocument(source);
                    source.setLogger(null);

                    // Links, replace with object references if required
                    links  = source.getAllElements(HTMLElementName.A);
                    for (Element link : links) {
                        Attributes attr = link.getAttributes();
                        String replacement = "<a ";
                        boolean target = false;
                        for (Iterator i = attr.iterator(); i.hasNext();) {
                            Attribute a = (Attribute) i.next();
                            if (a.getName().equals("href")) {
                                // Ignore legitimate web links
                                if (!a.getValue().startsWith("http") && !a.getValue().startsWith("mailto")) {
                                    String href = a.getValue().replace("%20", " ");
                                    String newLink = harvestLink(htmlFile, href, objectIdMap);
                                    if (!newLink.equals(href)) {
                                        target = true;
                                        log.debug(" *** ICE2 : Link : '" + href + "'");
                                        replacement += "href=\"" + newLink + "\" ";
                                    } else {
                                        replacement += "href=\"" + a.getValue() + "\" ";
                                    }
                                } else {
                                    replacement += "href=\"" + a.getValue() + "\" ";
                                }
                            } else {
                                //replacement += a.getName() + "=\"" + a.getValue() + "\" ";
                            }
                        }
                        replacement += ">" + link.getContent().toString() + "</a>";
                        if (target) {
                            htmlOut.replace(link, replacement);
                            //log.debug(" *** ICE2 : Original : '" + content + "'");
                            //log.debug(" *** ICE2 : New Version : '" + htmlOut.toString() + "'");
                        }
                    }

                    params = source.getAllElements(HTMLElementName.PARAM);
                    for (Element param : params) {
                        log.debug(" *** ICE2 : Param : '" + param.getAttributeValue("name") + "' => '" + param.getAttributeValue("value") + "'");
                    }

                    // Create digital object
                    try {
                        // Create the object of the original
                        DigitalObject object = createObject(rootFile);
                        // Stream our custom html back to disk
                        FileUtils.writeStringToFile(htmlFile, htmlOut.toString());
                        // Add the html render of the file
                        Payload payload = addPayload(object, htmlFile, "");
                        payload.setType(PayloadType.Preview);
                        payload.close();
                        File imgDir = new File(htmlDir, htmlDir.getName() + "_files");
                        if (imgDir.exists()) {
                            addPayload(object, imgDir, "");
                        }
                        // Log the object creation
                        objectIdMap.put(title, object.getId());
                    } catch (StorageException ex) {
                        log.error("Error storing html : '" + title + "' : ", ex);
                    }

                } catch (IOException ex) {
                    log.error("Error reading file : ", ex);
                }
            }
        }

        // Do we need to build a package for this level?
        if (children.size() > 0) {
            // Create the empty package
            // TODO

            // IF it exists, the top level document is this document
            if (objectIdMap.containsKey(title)) {
                // Add to package
                // TODO
            }

            // Followed by all the children
            for (JsonConfigHelper child : children) {
                childTitle = child.get("title");
                if (objectIdMap.containsKey(title)) {
                    // Add to package
                    // TODO
                }
            }
        }
        return objectIdMap;
    }

    private String harvestLink(File htmlFile, String oldLink, Map<String, String> objectIdMap) {
        // Normalise relative links
        String index = oldLink;
        if (oldLink.startsWith("../")) {
            index = index.substring(3);
        }
        // Is this an object we've previsouly harvested?
        if (objectIdMap.keySet().contains(index)) {
            return objectIdMap.get(index);
        }

        // Now sort out what type of link this is
        if (index.startsWith("media/")) {
            // Media, handle separately
            String mediaOid = harvestMedia(index, objectIdMap);
            if (mediaOid == null) {
                return oldLink;
            } else {
                return "tfObject:" + mediaOid;
            }

        } else {
            // A link to another document
            // Step one, find the real file
            // Step two, harvest it in turn : prepareObject()??

        }
        return oldLink;
    }

    private String harvestMedia(String filePath, Map<String, String> objectIdMap) {
        File media = new File(baseDir, filePath);
        DigitalObject object = null;
        if (media.exists()) {
            String fileType = FilenameUtils.getExtension(media.getName());
            String subFilePath = filePath.substring(6);
            int firstSlash = subFilePath.indexOf("/");
            String mediaType = subFilePath.substring(0, firstSlash);
            subFilePath = subFilePath.substring(firstSlash + 1);
            try {
                if (mediaType.equals("audio")) {
                    log.debug(" *** ICE2 : Audio => '"+ subFilePath + "'");
                    object = createObject(media);
                }
                if (mediaType.equals("flash")) {
                    // TODO
                }
                if (mediaType.equals("images")) {
                    log.debug(" *** ICE2 : Image => '"+ subFilePath + "'");
                    object = createObject(media);
                }
                if (mediaType.equals("presentations")) {
                    // Likely a package - simple (v1) answer is to 'gulch'
                    //  everything in the same directory and all sub-dirs
                    if (fileType.contains("htm")) {
                        log.debug(" *** ICE2 : HTML Presentation => '"+ subFilePath + "'");
                        object = createObject(media);
                        File mediaRoot = media.getParentFile();
                        File[] files = mediaRoot.listFiles(ignoreFilter);
                        for (File f : files) {
                            if (!f.getName().equals(".ice")) {
                                addPayload(object, f, "");
                            }
                        }

                    // Single Files
                    } else {
                        log.debug(" *** ICE2 : Presentation => '"+ subFilePath + "'");
                        object = createObject(media);
                    }
                }
                if (mediaType.equals("readings")) {
                    // TODO
                }
                if (mediaType.equals("video")) {
                    log.debug(" *** ICE2 : Video => '"+ subFilePath + "'");
                    object = createObject(media);
                }
            } catch (HarvesterException ex) {
                log.error("Error storing file : ", ex);
            } catch (StorageException ex) {
                log.error("Error storing file : ", ex);
            }
        }

        if (object == null) {
            log.error("Media object not found : '" + media.getAbsolutePath() + "'");
            return null;
        } else {
            objectIdMap.put(filePath, object.getId());
            return object.getId();
        }
    }

    private File getOriginalDoc(String manifestFileName) {
        File file = new File(baseDir, manifestFileName);
        String fileName = file.getName();

        String simpleName = FilenameUtils.getBaseName(fileName);
        File simpleDir = file.getParentFile();
        // If we can't find it now, it doesn't exist
        if (!simpleDir.exists()) {
            return null;
        }
        // Find the original name of our file
        File[] files = simpleDir.listFiles();
        for (File f : files) {
            if (f.isFile() && f.getName().startsWith(simpleName)) {
                return f;
            }
        }
        return null;
    }

    private File getHtmlRendition(File srcFile) throws IOException {
        if (srcFile == null) {
            return null;
        }
        // Prepate our temp space
        String simpleName = FilenameUtils.getBaseName(srcFile.getName());
        File htmlDir = new File(tempDir, simpleName);
        if (!htmlDir.exists()) {
            htmlDir.mkdir();
        }
        htmlDir.deleteOnExit();
        File imgDir = new File(htmlDir, simpleName + "_files");
        if (!imgDir.exists()) {
            imgDir.mkdir();
        }
        imgDir.deleteOnExit();

        // Go check for the renditions directory
        boolean found = false;
        File renditionDir = new File(srcFile.getParentFile(), ".ice/" + srcFile.getName());
        if (renditionDir.exists() && renditionDir.isDirectory()) {
            // Loop through all available renditions
            File[] files = renditionDir.listFiles();
            for (File f : files) {
                // Html rendition
                if (f.getName().endsWith("xhtml.body")) {
                    found = true;
                    File htmlFile = new File(htmlDir, simpleName + ".html");
                    if (!htmlFile.exists()) {
                        htmlFile.createNewFile();
                    }
                    htmlFile.deleteOnExit();
                    FileOutputStream htmlFileOut = new FileOutputStream(htmlFile);
                    FileInputStream htmlFileIn = new FileInputStream(f);
                    IOUtils.copy(htmlFileIn, htmlFileOut);
                    htmlFileIn.close();
                    htmlFileOut.close();
                }
                // Images
                if (f.getName().startsWith("image-")) {
                    File imgFile = new File(imgDir, f.getName().substring(6));
                    if (!imgFile.exists()) {
                        imgFile.createNewFile();
                    }
                    imgFile.deleteOnExit();
                    FileOutputStream imgFileOut = new FileOutputStream(imgFile);
                    FileInputStream imgFileIn = new FileInputStream(f);
                    IOUtils.copy(imgFileIn, imgFileOut);
                    imgFileIn.close();
                    imgFileOut.close();
                }
            }
        }

        if (found) {
            return htmlDir;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasMoreObjects() {
        return hasMore;
    }

    private DigitalObject createObject(String data) throws HarvesterException,
            StorageException {
        DigitalObject object = StorageUtils.storeFile(getStorage(), new File(data), link);

        // update object metadata
        Properties props = object.getMetadata();
        props.setProperty("render-pending", "true");

        object.close();
        return object;
    }

    private DigitalObject createObject(File file) throws HarvesterException,
            StorageException {
        DigitalObject object = StorageUtils.storeFile(getStorage(), file, link);

        // update object metadata
        Properties props = object.getMetadata();
        props.setProperty("render-pending", "true");

        object.close();
        return object;
    }

    private Payload addPayload(DigitalObject object, File file, String prefix)
            throws HarvesterException, StorageException {
        String pid = StorageUtils.generatePid(file);
        // Make sure we don't add the source again
        if (pid.equals(object.getSourceId())) {
            return null;
        }
        if (!prefix.equals("")) {
            prefix += "/";
        }
        pid = prefix + pid;

        //log.debug("Adding payload to object : '" + file.getAbsolutePath() + "'");
        if (file.isDirectory()) {
            File[] files = file.listFiles(ignoreFilter);
            for (File f : files) {
                if (!f.getName().equals(".ice")) {
                    addPayload(object, f, prefix + file.getName());
                }
            }
        } else {
            try {
                InputStream in = new FileInputStream(file);
                if (link) {
                    return StorageUtils.createOrUpdatePayload(object, pid, in, file.getAbsolutePath());
                } else {
                    return StorageUtils.createOrUpdatePayload(object, pid, in);
                }
            } catch (FileNotFoundException ex) {
                log.error("Error accessing file : ", ex);
            }
        }
        return null;
    }
}