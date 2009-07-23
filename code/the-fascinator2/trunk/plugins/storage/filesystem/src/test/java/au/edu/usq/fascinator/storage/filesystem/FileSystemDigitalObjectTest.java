package au.edu.usq.fascinator.storage.filesystem;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;

public class FileSystemDigitalObjectTest {

    @Test
    public void getPath1() {
        FileSystemDigitalObject fsdo = new FileSystemDigitalObject(
                new File("."), "file:///Users/lucido/Documents/test1.doc");
        String expected = FilenameUtils
                .separatorsToSystem("./9f/19/file%3A%2F%2F%2FUsers%2Flucido%2FDocuments%2Ftest1.doc");
        Assert.assertEquals(expected, fsdo.getPath().getPath());
    }
}
