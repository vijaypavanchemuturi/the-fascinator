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
package au.edu.usq.solr.harvest.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import au.edu.usq.solr.harvest.Harvester;
import au.edu.usq.solr.harvest.HarvesterException;
import au.edu.usq.solr.harvest.Registry;
import au.edu.usq.solr.harvest.fedora.FedoraRestClient;
import au.edu.usq.solr.harvest.fedora.types.DatastreamType;
import au.edu.usq.solr.harvest.fedora.types.ObjectDatastreamsType;
import au.edu.usq.solr.harvest.fedora.types.ObjectFieldType;
import au.edu.usq.solr.harvest.fedora.types.ResultType;

public class FedoraRestHarvester implements Harvester {

    private String solrUpdateUrl;

    private Registry registry;

    private int limit;

    private FedoraRestClient client;

    public FedoraRestHarvester(String solrUpdateUrl, Registry registry,
        int limit) {
        this.solrUpdateUrl = solrUpdateUrl;
        this.registry = registry;
        this.limit = limit;
    }

    public void setAuthentication(String username, String password) {
        // TODO
    }

    public void harvest(String name, String url) throws HarvesterException {
        client = new FedoraRestClient(url);
        ResultType results = client.findObjects("*", 25);
        for (ObjectFieldType object : results.getObjectFields()) {
            String pid = object.getPid();
            processObject(name, pid);
        }
    }

    private void processObject(String name, String pid) {
        ObjectDatastreamsType dsList = client.listDatastreams(pid);
        for (DatastreamType ds : dsList.getDatastreams()) {
            try {
                String dsId = ds.getDsid();
                if ("DC".equals(dsId)) {

                }

                File dsFile = new File("/tmp/" + name + "/" + pid + "/" + dsId);
                dsFile.getParentFile().mkdirs();
                OutputStream out = new FileOutputStream(dsFile);
                client.get(pid, dsId, out);
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 8) {
            System.out.println("Usage: "
                + FedoraRestHarvester.class.getCanonicalName()
                + " <solrUpdateUrl> "
                + "<registryUrl> <registryUser> <registryPassword> "
                + "<repBaseUrl> <repUser> <repPassword> <repName> "
                + "[requestLimit]");
        } else {
            try {
                String solrUpdateUrl = args[0];
                String regUrl = args[1];
                String regUser = args[2];
                String regPass = args[3];
                String repUrl = args[4];
                String repUser = args[5];
                String repPass = args[6];
                String repName = args[7];
                int limit = Integer.MAX_VALUE;
                if (args.length > 8) {
                    limit = Integer.parseInt(args[8]);
                }
                Registry registry = new Fedora30Registry(regUrl, regUser,
                    regPass);
                Harvester harvester = new FedoraRestHarvester(solrUpdateUrl,
                    registry, limit);
                harvester.setAuthentication(repUser, repPass);
                harvester.harvest(repName, repUrl);
            } catch (Exception e) {
                System.err.println("Failed to harvest");
                e.printStackTrace();
            }
        }
    }
}
