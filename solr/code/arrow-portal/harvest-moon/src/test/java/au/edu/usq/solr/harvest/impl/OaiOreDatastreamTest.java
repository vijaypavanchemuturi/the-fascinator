package au.edu.usq.solr.harvest.impl;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

public class OaiOreDatastreamTest {

    @Test
    public void getId() {
        OaiOreDatastream o = new OaiOreDatastream(
            "http://rspilot.usq.edu.au/7/1/manual.pdf", "1", "application/pdf");
        Assert.assertEquals(o.getId(), "7/1/manual.pdf");

    }

    @Test
    public void getLabel() {
        OaiOreDatastream o = new OaiOreDatastream(
            "http://rspilot.usq.edu.au/7/1/manual.pdf", "1", "application/pdf");
        Assert.assertEquals(o.getLabel(), "manual.pdf");

    }

    @Test
    public void getMimeType() {
        OaiOreDatastream o = new OaiOreDatastream(
            "http://rspilot.usq.edu.au/7/1/manual.pdf", "1", "application/pdf");
        Assert.assertEquals(o.getMimeType(), "application/pdf");

    }

    @Test
    public void getContent() {
        OaiOreDatastream o = new OaiOreDatastream(
            "http://rspilot.usq.edu.au/7/1/manual.pdf", "1", "application/pdf");
        byte[] testArray = null;
        try {
            testArray = o.getContent();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int arrayLength = testArray.length;
        Assert.assertEquals(27365, arrayLength);

    }

    @Test
    public void getContentAsString() {
        OaiOreDatastream o = new OaiOreDatastream(
            "http://rspilot.usq.edu.au/7/1/manual.pdf", "1", "application/pdf");
        String testContent = null;
        try {
            testContent = o.getContentAsString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(testContent);

        boolean isPdfString = testContent.startsWith("%PDF-1.3");

        Assert.assertTrue(isPdfString);

    }

    @Test
    public void getContentAsStream() {
        OaiOreDatastream o = new OaiOreDatastream(
            "http://rspilot.usq.edu.au/7/1/manual.pdf", "1", "application/pdf");
        InputStream testInputStream = null;
        try {
            testInputStream = o.getContentAsStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(testInputStream);
        double available = 0;
        try {
            available = testInputStream.available();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertTrue(available >= 1);

    }
}