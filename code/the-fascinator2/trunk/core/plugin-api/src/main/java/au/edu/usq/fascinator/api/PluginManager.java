/* 
 * The Fascinator - Plugin API
 * Copyright (C) 2009 University of Southern Queensland
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
package au.edu.usq.fascinator.api;

import java.util.ServiceLoader;

import au.edu.usq.fascinator.api.store.DataSource;
import au.edu.usq.fascinator.api.store.DataSourceException;
import au.edu.usq.fascinator.api.store.Storage;
import au.edu.usq.fascinator.api.store.StorageException;

public class PluginManager {

    public static Storage getStorage(String id) throws StorageException {
        ServiceLoader<Storage> plugins = ServiceLoader.load(Storage.class);
        for (Storage plugin : plugins) {
            if (id.equals(plugin.getId())) {
                return plugin;
            }
        }
        return null;
    }

    public static DataSource getDataSource(String id)
            throws DataSourceException {
        ServiceLoader<DataSource> plugins = ServiceLoader
                .load(DataSource.class);
        for (DataSource plugin : plugins) {
            if (id.equals(plugin.getId())) {
                return plugin;
            }
        }
        return null;
    }

}
