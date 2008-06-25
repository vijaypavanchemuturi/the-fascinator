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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.apache.tapestry.ioc.Resource;

import au.edu.usq.solr.portal.Portal;
import au.edu.usq.solr.portal.Role;
import au.edu.usq.solr.portal.RoleList;
import au.edu.usq.solr.util.InetAddressUtil;

public class RoleManagerImpl implements RoleManager {

    private static final String ROLES_XML = "roles.xml";

    private Logger log = Logger.getLogger(RoleManagerImpl.class);

    private RoleList roles;

    private File portalsDir;

    private Marshaller jaxbM;

    private Unmarshaller jaxbU;

    private HttpServletRequest request;

    public RoleManagerImpl(Resource configuration, HttpServletRequest request) {
        this.request = request;
        try {
            Properties props = new Properties();
            props.load(configuration.toURL().openStream());
            portalsDir = new File(props.getProperty(AppModule.PORTALS_DIR_KEY));
            JAXBContext ctx = JAXBContext.newInstance(RoleList.class);
            jaxbM = ctx.createMarshaller();
            jaxbM.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbU = ctx.createUnmarshaller();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RoleList getList() {
        if (roles == null) {
            load();
        }
        return roles;
    }

    public Role get(String name) {
        return getList().getRole(name);
    }

    public List<Role> getUserRoles(String username, Portal portal) {
        List<Role> userRoles = new ArrayList<Role>();
        userRoles.add(get(GUEST_ROLE));
        try {
            InetAddress net = InetAddress.getByName("0.0.0.0");
            InetAddress mask = InetAddress.getByName("0.0.0.0");
            InetAddress ip = InetAddress.getByName(request.getRemoteAddr());
            String nets = portal.getNetwork();
            String masks = portal.getNetmask();
            if (nets != null && masks != null) {
                net = InetAddress.getByName(nets);
                mask = InetAddress.getByName(masks);
                if (ip.isLoopbackAddress() || ip.isAnyLocalAddress()
                    || InetAddressUtil.contains(net, mask, ip)) {
                    userRoles.add(get("on_campus"));
                } else {
                    log.info(username + "@" + ip + " is not in the " + net
                        + "/" + mask + " network");
                }
            } else {
                log.info("No network address filter specified for " + portal);
            }
        } catch (UnknownHostException e) {
            log.warn(e);
        }
        for (Role role : getList().getRoles()) {
            if (role.getUserList().contains(username)) {
                userRoles.add(role);
            }
        }
        return userRoles;
    }

    public void save() {
        try {
            File rolesFile = new File(portalsDir, ROLES_XML);
            jaxbM.marshal(roles, rolesFile);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private void load() {
        try {
            File rolesFile = new File(portalsDir, ROLES_XML);
            roles = (RoleList) jaxbU.unmarshal(rolesFile);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
