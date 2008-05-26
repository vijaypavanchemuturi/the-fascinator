package au.edu.usq.solr.harvest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.log4j.Logger;

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

public class VitalFedoraHarvester implements Harvester {

    private Logger log = Logger.getLogger(VitalFedoraHarvester.class);

    private URL solrUpdateUrl;

    private Registry registry;

    private int requestLimit;

    private String username;

    private String password;

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
        try {
            FedoraClient client = new FedoraClient(url, username, password);
            FedoraAPIA access = client.getAPIA();
            registry.connect();

            String[] resultFields = { "pid" };
            FieldSearchQuery query = new FieldSearchQuery(null, "*");
            FieldSearchResult result = access.findObjects(resultFields,
                new NonNegativeInteger("20"), query);
            int count = 0;
            while (result != null) {
                count++;
                ObjectFields[] resultList = result.getResultList();
                for (ObjectFields objectField : resultList) {
                    String pid = objectField.getPid();
                    log.info("Processing PID=" + pid);
                    processObject(client, pid);
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
        } catch (Exception e) {
            throw new HarvesterException(e);
        }
    }

    private void processObject(FedoraClient client, String pid)
        throws Exception {
        FedoraAPIA access = client.getAPIA();
        DatastreamDef[] dsDefs = access.listDatastreams(pid, "");
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
                    registry.addDatastream(newPid, "DC0", data, options);
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
    }

    public static void main(String[] args) {
        if (args.length < 8) {
            System.out.println("Usage: "
                + VitalFedoraHarvester.class.getCanonicalName()
                + " <solrUpdateUrl> "
                + "<registryUrl> <registryUser> <registryPassword>"
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
