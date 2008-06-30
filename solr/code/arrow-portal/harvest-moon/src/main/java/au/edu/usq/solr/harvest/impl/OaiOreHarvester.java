package au.edu.usq.solr.harvest.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class OaiOreHarvester {
    private String harvestUrl;
    private Logger log = Logger.getLogger(OaiOreHarvester.class);

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

}
