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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Taxonomy {

    private Logger log = Logger.getLogger(Taxonomy.class);

    private Node root;

    private Map<String, Node> nodeMap;

    public Taxonomy() {
        nodeMap = new HashMap<String, Node>();
    }

    public void load(Reader reader) {
        try {
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                Node node = addNode(line);
                if (node.isRoot()) {
                    root = node;
                } else {
                    Node parentNode = getNode(node.getParent());
                    if (parentNode == null) {
                        log.error("Parent not found for: " + node);
                    } else {
                        node.setParentNode(parentNode);
                    }
                }
            }
        } catch (IOException ioe) {
            log.error(ioe);
        }
    }

    private Node getNode(String id) {
        return nodeMap.get(id);
    }

    private Node addNode(String def) {
        Node node = new Node(def);
        nodeMap.put(node.getId(), node);
        return node;
    }

    public Node getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return root.getLabel();
    }
}
