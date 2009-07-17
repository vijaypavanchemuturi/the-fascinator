package au.edu.usq.fascinator.api.storage.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import au.edu.usq.fascinator.api.storage.PayloadType;

public class FilePayload extends GenericPayload {

    private File file;

    public FilePayload(File file) {
        this.file = file;
        setId(file.getName());
        setLabel(file.getAbsolutePath());
        // TODO get proper MIME type
        setContentType("text/plain");
        setPayloadType(PayloadType.External);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

}
