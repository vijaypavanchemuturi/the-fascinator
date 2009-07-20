package au.edu.usq.fascinator.harvester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import au.edu.usq.fascinator.api.storage.impl.GenericPayload;
import au.edu.usq.fascinator.common.MimeTypeUtil;

public class FileSystemPayload extends GenericPayload {

    private File file;

    public FileSystemPayload(File file) {
        this.file = file;
        setId(file.getName());
        setLabel(file.getAbsolutePath());
        setContentType(MimeTypeUtil.getMimeType(file));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
}
