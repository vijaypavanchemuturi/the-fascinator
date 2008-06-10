/* 
 * Sun of Fedora - Solr Portal
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
package au.edu.usq.solr.portal.pages;

import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.Persist;

import au.edu.usq.solr.portal.State;

@IncludeStylesheet("context:css/default.css")
public class Login {

    @ApplicationState
    private State state;

    @InjectPage
    private Start startPage;

    private boolean cancelled;

    @Persist
    private String message;

    private String username;

    private String password;

    Object onSuccess() {
        message = null;
        if (!cancelled) {
            if (login()) {
                state.setProperty("user", username);
                state.setProperty("role", "admin");
            } else {
                message = "Invalid username or password!";
                return null;
            }
        }
        return startPage;
    }

    void onSelectedFromLogin() {
        cancelled = false;
    }

    void onSelectedFromCancel() {
        cancelled = true;
    }

    private boolean login() {
        System.out.println("username: " + username);
        return username != null && !"".equals(username);
    }

    public State getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFailed() {
        return message != null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
