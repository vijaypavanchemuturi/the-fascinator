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

package au.edu.usq.fascinator.harvester.restore.backup;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.Configurable;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * Restore files in a specified backup directory on the local file system
 * <p>
 * Configuration options:
 * <ul>
 * <li>baseDir: directory to harvest</li>
 * <li>recursive: set true to recurse into sub-directories (default: false)</li>
 * <li>ignoreFilter: wildcard patterns of files to ignore separated by '|'
 * (default: .svn)</li>
 * <li>force: set true to force harvest of all files (ignore cache)</li>
 * </ul>
 * 
 * @author Linda Octalina
 */

public class BackupRestoreHarvester implements Harvester, Configurable {

	/** Default storage type will be used if none defined **/
    private static final String DEFAULT_STORAGE_TYPE = "file-system";
    
    /** Default ignore filter if none defined **/
    private static final String DEFAULT_IGNORE_FILTER = ".svn|.ice|.*|~*|*~";
	
	/** Config file **/
	private JsonConfig config;
	
	/** System config file **/
	private JsonConfig systemConfig;
	
	/** Backup location list **/
    private Map<String, Map<String, Object>> restoreDirList = new HashMap<String, Map<String, Object>>();
    
    /** Restore email **/
    private String email;
    
    /** Logging **/
    private static Logger log = LoggerFactory.getLogger(BackupRestoreHarvester.class);
    
    /**
     * Get deleted Object
     * 
	 * @return list of DigitalObject
     */
	@Override
	public List<DigitalObject> getDeletedObjects() throws HarvesterException {
		List<DigitalObject> fileObjects = new ArrayList<DigitalObject>();
		return fileObjects;
	}

	/**
	 * Get List of objects in the backup directory
	 * 
	 * @return list of DigitalObject
	 */
	@Override
	public List<DigitalObject> getObjects() throws HarvesterException {
		List<DigitalObject> fileObjects = new ArrayList<DigitalObject>();
		
		for (String restorePath : restoreDirList.keySet()) {
            Map<String, Object> restoreProps = restoreDirList.get(restorePath);
            String filterString = restoreProps.get("ignoreFilter").toString();
            if (filterString == null) {
                filterString = DEFAULT_IGNORE_FILTER;
            }
            IgnoreFilter ignoreFilter = new IgnoreFilter(filterString
                    .split("\\|"));
            String includeMeta = String.valueOf(restoreProps
                    .get("include-rendition-meta"));
            String active = String.valueOf(restoreProps.get("active"));
            String includePortal = String.valueOf(restoreProps
                    .get("include-portal-view"));
            if (active == "true") {
            	File restoredDir = new File(restorePath, email);
            	if (restoredDir.exists()) {
            		//Start to process files folder
            		File filesFolder = new File(restoredDir, "files");
            		List<File> fileList = new ArrayList<File>();
            		List<String> notZipFileList = new ArrayList<String>();
            		listFileRecursive(filesFolder, fileList, notZipFileList, ignoreFilter);
            		for (File file : fileList) {
            			Boolean addAsDigitalObject = true;
            			// Need to ignore rendition zip file
            			if (file.getAbsolutePath().endsWith(".zip")) {
            				String fileWithoutZip = file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(".zip"));
            				if (notZipFileList.contains(fileWithoutZip)) {
            					addAsDigitalObject = false;
            				}
            			}
            			if (addAsDigitalObject) {
            				fileObjects.add(new BackupRestoreDigitalObject(file, Boolean.parseBoolean(includeMeta)));
            			}
            		}
            	
            		//Start to process portal files
	            	if (includePortal == "true") {
	                	File portalDir = new File(restoredDir, "config");
	                	if (portalDir.exists()) {
	                		File destDir = new File(systemConfig.get("fascinator-home")
	                                + "/portal/" + systemConfig.get("portal/home"));
	                		destDir.getParentFile().mkdirs();
	                		try {
								includePortalDir(portalDir, destDir, ignoreFilter);
							} catch (IOException e) {
								e.printStackTrace();
							}
	                	}
	                }
            	}
            }   
		}
		
		return fileObjects;
	}
	
	/**
	 * List files in portal directory 
	 * @param portalSrc
	 * @param portalDest
	 * @param ignoreFilter
	 * @throws IOException
	 */
	private void includePortalDir(File portalSrc, File portalDest,
            IgnoreFilter ignoreFilter) throws IOException {
        if (portalSrc.isDirectory()) {
            if (!portalDest.exists()) {
                portalDest.mkdir();
            }
            for (File file : portalSrc.listFiles(ignoreFilter)) {
                includePortalDir(new File(portalSrc, file.getName()), new File(
                        portalDest, file.getName()), ignoreFilter);
            }
        } else {
            InputStream in = new FileInputStream(portalSrc);
            OutputStream out = new FileOutputStream(portalDest);
            IOUtils.copy(in, out);
            in.close();
            out.close();
        }
    }
	
	/**
	 * List files in the backup directory
	 * @param path
	 * @param fileList
	 * @param notZipFileList
	 * @param ignoreFilter
	 */
	private void listFileRecursive(File path, List<File> fileList, List<String> notZipFileList, IgnoreFilter ignoreFilter) {
		if (path.isDirectory()) {
			for (File file : path.listFiles(ignoreFilter)) {
				if (path.isDirectory())
					listFileRecursive(file, fileList, notZipFileList, ignoreFilter);
				else {
					if (!path.getAbsolutePath().endsWith(".zip")) {
						notZipFileList.add(path.getAbsolutePath());
					}
					fileList.add(file);
				}	
			}
		} else {
			if (!path.getAbsolutePath().endsWith(".zip")) {
				notZipFileList.add(path.getAbsolutePath());
			}
			fileList.add(path);
		}
	}

	/**
	 * Has more Deleted object
	 */
	@Override
	public boolean hasMoreDeletedObjects() {
		return false;
	}

	/**
	 * Has more objects
	 */
	@Override
	public boolean hasMoreObjects() {
		return false;
	}

	/** 
	 * Get plugin id
	 * 
	 * @return plugin id
	 */
	@Override
	public String getId() {
		return "restore-backup";
	}

	/**
	 * Get plugin Name
	 * 
	 * @return plugin name
	 */
	@Override
	public String getName() {
		return "Restore backup Harvester";
	}	

	/**
	 * Plugin init function
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void init(File jsonFile) throws PluginException {
		try {
			config = new JsonConfig(jsonFile);
			systemConfig = new JsonConfig(config.getSystemFile());
			Map<String, Object> backupPaths = config.getMapWithChild("restore-paths");
		    Map<String, Map<String, Object>> backupPathsDict = new HashMap<String, Map<String, Object>>();
		    for (String key : backupPaths.keySet()) {
		        Map<String, Object> newObj = (Map<String, Object>) backupPaths
		                .get(key);
		        backupPathsDict.put(key, newObj);
		    }
		    setRestoreDir(backupPathsDict);
		    setEmail(config.get("restore-email"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	 /**
     * Set Backup location being used
     * 
     * @param backupDir
     */
    public void setRestoreDir(Map<String, Map<String, Object>> restoreDirs) {
        if (restoreDirs != null) {
            for (String backupPath : restoreDirs.keySet()) {
                Map<String, Object> backupPathProps = restoreDirs
                        .get(backupPath);
                restoreDirList.put(backupPath, backupPathProps);
            }
        }
    }

    /**
     * Return backup location
     * 
     * @return backupDir
     */
    public Map<String, Map<String, Object>> getRestoreDirList() {
        return restoreDirList;
    }

    /**
     * Create the md5 of the email for the user space
     * 
     * @param email
     */
    public void setEmail(String email) {
        if (email != null && email != "") {
            this.email = DigestUtils.md5Hex(email);
        }
    }

    /**
     * Get the email
     * 
     * @return email
     */
    public String getEmail() {
        return email;
    }
    
	@Override
	public void shutdown() throws PluginException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getConfig() {
		// TODO Auto-generated method stub
		return null;
	}
	
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
            return true;
        }
    }

}
