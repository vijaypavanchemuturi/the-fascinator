package au.edu.usq.fascinator.storage.filesystem;

import au.edu.usq.fascinator.api.storage.StorageException;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileSystemDigitalObjectTest {

    private FileSystemStorage fs;
    private FileSystemDigitalObject newObject;

    @Before
    public void setup() throws Exception {
        fs = new FileSystemStorage();
        fs.init(new File(getClass().getResource("/fs-config.json").toURI()));
    }

    @Test
    public void getPath1() {
        try {
            newObject = (FileSystemDigitalObject)
                    fs.createObject("file:///Users/lucido/Documents/test1.doc");
        } catch (StorageException ex) {
            System.err.println("Error creating object");
        }

        String expected = FilenameUtils.separatorsToSystem(
                fs.getHomeDir() + "/9f/19/35/9f193517165c524d485ddf8f1cf322da");
        Assert.assertEquals(expected, newObject.getPath());
    }

    @Test
    public void getPath2() {
        try {
            newObject = (FileSystemDigitalObject)
                    fs.createObject("oai:eprints.usq.edu.au:318");
        } catch (StorageException ex) {
            System.err.println("Error creating object");
        }
        String expected = FilenameUtils.separatorsToSystem(
                fs.getHomeDir() + "/e2/92/37/e292378c5b38b0d5a4aba11fd40e7151");
        Assert.assertEquals(expected, newObject.getPath());
    }
}
