package au.edu.usq.solr.index.rule.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import au.edu.usq.solr.index.AddDocType;

public class ModifyFieldRuleTest {

    private Logger log = Logger.getLogger(ModifyFieldRuleTest.class);

    @Test
    public void modifyDate() throws Exception {
        InputStream in = getClass().getResourceAsStream("/solrdoc.xml");
        ModifyFieldRule rule = new ModifyFieldRule("date", ".*(\\d{4}).*", "$1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rule.run(new InputStreamReader(in, "UTF-8"), new OutputStreamWriter(
            out, "UTF-8"));
        JAXBContext jc = JAXBContext.newInstance(AddDocType.class);
        Unmarshaller u = jc.createUnmarshaller();
        AddDocType doc = (AddDocType) u.unmarshal(new ByteArrayInputStream(
            out.toByteArray()));
        Assert.assertEquals("2007", doc.getFields("date").get(0).getValue());
    }

    @Test
    public void modifySubject() throws Exception {
        InputStream in = getClass().getResourceAsStream("/solrdoc.xml");
        ModifyFieldRule rule = new ModifyFieldRule("subject2",
            ".*(\\d{6}) (.+)$", "$2 ($1)");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rule.run(new InputStreamReader(in, "UTF-8"), new OutputStreamWriter(
            out, "UTF-8"));
        JAXBContext jc = JAXBContext.newInstance(AddDocType.class);
        Unmarshaller u = jc.createUnmarshaller();
        AddDocType doc = (AddDocType) u.unmarshal(new ByteArrayInputStream(
            out.toByteArray()));
        Assert.assertEquals(
            "Plant Protection (Pests, Diseases and Weeds) (300204)",
            doc.getFields("subject2").get(0).getValue());
    }

    @Test
    public void modifyUnchanged() throws Exception {
        InputStream in = getClass().getResourceAsStream("/solrdoc.xml");
        ModifyFieldRule rule = new ModifyFieldRule("year", ".*(\\d{4}).*", "$1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rule.run(new InputStreamReader(in, "UTF-8"), new OutputStreamWriter(
            out, "UTF-8"));
        JAXBContext jc = JAXBContext.newInstance(AddDocType.class);
        Unmarshaller u = jc.createUnmarshaller();
        AddDocType doc = (AddDocType) u.unmarshal(new ByteArrayInputStream(
            out.toByteArray()));
        Assert.assertEquals("1956", doc.getFields("year").get(0).getValue());
    }
}
