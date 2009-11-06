/* 
 * The Fascinator - Plugin - Harvester - OAI-PMH
 * Copyright (C) 2008-2009 University of Southern Queensland
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

package au.edu.usq.fascinator.harvester.backuprestore;

import java.io.File;

import java.io.FileFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.Configurable;
import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.common.JsonConfig;

public class BackupRestoreHarvester implements Harvester, Configurable {

	private JsonConfig jsonConfig;
	
	private Map<String, Map<String, Object>> restorePathList = new HashMap<String, Map<String, Object>>();
	
	private static Logger log = LoggerFactory.getLogger(BackupRestoreHarvester.class);
	
	private String email;
	
	private IgnoreFilter ignoreFilter;
	
    /** stack of sub-directories while harvesting */
    private Stack<File> subDirs;
    
    /** whether or not there are more files to harvest */
    private boolean hasMore;
    
    /** current directory while harvesting */
    private File currentDir;
	
	@Override
	public List<DigitalObject> getDeletedObjects() throws HarvesterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DigitalObject> getObjects() throws HarvesterException {
		List<DigitalObject> fileObjects = new ArrayList<DigitalObject>();
		for (String restorePath : restorePathList.keySet()) {
			// User the user's space email
			log.info("eeemail: " + email);
			File dirToBeRestored= new File(restorePath, getEmailMd5());
			log.info("Restoring {}, with user's space {}", restorePath, email);
			if (dirToBeRestored.exists()) {
				Map<String, Object> restorePros = restorePathList.get(restorePath);
				
                IgnoreFilter ignoreFilter = new IgnoreFilter(restorePros.get(
                        "ignoreFilter").toString().split("\\|"));
				//Original Files and metadata content (metadata content in zip format)
                File fileContentDir = new File(dirToBeRestored.getAbsoluteFile(), "files");
                
				
			} else {
				log.info("Path to be restored '{}' with user's space {} is not exist", restorePath, email);
			}
		}
		
		return fileObjects;
	}
	
	@Override
	public boolean hasMoreDeletedObjects() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasMoreObjects() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getId() {
		return "backup-restore";
	}

	@Override
	public String getName() {
		return "Restore backup Harvester";
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
    
    @SuppressWarnings("unchecked")
	public void setRestorePath() {
    	Map<String, Object> backupPaths = jsonConfig.getMapWithChild("restore-paths");
	    Map<String, Map<String, Object>> restorePathDict = new HashMap<String, Map<String, Object>>();
	    for (String key : backupPaths.keySet()) {
	        Map<String, Object> newObj = (Map<String, Object>) backupPaths
	                .get(key);
	        restorePathDict.put(key, newObj);
	    }
	    if (restorePathDict != null) {
            for (String backupPath : restorePathDict.keySet()) {
                Map<String, Object> backupPathProps = restorePathDict
                        .get(backupPath);
                restorePathList.put(backupPath, backupPathProps);
            }
        }
    }
    
    public Map<String, Map<String, Object>> getRestorePath() {
    	return restorePathList;
    }
    
    /**
     * Create the md5 of the email for the user space
     * 
     * @param email
     */
    public void setEmail(String email) {
        if (email != null && email != "") {
            this.email = email;
        }
    }
    
    public String getEmailMd5() {
    	return DigestUtils.md5Hex(email);
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
	public void init(File jsonFile) throws PluginException {
		try {
			jsonConfig = new JsonConfig(jsonFile);
			setRestorePath();
			setEmail(jsonConfig.get("restore-email"));
			subDirs = new Stack<File>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() throws PluginException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getConfig() {
		StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(getClass().getResourceAsStream(
                    "/" + getId() + "-config.html"), writer);
        } catch (IOException ioe) {
            writer.write("<span class=\"error\">" + ioe.getMessage()
                    + "</span>");
        }
        return writer.toString();
	}
}
