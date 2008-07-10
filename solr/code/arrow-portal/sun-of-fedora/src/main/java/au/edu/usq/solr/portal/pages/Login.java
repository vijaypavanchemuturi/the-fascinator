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
package au.edu.usq.solr.portal.pages;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.Request;

import au.edu.usq.solr.portal.State;
import au.edu.usq.solr.util.LdapAuthentication;

@IncludeStylesheet("context:css/default.css")
public class Login {

    private static final String FAILED = "failed";

    private static final String RETRY = "retry";

    private Logger log = Logger.getLogger(Login.class);

    @Inject
    private Request request;

    @ApplicationState
    private State state;

    @InjectPage
    private Start startPage;

    private boolean cancelled;

    private boolean failed;

    private String username;

    private String password;

    @Persist
    private URL refererUrl;

    Object onActivate(Object[] params) {
        String referer = request.getHeader("Referer");
        if (!referer.endsWith("/login") && !referer.endsWith("/login/failed")
            && !referer.endsWith("/logout")) {
            try {
                refererUrl = new URL(referer);
            } catch (MalformedURLException mue) {
                refererUrl = null;
                log.warn("Bad referer: " + referer + " (" + mue.getMessage()
                    + ")");
            }
        }
        log.info("Referer: " + referer + " (" + refererUrl + ")");
        if (state.isLoggedIn()) {
            return refererUrl == null ? startPage : refererUrl;
        }
        failed = false;
        if (params.length > 0) {
            String param = params[0].toString();
            failed = FAILED.equals(param) || RETRY.equals(param);
        }
        return null;
    }

    Object onSuccess() {
        if (!cancelled) {
            if (login()) {
                state.login(username);
            } else {
                return null;
            }
        }
        return refererUrl;
    }

    String onPassivate() {
        return failed ? FAILED : RETRY;
    }

    void onSelectedFromLogin() {
        cancelled = false;
    }

    void onSelectedFromCancel() {
        cancelled = true;
    }

    private boolean login() {
        boolean result = false;
        if (username != null && password != null) {
            try {
                LdapAuthentication ldap = new LdapAuthentication(
                    state.getProperty("ldap.base.url"),
                    state.getProperty("ldap.base.dn"));
                result = ldap.authenticate(username, password);
            } catch (NamingException e) {
                log.error("Could not establish connection", e);
            }
        }
        return result;
    }

    public State getState() {
        return state;
    }

    public boolean getFailed() {
        return failed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRefererUrl(URL refererUrl) {
        this.refererUrl = refererUrl;
    }
}
