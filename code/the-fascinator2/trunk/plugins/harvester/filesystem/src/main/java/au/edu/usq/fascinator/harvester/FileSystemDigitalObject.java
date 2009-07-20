package au.edu.usq.fascinator.harvester;

import java.io.File;

import au.edu.usq.fascinator.api.storage.impl.FilePayload;
import au.edu.usq.fascinator.api.storage.impl.GenericDigitalObject;

public class FileSystemDigitalObject extends GenericDigitalObject {

    public FileSystemDigitalObject(File file) {
        super(file.getAbsolutePath());
        addPayload(new FilePayload(file));
    }
}
