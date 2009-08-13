package au.edu.usq.fascinator.storage.filesystem;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class FileSystemDigitalObjectTest {

    @Test
    public void getPath1() {
        FileSystemDigitalObject fsdo = new FileSystemDigitalObject(
                new File("."), "file:///Users/lucido/Documents/test1.doc");
        String expected = FilenameUtils
                .separatorsToSystem("./9f/19/9f193517165c524d485ddf8f1cf322da");
        Assert.assertEquals(expected, fsdo.getPath().getPath());
    }
}
