package au.edu.usq.fascinator.portal.services;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.AbstractResource;

public class UriResource extends AbstractResource {

    private URI uri;

    public UriResource(String path) {
        super(path);
        try {
            uri = new URI(path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public UriResource(URI uri) throws MalformedURLException {
        this(uri.toURL().toString());
    }

    public UriResource(File file) throws MalformedURLException {
        this(file.toURI());
    }

    public UriResource(URL url) throws URISyntaxException,
        MalformedURLException {
        this(url.toURI());
    }

    /**
     * Factory method provided by subclasses.
     */
    @Override
    protected Resource newResource(String path) {
        return new UriResource(path);
    }

    /**
     * Returns the URL for the resource, or null if it does not exist.
     */
    public URL toURL() {
        try {
            if (uri.toURL().openConnection().getContentLength() > 0)
                return uri.toURL();
        } catch (Exception e) {
            // do nothing
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj == this)
            return true;

        if (obj.getClass() != getClass())
            return false;

        UriResource other = (UriResource) obj;

        return other.getPath().equals(getPath());
    }

    @Override
    public int hashCode() {
        return 227 ^ getPath().hashCode();
    }

    @Override
    public String toString() {
        return String.format("uri:%s", getPath());
    }
}
