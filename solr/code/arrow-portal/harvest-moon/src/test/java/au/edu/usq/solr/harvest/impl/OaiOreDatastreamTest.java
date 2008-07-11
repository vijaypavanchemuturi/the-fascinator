package au.edu.usq.solr.harvest.impl;

import org.junit.Assert;
import org.junit.Test;

public class OaiOreDatastreamTest {

    @Test
    public void getId() {
        OaiOreDatastream o = new OaiOreDatastream(
            "http://rspilot.usq.edu.au/7/1/manual.pdf", "1", "application/pdf");
        Assert.assertEquals(o.getId(), "7/1/manual.pdf");

    }

    public void getLabel() {
        OaiOreDatastream o = new OaiOreDatastream(
            "http://rspilot.usq.edu.au/7/1/manual.pdf", "1", "application/pdf");
        Assert.assertEquals(o.getLabel(), "manual.pdf");

    }

    public void getMimeType() {
        OaiOreDatastream o = new OaiOreDatastream(
            "http://rspilot.usq.edu.au/7/1/manual.pdf", "1", "application/pdf");
        Assert.assertEquals(o.getMimeType(), "application/pdf");

    }
}