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
package au.edu.usq.solr.portal;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PortalTest {

    private Portal portal;

    @Before
    public void setup() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Portal.class);
        Unmarshaller um = jc.createUnmarshaller();
        portal = (Portal) um.unmarshal(getClass().getResourceAsStream(
            "/portal.xml"));
    }

    @Test
    public void load() {
        Assert.assertEquals("default", portal.getName());
        Assert.assertEquals("Everything", portal.getDescription());
        Assert.assertEquals(10, portal.getRecordsPerPage());
        Assert.assertEquals(25, portal.getFacetCount());

        Map<String, String> map = new HashMap<String, String>();
        map.put("repository_name", "Repository");
        map.put("f_creator", "Author");
        map.put("f_subject", "Subject");
        map.put("f_type", "Type");
        Map<String, String> facetFields = portal.getFacetFields();
        Assert.assertEquals(map, facetFields);
    }
}
