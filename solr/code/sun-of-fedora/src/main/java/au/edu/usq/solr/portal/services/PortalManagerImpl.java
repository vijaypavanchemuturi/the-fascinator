/* 
 * Sun of Fedora - Solr Portal
 * Copyright (C) 2008  University of Southern Queensland
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
package au.edu.usq.solr.portal.services;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.apache.tapestry.ioc.Resource;

import au.edu.usq.solr.portal.Portal;

public class PortalManagerImpl implements PortalManager {

    private static final String PORTAL_XML = "portal.xml";

    private static final String GROUPS_XML = "groups.xml";

    private Logger log = Logger.getLogger(PortalManagerImpl.class);

    private Map<String, Portal> portals;

    private File portalsDir;

    private Marshaller jaxbM;

    private Unmarshaller jaxbU;

    public PortalManagerImpl(Resource configuration) {
        Properties props = new Properties();
        try {
            props.load(configuration.toURL().openStream());
            portalsDir = new File(props.getProperty(AppModule.PORTALS_DIR_KEY));
            JAXBContext ctx = JAXBContext.newInstance(Portal.class);
            jaxbM = ctx.createMarshaller();
            jaxbM.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbU = ctx.createUnmarshaller();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Portal> getPortals() {
        if (portals == null) {
            portals = new HashMap<String, Portal>();
            loadPortals();
        }
        return portals;
    }

    public Portal getDefault() {
        return get("default");
    }

    public Portal get(String name) {
        Portal portal = null;
        if (getPortals().containsKey(name)) {
            portal = getPortals().get(name);
        } else {
            portal = loadPortal(name);
        }
        return portal;
    }

    public void add(Portal portal) {
        String portalName = portal.getName();
        Map<String, String> facetFields = portal.getFacetFields();
        if (!portalName.equals("default") && facetFields.isEmpty()) {
            facetFields.putAll(getDefault().getFacetFields());
        }
        getPortals().put(portalName, portal);
    }

    public void remove(String name) {
        // delete the portal.xml
        File portalDir = new File(portalsDir, name);
        File portalFile = new File(portalDir, PORTAL_XML);
        portalFile.delete();
        getPortals().remove(name);
    }

    public void save(Portal portal) {
        String portalName = portal.getName();
        File portalFile = new File(new File(portalsDir, portalName), PORTAL_XML);
        portalFile.getParentFile().mkdirs();
        try {
            jaxbM.marshal(portal, portalFile);
            log.info("Saved portal: " + portal);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        // TODO copy velocity templates
    }

    private void loadPortals() {
        File[] portalDirs = portalsDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                String name = file.getName();
                return file.isDirectory() && !name.equals(".svn");
            }
        });
        for (File dir : portalDirs) {
            loadPortal(dir.getName());
        }
    }

    private Portal loadPortal(String name) {
        Portal portal = null;
        File portalFile = new File(new File(portalsDir, name), PORTAL_XML);
        if (portalFile.exists()) {
            try {
                portal = (Portal) jaxbU.unmarshal(portalFile);
                add(portal);
                log.info("Loaded portal: " + portal);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return portal;
    }
}
