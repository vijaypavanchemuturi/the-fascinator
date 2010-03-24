package au.edu.usq.fascinator.portal.velocity;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;

public class JythonLogger extends OutputStream {

    private Logger log;

    private StringBuilder buf;

    public JythonLogger(Logger log) {
        this.log = log;
        buf = new StringBuilder();
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    @Override
    public void flush() throws IOException {
        log.debug(buf.toString());
        buf = new StringBuilder();
    }

    @Override
    public void write(int b) throws IOException {
        buf.append((char) b);
    }
}
