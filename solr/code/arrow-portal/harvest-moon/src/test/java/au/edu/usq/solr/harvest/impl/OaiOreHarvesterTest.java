package au.edu.usq.solr.harvest.impl;

import java.util.ArrayList;

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
    public void serializeResMap() throws Exception {
        String url = "http://rspilot.usq.edu.au/cgi/export/11/ResMap/rspilot-eprint-11.xml";
        // ResourceMapFactory rmf = new ResourceMapFactory.class;
        // ResourceMap rm = ResourceMapFa

        // Integer ints = rm.getAggregation().numberOfResources();
        // Assert.assertEquals("rta", "10", rm.getId());
        // Assert.assertEquals("10", ints);

    }

}
