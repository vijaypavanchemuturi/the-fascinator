package au.edu.usq.fascinator.common;

import java.io.File;
import java.util.Collection;

import eu.medsea.mimeutil.MimeUtil;

public class MimeTypeUtil {

    static {
        MimeUtil
                .registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
        MimeUtil
                .registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        MimeUtil
                .registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistoryMimeDetector");
    }

    @SuppressWarnings("unchecked")
    public static String getMimeType(File file) {
        Collection<String> types = MimeUtil.getMimeTypes(file);
        return types.iterator().next();
    }
}
