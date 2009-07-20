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
package au.edu.usq.fascinator.portal.pages.user;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import au.edu.usq.fascinator.portal.State;
import au.edu.usq.fascinator.portal.User;
import au.edu.usq.fascinator.portal.pages.Start;
import au.edu.usq.fascinator.portal.services.UserManager;

@IncludeStylesheet("context:css/default.css")
public class Create {
    private Logger log = Logger.getLogger(Create.class);

    private User user;

    @InjectPage
    private Create createPage;

    @SessionState
    private State state;

    @Inject
    private UserManager userManager;

    private String username;

    Object onSuccess() {

        log.info("added: " + user.getUsername());

        String username = user.getUsername();
        String password = user.getPassword();

        password = userManager.encryptPassword(password);

        userManager.add(username, password);

        return List.class;
    }

    Object onActivate(Object[] params) {
        if (!state.userInRole("admin")) {
            return Start.class;
        }

        if (params.length == 0) {
            if (user == null) {
                user = new User("");
            }
        }
        return null;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String[] getUsers() {
        return userManager.getUsers();
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
