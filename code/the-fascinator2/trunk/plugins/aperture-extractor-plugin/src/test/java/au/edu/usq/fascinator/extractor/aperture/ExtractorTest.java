package au.edu.usq.fascinator.extractor.aperture;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

import au.edu.usq.fascinator.api.transformer.TransformerException;

public class ExtractorTest {
    private Extractor ex = new Extractor("/tmp");

    @Test
    public void testPdfFile() throws URISyntaxException, TransformerException {
        File fileNameodp = new File(getClass().getResource("/AboutStacks.pdf")
                .toURI());
        File out = ex.transform(fileNameodp);
        System.out.println(out.getAbsolutePath());
    }

    @Test
    public void testOdtFile() throws URISyntaxException, TransformerException {
        File fileNameodt = new File(getClass().getResource("/testImage.odt")
                .toURI());
        File out = ex.transform(fileNameodt);
        System.out.println(out.getAbsolutePath());
    }
}
