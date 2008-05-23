package au.edu.usq.solr.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.solr.util.SimplePostTool;

public class Optimize {

    private URL solrUpdateUrl;

    public Optimize(String solrUpdateUrl) throws MalformedURLException {
        this.solrUpdateUrl = new URL(solrUpdateUrl);
    }

    public void optimize() throws IOException {
        SimplePostTool postTool = new SimplePostTool(solrUpdateUrl);
        Writer result = new OutputStreamWriter(System.out);
        postTool.postData(new StringReader("<optimize/>"), result);
        postTool.commit(result);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: " + Optimize.class.getCanonicalName()
                    + " <solrUpdateUrl>");
        } else {
            try {
                String solrUpdateUrl = args[0];
                Optimize optimize = new Optimize(solrUpdateUrl);
                optimize.optimize();
            } catch (MalformedURLException e) {
                System.err.println("Invalid Solr URL: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Failed to commit" + e.getMessage());
            }
        }
    }
}
