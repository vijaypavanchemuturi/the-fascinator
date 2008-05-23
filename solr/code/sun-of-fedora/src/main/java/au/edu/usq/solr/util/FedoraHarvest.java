package au.edu.usq.solr.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.axis.types.NonNegativeInteger;

import fedora.client.FedoraClient;
import fedora.server.access.FedoraAPIA;
import fedora.server.types.gen.DatastreamDef;
import fedora.server.types.gen.FieldSearchQuery;
import fedora.server.types.gen.FieldSearchResult;
import fedora.server.types.gen.ObjectFields;

public class FedoraHarvest {

    private URL solrUpdateUrl;

    public FedoraHarvest(String solrUpdateUrl) throws MalformedURLException {
        this.solrUpdateUrl = new URL(solrUpdateUrl);
    }

    public void harvest() throws Exception {
        FedoraClient client = new FedoraClient(
                "http://139.86.13.194:8080/fedora", "fedoraAdmin",
                "fedoraAdmin");
        FedoraAPIA access = client.getAPIA();
        String[] resultFields = { "pid" };
        FieldSearchQuery query = new FieldSearchQuery(null, "uon:7??");
        FieldSearchResult result = access.findObjects(resultFields,
                new NonNegativeInteger("10"), query);
        ObjectFields[] resultList = result.getResultList();
        for (ObjectFields objectField : resultList) {
            String pid = objectField.getPid();
            System.out.println("PID=" + pid);
            DatastreamDef[] dsDefs = access.listDatastreams(pid, "");
            for (DatastreamDef dsDef : dsDefs) {
                System.out.println("DS=" + dsDef.getID() + ","
                        + dsDef.getMIMEType() + "," + dsDef.getLabel());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: "
                    + FedoraHarvest.class.getCanonicalName()
                    + " <solrUpdateUrl>");
        } else {
            try {
                String solrUpdateUrl = args[0];
                FedoraHarvest optimize = new FedoraHarvest(solrUpdateUrl);
                optimize.harvest();
            } catch (MalformedURLException e) {
                System.err.println("Invalid Solr URL: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Failed: " + e.getMessage());
            }
        }
    }
}
