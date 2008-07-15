package au.edu.usq.solr.harvest.impl;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
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

import au.edu.usq.solr.harvest.Datastream;

public class OaiOreItemTest {

    @Test
    public void printOutTripplesUsingPdfExample() throws Exception {
        OaiOreHarvester o = new OaiOreHarvester(
            "http://rspilot.usq.edu.au/cgi/search/simple/export_rspilot_ResMapUrls.xml?exp=0|1|-date/creators_name/title|archive|-|q:_fulltext_/abstract/creators_name/date/title:ALL:IN:the|-|eprint_status:eprint_status:ALL:EQ:archive|metadata_visibility:metadata_visibility:ALL:EX:show&output=ResMapUrls&_action_export=1&screen=Public::EPrintSearch&cache=47485");
        InputStream in = getClass().getResourceAsStream("/pdf_example.xml");
        InputStream in2 = o.cleanRawData(in);

        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem2 = parser.parse(in2);
        // try to serialize this
        ORESerialiser serial = ORESerialiserFactory.getInstance("N3");
        ResourceMapDocument doc2 = null;
        try {
            doc2 = serial.serialise(rem2);
        } catch (Exception e) {
            e.printStackTrace();

        }
        String serialisation = doc2.toString();
        System.out.print(serialisation);

        for (AggregatedResource iterator : rem2.getAggregatedResources()) {
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

    @Test
    public void getTrippy() throws Exception {
        InputStream input = getClass().getResourceAsStream("/pdf_example.xml");
        InputStream in2 = OaiOreHarvester.cleanRawData(input);
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = parser.parse(in2);
        OaiOreItem item = new OaiOreItem(rem);
        ArrayList<OaiOreItem.Trippy> list = item.getTrippy();
        input.close();
    }

    @Test
    public void getDatastreamsPdfExample() throws Exception {
        InputStream input = getClass().getResourceAsStream("/pdf_example.xml");
        InputStream in2 = OaiOreHarvester.cleanRawData(input);
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = parser.parse(in2);
        OaiOreItem item = new OaiOreItem(rem);
        List<Datastream> ds = item.getDatastreams();
        for (Datastream i : ds) {
            System.out.print("Datastream Id: ");
            System.out.println(i.getId());
            System.out.print("Datastream label: ");
            System.out.println(i.getLabel());
            System.out.print("Mime Type: ");
            System.out.println(i.getMimeType());
            System.out.print("Content As String: ");
            System.out.println(i.getContentAsString());

        }

        Assert.assertEquals(3, item.getDatastreams().size());

        input.close();
    }

    @Test
    public void getDatastreamsHiddenJpegExample() throws Exception {
        InputStream input = getClass().getResourceAsStream("/jpeg_hidden.xml");
        InputStream in2 = OaiOreHarvester.cleanRawData(input);
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = parser.parse(in2);
        OaiOreItem item = new OaiOreItem(rem);
        List<Datastream> ds = item.getDatastreams();
        for (Datastream i : ds) {
            System.out.print("Datastream Id: ");
            System.out.println(i.getId());
            System.out.print("Datastream label: ");
            System.out.println(i.getLabel());
            System.out.print("Mime Type: ");
            System.out.println(i.getMimeType());
            System.out.print("Content As String: ");
            System.out.println(i.getContentAsString());

        }

        Assert.assertEquals(3, item.getDatastreams().size());

        input.close();
    }

    @Test
    public void getDatastreamsVisibleJpegExample() throws Exception {
        InputStream input = getClass().getResourceAsStream("/jpeg_visible.xml");
        InputStream in2 = OaiOreHarvester.cleanRawData(input);
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = parser.parse(in2);
        OaiOreItem item = new OaiOreItem(rem);
        List<Datastream> ds = item.getDatastreams();
        for (Datastream i : ds) {
            System.out.print("Datastream Id: ");
            System.out.println(i.getId());
            System.out.print("Datastream label: ");
            System.out.println(i.getLabel());
            System.out.print("Mime Type: ");
            System.out.println(i.getMimeType());
            System.out.print("Content As String: ");
            System.out.println(i.getContentAsString());

        }

        Assert.assertEquals(3, item.getDatastreams().size());

        input.close();
    }

    @Test
    public void getMetadata() throws Exception {
        InputStream input = getClass().getResourceAsStream("/pdf_example.xml");
        InputStream in2 = OaiOreHarvester.cleanRawData(input);
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = parser.parse(in2);
        OaiOreItem item = new OaiOreItem(rem);
        Element meta = item.getMetadata();
        System.out.println(item.getMetadataAsString());
        input.close();
    }
}
