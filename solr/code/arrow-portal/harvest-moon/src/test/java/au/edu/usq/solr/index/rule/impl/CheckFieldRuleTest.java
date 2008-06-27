package au.edu.usq.solr.index.rule.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import au.edu.usq.solr.index.rule.RuleException;

public class CheckFieldRuleTest {

    private Logger log = Logger.getLogger(CheckFieldRuleTest.class);

    @Test
    public void checkPass1() throws Exception {
        // one match - no ex
        InputStream in = getClass().getResourceAsStream("/solrdoc.xml");
        CheckFieldRule rule = new CheckFieldRule("identifier1", "http.*", false);
        try {
            rule.run(new InputStreamReader(in, "UTF-8"),
                new OutputStreamWriter(System.out, "UTF-8"));
        } catch (RuleException re) {
            log.error("Test FAIL: " + re.getMessage());
            Assert.fail();
        }
    }

    @Test
    public void checkFail1() throws Exception {
        // no match - throw ex
        InputStream in = getClass().getResourceAsStream("/solrdoc.xml");
        CheckFieldRule rule = new CheckFieldRule("identifier1", "http.*", true);
        try {
            rule.run(new InputStreamReader(in, "UTF-8"),
                new OutputStreamWriter(System.out, "UTF-8"));
            Assert.fail();
        } catch (RuleException re) {
            log.info("Test PASS: " + re.getMessage());
        }
    }

    @Test
    public void checkPass2() throws Exception {
        // no match - throw ex
        InputStream in = getClass().getResourceAsStream("/solrdoc.xml");
        CheckFieldRule rule = new CheckFieldRule("identifier2", "http.*", false);
        try {
            rule.run(new InputStreamReader(in, "UTF-8"),
                new OutputStreamWriter(System.out, "UTF-8"));
            Assert.fail();
        } catch (RuleException re) {
            log.error("Test PASS: " + re.getMessage());
        }
    }

    @Test
    public void checkFail2() throws Exception {
        // no match - throw ex
        InputStream in = getClass().getResourceAsStream("/solrdoc.xml");
        CheckFieldRule rule = new CheckFieldRule("identifier2", "http.*", true);
        try {
            rule.run(new InputStreamReader(in, "UTF-8"),
                new OutputStreamWriter(System.out, "UTF-8"));
            Assert.fail();
        } catch (RuleException re) {
            log.info("Test PASS: " + re.getMessage());
        }
    }
}
