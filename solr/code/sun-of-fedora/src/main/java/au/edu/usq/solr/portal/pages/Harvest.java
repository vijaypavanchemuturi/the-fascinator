package au.edu.usq.solr.portal.pages;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.apache.tapestry.Asset;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.ioc.annotations.Inject;

import au.edu.usq.solr.Configuration;
import au.edu.usq.solr.util.Harvester;

public class Harvest {

    private Logger log = Logger.getLogger(Harvest.class);

    @Inject
    @Path(value = "context:css/default.css")
    private Asset stylesheet;

    @ApplicationState
    private Configuration config;

    private String reposUrl;

    private String reposName;

    private int limit;

    void onSubmit() {
        log.info("Start harvesting");
        try {
            Harvester harvester = new Harvester(config.getSolrBaseUrl()
                    + "/update");
            harvester.harvest(reposUrl, reposName, limit);
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

    public String getReposUrl() {
        return reposUrl;
    }

    public void setReposUrl(String reposUrl) {
        this.reposUrl = reposUrl;
    }

    public String getReposName() {
        return reposName;
    }

    public void setReposName(String reposName) {
        this.reposName = reposName;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
