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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.lang.NotImplementedException;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.Context;

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

    @Override
    public boolean exists() {
        System.err.println(" *** VelocityResource.exists");
        return true;
    }

    @Override
    public InputStream openStream() throws IOException {
        System.err.println(" *** VelocityResource.openStream");
        return null;
    }
}
