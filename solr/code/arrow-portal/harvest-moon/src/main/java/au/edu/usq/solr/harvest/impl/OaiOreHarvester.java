package au.edu.usq.solr.harvest.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.foresite.OREParser;
import org.dspace.foresite.OREParserException;
import org.dspace.foresite.OREParserFactory;
import org.dspace.foresite.ResourceMap;

import au.edu.usq.solr.harvest.Harvester;
import au.edu.usq.solr.harvest.HarvesterException;
import au.edu.usq.solr.harvest.Item;

public class OaiOreHarvester implements Harvester {
    private String harvestUrl;
    private Logger log = Logger.getLogger(OaiOreHarvester.class);
    private ResourceMap resObject;
    private boolean hasMoreItems = true;

    public OaiOreHarvester(String harvestUrl) {
        this.setHarvestUrl(harvestUrl);
    }

    private void setHarvestUrl(String harvestUrl) {
        this.harvestUrl = harvestUrl;
    }

    private void addResMapUrl(ArrayList<String> arrayList, String nextUrl) {
        arrayList.add(nextUrl);
    }

    public ArrayList<String> fetchResMapUrls() {
        ArrayList<String> resMapUrls = new ArrayList<String>();
        try {
            URL u = new URL(this.harvestUrl);
            BufferedReader input = new BufferedReader(new InputStreamReader(
                u.openStream()));
            String inputLine;
            while ((inputLine = input.readLine()) != null) {
                inputLine = inputLine.trim();
                if (inputLine.startsWith("http://")) {
                    this.addResMapUrl(resMapUrls, inputLine);
                }
            }

            input.close();
        } catch (Exception e) {
            String message = e.getMessage();
            System.out.println("Unable to harvest Resource Map Urls from "
                + this.harvestUrl);
            System.out.println(message);
            log.info("Unable to harvest Resource Map Urls from "
                + this.harvestUrl
                + ". Perhaps check this url in a browser, also make sure your proxy settings are correct.");
            System.exit(0);
        }
        return resMapUrls;

    }

    public ResourceMap createNewResourceMapObject(String fileName) {
        InputStream input = getClass().getResourceAsStream(this.harvestUrl);
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = null;
        try {
            rem = parser.parse(input);
        } catch (OREParserException e) {
            System.out.println("Unable to parse Resource Map "
                + this.harvestUrl);
            e.printStackTrace();
        }
        return rem;

    }

    public List<Item> getItems(Date since) throws HarvesterException {

        ArrayList<Item> items = new ArrayList<Item>();
        ArrayList<String> urls = this.fetchResMapUrls();
        for (String singleUrl : urls) {
            resObject = createNewResourceMapObject(singleUrl);
            OaiOreItem item = new OaiOreItem(resObject);
            items.add(item);
        }
        hasMoreItems = false;
        return items;

    }

    public boolean hasMoreItems() {
        return hasMoreItems;

    }
}
