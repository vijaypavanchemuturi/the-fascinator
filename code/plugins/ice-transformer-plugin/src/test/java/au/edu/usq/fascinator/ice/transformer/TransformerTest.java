package au.edu.usq.fascinator.ice.transformer;

import org.junit.Assert;
import org.junit.Test;

public class TransformerTest {
    private IceTransformer tf = new IceTransformer();

    String fileNameodp = "/home/octalina/Desktop/presentation.odp";
    String fileNameodt = "/home/octalina/Desktop/testImage.odt";

    @Test
    public void getNotExistFile() {
        System.out.println(getClass().getResource("/presentation.odp").toString());
        String fileName = "/home/octalina/Desktop/presentation1.odp";
        Assert.assertEquals(false, tf.getRendition(fileName));
    }

    @Test
    public void fileWithoutExtension() {
        String fileName = "/home/octalina/Desktop/presentation1";
        Assert.assertEquals(false, tf.getRendition(fileName));
    }

    // @Test
    public void testPdfFile() {
        // Assert.assertEquals(true, tf.getRendition(fileNameodp));
    }

    // @Test
    public void testOdtFile() {
        // Assert.assertEquals(true, tf.getRendition(fileNameodt));
    }
}
