/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.usq.fascinator.portal.services;

import java.io.InputStream;
import java.io.Writer;
import org.apache.velocity.context.Context;

/**
 *
 * @author lucido
 */
public interface VelocityService {

    public InputStream getResource(String resourcePath);

    public InputStream getResource(String portalId, String resourceName);

    public String resourceExists(String portalId, String resourceName);

    public void renderTemplate(String portalId, String templateName,
            Context context, Writer writer) throws Exception;
}
