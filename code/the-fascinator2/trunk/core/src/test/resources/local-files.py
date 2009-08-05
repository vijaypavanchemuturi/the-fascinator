import time
from au.edu.usq.fascinator.indexer.rules import AddField, New
from org.dom4j.io import SAXReader

#
# Available objects:
#    indexer   : Indexer instance
#    rules     : RuleManager instance
#    object    : DigitalObject to index
#    payloadId : Payload identifier
#    storageId : Storage layer identifier
#

def indexNode(doc, name, xPath="", value=""):
    if value:
        rules.add(AddField(name, value))
        return value
    node = doc.selectSingleNode(xPath)
    if node is not None:
        rules.add(AddField(name, node.getText()))
        return node.getText()
    return ""

rules.add(New())

if isMetadata:
    solrId = object.getId();
    itemType = "object"
else:
    solrId = object.getId() + "/" + payloadId
    itemType = "datastream"
    rules.add(AddField("identifier", payloadId))

# path
path = object.getId().replace("\\", "/")
parts = path.split("/")
for i in range(1, len(parts)):
    part = "/".join(parts[:i])
    if part != "":
        if part.startswith("/"):
            part = part[1:]
        rules.add(AddField("file_path", part))

rules.add(AddField("id", solrId))
rules.add(AddField("last_modified", time.strftime("%Y-%m-%dT%H:%M:%SZ")))
rules.add(AddField("storageId", storageId))
rules.add(AddField("item_type", itemType))
rules.add(AddField("group_access", "guest"))
rules.add(AddField("item_class", "document"))

rules.add(AddField("repository_name", params["repository.name"]))
rules.add(AddField("repository_type", params["repository.type"]))

indexer.registerNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
indexer.registerNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
indexer.registerNamespace("dc", "http://purl.org/dc/elements/1.1/")
indexer.registerNamespace("foaf", "http://xmlns.com/foaf/0.1/")
indexer.registerNamespace("j.1", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0/")
indexer.registerNamespace("j.0", "http://www.semanticdesktop.org/ontologies/2007/01/19/nie#")
indexer.registerNamespace("j.2", "http://www.semanticdesktop.org/ontologies/2007/03/22/nco#")
indexer.registerNamespace("j.3", "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#")
indexer.registerNamespace("dcterms", "http://purl.org/dc/terms/")


rdf = indexer.getXmlDocument(object.getMetadata())
if rdf is not None:
    rootNode = rdf.getRootElement()
    #indexNode(rootNode, "title", "./rdf:Description/dc:title")
else:
    print " * No RDF metadata found!"

#rdfString = object.getMetadata()
#if rdfString is None:
#    rdfString = object.getPayload("rdf")  #in future use object.getMetadata("rdf")

#rootNode = indexer.getXmlDocument(rdfString.getInputStream())
#rootNode = indexer.getXmlDocument(object.getMetadata())

   
if rdf is not None:
    title = indexNode(rootNode, "dc.title", "/rdf:RDF/rdf:Description/dcterms:title")
    print "title: 1", title
    if title=="":
        title = indexNode(rootNode, "dc.title", "/rdf:RDF/rdf:Description/dc:title")
        print "title: 2", title
    if title=="":
        title = indexNode(rootNode, "dc.title", "/rdf:RDF/rdf:Description/j.0:title")
        print "title: 3", title
    if title:
        print "title: 4", title
        indexNode(rootNode, "dc.subject", value=title)

if isMetadata:
    indexer.registerNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    indexer.registerNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
    indexer.registerNamespace("dc", "http://purl.org/dc/elements/1.1/")
    indexer.registerNamespace("foaf", "http://xmlns.com/foaf/0.1/")
    indexer.registerNamespace("j.1", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0/")
    indexer.registerNamespace("j.0", "http://www.semanticdesktop.org/ontologies/2007/01/19/nie#")
    indexer.registerNamespace("j.2", "http://www.semanticdesktop.org/ontologies/2007/03/22/nco#")
    indexer.registerNamespace("j.3", "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#")
    indexer.registerNamespace("dcterms", "http://purl.org/dc/terms/")

    rdf = indexer.getXmlDocument(object.getMetadata())
    if rdf is not None:
        rootNode = rdf.getRootElement()
        #indexNode(rootNode, "title", "./rdf:Description/dc:title")
    else:
        print " * No RDF metadata found!"
    
    #rdfString = object.getMetadata()
    #if rdfString is None:
    #    rdfString = object.getPayload("rdf")  #in future use object.getMetadata("rdf")
    
    #rootNode = indexer.getXmlDocument(rdfString.getInputStream())
    #rootNode = indexer.getXmlDocument(object.getMetadata())
    
       
    if rdf is not None:
        title = indexNode(rootNode, "dc.title", "/rdf:RDF/rdf:Description/dcterms:title")        
        if title=="":
            title = indexNode(rootNode, "dc.title", "/rdf:RDF/rdf:Description/dc:title")
        if title=="":
            title = indexNode(rootNode, "dc.title", "/rdf:RDF/rdf:Description/j.0:title")
        if title:
            indexNode(rootNode, "dc.subject", value=title)
        
        dcDate = indexNode(rootNode, "dc.date", "/rdf:RDF/rdf:Description/dcterms:date")
        if dcDate:
            dcDate = indexNode(rootNode, "dc.date", "/rdf:RDF/rdf:Description/dc:date")
            
        creator = indexNode(rootNode, "dc.creator", "/rdf:RDF/rdf:Description/dcterms:creator")
        if creator=="":
            creator = indexNode(rootNode, "dc.creator", "/rdf:RDF/rdf:Description/dc:creator")
        if creator=="":
            creator = indexNode(rootNode, "dc.creator", "/rdf:RDF/rdf:Description/j.1:initial-creator")
        if creator=="":
            creator = indexNode(rootNode, "dc.creator", "/rdf:RDF/rdf:Description/j.1:fullname")
        if creator: 
            indexNode(rootNode, "dc.author", creator)
        
        format = indexNode(rootNode, "dc.creator", "/rdf:RDF/rdf:Description/dcterms:format")
        if format=="":
            format = indexNode(rootNode, "dc.format", "/rdf:RDF/rdf:Description/j.0:mimeType")
        
        indexNode(rootNode, "full_text", "/rdf:RDF/rdf:Description/j.0:plainTextContent")
        indexNode(rootNode, "dc.creationdate", "/rdf:RDF/rdf:Description/j.1:creation-date")












