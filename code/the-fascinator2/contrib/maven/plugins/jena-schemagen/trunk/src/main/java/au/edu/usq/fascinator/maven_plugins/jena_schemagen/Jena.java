/*
 * Copyright (C) 2010 University of Southern Queensland
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
package au.edu.usq.fascinator.maven_plugins.jena_schemagen;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author dickinso
 * @see http://jena.sourceforge.net/how-to/schemagen.html
 * @goal jena
 */
public class Jena extends SchemaGenerator {

    /**
     * @param args
     */
    public static void main(String[] args) {
        jena.schemagen.main(args);

    }

    /* (non-Javadoc)
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        File outFolder = new File(getOutputFolder().getPath() + File.separator
                + getPackageName().replace('.', File.separatorChar));
        outFolder.mkdirs();

        getLog().info("Output: " + outFolder.getPath());

        for (String item : getSchema().keySet()) {
            getLog().info("Loading: " + item + " - " + getSchema().get(item));
            String[] args = { "-i", getSchema().get(item), "-o",
                    outFolder.getPath(), "--package", getPackageName(), "-n",
                    item };
            main(args);
        }
    }

}
