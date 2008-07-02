package au.edu.usq.solr.harvest.impl;

import java.io.InputStream;
import java.util.ArrayList;

import org.dspace.foresite.OREParser;
import org.dspace.foresite.OREParserFactory;
import org.dspace.foresite.ResourceMap;
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
        InputStream input = getClass().getResourceAsStream("/resmap.xml");
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = parser.parse(input);
        Assert.assertEquals(3, rem.getAggregatedResources().size());
        input.close();
    }

}
