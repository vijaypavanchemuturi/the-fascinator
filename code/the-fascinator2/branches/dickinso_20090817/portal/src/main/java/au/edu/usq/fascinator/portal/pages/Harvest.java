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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.SessionState;

import au.edu.usq.fascinator.portal.HarvestData;
import au.edu.usq.fascinator.portal.State;

@IncludeStylesheet("context:css/default.css")
public class Harvest {
    private Logger log = Logger.getLogger(Harvest.class);

    private HarvestData harvestData;

    @InjectPage
    private Harvest harvestPage;

    @SessionState
    private State state;

    @Persist
    private String message;

    Object onSuccess() {
        log.info("harvest location: " + harvestData.getLocation());

        String propFile = System.getenv("FASCINATOR_HOME")
            + "/harvest/config/local-files.properties";

        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(propFile + ".sample"));
            prop.put("repository.url", harvestData.getLocation());
            prop.store(new FileOutputStream(propFile), null);

            // au.edu.usq.solr.harvest.Harvest.main(new String[] { propFile,
            // "-all" });

            message = "";
        } catch (Exception e) {
            message = "failed";
        }

        return harvestPage;
    }

    Object onActivate(Object[] params) {
        if (!state.userInRole("admin")) {
            return Start.class;
        }
        return null;
    }

    void afterRender() {
        message = "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public HarvestData getHarvestData() {
        return harvestData;
    }

    public void setHarvestData(HarvestData harvestData) {
        this.harvestData = harvestData;
    }
}
