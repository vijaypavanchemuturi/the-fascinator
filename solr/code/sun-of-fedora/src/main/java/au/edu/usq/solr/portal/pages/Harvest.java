/* 
 * Sun of Fedora - Solr Portal
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
package au.edu.usq.solr.portal.pages;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.apache.tapestry.Asset;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.ioc.annotations.Inject;

import au.edu.usq.solr.Configuration;
import au.edu.usq.solr.harvest.Fedora30Registry;
import au.edu.usq.solr.harvest.Harvester;
import au.edu.usq.solr.harvest.OaiPmhHarvester;
import au.edu.usq.solr.harvest.Registry;
import au.edu.usq.solr.harvest.VitalFedoraHarvester;

public class Harvest {

    public enum RepositoryType {
        OAI_PMH, VITAL_FEDORA
    };

    private Logger log = Logger.getLogger(Harvest.class);

    @Inject
    @Path(value = "context:css/default.css")
    private Asset stylesheet;

    @ApplicationState
    private Configuration config;

    private String registryPassword;

    private RepositoryType reposType;

    private String reposName;

    private String reposUrl;

    private String reposUser;

    private String reposPassword;

    private int limit;

    void onSubmit() {
        log.info("Start harvesting");
        try {
            String solrUpdateUrl = config.getSolrBaseUrl() + "/update";
            Registry registry = new Fedora30Registry(
                config.getRegistryBaseUrl(), config.getRegistryUser(),
                registryPassword);
            Harvester harvester = null;
            if (reposType == RepositoryType.OAI_PMH) {
                harvester = new OaiPmhHarvester(solrUpdateUrl, registry, limit);
            } else if (reposType == RepositoryType.VITAL_FEDORA) {
                harvester = new VitalFedoraHarvester(solrUpdateUrl, registry,
                    limit);
            }
            if (harvester != null) {
                harvester.setAuthentication(reposUser, reposPassword);
                harvester.harvest(reposName, reposUrl);
            }
        } catch (MalformedURLException e) {
            log.error("Invalid Solr URL", e);
        } catch (Exception e) {
            log.error("Failed to harvest", e);
        }
    }

    public Asset getStylesheet() {
        return stylesheet;
    }

    public void setStylesheet(Asset stylesheet) {
        this.stylesheet = stylesheet;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public String getRegistryPassword() {
        return registryPassword;
    }

    public void setRegistryPassword(String registryPassword) {
        this.registryPassword = registryPassword;
    }

    public RepositoryType getReposType() {
        return reposType;
    }

    public void setReposType(RepositoryType reposType) {
        this.reposType = reposType;
    }

    public String getReposName() {
        return reposName;
    }

    public void setReposName(String reposName) {
        this.reposName = reposName;
    }

    public String getReposUrl() {
        return reposUrl;
    }

    public void setReposUrl(String reposUrl) {
        this.reposUrl = reposUrl;
    }

    public String getReposUser() {
        return reposUser;
    }

    public void setReposUser(String reposUser) {
        this.reposUser = reposUser;
    }

    public String getReposPassword() {
        return reposPassword;
    }

    public void setReposPassword(String reposPassword) {
        this.reposPassword = reposPassword;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
