package au.edu.usq.solr.index.rule.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Assert;
import org.junit.Test;

import au.edu.usq.solr.index.AddDocType;

public class DeleteFieldRuleTest {

    @Test
    public void deleteBlankSubject() throws Exception {
        InputStream in = getClass().getResourceAsStream("/solrdoc.xml");
        DeleteFieldRule rule = new DeleteFieldRule("subject", "^\\s*$");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rule.run(new InputStreamReader(in, "UTF-8"), new OutputStreamWriter(
            out, "UTF-8"));
        JAXBContext jc = JAXBContext.newInstance(AddDocType.class);
        Unmarshaller u = jc.createUnmarshaller();
        AddDocType doc = (AddDocType) u.unmarshal(new ByteArrayInputStream(
            out.toByteArray()));
        Assert.assertEquals(1, doc.getFields("subject").size());
        Assert.assertEquals("test", doc.getFields("subject").get(0).getValue());
    }
}
