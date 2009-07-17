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

def indexNode(doc, name, xPath):
    node = doc.selectSingleNode(xPath)
    if node is not None:
        rules.add(AddField(name, node.getText()))

rules.add(New())

if isMetadata:
    solrId = object.getId();
    itemType = "object"
else:
    solrId = object.getId() + "/" + payloadId
    itemType = "datastream"
    rules.add(AddField("identifier", payloadId))

rules.add(AddField("id", solrId))
rules.add(AddField("last_modified", time.strftime("%Y-%m-%dT%H:%M:%SZ")))
rules.add(AddField("storageId", storageId))
rules.add(AddField("item_type", itemType))
rules.add(AddField("group_access", "guest"))
rules.add(AddField("item_class", "document"))

rules.add(AddField("repository_name", params["repository.name"]))
rules.add(AddField("repository_type", params["repository.type"]))

indexer.registerNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
indexer.registerNamespace("dc", "http://purl.org/dc/elements/1.1/")
indexer.registerNamespace("nie", "http://www.semanticdesktop.org/ontologies/2007/01/19/nie#")   #j.0
indexer.registerNamespace("nfo", "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#")   #j.1

rdf = indexer.getXmlDocument(object.getMetadata())
if rdf is not None:
    rootNode = rdf.getRootElement()
    indexNode(rootNode, "title", "./rdf:Description/dc:title")
else:
    print " * No RDF metadata found!"

#    rootNode = rdf.getRootElement()
#    
#    ns = rootNode.getNamespace().getURI()
#    
#    #j.2:pageCount for pdf and no dc
#    
#    if rootNode.getNamespaceForPrefix("dc"):
#        indexNode(rootNode, "dc.title", "./rdf:Description/dc:title")
#        indexNode(rootNode, "dc.subject", "./rdf:Description/dc:title")
#        indexNode(rootNode, "dc.date", "./rdf:Description/dc:date")
#        indexNode(rootNode, "dc.author", "./rdf:Description/dc:creator")
#    
#    if rootNode.getNamespaceForPrefix("j.1"):
#        indexNode(rootNode, "dc.creator", "./rdf:Description/j.1:initial-creator")
#        indexNode(rootNode, "dc.creator", "./rdf:Description/j.1:fullname")
#        
#    if rootNode.getNamespaceForPrefix("j.0"):
#        indexNode(rootNode, "dc.title", "./rdf:Description/j.0:title")
#        indexNode(rootNode, "full_text", "./rdf:Description/j.0:plainTextContent")
#        indexNode(rootNode, "dc.format", "./rdf:Description/j.0:mimeType")
#        indexNode(rootNode, "dc.creationdate", "./rdf:Description/j.1:creation-date")
    
    












