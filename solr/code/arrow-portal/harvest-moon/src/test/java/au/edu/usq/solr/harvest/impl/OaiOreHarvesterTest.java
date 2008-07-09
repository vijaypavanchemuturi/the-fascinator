package au.edu.usq.solr.harvest.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
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
        String hUrl = "http://rspilot.usq.edu.au/cgi/search/simple/export_rspilot_ResMapUrls.xml?exp=0|1|-date/creators_name/title|archive|-|q:_fulltext_/abstract/creators_name/date/title:ALL:IN:the|-|eprint_status:eprint_status:ALL:EQ:archive|metadata_visibility:metadata_visibility:ALL:EX:show&output=ResMapUrls&_action_export=1&screen=Public::EPrintSearch&cache=47485";
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
        boolean containsDirtyChar = false;
        boolean dirtyCharGone = false;
        OaiOreHarvester o = new OaiOreHarvester(
            "http://rspilot.usq.edu.au/cgi/search/simple/export_rspilot_ResMapUrls.xml?exp=0|1|-date/creators_name/title|archive|-|q:_fulltext_/abstract/creators_name/date/title:ALL:IN:the|-|eprint_status:eprint_status:ALL:EQ:archive|metadata_visibility:metadata_visibility:ALL:EX:show&output=ResMapUrls&_action_export=1&screen=Public::EPrintSearch&cache=47485");
        InputStream in = getClass().getResourceAsStream("/resmapDirty.xml");
        Scanner readDirty = new Scanner(new InputStreamReader(in));
        while (readDirty.hasNextLine()) {
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            System.out.println(readDirty.nextLine());
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            if (readDirty.nextLine().contains("dc:modified")) {
                containsDirtyChar = true;
            }
        }
        Assert.assertTrue(containsDirtyChar);
        in = o.cleanRawData(in);
        Scanner readClean = new Scanner(new InputStreamReader(in));
        if (readDirty.hasNextLine()) {
            if (readDirty.nextLine().contains("<dc:modified") == false) {
                dirtyCharGone = true;
            }
        }
        Assert.assertTrue(dirtyCharGone);

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
