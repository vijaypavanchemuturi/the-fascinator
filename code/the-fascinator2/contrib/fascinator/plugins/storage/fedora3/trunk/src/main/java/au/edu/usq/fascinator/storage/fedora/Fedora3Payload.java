package au.edu.usq.fascinator.storage.fedora;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.MimeTypeUtil;
import au.edu.usq.fascinator.common.storage.impl.GenericPayload;
import au.edu.usq.fedora.RestClient;
import au.edu.usq.fedora.types.DatastreamProfile;

public class Fedora3Payload extends GenericPayload {

    // private Logger log = LoggerFactory.getLogger(Fedora3Payload.class);

    private InputStream stream;

    private String fedoraPid;

    private String dsId;

    private RestClient client;

    public Fedora3Payload(String pid, String fedoraPid, String dsId,
            RestClient client) {
        super(pid, pid, MimeTypeUtil.DEFAULT_MIME_TYPE);
        // log.debug("Construct NEW({},{},{})", new String[] { pid, fedoraPid,
        // dsId });
        init(fedoraPid, dsId, client);
    }

    public Fedora3Payload(DatastreamProfile dsp, String pid, String fedoraPid,
            String dsId, RestClient client) {
        super(pid, dsp.getDsLabel(), dsp.getDsMIME(), PayloadType.valueOf(dsp
                .getDsAltID()));
        // log.debug("Construct EXISTING ({},{},{})", new String[] { pid,
        // fedoraPid, dsId });
        init(fedoraPid, dsId, client);
    }

    private void init(String fedoraPid, String dsId, RestClient client) {
        stream = null;
        this.fedoraPid = fedoraPid;
        this.dsId = dsId;
        this.client = client;
    }

    @Override
    public InputStream open() throws StorageException {
        close();
        try {
            // log.debug("open({})", getId());
            stream = client.getStream(fedoraPid, dsId);
        } catch (IOException ioe) {
            throw new StorageException(ioe);
        }
        return stream;
    }

    @Override
    public void close() throws StorageException {
        if (stream != null) {
            try {
                // log.debug("close({})", getId());
                stream.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
        stream = null;
        if (hasMetaChanged()) {
            updateMeta();
        }
    }

    private void updateMeta() throws StorageException {
        // log.debug("updateMeta({})", getId());
        Properties options = new Properties();
        PayloadType type = getType();
        if (type == null) {
            type = PayloadType.Enrichment;
        }
        options.setProperty("altIDs", getType().toString());
        options.setProperty("label", getLabel());
        options.setProperty("mimeType", getContentType());
        options.setProperty("ignoreContent", "true");
        try {
            client.modifyDatastream(fedoraPid, dsId, options);
            setMetaChanged(false);
        } catch (IOException ioe) {
            throw new StorageException(ioe);
        }
    }
}
