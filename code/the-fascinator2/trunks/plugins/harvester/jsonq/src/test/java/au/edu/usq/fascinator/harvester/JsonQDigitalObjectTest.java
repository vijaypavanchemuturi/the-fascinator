package au.edu.usq.fascinator.harvester;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

public class JsonQDigitalObjectTest {

    @Test
    public void create() throws Exception {
        String uri = getClass().getResource("/sample.xml").toURI().toString();
        Map<String, String> info = new HashMap<String, String>();
        info.put("state", "mod");
        info.put("time", "2009-07-07 16:19:46");
        JsonQDigitalObject obj = new JsonQDigitalObject(uri, info);
        Properties props = new Properties();
        props.load(obj.getMetadata().getInputStream());
        Assert.assertEquals("mod", props.getProperty("state"));
        Assert.assertEquals("2009-07-07 16:19:46", props.getProperty("time"));
    }
}
