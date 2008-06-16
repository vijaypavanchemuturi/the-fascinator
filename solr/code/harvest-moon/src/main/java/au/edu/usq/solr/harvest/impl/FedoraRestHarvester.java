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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.util.SimplePostTool;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

import au.edu.usq.solr.harvest.Harvester;
import au.edu.usq.solr.harvest.HarvesterException;
import au.edu.usq.solr.harvest.Registry;
import au.edu.usq.solr.harvest.fedora.FedoraRestClient;
import au.edu.usq.solr.harvest.fedora.types.DatastreamType;
import au.edu.usq.solr.harvest.fedora.types.ListSessionType;
import au.edu.usq.solr.harvest.fedora.types.ObjectDatastreamsType;
import au.edu.usq.solr.harvest.fedora.types.ObjectFieldType;
import au.edu.usq.solr.harvest.fedora.types.ResultType;
import au.edu.usq.solr.harvest.filter.DatastreamFilter;
import au.edu.usq.solr.harvest.filter.FilterException;
import au.edu.usq.solr.harvest.filter.FilterManager;
import au.edu.usq.solr.harvest.filter.SolrFilter;
import au.edu.usq.solr.harvest.filter.impl.AddFieldFilter;
import au.edu.usq.solr.harvest.filter.impl.DsIdDatastreamFilter;
import au.edu.usq.solr.harvest.filter.impl.StylesheetFilter;
import au.edu.usq.solr.util.NullWriter;
import au.edu.usq.solr.util.StreamUtils;

public class FedoraRestHarvester implements Harvester {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private Logger log = Logger.getLogger(FedoraRestHarvester.class);

    private Registry registry;

    private int limit;

    private FedoraRestClient client;

    private FilterManager filterManager;

    private AddFieldFilter addIdFilter;

    private AddFieldFilter addFullTextFilter;

    private SimplePostTool postTool;

    private DatastreamFilter dsFilter;

    public FedoraRestHarvester(String solrUpdateUrl, Registry registry,
        DatastreamFilter dsFilter, int limit) throws MalformedURLException {
        this.registry = registry;
        this.dsFilter = dsFilter;
        this.limit = limit;
        postTool = new SimplePostTool(new URL(solrUpdateUrl));
    }

    public void setAuthentication(String username, String password) {
        // TODO
    }

    public void harvest(String name, String url) throws HarvesterException {
        try {
            SolrFilter dcToSolr = new StylesheetFilter(
                getClass().getResourceAsStream("/xsl/dc_solr.xsl"));
            dcToSolr.setName("Dublin Core To Solr");

            addIdFilter = new AddFieldFilter("id");
            addFullTextFilter = new AddFieldFilter("full_text");

            filterManager = new FilterManager();
            filterManager.setWorkDir(new File(TMP_DIR, name));
            filterManager.addFilter(dcToSolr);
            filterManager.addFilter(new AddFieldFilter("repository_name", name));
            filterManager.addFilter(addIdFilter);
            filterManager.addFilter(addFullTextFilter);

            client = new FedoraRestClient(url);
            ResultType results = client.findObjects("*", 25);
            String token = null;
            int count = 0;
            do {
                count++;
                if (!results.getObjectFields().isEmpty()) {
                    for (ObjectFieldType object : results.getObjectFields()) {
                        String pid = object.getPid();
                        processObject(name, pid);
                    }
                    postTool.commit(NullWriter.getInstance());
                }
                ListSessionType listSession = results.getListSession();
                if (listSession != null) {
                    token = listSession.getToken();
                    results = client.resumeFindObjects(token);
                }
            } while (token != null && count < limit);
        } catch (FilterException fe) {
            throw new HarvesterException(fe);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        log.info("Harvest completed.");
    }

    private void processObject(String name, String pid) {
        log.info("Processing " + pid + "...");
        List<File> fullTextFiles = new ArrayList<File>();
        InputStream dcIn = null;
        OutputStream solrOut = null;
        ObjectDatastreamsType dsList = client.listDatastreams(pid);
        for (DatastreamType ds : dsList.getDatastreams()) {
            try {
                String dsId = ds.getDsid();
                File dsFile = new File(TMP_DIR, name + "/" + pid + "/" + dsId);
                dsFile.getParentFile().mkdirs();
                OutputStream out = new FileOutputStream(dsFile);
                client.get(pid, dsId, out);
                out.close();
                if ("DC".equals(dsId)) {
                    dcIn = new FileInputStream(dsFile);
                }
                if (dsFilter != null && dsFilter.isFullTextStream(ds)) {
                    log.info("adding " + dsFile + " for full text");
                    fullTextFiles.add(dsFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!fullTextFiles.isEmpty()) {
            if (false) {
                // PDF files
                try {
                    PDFTextStripper stripper = new PDFTextStripper();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Writer out = new OutputStreamWriter(baos, "UTF-8");
                    for (File f : fullTextFiles) {
                        try {
                            PDDocument doc = PDDocument.load(f);
                            if (doc.isEncrypted()) {
                                log.warn(f + " ENCRYPTED!");
                            } else {
                                log.info("Extracting text from " + f);
                                stripper.writeText(doc, out);
                            }
                            doc.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    out.close();
                    System.out.println("fulltext=" + baos.toString("UTF-8"));
                    addFullTextFilter.setValue(out.toString());
                } catch (UnsupportedEncodingException uee) {
                    uee.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } else {
                // plain text
                String fullText = "";
                for (File f : fullTextFiles) {
                    try {
                        FileInputStream in = new FileInputStream(f);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        StreamUtils.copyStream(in, out);
                        out.close();
                        in.close();
                        fullText += out.toString("UTF-8");
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
                addFullTextFilter.setValue(fullText);
            }
        }

        addIdFilter.setValue(pid);

        try {
            if (dcIn != null) {
                File solrFile = new File(TMP_DIR, name + "/" + pid
                    + "/solr.xml");
                solrOut = new FileOutputStream(solrFile);
                filterManager.run(dcIn, solrOut);
                solrOut.close();
                postTool.postFile(solrFile, NullWriter.getInstance());
                solrFile.delete();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 9) {
            System.out.println("Usage: "
                + FedoraRestHarvester.class.getCanonicalName()
                + " <solrUpdateUrl> "
                + "<registryUrl> <registryUser> <registryPassword> "
                + "<repBaseUrl> <repUser> <repPassword> <repName> "
                + "<fullText> <fullTextId>[requestLimit]");
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
                boolean fullText = args[8].equals("true");
                String fullTextId = args[9];
                int limit = Integer.MAX_VALUE;
                if (args.length > 10) {
                    limit = Integer.parseInt(args[10]);
                }
                DatastreamFilter dsFilter = null;
                if (fullText) {
                    dsFilter = new DsIdDatastreamFilter(fullTextId);
                }
                Registry registry = new Fedora30Registry(regUrl, regUser,
                    regPass);
                Harvester harvester = new FedoraRestHarvester(solrUpdateUrl,
                    registry, dsFilter, limit);
                harvester.setAuthentication(repUser, repPass);
                harvester.harvest(repName, repUrl);
            } catch (Exception e) {
                System.err.println("Failed to harvest");
                e.printStackTrace();
            }
        }
    }
}
