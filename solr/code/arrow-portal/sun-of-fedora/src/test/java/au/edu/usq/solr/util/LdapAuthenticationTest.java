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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class LdapAuthenticationTest {

    @Ignore
    @Test
    public void rubricLdap() throws Exception {
        LdapAuthentication ldap = new LdapAuthentication(
            "ldap://rubric-idp.usq.edu.au:389",
            "ou=tech-team,dc=rubric,dc=edu,dc=au");
        boolean result = ldap.authenticate("unknown", "wrong");
        Assert.assertFalse(result);
    }
}
