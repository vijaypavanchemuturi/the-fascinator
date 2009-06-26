package au.edu.usq.fascinator.store;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PayloadType;
import au.edu.usq.fascinator.api.impl.BasicDigitalObject;
import au.edu.usq.fascinator.api.impl.BasicPayload;
import eu.medsea.mimeutil.MimeUtil;

public class ZipDigitalObject extends BasicDigitalObject {

    private Logger log = LoggerFactory.getLogger(ZipDigitalObject.class);

    public ZipDigitalObject(ZipFile zipFile) {
        Enumeration zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipEntries.nextElement();
            if (!entry.isDirectory()) {
                String name = entry.getName();
                log.info("Processing '{}'", name);
                try {
                    InputStream in = zipFile.getInputStream(entry);
                    BasicPayload payload = new BasicPayload();
                    payload.setId(name);
                    payload.setLabel(name);
                    payload.setInputStream(in);
                    Collection mimeTypes = MimeUtil.getMimeTypes(name);
                    payload.setContentType(mimeTypes.iterator().next()
                            .toString());
                    if ("link.txt".equals(name)) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        IOUtils.copy(in, out);
                        in.close();
                        setId(out.toString().trim());
                    } else if (name.startsWith("original/")) {
                        payload.setId(name.substring(9));
                    } else if (name.startsWith("renditions/")) {
                        payload.setId(name.substring(11));
                        payload.setLabel(name);
                        payload.setPayloadType(PayloadType.Enrichment);
                    } else if ("renditions.zip".equals(name)) {
                        // ??
                    } else if ("fulltext.txt".equals(name)) {
                        payload.setId(name);
                        payload.setLabel("Full Text");
                        payload.setContentType("text/plain");
                    } else if ("metadata.rdf".equals(name)) {
                        payload.setId(name);
                        payload.setLabel("RDF Metadata");
                        payload.setContentType("application/xml+rdf");
                    } else {
                        continue;
                    }
                    addPayload(payload);
                } catch (IOException ioe) {
                    log.error("", ioe);
                }
            }
        }
    }

}
