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

import org.apache.log4j.Logger;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.Request;

import au.edu.usq.solr.portal.State;

@IncludeStylesheet("context:css/default.css")
public class Logout {

    private Logger log = Logger.getLogger(Logout.class);

    @ApplicationState
    private State state;

    @InjectPage
    private Login loginPage;

    @Inject
    private Request request;

    @Persist
    private URL refererUrl;

    void onActivate() {
        String referer = request.getHeader("Referer");
        if (!referer.endsWith("/login") && !referer.endsWith("/logout")) {
            try {
                refererUrl = new URL(referer);
            } catch (MalformedURLException mue) {
                refererUrl = null;
                log.warn("Bad referer: " + referer + " (" + mue.getMessage()
                    + ")");
            }
        }
        log.info("Referer: " + referer + " (" + refererUrl + ")");
        loginPage.setRefererUrl(null);
        state.logout();
    }

    public State getState() {
        return state;
    }

    public URL getRefererUrl() {
        return refererUrl;
    }
}
