package au.edu.usq.fascinator.model.taxonomy;

import junit.framework.Assert;

import org.junit.Test;

public class NodeTest {

    @Test
    public void load() throws Exception {
        Node n = new Node(
            "anzsrc|Australian and New Zealand Standard Research Classification|root|");
        Assert.assertEquals("anzsrc", n.getId());
        Assert.assertEquals(
            "Australian and New Zealand Standard Research Classification",
            n.getLabel());
        Assert.assertTrue(n.isRoot());
    }
}
