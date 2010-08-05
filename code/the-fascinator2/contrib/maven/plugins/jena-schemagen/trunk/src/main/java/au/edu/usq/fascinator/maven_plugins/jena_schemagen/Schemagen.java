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
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author dickinso
 * @see http://jena.sourceforge.net/how-to/schemagen.html
 * @goal schemagen
 */
public class Schemagen extends AbstractMojo {
    /**
     * The file or URI of the schema
     * 
     * @parameter
     * @required
     */
    private Map<String, String> schema;

    /**
     * The file or directory for the output
     * 
     * @parameter expression="${project.build.directory}/schemagen"
     */
    private File outputFolder;

    /**
     * The Java package name
     * 
     * @parameter expression=""
     */
    private String packageName;

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
        File outFolder = new File(outputFolder.getPath() + File.separator
                + packageName.replace('.', File.separatorChar));
        outFolder.mkdirs();

        getLog().info("Output: " + outFolder.getPath());

        for (String item : schema.keySet()) {
            getLog().info("Loading: " + item + " - " + schema.get(item));
            String[] args = { "-i", schema.get(item), "-o",
                    outFolder.getPath(), "--package", packageName, "-n", item };
            jena.schemagen.main(args);
        }
    }
}
