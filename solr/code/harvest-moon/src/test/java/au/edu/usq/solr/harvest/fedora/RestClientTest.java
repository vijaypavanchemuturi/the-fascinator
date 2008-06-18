package au.edu.usq.solr.harvest.fedora;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Assert;
import org.junit.Test;

import au.edu.usq.solr.harvest.fedora.types.DatastreamType;
import au.edu.usq.solr.harvest.fedora.types.ObjectDatastreamsType;
import au.edu.usq.solr.harvest.fedora.types.ResultType;

public class RestClientTest {

    @Test
    public void result() throws Exception {
        JAXBContext jc = JAXBContext.newInstance(ResultType.class);
        Unmarshaller um = jc.createUnmarshaller();
        ResultType result = (ResultType) um.unmarshal(getClass().getResourceAsStream(
            "/result.xml"));
        Assert.assertEquals(0, result.getListSession().getCursor());
        Assert.assertEquals("c29863f172bcaad416db2133083e0edc",
            result.getListSession().getToken());
        Assert.assertEquals(10, result.getObjectFields().size());
    }

    @Test
    public void objectDatastreams() throws Exception {
        JAXBContext jc = JAXBContext.newInstance(ObjectDatastreamsType.class);
        Unmarshaller um = jc.createUnmarshaller();
        ObjectDatastreamsType result = (ObjectDatastreamsType) um.unmarshal(getClass().getResourceAsStream(
            "/object-datastreams.xml"));
        Assert.assertEquals("uon:1", result.getPid());
        Assert.assertEquals(2, result.getDatastreams().size());
        DatastreamType dc = result.getDatastreams().get(0);
        Assert.assertEquals("DS1", dc.getDsid());
    }

    @Test
    public void create() throws Exception {
        FedoraRestClient client = new FedoraRestClient(
            "http://localhost:8080/fedora");
        String pid = client.createObject("Test", "uuid");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.get(pid, out);
        client.authenticate("fedoraAdmin", "fedoraAdmin");
        Assert.assertEquals(2500, out.size());
        Map<String, String> options = new HashMap<String, String>();
        client.addDatastream(pid, "DC", options,
            getClass().getResourceAsStream("/dc.xml"), "text/xml");
    }
}
