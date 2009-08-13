package au.edu.usq.fascinator.storage.filesystem;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;
import au.edu.usq.fascinator.common.storage.impl.GenericPayload;

@Ignore
public class FileSystemStorageTest {

    private FileSystemStorage fs;

    private GenericDigitalObject testObject1, testObject2;

    @Before
    public void setup() {
        fs = new FileSystemStorage();
        testObject1 = new GenericDigitalObject("oai:eprints.usq.edu.au:318",
                "DC");
        GenericPayload testPayload1 = new GenericPayload("DC",
                "Dublin Core Metadata", "text/xml");
        testPayload1.setInputStream(getClass().getResourceAsStream("/dc.xml"));
        testObject1.addPayload(testPayload1);

        testObject2 = new GenericDigitalObject(
                "/Users/fascinator/Documents/sample.odt");
        GenericPayload testPayload2 = new GenericPayload("sample.odt",
                "ICE Sample Document",
                "application/vnd.oasis.opendocument.text");
        testPayload2.setInputStream(getClass().getResourceAsStream(
                "/sample.odt"));
        testObject2.addPayload(testPayload2);
    }

    @After
    public void cleanup() {
        FileUtils.deleteQuietly(fs.getHomeDir());
    }

    @Test
    public void addAndGetObjects() throws Exception {
        fs.init(new File(getClass().getResource("/fs-config.json").toURI()));
        fs.addObject(testObject1);
        FileSystemDigitalObject addedObject1 = (FileSystemDigitalObject) fs
                .getObject("oai:eprints.usq.edu.au:318");
        String filename1 = FilenameUtils
                .separatorsToSystem("/tmp/_fs_test/e2/92/e292378c5b38b0d5a4aba11fd40e7151");
        Assert
                .assertEquals(filename1, addedObject1.getPath()
                        .getAbsolutePath());

        fs.addObject(testObject2);
        FileSystemDigitalObject addedObject2 = (FileSystemDigitalObject) fs
                .getObject("/Users/fascinator/Documents/sample.odt");
        String filename2 = FilenameUtils
                .separatorsToSystem("/tmp/_fs_test/11/b4/11b498d057256a0b602fa0e7c4073fc3");
        Assert
                .assertEquals(filename2, addedObject2.getPath()
                        .getAbsolutePath());
    }
}
