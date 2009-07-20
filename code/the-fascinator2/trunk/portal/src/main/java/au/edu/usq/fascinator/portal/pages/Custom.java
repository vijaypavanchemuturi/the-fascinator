/* 
 * The Fascinator - Solr Portal
 * Copyright (C) 2008  University of Southern Queensland
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package au.edu.usq.fascinator.portal.pages;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.util.TextStreamResponse;

import au.edu.usq.fascinator.portal.State;
import au.edu.usq.fascinator.portal.components.VelocityPanel;
import au.edu.usq.fascinator.util.BinaryStreamResponse;

@IncludeStylesheet("context:css/default.css")
@IncludeJavaScriptLibrary("context:js/default.js")
public class Custom {

    private Logger log = Logger.getLogger(Custom.class);

    @SessionState
    private State state;

    private String portalName;

    private String template;

    private String contentType;

    private List<String> params;

    @InjectComponent
    private VelocityPanel velocityPanel;

    Object onActivate(Object[] params) {
        for (Object param : params) {
            getParams().add(param.toString());
        }
        try {
            portalName = params[0].toString();
            template = params[1].toString() + ".vm";
            contentType = params[2].toString();
            return new BinaryStreamResponse("text/" + contentType,
                new ByteArrayInputStream(velocityPanel.render().getBytes(
                    "UTF-8")));
        } catch (Exception e) {
            log.error(e);
            return new TextStreamResponse("text/xml", "<the-fascinator><error>"
                + e.getMessage() + "</error></the-fascinator>");
        }
    }

    public State getState() {
        return state;
    }

    public String getPortalName() {
        return portalName;
    }

    public String getTemplate() {
        return template;
    }

    public String getContentType() {
        return contentType;
    }

    public List<String> getParams() {
        if (params == null) {
            params = new ArrayList<String>();
        }
        return params;
    }

    public String getParamString() {
        String s = "";
        int len = getParams().size();
        if (len > 3) {
            for (int i = 3; i < len; i++) {
                s += getParams().get(i) + "/";
            }
        }
        return s.substring(0, s.length() - 1);
    }
}
