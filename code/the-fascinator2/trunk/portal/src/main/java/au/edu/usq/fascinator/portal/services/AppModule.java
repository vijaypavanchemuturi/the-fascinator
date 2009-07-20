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
package au.edu.usq.fascinator.portal.services;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.internal.services.ContextResource;
import org.apache.tapestry5.internal.services.RequestPathOptimizer;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.services.ApplicationStateContribution;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.Context;
import org.apache.tapestry5.services.Request;

import au.edu.usq.fascinator.portal.State;

public class AppModule {

    public static final String PORTALS_DIR_KEY = "portals.dir";

    public static final String REGISTRY_URL_KEY = "registry.url";

    public static final String REGISTRY_USER_KEY = "registry.user";

    public static final String REGISTRY_PASS_KEY = "registry.pass";

    private static final String VELOCITY_CONFIG_KEY = "velocity.configuration";

    public void contributeApplicationStateManager(
        MappedConfiguration<Class<State>, ApplicationStateContribution> configuration,
        @InjectService("Context") final Context context,
        @InjectService("PortalManager") final PortalManager portalManager,
        @InjectService("RoleManager") final RoleManager roleManager) {
        ApplicationStateCreator<State> creator = new ApplicationStateCreator<State>() {
            public State create() {
                return new State(context, portalManager, roleManager);
            }
        };
        configuration.add(State.class, new ApplicationStateContribution(
            "session", creator));
    }

    public PortalManager buildPortalManager() {
        return new PortalManagerImpl();
    }

    // public RegistryManager buildRegistryManager(
    // Map<String, Resource> configuration) {
    // return new RegistryManagerImpl(configuration.get(REGISTRY_URL_KEY));
    // }

    // public void contributeRegistryManager(Context context,
    // MappedConfiguration<String, Resource> configuration) {
    // Resource configProps = new ContextResource(context,
    // "/WEB-INF/config.properties");
    // configuration.add(REGISTRY_URL_KEY, configProps);
    // }

    public RoleManager buildRoleManager(HttpServletRequest request) {
        return new RoleManagerImpl(request);
    }

    // public void contributeRoleManager(Context context,
    // MappedConfiguration<String, Resource> configuration) {
    // Resource configProps = new ContextResource(context,
    // "/WEB-INF/config.properties");
    // configuration.add(PORTALS_DIR_KEY, configProps);
    // }

    public VelocityResourceLocator buildVelocityResourceLocator(
        @InjectService("VelocityService") VelocityService velocityService,
        @InjectService("AssetSource") AssetSource assetSource) {
        return new VelocityResourceLocator(velocityService, assetSource,
            PortalManagerImpl.DEFAULT_PORTAL_NAME);
    }

    public VelocityService buildVelocityService(
        Map<String, Resource> configuration) {
        return new VelocityServiceImpl(configuration.get(VELOCITY_CONFIG_KEY));
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
        @InjectService("VelocityAssetFactory") AssetFactory velocityAssetFactory) {
        configuration.add("velocity", velocityAssetFactory);
    }

    public UserManager buildUserManager(Map<String, Resource> configuration) {
        return new UserManagerImpl(configuration.get(PORTALS_DIR_KEY));
    }

    public void contributeUserManager(Context context,
        MappedConfiguration<String, Resource> configuration) {

        Resource configProps = new ContextResource(context,
            "/WEB-INF/config.properties");
        configuration.add(PORTALS_DIR_KEY, configProps);
    }

}
