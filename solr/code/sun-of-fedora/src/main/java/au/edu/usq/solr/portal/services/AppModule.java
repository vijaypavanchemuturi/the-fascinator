package au.edu.usq.solr.portal.services;

import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.services.ApplicationStateContribution;
import org.apache.tapestry.services.ApplicationStateCreator;

import au.edu.usq.solr.Configuration;
import au.edu.usq.solr.PropertiesConfiguration;

public class AppModule {

    public void contributeApplicationStateManager(
            MappedConfiguration<Class<Configuration>, ApplicationStateContribution> config) {
        ApplicationStateCreator<Configuration> creator = new ApplicationStateCreator<Configuration>() {
            public Configuration create() {
                return new PropertiesConfiguration();
            }
        };
        config.add(Configuration.class, new ApplicationStateContribution(
                "session", creator));
    }

}
