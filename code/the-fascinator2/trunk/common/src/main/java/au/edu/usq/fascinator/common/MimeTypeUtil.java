package au.edu.usq.fascinator.common;

import java.io.File;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.medsea.mimeutil.MimeUtil;

public class MimeTypeUtil {
    private static Logger log = LoggerFactory.getLogger(MimeTypeUtil.class);
    static {
        MimeUtil
                .registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
        MimeUtil
                .registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        // MimeUtil
        // .registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistoryMimeDetector");
    }

    @SuppressWarnings("unchecked")
    public static String getMimeType(File file) {
        Collection types = MimeUtil.getMimeTypes(file);
        return types.iterator().next().toString();
    }
}
