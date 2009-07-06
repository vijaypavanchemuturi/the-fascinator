package au.edu.usq.fascinator.api.transformer;

import java.io.InputStream;
import java.io.OutputStream;

import au.edu.usq.fascinator.api.Plugin;

public interface Transformer extends Plugin {

    public void transform(InputStream in, OutputStream out)
            throws TransformerException;

}
