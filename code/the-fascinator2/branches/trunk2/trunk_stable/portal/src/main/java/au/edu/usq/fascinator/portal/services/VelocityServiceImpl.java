/*
 * Copyright (c) 2008, Your Corporation. All Rights Reserved.
 */

package au.edu.usq.fascinator.portal.services;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import org.apache.tapestry5.ioc.Resource;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class VelocityServiceImpl implements VelocityService {

    public VelocityServiceImpl(Resource config) {
        try {
            Properties properties = new Properties();
            properties.load(config.toURL().openStream());
            Velocity.init(properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void mergeDataWithResource(Resource template,
        OutputStream outputStream, Map<String, Object> parameterMap) {
        try {
            VelocityContext context = new VelocityContext();
            for (String key : parameterMap.keySet()) {
                context.put(key, parameterMap.get(key));
            }
            System.err.println(" *** " + template.getPath());

            Template velocityTemplate = Velocity.getTemplate(template.getPath());
            if (template != null) {
                Writer out = new OutputStreamWriter(outputStream);
                velocityTemplate.merge(context, out);
                out.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
