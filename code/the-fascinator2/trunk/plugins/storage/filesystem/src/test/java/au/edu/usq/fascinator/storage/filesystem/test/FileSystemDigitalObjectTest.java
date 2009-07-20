package au.edu.usq.fascinator.storage.filesystem.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import au.edu.usq.fascinator.storage.filesystem.FileSystemDigitalObject;

public class FileSystemDigitalObjectTest {

    @Test
    public void getPath1() {
        FileSystemDigitalObject fsdo = new FileSystemDigitalObject(
                new File("."), "file:///Users/lucido/Documents/test1.doc");
        String expected = "./9f/19/file%3A%2F%2F%2FUsers%2Flucido%2FDocuments%2Ftest1.doc";
        Assert.assertEquals(expected, fsdo.getPath().getPath());
    }
}
