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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.internal.services.RequestPathOptimizer;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.Context;
import org.apache.tapestry5.services.Request;

public class VelocityAssetFactory implements AssetFactory {

    private Request request;

    private Context context;

    private RequestPathOptimizer optimizer;

    public VelocityAssetFactory(Request request, Context context,
        RequestPathOptimizer optimizer) {
        this.request = request;
        this.context = context;
        this.optimizer = optimizer;
    }

    public Asset createAsset(final Resource resource) {
        final String contextPath = request.getContextPath()
            + "/getvelocityresource/" + resource.getPath();
        Asset asset = new Asset() {
            public Resource getResource() {
                return resource;
            }

            public String toClientURL() {
                return optimizer.optimizePath(contextPath);
            }

            @Override
            public String toString() {
                return toClientURL();
            }
        };
        return asset;
    }

    public Resource getRootResource() {
        return new VelocityResource(context, "/");
    }
}
