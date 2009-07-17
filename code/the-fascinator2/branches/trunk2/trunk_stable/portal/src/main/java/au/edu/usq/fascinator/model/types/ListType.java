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
package au.edu.usq.fascinator.model.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import au.edu.usq.fascinator.common.jaxb.MapEntryType;

@XmlRootElement(name = "lst")
@XmlAccessorType(XmlAccessType.NONE)
public class ListType {

    @XmlAttribute
    private String name;

    @XmlElement(name = "lst")
    private List<ListType> lists;

    private Map<String, ListType> listMap;

    @XmlElement(name = "arr")
    private List<ArrayType> arrays;

    private Map<String, ArrayType> arrayMap;

    @XmlElements( { @XmlElement(name = "int"), @XmlElement(name = "str") })
    private List<MapEntryType> values;

    private Map<String, String> valueMap;

    public String getName() {
        return name;
    }

    public List<ListType> getLists() {
        return lists;
    }

    public ListType getList(String name) {
        if (listMap == null) {
            listMap = new HashMap<String, ListType>();
            for (ListType entry : lists) {
                listMap.put(entry.getName(), entry);
            }
        }
        return listMap.get(name);
    }

    public ArrayType getArray(String name) {
        if (arrayMap == null) {
            arrayMap = new HashMap<String, ArrayType>();
            for (ArrayType entry : arrays) {
                arrayMap.put(entry.getName(), entry);
            }
        }
        return arrayMap.get(name);
    }

    public List<MapEntryType> getValues() {
        if (values == null) {
            values = new ArrayList<MapEntryType>();
        }
        return values;
    }

    public String getValue(String name) {
        if (valueMap == null) {
            valueMap = new HashMap<String, String>();
            for (MapEntryType entry : values) {
                valueMap.put(entry.getName(), entry.getValue());
            }
        }
        return valueMap.get(name);
    }
}
