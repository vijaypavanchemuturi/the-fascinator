/* 
 * The Fascinator - File System Harvester Plugin
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
package au.edu.usq.fascinator.harvester.filesystem;

import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.harvester.impl.GenericHarvester;
import au.edu.usq.fascinator.common.storage.StorageUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Harvests files in a specified directory on the local file system
 * <p>
 * Configuration options:
 * <ul>
 * <li>baseDir: directory to harvest</li>
 * <li>recursive: set true to recurse into sub-directories (default: false)</li>
 * <li>ignoreFilter: wildcard patterns of files to ignore separated by '|'
 * (default: .svn)</li>
 * <li>cacheDir: location for cache files</li>
 * <li>force: set true to force harvest of all files (ignore cache)</li>
 * <li>link: set true to link to original files, false to create copies</li>
 * </ul>
 * 
 * @author Oliver Lucido
 */
public class FileSystemHarvester extends GenericHarvester {

    /** file containing the full path for caching purposes */
    private static final String ID_FILE = "id.txt";

    /** logging */
    private Logger log = LoggerFactory.getLogger(FileSystemHarvester.class);

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

    /** current directory while harvesting */
    private File cacheCurrentDir;

    /** stack of sub-directories while harvesting */
    private Stack<File> cacheSubDirs;

    /** whether or not there are more files to harvest */
    private boolean hasMoreDeleted;

    /** filter used to ignore files matching specified patterns */
    private IgnoreFilter ignoreFilter;

    /** cache directory */
    private File cacheDir;

    /** force harvesting all files */
    private boolean force;

    /** use links instead of copying */
    private boolean link;

    /** filter used when detecting deleted files */
    private FileFilter idTxtFilter;

    /** Render chains */
    private Map<String, Map<String, List<String>>> renderChains;

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

    /**
     * File System Harvester Constructor
     */
    public FileSystemHarvester() {
        super("file-system", "File System Harvester");
    }

    /**
     * Initialisation of File system harvester plugin
     * 
     * @throws HarvesterException if fails to initialise
     */
    @Override
    public void init() throws HarvesterException {
        JsonConfig config = getJsonConfig();
        baseDir = new File(config.get("harvester/file-system/baseDir", "."));
        if (!baseDir.exists()) {
            throw new HarvesterException("Directory does not exist: '"
                    + baseDir.getAbsolutePath() + "'");
        }

        log.info("Harvesting directory: {}", baseDir);
        recursive = Boolean.parseBoolean(config.get(
                "harvester/file-system/recursive", "false"));
        ignoreFilter = new IgnoreFilter(config.get(
                "harvester/file-system/ignoreFilter", DEFAULT_IGNORE_PATTERNS)
                .split("\\|"));
        cacheDir = null;
        String cacheDirValue = config.get("harvester/file-system/cacheDir");
        if (cacheDirValue != null) {
            cacheDir = new File(cacheDirValue);
            cacheDir.mkdirs();
            log.info("File system state will be cached in {}", cacheDir);
        }
        force = Boolean.parseBoolean(config.get("harvester/file-system/force",
                "false"));
        if (force) {
            log.info("Forcing harvest of all files...");
        }
        link = Boolean.parseBoolean(config.get("harvester/file-system/link",
                "false"));
        currentDir = baseDir;
        subDirs = new Stack<File>();
        hasMore = true;

        // Order is significant
        renderChains = new LinkedHashMap();
        Map<String, JsonConfigHelper> renderTypes =
                config.getJsonMap("renderTypes");
        for (String name : renderTypes.keySet()) {
            Map<String, List<String>> details = new HashMap();
            JsonConfigHelper chain = renderTypes.get(name);
            details.put("fileTypes",      getList(chain, "fileTypes"));
            details.put("extractor",      getList(chain, "extractor"));
            details.put("indexOnHarvest", getList(chain, "indexOnHarvest"));
            details.put("render",         getList(chain, "render"));
            renderChains.put(name, details);
        }

        cacheCurrentDir = cacheDir;
        cacheSubDirs = new Stack<File>();
        hasMoreDeleted = true;
        idTxtFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || ID_FILE.equals(file.getName());
            }
        };
    }

    @Override
    public void shutdown() throws HarvesterException {
    }

    @Override
    public Set<String> getObjectIdList() throws HarvesterException {
        Set<String> fileObjectIdList = new HashSet<String>();
        if (currentDir.isDirectory()) {
            File[] files = currentDir.listFiles(ignoreFilter);
            for (File file : files) {
                if (file.isDirectory()) {
                    if (recursive) {
                        subDirs.push(file);
                    }
                } else {
                    if (force || hasFileChanged(file)) {
                        try {
                            String oid = createDigitalObject(file);
                            fileObjectIdList.add(oid);
                        } catch (StorageException se) {
                            log.warn("File not harvested {}: {}", file, se
                                    .getMessage());
                        }
                    }
                }
            }
            hasMore = !subDirs.isEmpty();
            if (hasMore) {
                currentDir = subDirs.pop();
            }
        } else {
            if (force || hasFileChanged(currentDir)) {
                try {
                    fileObjectIdList.add(createDigitalObject(currentDir));
                } catch (StorageException se) {
                    log.warn("File not harvested {}: {}", currentDir, se
                            .getMessage());
                }
            }
            hasMore = false;
        }
        return fileObjectIdList;
    }

    /**
     * Check if file has changed
     * 
     * @param file File to be checked
     * @return <code>true</code> if changed, <code>false</code> otherwise
     */
    private boolean hasFileChanged(File file) {
        boolean changed = true;
        if (cacheDir != null) {
            try {
                File parentDir = getCacheDirForFile(file);
                File cacheFile = new File(parentDir, "checksum.txt");
                File idFile = new File(parentDir, ID_FILE);
                if (cacheFile != null) {
                    InputStream fis = new FileInputStream(file);
                    String sha1 = DigestUtils.shaHex(fis);
                    fis.close();
                    if (cacheFile.exists()) {
                        String cachedSha1 = FileUtils
                                .readFileToString(cacheFile);
                        log.trace("Comparing {} with {}", sha1, cachedSha1);
                        if (sha1.equals(cachedSha1)) {
                            log.debug("{} has not changed", file);
                            changed = false;
                        } else {
                            log.debug("{} has changed", file);
                            changed = true;
                            FileUtils.writeStringToFile(cacheFile, sha1);
                            FileUtils.writeStringToFile(idFile, file
                                    .getAbsolutePath());
                        }
                    } else {
                        log.debug("Caching checksum for {}", file);
                        FileUtils.writeStringToFile(cacheFile, sha1);
                        FileUtils.writeStringToFile(idFile, file
                                .getAbsolutePath());
                    }
                }
            } catch (IOException ioe) {
                log.error("Failed to cache " + file, ioe);
            }
        }
        return changed;
    }

    /**
     * Get the cache directory for the file
     * 
     * @param file File to be checked
     * @return the cache directory if found, <code>null</code> otherwise
     */
    private File getCacheDirForFile(File file) {
        if (cacheDir != null) {
            String hash = DigestUtils.shaHex(file.getAbsolutePath());
            File shaDir = new File(cacheDir, hash.substring(0, 2));
            shaDir = new File(shaDir, hash.substring(2, 4));
            shaDir = new File(shaDir, hash);
            shaDir.mkdirs();
            return shaDir;
        }
        return null;
    }

    @Override
    public boolean hasMoreObjects() {
        return hasMore;
    }

    @Override
    public Set<String> getDeletedObjectIdList() throws HarvesterException {
        Set<String> fileObjects = new HashSet<String>();
        if (cacheCurrentDir == null) {
            hasMoreDeleted = false;
        } else {
            if (cacheCurrentDir.isDirectory()) {
                File[] files = cacheCurrentDir.listFiles(idTxtFilter);
                if (files.length == 0) {
                    // remove it
                    FileUtils.deleteQuietly(cacheCurrentDir);
                } else {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            cacheSubDirs.push(file);
                        } else {
                            addDeletedObject(fileObjects, file);
                        }
                    }
                }
                hasMoreDeleted = !cacheSubDirs.isEmpty();
                if (hasMoreDeleted) {
                    cacheCurrentDir = cacheSubDirs.pop();
                }
            } else {
                addDeletedObject(fileObjects, cacheCurrentDir);
                hasMoreDeleted = false;
            }
        }
        return fileObjects;
    }

    /**
     * Add the deleted object to the file object list and delete the file
     * quietly
     * 
     * @param fileObjectIdList list of files to be deleted
     * @param idFile file to be deleted
     * @throws HarvesterException if fail to read the file
     */
    private void addDeletedObject(Set<String> fileObjectIdList, File idFile)
            throws HarvesterException {
        if (ID_FILE.equals(idFile.getName())) {
            try {
                String id = FileUtils.readFileToString(idFile);
                File realFile = new File(id);
                if (!realFile.exists()) {
                    FileUtils.deleteQuietly(idFile.getParentFile());
                    fileObjectIdList.add(id);
                }
            } catch (IOException ioe) {
                log.warn("Failed to read {}", idFile);
            }
        }
    }

    @Override
    public boolean hasMoreDeletedObjects() {
        return hasMoreDeleted;
    }

    /**
     * Create digital object
     * 
     * @param file File to be transformed to be ditigal object
     * @return object id of created digital object
     * @throws HarvesterException if fail to create the object
     * @throws StorageException if fail to save the file to the storage
     */
    private String createDigitalObject(File file) throws HarvesterException,
            StorageException {
        DigitalObject object = StorageUtils.storeFile(getStorage(), file, link);
        String oid = object.getId();

        // update object metadata
        Properties props = object.getMetadata();
        props.setProperty("render-pending", "true");
        props.setProperty("file.path", FilenameUtils.separatorsToUnix(file
                .getAbsolutePath()));

        // Store rendition information if we have it
        String ext = FilenameUtils.getExtension(file.getName());
        for (String chain : renderChains.keySet()) {
            Map<String, List<String>> details = renderChains.get(chain);
            if (details.get("fileTypes").contains(ext)) {
                storeList(props, details, "extractor");
                storeList(props, details, "indexOnHarvest");
                storeList(props, details, "render");
            }
        }

        object.close();
        return oid;
    }

    private List<String> getList(JsonConfigHelper json, String field) {
        List<String> result = new ArrayList();
        List<Object> list = json.getList(field);
        for (Object object : list) {
            result.add((String)object);
        }
        return result;
    }

    private void storeList(Properties props, Map<String, List<String>> details,
            String field) {
        String joinedList = StringUtils.join(details.get(field), ",");
        props.setProperty(field, joinedList);
    }
}
