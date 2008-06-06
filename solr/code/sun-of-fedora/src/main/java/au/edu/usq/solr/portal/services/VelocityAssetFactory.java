package au.edu.usq.solr.portal.services;

import org.apache.tapestry.Asset;
import org.apache.tapestry.internal.services.RequestPathOptimizer;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.services.AssetFactory;
import org.apache.tapestry.services.Context;
import org.apache.tapestry.services.Request;

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
