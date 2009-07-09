package au.edu.usq.fascinator.storage.filesystem.test;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.edu.usq.fascinator.api.storage.impl.GenericDigitalObject;
import au.edu.usq.fascinator.api.storage.impl.GenericPayload;
import au.edu.usq.fascinator.storage.filesystem.FileSystemDigitalObject;
import au.edu.usq.fascinator.storage.filesystem.FileSystemStorage;

public class FileSystemStorageTest {

    private FileSystemStorage fs;

    private GenericDigitalObject testObject1;

    @Before
    public void setup() {
        fs = new FileSystemStorage();
        testObject1 = new GenericDigitalObject("oai:eprints.usq.edu.au:318", "DC");
        GenericPayload testPayload1 = new GenericPayload("DC",
                "Dublin Core Metadata", "text/xml");
        testPayload1.setInputStream(getClass().getResourceAsStream("/dc.xml"));
        testObject1.addPayload(testPayload1);
    }

    @After
    public void cleanup() {
        FileUtils.deleteQuietly(fs.getHomeDir());
    }

    @Test
    public void addAndGetObject() throws Exception {
        fs.init(new File(getClass().getResource("/fs-config.json").toURI()));
        fs.addObject(testObject1);
        FileSystemDigitalObject addedObject = (FileSystemDigitalObject) fs
                .getObject(testObject1.getId());
        Assert.assertEquals(
                "/tmp/_fs_test/e2/92/oai%3Aeprints.usq.edu.au%3A318",
                addedObject.getPath().getAbsolutePath());
    }

}
