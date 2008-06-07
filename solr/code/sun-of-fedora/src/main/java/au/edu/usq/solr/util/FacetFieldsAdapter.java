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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class FacetFieldsAdapter extends
    XmlAdapter<FacetFieldsType, Map<String, String>> {

    @Override
    public FacetFieldsType marshal(Map<String, String> facetFields)
        throws Exception {
        return new FacetFieldsType(facetFields);
    }

    @Override
    public Map<String, String> unmarshal(FacetFieldsType fields)
        throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        for (FacetFieldsEntryType entry : fields.getEntries()) {
            map.put(entry.getName(), entry.getLabel());
        }
        return map;
    }
}