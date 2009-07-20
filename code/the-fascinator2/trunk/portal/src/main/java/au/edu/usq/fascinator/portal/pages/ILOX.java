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
package au.edu.usq.fascinator.portal.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.ContentType;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import au.edu.usq.fascinator.model.types.DocumentType;

@ContentType("text/xml")
public class ILOX extends Search {

    public class Expression {
        private String id = "";
        private Document lom = null;
        private HashMap<String, Manifestation> manifestations;

        public Expression(String id, Document lom) {
            this.id = id;
            this.lom = lom;
            manifestations = new HashMap<String, Manifestation>();
        }

        public String getId() {
            return id;
        }

        public String getLomStr() {
            return lom.getRootElement().asXML();
        }

        public HashMap<String, Manifestation> getManifestations() {
            return manifestations;
        }

        public Document getLom() {
            return lom;
        }

        public void setLom(Document lom) {
            this.lom = lom;
        }

        public Manifestation getManifestation(String id, String name) {
            Manifestation manifestation = null;
            if (manifestations.containsKey(id)) {
                manifestation = manifestations.get(id);
            } else {
                manifestation = new Manifestation(id, name, null);
                manifestations.put(id, manifestation);
            }
            return manifestation;
        }

        public List<Document> getItemLoms() {
            List<Document> loms = new ArrayList<Document>();
            for (Manifestation manifestation : manifestations.values()) {
                loms.addAll(manifestation.getItemLoms());
            }
            return loms;
        }
    }

    public class Manifestation {
        private String id = "";
        private String name = "";
        private Document lom = null;
        private List<Item> items;

        public Manifestation(String id, String name, Document lom) {
            this.id = id;
            this.name = name;
            this.lom = lom;
            items = new ArrayList<Item>();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLomStr() {
            return lom.getRootElement().asXML();
        }

        public List<Item> getItems() {
            return items;
        }

        public Document getLom() {
            return lom;
        }

        public void setLom(Document lom) {
            this.lom = lom;
        }

        public List<Document> getItemLoms() {
            List<Document> loms = new ArrayList<Document>();
            for (Item item : items) {
                loms.add(item.getLom());
            }
            return loms;
        }
    }

    public class Item {
        private Document lom = null;
        private String uri = "";
        private String expressionId = "";
        private String manifestationId = "";
        private String manifestationName = "";
        private String catalog = "";
        private String entry = "";

        public Item(Document lom) {
            this.lom = lom;
            this.uri = getItemUri(lom);
            processLom(lom);
        }

        public String getUri() {
            return uri;
        }

        public String getLomStr() {
            return lom.getRootElement().asXML();
        }

        public String getExpressionId() {
            return expressionId;
        }

        public String getManifestationId() {
            return manifestationId;
        }

        public String getManifestationName() {
            return manifestationName;
        }

        public String getCatalog() {
            return catalog;
        }

        public String getEntry() {
            return entry;
        }

        public Document getLom() {
            return lom;
        }

        private String getItemUri(Document lom) {
            String uri = "";
            Node location = lom.selectSingleNode("lom:lom/lom:technical/lom:location");
            if (location != null) {
                uri = location.getText().replace("&", "&amp;");
            }
            return uri;
        }

        private void processLom(Document lom) {
            Node taxonpath = lom.selectSingleNode("lom:lom/lom:classification/lom:taxonpath"
                + "[lom:taxon/lom:id]"
                + "[starts-with(./lom:source/lom:langstring, 'FRBR')"
                + "or starts-with(./lom:source/lom:string, 'FRBR')]");
            List<Node> taxons = taxonpath.selectNodes("lom:taxon");
            try {
                expressionId = taxons.get(1)
                    .selectSingleNode("lom:id")
                    .getText();
                manifestationId = taxons.get(2)
                    .selectSingleNode("lom:id")
                    .getText();
                manifestationName = taxons.get(2)
                    .selectSingleNode(
                        "lom:entry/lom:langstring | lom:entry/lom:string")
                    .getText();
            } catch (Exception e) {
                log.info("** Item.processLom execption - " + e.getMessage());
            }
            Node node;
            node = lom.selectSingleNode("lom:lom/lom:general/lom:catalogentry/lom:catalog | "
                + "lom:lom/lom:general/lom:identifier/lom:catalog");
            if (node != null) {
                catalog = node.getText();
            }
            node = lom.selectSingleNode("lom:lom/lom:general/lom:catalogentry/lom:entry/lom:langstring | "
                + "lom:lom/lom:general/lom:identifier/lom:entry");
            if (node != null) {
                entry = node.getText();
            }
            // log.info("\n expressionId="+expressionId+
            // "\n manifestationId="+manifestationId+", name="+manifestationName+
            // "\n catalog="+catalog+", entry="+entry);
        }
    }

    private Logger log = Logger.getLogger(ILOX.class);

    // @Inject
    // private RegistryManager registryManager;

    @Inject
    private org.apache.tapestry5.services.Response httpResponse;

    private List<String> uuids = new ArrayList<String>();
    private String idCatalog = "";
    private String idEntry = "";
    private Document workLom = null;
    private HashMap<String, Expression> expressions = new HashMap<String, Expression>();

    @Override
    void onActivate(Object[] params) {
        String uuid = null; // "uuid:10e34f01-63d4-437c-bc59-c10975d44f42";
        uuids.clear();
        expressions.clear();
        for (Object o : params) {
            String param = o.toString();
            if (param.startsWith("uuid:")) {
                uuid = param;
            }
        }

        super.setSearchEscape(uuid != null);
        super.onActivate(params);
        try {
            List<DocumentType> items = getResponse().getResult().getItems();
            for (DocumentType doc : items) {
                uuids.add(doc.field("pid"));
                // log.info(" pid=" + doc.field("pid"));
            }
        } catch (Exception e) {
            log.info("No uuid(s) found! *********");
            return;
        }

        process(uuids);
    }

    private void process(List<String> uuids) {
        log.info("process()");
        List<Document> loms = new ArrayList<Document>();
        if (uuids.isEmpty())
            return;

        // Get all the Item LOM's
        for (String uuid : uuids) {
            Document lom = getLom(uuid);
            loms.add(lom);
        }

        for (Document lom : loms) {
            Item item = new Item(lom);
            idCatalog = item.catalog;
            idEntry = item.entry;
            Expression expression = getExpression(item.getExpressionId());
            Manifestation manifestation = expression.getManifestation(
                item.getManifestationId(), item.getManifestationName());
            manifestation.getItems().add(item);
        }

        // Work (LOM) - workLom
        workLom = extractCommon(loms);

        // Expression(s) - Group LOMs by expressions
        for (Expression expression : expressions.values()) {
            // get and set the expression's LOM
            List<Document> expressionLoms = expression.getItemLoms();
            Document expressionLom = extractCommon(expressionLoms);
            expression.setLom(expressionLom);

            // Manifestation(s) - Group LOMs by manifestations
            for (Manifestation manifestation : expression.getManifestations()
                .values()) {
                // get and set the manifestation's LOM
                List<Document> manifestationLoms = manifestation.getItemLoms();
                Document manifestationLom = extractCommon(manifestationLoms);
                manifestation.setLom(manifestationLom);
            }
        }
    }

    private Expression getExpression(String id) {
        Expression expression = null;
        if (expressions.containsKey(id)) {
            expression = expressions.get(id);
        } else {
            expression = new Expression(id, null);
            expressions.put(id, expression);
        }
        return expression;
    }

    private Document extractCommon(List<Document> loms) {
        Document lom = (Document) loms.get(0).clone();
        List<Node> nodes;
        // log.info("\n\n*** uniquePath=" + node.getUniquePath());
        HashMap<String, Node> commonNodes = new HashMap<String, Node>();

        // get a list of all nodes that are the same in all the LOMs
        // remove this list of nodes from all the LOMs
        // keep only this list of nodes (and there parents) in the work LOM
        nodes = lom.selectNodes("//*");
        ArrayList<String> xpaths = new ArrayList<String>();
        // log.info("\n\n extractCommon()");
        // log.info("nodes=" + nodes.size());
        for (Node n : nodes) {
            String xpath = n.getUniquePath();
            xpaths.add(xpath);
            commonNodes.put(xpath, n);
        }
        // log.info("commonNodes="+commonNodes.size());
        for (Document doc : loms) {
            String[] array = new String[commonNodes.size()];
            array = commonNodes.keySet().toArray(array);
            for (String xpath : array) {
                Node cNode = commonNodes.get(xpath);
                Node tNode = doc.selectSingleNode(xpath);
                if (tNode == null || !tNode.asXML().equals(cNode.asXML())) {
                    // log.info("remove. " + xpath);
                    commonNodes.remove(xpath);
                }
            }
        }
        // log.info("commonNodes="+commonNodes.size());
        // ok now we have a list of known common nodes
        // now add back in any missing parent nodes (e.g. the root node etc)
        for (Object o : commonNodes.values().toArray()) {
            Node n = (Node) o;
            Node parent = n.getParent();
            if (parent == null) {
                // log.info(" *** parent is null - " + n.getUniquePath());
                continue;
            }
            commonNodes.put(parent.getUniquePath(), parent);
        }
        // log.info("commonNodes="+commonNodes.size());
        // ok now get a list of nodes which are to be removed (from the lom) -
        // not common
        ArrayList<String> removeXPaths = new ArrayList<String>();
        for (String xp : xpaths) {
            if (!commonNodes.containsKey(xp)) {
                removeXPaths.add(xp);
                // log.info("-- remove xpath="+xp);
            }
        }
        ArrayList<Node> removeNodes = new ArrayList<Node>();
        for (String xp : removeXPaths) {
            // log.info("*** remove " + xp);
            Node n = lom.selectSingleNode(xp);
            if (n != null)
                removeNodes.add(n);
        }
        for (Node n : removeNodes) {
            if (n == null) {
                continue;
            }
            Element parent = n.getParent();
            if (parent != null) {
                parent.remove(n);
            }
        }
        // now for all lom's remove all nodes that are not in the remove list
        // (or its parents)
        // add all parent nodes
        for (Object o : removeXPaths.toArray()) {
            String xp = o.toString();
            String pxp = xp;
            while (true) {
                int lastIndex = pxp.lastIndexOf("/");
                if (lastIndex == -1) {
                    break;
                }
                pxp = pxp.substring(0, lastIndex);
                if (pxp == null || !"".equals(pxp)) {
                    break;
                }
                if (!removeXPaths.contains(pxp)) {
                    removeXPaths.add(pxp);
                }
            }
        }

        for (Document doc : loms) {
            nodes = doc.selectNodes("//*");
            removeNodes.clear();
            if (false) { // Do not remove the the classification node that
                // contains FRBR taxonpath
                Node node = doc.selectSingleNode("lom:lom/lom:classification["
                    + "starts-with(./lom:taxonpath/lom:source/lom:langstring, 'FRBR')"
                    + "or starts-with(./lom:taxonpath/lom:source/lom:string, 'FRBR')"
                    + "]");
                if (node != null) {
                    String xpath = node.getUniquePath();
                    if (!removeXPaths.contains(xpath))
                        removeXPaths.add(xpath);
                    for (Object o : node.selectNodes(".//*")) {
                        Node n = (Node) o;
                        xpath = n.getUniquePath();
                        if (!removeXPaths.contains(xpath))
                            removeXPaths.add(xpath);
                    }
                }
            }
            for (Node n : nodes) {
                String xpath = n.getUniquePath();
                if (removeXPaths.contains(xpath)) {
                    // keep
                } else {
                    removeNodes.add(n);
                }
            }
            for (Node n : removeNodes) {
                Element parent = n.getParent();
                if (parent != null) {
                    parent.remove(n);
                }
            }
        }
        return lom;
    }

    @Override
    public boolean getHasResults() {
        return !uuids.isEmpty();
    }

    public String getIdCatalog() {
        return idCatalog;
    }

    public String getIdEntry() {
        return idEntry;
    }

    public String getWorkLomStr() {
        return workLom.getRootElement().asXML();
    }

    public HashMap<String, Expression> getExpressions() {
        return expressions;
    }

    // ---------------------------------
    private Document getLom(String uuid) {
        Document doc = null;
        Node node;
        String lomStr = getLomString(uuid);
        String[] lomNSs = { "http://www.imsglobal.org/xsd/imsmd_rootv1p2p1",
            "http://ltsc.ieee.org/xsd/LOM",
            "http://ltsc.ieee.org/xsd/imscc/LOM" };
        for (String lomNS : lomNSs) {
            doc = getLomWithNS(lomStr, lomNS);
            if (doc != null) {
                node = doc.selectSingleNode("lom:lom");
                if (node != null) {
                    // log.info(" using ns '" + lomNS + "'");
                    break;
                }
            }
        }
        return doc;
    }

    private Document getLomWithNS(String lomStr, String lomNS) {
        HashMap<String, String> nsMap;
        nsMap = new HashMap<String, String>();
        nsMap.put("lom", lomNS);
        DocumentFactory.getInstance().setXPathNamespaceURIs(nsMap);
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(lomStr);
        } catch (DocumentException e) {

        }
        return doc;
    }

    private String getLomString(String uuid) {
        String dsId = "LOM";
        // if (true) {
        // DatastreamType ds = registryManager.getDatastream(uuid, dsId);
        // // String contentType = ds.getMimeType();
        // InputStream content = registryManager.getDatastreamAsStream(uuid,
        // dsId);
        // BufferedReader reader = new BufferedReader(new InputStreamReader(
        // content));
        // StringBuilder sb = new StringBuilder();
        // String line;
        // try {
        // while ((line = reader.readLine()) != null) {
        // sb.append(line + "\n");
        // }
        // } catch (IOException e) {
        // }
        // return sb.toString();
        // }
        return "<lom xmlns='http://ltsc.ieee.org/xsd/LOM' />";
    }

    /*
     * 
     * private Document getExpressionLom(Document lom) { // keep
     * general/language and lifecycle/version & lifecycle/status Element newLom
     * = DocumentHelper.createElement("lom:lom"); Document docLom =
     * DocumentHelper.createDocument(newLom); Element general =
     * DocumentHelper.createElement("lom:general"); Element lifeCycle =
     * DocumentHelper.createElement("lom:lifecycle"); newLom.add(general);
     * newLom.add(lifeCycle); Node languageNode = null; Node version = null;
     * Node status = null; languageNode =
     * lom.selectSingleNode("lom:lom/lom:general/lom:language"); version =
     * lom.selectSingleNode
     * ("lom:lom/lom:lifecycle/lom:version | lom:lom/lom:lifeCycle/lom:version"
     * ); status =lom.selectSingleNode(
     * "lom:lom/lom:lifecycle/lom:status | lom:lom/lom:lifeCycle/lom:status");
     * Node node = status; if(languageNode == null){
     * //log.info("* Failed to find langauge node!"); } else { languageNode =
     * (Node) languageNode.clone(); general.add(languageNode); } if(version ==
     * null){ //log.info("* Failed to find version node!"); } else { version =
     * (Node) version.clone(); lifeCycle.add(version); } if(status == null){
     * //log.info("* Failed to find status node!"); } else { status = (Node)
     * status.clone(); lifeCycle.add(status); } return docLom; }
     * 
     * private Document getManifestationLom(Document lom) { // keep
     * technical/format & technical/size Element newLom =
     * DocumentHelper.createElement("lom:lom"); Document docLom =
     * DocumentHelper.createDocument(newLom); Node technical = (Node)
     * lom.selectSingleNode( "lom:lom/lom:technical").clone(); Node location =
     * technical.selectSingleNode("lom:location");
     * location.getParent().remove(location); newLom.add(technical); return
     * docLom; }
     * 
     * private void processLom_OldX(Document lom){ Node node; List<Node> list;
     * String id; String name; Expression expression; HashMap<String,
     * Manifestation> manifestations; Manifestation manifestation; List<Item>
     * items; // expressionId, manifestationId & manifestationName list =
     * lom.selectNodes
     * ("lom:lom/lom:classification/lom:taxonpath[lom:taxon/lom:id]"); for (Node
     * taxonpath : list) { node =taxonpath.selectSingleNode(
     * "lom:source/lom:langstring | lom:source/lom:string"); if (node != null) {
     * if (node.getText().startsWith("FRBR")) { // OK we have found the correct
     * taxonpath //log.info("** OK we have found a FRBR taxonpath"); List<Node>
     * taxons = taxonpath.selectNodes("lom:taxon"); try { id =
     * taxons.get(1).selectSingleNode("lom:id").getText(); if
     * (!expressions.containsKey(id)) { expressions.put(id, new Expression(id,
     * getExpressionLom(lom))); } expression = expressions.get(id); id =
     * taxons.get(2).selectSingleNode( "lom:id").getText(); name =
     * taxons.get(2).selectSingleNode(
     * "lom:entry/lom:langstring | lom:entry/lom:string").getText();
     * manifestations = expression.getManifestations();
     * if(!manifestations.containsKey(id)) { manifestations.put(id, new
     * Manifestation(id, name, getManifestationLom(lom))); } manifestation =
     * manifestations.get(id); items = manifestation.getItems(); items.add(new
     * Item(lom)); break; } catch (Exception e) {
     * log.info("** processLom execption - " + e.getMessage()); } } } } } /
     */
}
