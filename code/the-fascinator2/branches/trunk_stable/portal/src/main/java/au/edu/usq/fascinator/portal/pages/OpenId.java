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

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;

import au.edu.usq.fascinator.portal.State;
import au.edu.usq.fascinator.util.OpenIdAuthentication;

public class OpenId {

    private Logger log = Logger.getLogger(OpenId.class);

    @Inject
    private Request request;

    @SessionState
    private State state;

    @InjectPage
    private Login loginPage;

    @InjectPage
    private Start startPage;

    @Inject
    private RequestGlobals requestGlobals;

    Object onActivate(Object[] params) {
        String openId = request.getParameter("openid.identity");
        for (String name : request.getParameterNames()) {
            log.debug("***** " + name + ": " + request.getParameter(name));
        }
        OpenIdAuthentication oid = new OpenIdAuthentication();
        try {
            boolean result = oid.verify(requestGlobals.getHTTPServletRequest());
            if (result) {
                state.login(openId);
            } else {
                loginPage.setFailed(true);
                return loginPage;
            }
        } catch (Exception e) {
            loginPage.setFailed(true);
            return loginPage;
        }
        URL refererUrl = loginPage.getRefererUrl();
        if (refererUrl == null) {
            return startPage;
        }
        log.debug("REDIRECTING to: " + refererUrl);
        return refererUrl;
    }

}
