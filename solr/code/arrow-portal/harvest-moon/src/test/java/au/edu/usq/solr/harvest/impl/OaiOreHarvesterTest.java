package au.edu.usq.solr.harvest.impl;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Scanner;

import org.dspace.foresite.AggregatedResource;
import org.dspace.foresite.OREParser;
import org.dspace.foresite.OREParserFactory;
import org.dspace.foresite.ORESerialiser;
import org.dspace.foresite.ORESerialiserFactory;
import org.dspace.foresite.ResourceMap;
import org.dspace.foresite.ResourceMapDocument;
import org.dspace.foresite.Triple;
import org.junit.Assert;
import org.junit.Test;

public class OaiOreHarvesterTest {

    @Test
    public void fetchResMapUrls() throws Exception {
        String hUrl = "http://rspilot.usq.edu.au/cgi/ore";
        OaiOreHarvester harvester = new OaiOreHarvester(hUrl);
        ArrayList<String> testArrayList = harvester.fetchResMapUrls();
        Assert.assertEquals(14, testArrayList.size());
    }

    @Test
    public void getNumberOfAggregatedResources() throws Exception {
        InputStream input = getClass().getResourceAsStream("/resmap.xml");
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = parser.parse(input);
        Assert.assertEquals(3, rem.getAggregatedResources().size());
        input.close();
    }

    @Test
    public void getId() throws Exception {
        InputStream input = getClass().getResourceAsStream("/resmap.xml");
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = parser.parse(input);
        final OaiOreItem item = new OaiOreItem(rem);
        String id = item.getId();
        System.out.println("Item ID is :" + id);
        Assert.assertEquals("rspilot-eprint-11", id);
        input.close();
    }

    @Test
    public void normaliseDate() throws Exception {
        boolean stillDirty = true;
        InputStream in = getClass().getResourceAsStream("/resmapDirty.xml");
        InputStream in2 = OaiOreHarvester.cleanRawData(in);
        Scanner readClean = new Scanner(in2);
        if (readClean.hasNextLine()) {
            String line2 = readClean.nextLine();
            if (line2.contains("<dc:modified") == false) {
                stillDirty = false;

            }
        }
        Assert.assertFalse(stillDirty);
    }

    @Test
    public void printOutTripples() throws Exception {
        InputStream in = getClass().getResourceAsStream("/resmap.xml");
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = parser.parse(in);
        // try to serialize this
        ORESerialiser serial = ORESerialiserFactory.getInstance("N3");
        ResourceMapDocument doc = serial.serialise(rem);
        String serialisation = doc.toString();
        System.out.print(serialisation);

        for (AggregatedResource iterator : rem.getAggregatedResources()) {
            for (URI u : iterator.getTypes()) { // No types apparently
                System.out.print("Type URI:");
                System.out.println(u);
            }

            for (Triple t : iterator.listAllTriples()) {

                System.out.print("Subject: ");
                System.out.println(t.getSubjectURI());
                System.out.print("Predicate: ");
                System.out.println(t.getPredicate().getURI());
                System.out.print("Object: ");
                try {
                    System.out.println("Object URI:" + t.getObjectURI());
                } catch (Exception e) {
                    System.out.println("No Object URI!");
                } finally {
                }

                try {
                    System.out.println("Object Literal" + t.getObjectLiteral());
                } catch (Exception e) {
                    System.out.println("No Object Literal!");
                } finally {
                }

            }

        }

        in.close();
    }
}
