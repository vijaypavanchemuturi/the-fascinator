package au.edu.usq.fascinator.model.taxonomy;

import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.Test;

public class TaxonomyTest {

    @Test
    public void load() throws Exception {
        Taxonomy t = new Taxonomy();
        t.load(new InputStreamReader(getClass().getResourceAsStream(
            "/anzsrc_for")));
        Assert.assertEquals("anzsrc", t.getRoot().getId());
        Assert.assertEquals(
            "Australian and New Zealand Standard Research Classification",
            t.getRoot().getLabel());
        Assert.assertTrue(t.getRoot().hasChildren());
    }
}
