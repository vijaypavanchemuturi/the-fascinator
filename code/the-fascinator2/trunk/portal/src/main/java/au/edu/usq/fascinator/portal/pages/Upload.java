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
package au.edu.usq.fascinator.portal.pages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.upload.services.UploadedFile;

import au.edu.usq.fascinator.portal.State;

@IncludeStylesheet("context:css/default.css")
public class Upload {

    private Logger log = Logger.getLogger(Upload.class);

    @Property
    private UploadedFile file;

    @Property
    private String url;

    @Property
    private String metaPrefix;

    @InjectPage
    private Search searchPage;

    @SessionState
    private State state;

    private String fascinatorHome;

    public Upload() {
        fascinatorHome = System.getenv("FASCINATOR_HOME");
        if (fascinatorHome == null) {
            fascinatorHome = "/opt/the-fascinator";
        }
    }

    Search onSubmitFromUploadForm() {
        log.info("-- file upload fileName='" + file.getFileName() + "'");
        try {
            File propsFile = new File(fascinatorHome,
                "/harvest/config/der.properties");
            File tmpFile = File.createTempFile("uploadTmpFile", null);
            file.write(tmpFile);
            log.info("-- written to tmpFile '" + tmpFile.getPath() + "' ok");
            String[] args = new String[2];
            args[0] = propsFile.getAbsolutePath();
            args[1] = tmpFile.getPath();
            // Harvest.main(args);
            tmpFile.delete();
        } catch (IOException ex) {
            log.warn("Upload io error - " + ex.getMessage());
        } catch (Exception ex) {
            log.warn("Upload error - " + ex.getMessage());
        }

        try {
            Thread.currentThread();
            Thread.sleep(1000);// sleep for 1000 ms
        } catch (InterruptedException ie) {

        }

        searchPage.setPortalName(this.state.getPortalName());
        return searchPage;
    }

    Search onSubmitFromHarvestForm() {
        log.info("-- OAI-PMH harvest from " + url);
        try {
            File iloxFile = new File(fascinatorHome,
                "/harvest/config/oai-pmh/ilox.properties");
            Properties props = new Properties();
            props.load(new FileReader(iloxFile));
            props.setProperty("repository.url", url);
            props.setProperty("oai.pmh.metadataPrefix", metaPrefix);
            props.setProperty("params.metadataPrefix", metaPrefix);
            log.info("metaPrefix in upload.java:" + metaPrefix);
            log.info("iloxFile parent path:" + iloxFile.getParent()
                + "/tmpIlox");
            // File tmpFile = File.createTempFile("/tmpIlox", ".properties");
            File tmpFile = new File(iloxFile.getParent()
                + "/tmpIlox.properties");
            OutputStream out = new FileOutputStream(tmpFile);
            props.store(out, "");
            out.close();
            log.info("-- written to tmpFile '" + tmpFile.getPath() + "' ok");
            String[] args = new String[] { tmpFile.getAbsolutePath(), "-all" };
            log.info(args);
            // Harvest.main(args);
            tmpFile.delete();
        } catch (IOException ioe) {
            log.warn("Harvest io error - " + ioe.getMessage());
            ioe.printStackTrace();
        } catch (Exception ex) {
            log.warn("Harvest error - " + ex.getMessage());
            ex.printStackTrace();
        }

        try {
            Thread.currentThread();
            Thread.sleep(1000);// sleep for 1000 ms
        } catch (InterruptedException ie) {

        }

        searchPage.setPortalName(this.state.getPortalName());
        return searchPage;
    }
}
