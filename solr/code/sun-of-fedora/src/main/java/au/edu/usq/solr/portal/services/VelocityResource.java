package au.edu.usq.solr.portal.services;

import java.net.URL;
import java.util.Locale;

import org.apache.commons.lang.NotImplementedException;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.services.Context;

public class VelocityResource implements Resource {

    private String path;

    private Context context;

    public VelocityResource(Context context, String path) {
        this.path = path;
        this.context = context;
    }

    public Resource forFile(String relativePath) {
        // TODO
        return new VelocityResource(context, relativePath);
    }

    public Resource forLocale(Locale locale) {
        // TODO
        return new VelocityResource(context, path);
    }

    public String getFile() {
        throw new NotImplementedException();
    }

    public String getFolder() {
        throw new NotImplementedException();
    }

    public String getPath() {
        return path;
    }

    public URL toURL() {
        throw new NotImplementedException();
    }

    public Resource withExtension(String extension) {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return String.format("velocity:%s", path);
    }
}
