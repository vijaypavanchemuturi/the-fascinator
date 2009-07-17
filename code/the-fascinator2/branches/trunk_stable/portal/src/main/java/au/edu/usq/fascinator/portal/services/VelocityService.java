package au.edu.usq.fascinator.portal.services;

import java.io.OutputStream;
import java.util.Map;

import org.apache.tapestry5.ioc.Resource;

public interface VelocityService {

    public void mergeDataWithResource(Resource template,
        OutputStream outputStream, Map<String, Object> parameterMap);

}
