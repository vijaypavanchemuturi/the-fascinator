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

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.SessionState;

import au.edu.usq.fascinator.portal.MyData;
import au.edu.usq.fascinator.portal.State;

@IncludeStylesheet("context:css/default.css")
public class MyDetails {

    private Logger log = Logger.getLogger(MyDetails.class);

    @SessionState
    private State state;

    @Persist
    private MyData myData;

    @InjectPage
    private MyDetails myDetailsPage;

    Object onSuccess() {

        return myDetailsPage;
    }

    Object onActivate(Object[] params) {
        if (!state.userInRole("admin")) {
            return Start.class;
        }
        return null;
    }

    public MyData getMyData() {
        return myData;
    }

    public void setMyData(MyData myData) {
        this.myData = myData;
    }
}
