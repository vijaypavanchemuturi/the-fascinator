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
package au.edu.usq.solr.model.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import au.edu.usq.solr.util.MapEntryType;

@XmlRootElement(name = "doc")
@XmlAccessorType(XmlAccessType.NONE)
public class DocumentType {

    private static final int MAX_LENGTH = 500;

    @XmlElement(name = "arr")
    private List<ArrayType> arrays;

    private Map<String, ArrayType> arrayMap;

    @XmlElement(name = "float")
    private List<MapEntryType> floats;

    private Map<String, Float> floatMap;

    @XmlElement(name = "str")
    private List<MapEntryType> values;

    private Map<String, String> valueMap;

    public ArrayType getArray(String name) {
        if (arrayMap == null) {
            arrayMap = new HashMap<String, ArrayType>();
            for (ArrayType entry : arrays) {
                arrayMap.put(entry.getName(), entry);
            }
        }
        return arrayMap.get(name);
    }

    public Float getFloat(String name) {
        if (floatMap == null) {
            floatMap = new HashMap<String, Float>();
            for (MapEntryType entry : floats) {
                floatMap.put(entry.getName(), new Float(entry.getValue()));
            }
        }
        return floatMap.get(name);
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

    public String field(String name) {
        String value = getValue(name);
        if (value == null) {
            try {
                value = getArray(name).getValues().get(0);
            } catch (NullPointerException e) {
                value = null;
            }
        }
        if (value == null) {
            try {
                value = Float.toString(getFloat(name));
            } catch (NullPointerException e) {
                value = null;
            }
        }
        return value;
    }

    public String shortField(String name) {
        String value = field(name);
        if (value != null && value.length() > MAX_LENGTH) {
            value = value.substring(0, MAX_LENGTH);
        }
        return value;
    }

    public List<String> fields(String name) {
        ArrayType array = getArray(name);
        if (array == null) {
            return Collections.emptyList();
        }
        return array.getValues();
    }

    public int getMaxLength() {
        return MAX_LENGTH;
    }
}
