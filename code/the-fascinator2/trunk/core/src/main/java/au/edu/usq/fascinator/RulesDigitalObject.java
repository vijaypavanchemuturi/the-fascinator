package au.edu.usq.fascinator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import au.edu.usq.fascinator.api.storage.impl.GenericDigitalObject;
import au.edu.usq.fascinator.api.storage.impl.GenericPayload;

public class RulesDigitalObject extends GenericDigitalObject {

    private class FilePayload extends GenericPayload {

        private File rulesFile;

        public FilePayload(File rulesFile) {
            this.rulesFile = rulesFile;
            setId(rulesFile.getName());
            setLabel("Fascinator Indexing Rules");
            setContentType("text/plain");
        }

        @Override
        public InputStream getInputStream() {
            try {
                return new FileInputStream(rulesFile);
            } catch (FileNotFoundException e) {
            }
            return null;
        }

    }

    public RulesDigitalObject(File rulesFile) {
        super(rulesFile.getAbsolutePath());
        addPayload(new FilePayload(rulesFile));
    }

}
