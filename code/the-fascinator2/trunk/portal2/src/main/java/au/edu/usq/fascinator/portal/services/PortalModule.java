/* 
 * The Fascinator - Portal
 * Copyright (C) 2008-2009 University of Southern Queensland
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

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.services.ApplicationStateContribution;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.urlrewriter.RewriteRuleApplicability;
import org.apache.tapestry5.urlrewriter.SimpleRequestWrapper;
import org.apache.tapestry5.urlrewriter.URLRewriteContext;
import org.apache.tapestry5.urlrewriter.URLRewriterRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.portal.JsonSessionState;
import au.edu.usq.fascinator.portal.services.impl.HarvestManagerImpl;
import au.edu.usq.fascinator.portal.services.impl.DynamicPageServiceImpl;
import au.edu.usq.fascinator.portal.services.impl.PortalManagerImpl;
import au.edu.usq.fascinator.portal.services.impl.ScriptingServicesImpl;

public class PortalModule {

    private static final String DEFAULT_INDEXER_TYPE = "solr";

    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(PortalModule.class);

    public static void bind(ServiceBinder binder) {
        binder.bind(HarvestManager.class, HarvestManagerImpl.class);
        binder.bind(DynamicPageService.class, DynamicPageServiceImpl.class);
        binder.bind(PortalManager.class, PortalManagerImpl.class);
        binder.bind(ScriptingServices.class, ScriptingServicesImpl.class);
    }

    public static Indexer buildIndexer() {
        try {
            JsonConfig config = new JsonConfig();
            Indexer indexer = PluginManager.getIndexer(config.get(
                    "indexer/type", DEFAULT_INDEXER_TYPE));
            indexer.init(config.getSystemFile());
            return indexer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void contributeApplicationStateManager(
            MappedConfiguration<Class<?>, ApplicationStateContribution> configuration) {
        ApplicationStateCreator<JsonSessionState> creator = new ApplicationStateCreator<JsonSessionState>() {
            public JsonSessionState create() {
                return new JsonSessionState();
            }
        };
        ApplicationStateContribution contribution = new ApplicationStateContribution(
                "session", creator);
        configuration.add(JsonSessionState.class, contribution);
    }

    public static void contributeURLRewriter(
            OrderedConfiguration<URLRewriterRule> configuration) {
        URLRewriterRule rule = new URLRewriterRule() {
            public Request process(Request request, URLRewriteContext context) {
                String path = request.getPath();
                String[] parts = path.substring(1).split("/");
                if (parts.length > 0) {
                    String start = parts[0];
                    if (!"assets".equals(start) && !"dispatch".equals(start)) {
                        path = "/dispatch" + path;
                    }
                } else {
                    path = "/dispatch";
                }
                return new SimpleRequestWrapper(request, path);
            }

            public RewriteRuleApplicability applicability() {
                return RewriteRuleApplicability.INBOUND;
            }
        };
        configuration.add("dispatch", rule);
    }
}
