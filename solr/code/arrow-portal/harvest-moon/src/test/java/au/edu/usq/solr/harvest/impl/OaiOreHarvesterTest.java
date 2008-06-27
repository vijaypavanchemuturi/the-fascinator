package au.edu.usq.solr.harvest.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import au.edu.usq.solr.harvest.Item;

public class OaiOreHarvesterTest {

    @Test
    public void getResMapUrls() throws Exception {
        OaiOreHarvester harvester = new OaiOreHarvester(
            "http://rspilot.usq.edu.au/cgi/search/simple/export_rspilot_ResMapUrls.xml?exp=0|1|-date/creators_name/title|archive|-|q:_fulltext_/abstract/creators_name/date/title:ALL:IN:the|-|eprint_status:eprint_status:ALL:EQ:archive|metadata_visibility:metadata_visibility:ALL:EX:show&output=ResMapUrls&_action_export=1&screen=Public::EPrintSearch&cache=47485")
        List<ResMapUrl> resMapUrls = harvester.fetchResMapUrls()
        Assert.assertEquals(14, resMapUrls.size())
        ResMapUrl url1 = resMapUrls(0)
        Assert.assertEquals(
            "http://rspilot.usq.edu.au/cgi/export/11/ResMap/rspilot-eprint-11.xml", url1.)
    }

    @Test
    public void getOreItems() throws Exception {
        OaiOreHarvester harvester = new OaiOreHarvester(
            "http://rspilot.usq.edu.au/cgi/search/simple/export_rspilot_ResMapUrls.xml?exp=0|1|-date/creators_name/title|archive|-|q:_fulltext_/abstract/creators_name/date/title:ALL:IN:the|-|eprint_status:eprint_status:ALL:EQ:archive|metadata_visibility:metadata_visibility:ALL:EX:show&output=ResMapUrls&_action_export=1&screen=Public::EPrintSearch&cache=47485");
        List<Item> items = harvester.getItems();
        Assert.assertEquals(10, items.size());

    }

}
