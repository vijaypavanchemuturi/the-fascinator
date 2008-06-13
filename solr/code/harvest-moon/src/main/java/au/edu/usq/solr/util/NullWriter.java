package au.edu.usq.solr.util;

import java.io.IOException;
import java.io.Writer;

public class NullWriter extends Writer {

    private static NullWriter instance;

    public static final NullWriter getInstance() {
        if (instance == null) {
            instance = new NullWriter();
        }
        return instance;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    @Override
    public void flush() throws IOException {
        // do nothing
    }

    @Override
    public void write(char[] arg0, int arg1, int arg2) throws IOException {
        // do nothing
    }

}
