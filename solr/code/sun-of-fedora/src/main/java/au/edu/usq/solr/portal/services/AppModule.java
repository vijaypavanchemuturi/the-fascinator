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
package au.edu.usq.solr.portal.services;

import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.services.ApplicationStateContribution;
import org.apache.tapestry.services.ApplicationStateCreator;

import au.edu.usq.solr.Configuration;
import au.edu.usq.solr.DefaultConfiguration;

public class AppModule {

    public void contributeApplicationStateManager(
        MappedConfiguration<Class<Configuration>, ApplicationStateContribution> config) {
        ApplicationStateCreator<Configuration> creator = new ApplicationStateCreator<Configuration>() {
            public Configuration create() {
                return new DefaultConfiguration();
            }
        };
        config.add(Configuration.class, new ApplicationStateContribution(
            "session", creator));
    }

}
