package au.edu.usq.fascinator.api.processor;

import au.edu.usq.fascinator.api.Plugin;
import au.edu.usq.fascinator.api.storage.DigitalObject;

public interface Processor extends Plugin {

    public DigitalObject transform(DigitalObject object)
            throws ProcessorException;

}
