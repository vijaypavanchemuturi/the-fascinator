package au.edu.usq.fascinator.harvester;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import au.edu.usq.fascinator.api.storage.DigitalObject;

public class FileSystemHarvesterTest {

    @Test
    public void getObjects() throws Exception {
        System.setProperty("test.dir", getClass().getResource(
                "/fs-harvest-root").toURI().getPath());
        System.out.println("test.dir=" + System.getProperty("test.dir"));
        FileSystemHarvester h = new FileSystemHarvester();
        h.init(new File(getClass().getResource("/fsh-config.json").toURI()));
        // only directories at root level
        List<DigitalObject> items = h.getObjects();
        Assert.assertEquals(0, items.size());
        Assert.assertEquals(true, h.hasMoreObjects());
        items = h.getObjects();
        Assert.assertEquals(false, items.isEmpty());
        DigitalObject item = items.get(0);
        String id = item.getId();
        int expectedSize = 0;
        if (id.contains("/books/")) {
            expectedSize = 3;
        } else if (id.contains("/music/")) {
            expectedSize = 2;
        } else if (id.contains("/pictures/")) {
            expectedSize = 4;
        }
        Assert.assertEquals(expectedSize, items.size());
    }
}
