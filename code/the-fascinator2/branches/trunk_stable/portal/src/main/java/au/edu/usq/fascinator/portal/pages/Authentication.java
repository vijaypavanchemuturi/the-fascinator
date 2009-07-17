/* 
 * The Fascinator - Solr Portal
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
package au.edu.usq.fascinator.portal.pages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ApplicationGlobals;

import au.edu.usq.fascinator.portal.AuthenticationData;
import au.edu.usq.fascinator.portal.State;

@IncludeStylesheet("context:css/default.css")
public class Authentication {
    private Logger log = Logger.getLogger(Authentication.class);

    @Persist
    private AuthenticationData authenticationData;

    @InjectPage
    private Authentication authPage;

    @SessionState
    private State state;

    @Persist
    private String message;

    @Inject
    private ApplicationGlobals globals;

    private String configFile;

    Object onSuccess() {
        log.info("LDAP Base URL: " + authenticationData.getLDAPBaseUrl());
        log.info("LDAP Base DN: " + authenticationData.getLDAPBaseDN());

        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(new File(configFile)));

            if (authenticationData.getLDAPBaseUrl() == null
                || authenticationData.getLDAPBaseDN() == null) {
                // invalid
                authenticationData.setLDAPBaseUrl("");
                authenticationData.setLDAPBaseDN("");
            }

            prop.put("ldap.base.url", authenticationData.getLDAPBaseUrl());
            prop.put("ldap.base.dn", authenticationData.getLDAPBaseDN());

            prop.store(new FileOutputStream(new File(configFile)), null);

        } catch (Exception e) {
            log.error(e);
        }

        return authPage;
    }

    Object onActivate(Object[] params) {
        if (!state.userInRole("admin")) {
            return Start.class;
        } else {

            if (configFile == null) {
                configFile = globals.getServletContext().getRealPath(
                    "WEB-INF/config.properties");
            }

            if (authenticationData == null) {

                log.info("Creating an authentication data object from a properties file");

                try {
                    Properties prop = new Properties();

                    prop.load(new FileInputStream(new File(configFile)));

                    String url = (String) prop.get("ldap.base.url");
                    String dn = (String) prop.get("ldap.base.dn");

                    if (url != null && dn != null) {
                        authenticationData = new AuthenticationData();

                        authenticationData.setLDAPBaseUrl(url);
                        authenticationData.setLDAPBaseDN(dn);
                    }

                } catch (Exception e) {
                    log.error(e);
                }
            }

        }
        return null;
    }

    public AuthenticationData getAuthenticationData() {
        return authenticationData;
    }

    public void setAuthenticationData(AuthenticationData authenticationData) {
        this.authenticationData = authenticationData;
    }
}
