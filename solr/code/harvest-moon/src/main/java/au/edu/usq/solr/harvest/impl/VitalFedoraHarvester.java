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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.log4j.Logger;
import org.apache.solr.util.SimplePostTool;

import au.edu.usq.solr.harvest.Harvester;
import au.edu.usq.solr.harvest.HarvesterException;
import au.edu.usq.solr.harvest.Registry;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;

import fedora.client.FedoraClient;
import fedora.server.access.FedoraAPIA;
import fedora.server.types.gen.DatastreamDef;
import fedora.server.types.gen.FieldSearchQuery;
import fedora.server.types.gen.FieldSearchResult;
import fedora.server.types.gen.ListSession;
import fedora.server.types.gen.ObjectFields;
import fedora.server.utilities.StreamUtility;

public class VitalFedoraHarvester implements Harvester {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private Logger log = Logger.getLogger(VitalFedoraHarvester.class);

    private URL solrUpdateUrl;

    private Registry registry;

    private int requestLimit;

    private String username;

    private String password;

    private Transformer dcToSolr;

    private SimplePostTool postTool;

    public VitalFedoraHarvester(String solrUpdateUrl, Registry registry,
        int requestLimit) throws Exception {
        this.solrUpdateUrl = new URL(solrUpdateUrl);
        this.registry = registry;
        this.requestLimit = requestLimit;
    }

    public void setAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void harvest(String name, String url) throws HarvesterException {
        postTool = new SimplePostTool(solrUpdateUrl);

        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Templates t = tf.newTemplates(new StreamSource(
                getClass().getResourceAsStream("/xsl/dc_solr.xsl")));
            dcToSolr = t.newTransformer();
            dcToSolr.setParameter("repository-name", name);
            dcToSolr.setParameter("tmp-dir", TMP_DIR);

            FedoraClient client = new FedoraClient(url, username, password);
            FedoraAPIA access = client.getAPIA();
            registry.connect();

            String[] resultFields = { "pid" };
            FieldSearchQuery query = new FieldSearchQuery(null, "uon:7??");
            FieldSearchResult result = access.findObjects(resultFields,
                new NonNegativeInteger("5"), query);
            int count = 0;
            while (result != null) {
                count++;
                ObjectFields[] resultList = result.getResultList();
                for (ObjectFields objectField : resultList) {
                    String pid = objectField.getPid();
                    log.info("Processing PID=" + pid);
                    try {
                        processObject(client, pid);
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
                ListSession session = result.getListSession();
                if (session != null && count < requestLimit) {
                    String token = session.getToken();
                    log.debug("Resuming with token=" + token);
                    result = access.resumeFindObjects(token);
                } else {
                    result = null;
                }
            }
            log.info("Commiting solr index...");
            postTool.commit(new OutputStreamWriter(System.out));
        } catch (Exception e) {
            throw new HarvesterException(e);
        }
    }

    private void processObject(FedoraClient client, String pid)
        throws Exception {
        FedoraAPIA access = client.getAPIA();
        DatastreamDef[] dsDefs = access.listDatastreams(pid, "");
        File dcFile = File.createTempFile("solrdc", ".xml");
        dcToSolr.setParameter("has-full-text", false);
        for (DatastreamDef dsDef : dsDefs) {
            // data model is specific to USQ VITAL test repos
            // DC = dublin core
            // DS1 = marc xml
            // DS2 = cover page (PDF)
            // DS3+ = main datastreams (PDF)
            // FULLTEXT = text version of DS3+
            log.debug("DS=" + dsDef.getID() + "," + dsDef.getMIMEType() + ","
                + dsDef.getLabel());
            String dsId = dsDef.getID();
            if (dsId.equals("DC") || dsId.equals("DS3")
                || dsId.equals("FULLTEXT")) {
                Map<String, String> options = new HashMap<String, String>();
                options.put("mimeType", dsDef.getMIMEType());

                String newPid = registry.createObject(options);
                client.FOLLOW_REDIRECTS = true;
                InputStream data = client.get(String.format(
                    "info:fedora/%s/%s", pid, dsId), false);
                if (dsId.equals("DC")) {
                    options.put("dsLabel", "Dublin Core (Source)");
                    options.put("controlGroup", "X");
                    FileOutputStream fos = new FileOutputStream(dcFile);
                    StreamUtility.pipeStream(data, fos, 4096);
                    fos.close();
                    FileInputStream fis = new FileInputStream(dcFile);
                    registry.addDatastream(newPid, "DC0", fis, options);
                    fis.close();
                } else if (dsId.equals("FULLTEXT")) {
                    dcToSolr.setParameter("has-full-text", true);
                    File tmpFile = File.createTempFile("fulltext", ".txt");
                    FileOutputStream fos = new FileOutputStream(tmpFile);
                    StreamUtility.pipeStream(data, fos, 4096);
                    fos.close();
                    String fname = pid.replace(":", ".").replace("/", "_")
                        + ".fulltext.xml";
                    File fullTextFile = new File(TMP_DIR, fname);
                    log.info("fullTextFile=" + fullTextFile);

                    OutputStream fOut = new FileOutputStream(fullTextFile);
                    fOut.write("<?xml version='1.0' encoding='UTF-8'?>\n".getBytes());
                    fOut.write("<fulltext><![CDATA[".getBytes());
                    FileInputStream fis = new FileInputStream(tmpFile);
                    StreamUtility.pipeStream(fis, fOut, 4096);
                    fis.close();
                    fOut.close();

                    fOut = new FileOutputStream(fullTextFile, true);
                    fOut.write("]]></fulltext>".getBytes());
                    fOut.close();
                    options.put("dsLabel", dsDef.getLabel());
                    options.put("controlGroup", "M");
                    fis = new FileInputStream(tmpFile);
                    registry.addDatastream(newPid, "FULLTEXT", fis, options);
                    fis.close();
                } else {
                    options.put("dsLabel", dsDef.getLabel());
                    options.put("controlGroup", "M");
                    registry.addDatastream(newPid, dsDef.getID(), data, options);
                }
                data.close();

                // create the RELS-INT datastream
                Model model = ModelFactory.createDefaultModel();
                model.setNsPrefix("dcterms", DCTerms.getURI());

                if (dsId.equals("DS3")) {
                    options.put("mimeType", "text/xml");
                    options.put("dsLabel", "Relationships (Internal)");
                    options.put("controlGroup", "X");
                    Resource ds = model.createResource(String.format(
                        "info:fedora/%s/%s", newPid, dsId));
                    Resource fulltextds = model.createResource("info:fedora/changeme:1/FULLTEXT");
                    ds.addProperty(DCTerms.hasFormat, fulltextds);
                }

                Resource dc0 = model.createResource(String.format(
                    "info:fedora/%s/DC0", newPid));
                dc0.addProperty(DCTerms.conformsTo, DC.getURI());

                File relsIntFile = File.createTempFile("rels-int", ".xml");
                FileOutputStream out = new FileOutputStream(relsIntFile);
                model.write(out);
                out.close();
                FileInputStream in = new FileInputStream(relsIntFile);
                registry.addDatastream(newPid, "RELS-INT", in, options);
                in.close();
            }
        }

        // index to solr
        Source source = new StreamSource(dcFile);
        File solrDocFile = File.createTempFile("solrdoc", ".xml");
        log.debug("solrDocFile=" + solrDocFile);
        dcToSolr.setParameter("doc-id", pid);
        dcToSolr.transform(source, new StreamResult(solrDocFile));
        postTool.postFile(solrDocFile, new OutputStreamWriter(System.out));
    }

    public static void main(String[] args) {
        if (args.length < 8) {
            System.out.println("Usage: "
                + VitalFedoraHarvester.class.getCanonicalName()
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
                Harvester harvester = new VitalFedoraHarvester(solrUpdateUrl,
                    registry, limit);
                harvester.setAuthentication(repUser, repPass);
                harvester.harvest(repName, repUrl);
            } catch (MalformedURLException e) {
                System.err.println("Invalid Solr URL: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Failed to harvest");
                e.printStackTrace();
            }
        }
    }
}
