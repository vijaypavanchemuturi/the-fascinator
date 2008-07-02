package au.edu.usq.solr.harvest.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import au.edu.usq.solr.harvest.Item;

public class OaiPmhHarvesterTest {

    @Ignore
    @Test
    public void getItems() throws Exception {
        OaiPmhHarvester h = new OaiPmhHarvester(
            "http://rubric-vitalnew.usq.edu.au:8080/fedora/oai");
        List<Item> items = h.getItems(null);
        Assert.assertEquals(100, items.size());
        Item item1 = items.get(0);
        Assert.assertEquals("oai:rubric-vitalnew.usq.edu.au:uon:1",
            item1.getId());
    }

}
