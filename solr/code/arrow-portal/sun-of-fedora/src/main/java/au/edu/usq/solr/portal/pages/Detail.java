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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.ioc.annotations.Inject;

import au.edu.usq.solr.portal.State;
import au.edu.usq.solr.portal.services.RegistryManager;
import au.edu.usq.solr.portal.services.VelocityResourceLocator;

@IncludeStylesheet("context:css/default.css")
public class Detail {

    @ApplicationState
    private State state;

    @Inject
    private VelocityResourceLocator locator;

    @Inject
    private RegistryManager registryManager;

    private String uuid;

    private String item;

    Object onActivate(Object[] params) {
        if (params.length > 0) {
            try {
                uuid = URLDecoder.decode(params[0].toString(), "UTF-8");
                return null;
            } catch (UnsupportedEncodingException e) {
                return Start.class;
            }
        }
        return Start.class;
    }

    String onPassivate() {
        return uuid;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getStylesheet() {
        return locator.getLocation(state.getPortalName(), "style.css");
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getItem() {
        if (item == null) {
            item = registryManager.getMetadata(uuid, state.getPortalName());
        }
        return item;
    }
}
