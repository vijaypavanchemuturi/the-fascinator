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

import org.apache.log4j.Logger;
import org.apache.tapestry.Asset;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.BeanModelSource;

import au.edu.usq.solr.Configuration;
import au.edu.usq.solr.portal.Portal;

public class Index {

    private Logger log = Logger.getLogger(Index.class);

    @Inject
    @Path(value = "context:css/default.css")
    private Asset stylesheet;

    @ApplicationState
    private Configuration config;

    private Portal portal;

    @Inject
    private ComponentResources resources;

    @Inject
    private BeanModelSource beanModelSource;

    private BeanModel<Portal> model;

    public Asset getStylesheet() {
        return stylesheet;
    }

    public void setStylesheet(Asset stylesheet) {
        this.stylesheet = stylesheet;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public Portal getPortal() {
        return portal;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public boolean isEditable() {
        return !portal.equals(config.getPortalManager().getDefaultPortal());
    }

    public BeanModel<Portal> getModel() {
        if (model == null) {
            model = beanModelSource.create(Portal.class, true, resources);
            model.add("edit", null);
        }
        return model;
    }

    public void setModel(BeanModel<Portal> model) {
        this.model = model;
    }
}
