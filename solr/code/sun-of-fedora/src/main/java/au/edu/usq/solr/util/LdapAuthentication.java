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
package au.edu.usq.solr.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.log4j.Logger;

public class LdapAuthentication {

    private Logger log = Logger.getLogger(LdapAuthentication.class);

    private Hashtable<String, String> env;

    private String baseDn;

    private String idAttr;

    public LdapAuthentication(String baseUrl, String baseDn)
        throws NamingException {
        this(baseUrl, baseDn, "uid");
    }

    public LdapAuthentication(String baseUrl, String baseDn, String idAttr) {
        this.baseDn = baseDn;
        this.idAttr = idAttr;
        env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, baseUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
    }

    public boolean authenticate(String username, String password) {
        try {
            String principal = String.format("%s=%s,%s", idAttr, username,
                baseDn);
            env.put(Context.SECURITY_PRINCIPAL, principal);
            env.put(Context.SECURITY_CREDENTIALS, password);
            DirContext ctx = new InitialDirContext(env);
            ctx.lookup(principal);
            ctx.close();
            return true;
        } catch (NamingException e) {
            log.warn(e);
        }
        return false;
    }
}
