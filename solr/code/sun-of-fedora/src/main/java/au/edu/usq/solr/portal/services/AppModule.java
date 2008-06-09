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

import java.util.Map;

import org.apache.tapestry.contrib.services.TemplateService;
import org.apache.tapestry.internal.services.ContextResource;
import org.apache.tapestry.internal.services.RequestPathOptimizer;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.services.ApplicationStateContribution;
import org.apache.tapestry.services.ApplicationStateCreator;
import org.apache.tapestry.services.AssetFactory;
import org.apache.tapestry.services.AssetSource;
import org.apache.tapestry.services.Context;
import org.apache.tapestry.services.Request;

import au.edu.usq.solr.portal.State;

public class AppModule {

    private static final String PORTALS_DIR_KEY = "portals.dir";

    public PortalManager buildPortalManager(Map<String, Resource> configuration) {
        return new PortalManagerImpl(configuration.get(PORTALS_DIR_KEY));
    }

    public void contributePortalManager(Context context,
        MappedConfiguration<String, Resource> configuration) {
        Resource configProps = new ContextResource(context,
            "/WEB-INF/config.properties");
        configuration.add(PORTALS_DIR_KEY, configProps);
    }

    public VelocityResourceLocator buildVelocityResourceLocator(
        @InjectService("VelocityService")
        TemplateService templateService, @InjectService("AssetSource")
        AssetSource assetSource) {
        return new VelocityResourceLocator(templateService, assetSource,
            PortalManagerImpl.DEFAULT_PORTAL_NAME);
    }

    public void contributeApplicationStateManager(
        MappedConfiguration<Class<State>, ApplicationStateContribution> configuration,
        @InjectService("Context")
        final Context context) {
        ApplicationStateCreator<State> creator = new ApplicationStateCreator<State>() {
            public State create() {
                return new State(context);
            }
        };
        configuration.add(State.class, new ApplicationStateContribution(
            "session", creator));
    }

    public void contributeVelocityService(
        MappedConfiguration<String, Resource> configuration, Context context) {
        Resource velocityProps = new ContextResource(context,
            "/WEB-INF/velocity.properties");
        configuration.add("velocity.configuration", velocityProps);
    }

    public AssetFactory buildVelocityAssetFactory(Request request,
        Context context, RequestPathOptimizer optimizer) {
        return new VelocityAssetFactory(request, context, optimizer);
    }

    public void contributeAssetSource(
        MappedConfiguration<String, AssetFactory> configuration,
        @InjectService("VelocityAssetFactory")
        AssetFactory velocityAssetFactory) {
        configuration.add("velocity", velocityAssetFactory);
    }
}
