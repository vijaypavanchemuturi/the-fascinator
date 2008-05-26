package au.edu.usq.solr.harvest;


public interface Harvester {

    public void setAuthentication(String username, String password);

    public void harvest(String name, String url) throws HarvesterException;

}
