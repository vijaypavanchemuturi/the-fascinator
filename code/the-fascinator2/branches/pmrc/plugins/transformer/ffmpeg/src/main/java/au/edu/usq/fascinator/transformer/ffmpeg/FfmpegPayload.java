package au.edu.usq.fascinator.transformer.ffmpeg;

import java.io.File;

import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.common.storage.impl.FilePayload;

public class FfmpegPayload extends FilePayload {

    public FfmpegPayload(File payloadFile) {
        super(payloadFile);
        setType(PayloadType.Enrichment);
    }

}
