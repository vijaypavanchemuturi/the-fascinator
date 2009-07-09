package au.edu.usq.fascinator.api.transformer;

import java.io.File;

import au.edu.usq.fascinator.api.Plugin;

public interface Transformer extends Plugin {

    public File transform(File in) throws TransformerException;

}
