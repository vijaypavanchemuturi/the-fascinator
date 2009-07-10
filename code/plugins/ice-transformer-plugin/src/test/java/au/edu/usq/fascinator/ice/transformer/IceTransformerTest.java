package au.edu.usq.fascinator.ice.transformer;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class IceTransformerTest {
	private IceTransformer tf = new IceTransformer("", "/tmp");

    @Test
    public void testOdpFile() throws URISyntaxException {
    	File fileNameodp = new File(getClass().getResource("/presentation.odp").toURI());
        Assert.assertEquals(true, tf.getRendition(fileNameodp));
    }

    @Test
    public void testOdtFile() throws URISyntaxException {
    	File fileNameodt = new File(getClass().getResource("/testImage.odt").toURI());
        Assert.assertEquals(true, tf.getRendition(fileNameodt));
    }
}
