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
package au.edu.usq.fascinator.model.taxonomy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class Node {

    private Logger log = Logger.getLogger(Node.class);

    private String def;

    private String id;

    private String label;

    private String parent;

    private Node parentNode;

    private List<Node> children;

    public Node(String def) {
        this.def = def;
        String[] parts = StringUtils.split(def, '|');
        id = parts[0];
        label = parts[1];
        parent = parts[2];
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
        parentNode.addChild(this);
    }

    public List<Node> getChildren() {
        if (children == null) {
            children = new ArrayList<Node>();
        }
        return children;
    }

    public void addChild(Node child) {
        getChildren().add(child);
    }

    public boolean isRoot() {
        return "root".equals(parent);
    }

    public boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    @Override
    public String toString() {
        return def;
    }
}
