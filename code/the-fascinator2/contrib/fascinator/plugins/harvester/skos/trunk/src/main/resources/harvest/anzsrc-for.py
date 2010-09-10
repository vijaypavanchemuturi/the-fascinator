import time
from au.edu.usq.fascinator.api.storage import PayloadType
from au.edu.usq.fascinator.indexer.rules import AddField, CopyField, New, XslTransform

#
# Available objects:
#    indexer    : Indexer instance
#    jsonConfig : JsonConfigHelper of our harvest config file
#    rules      : RuleManager instance
#    object     : DigitalObject to index
#    payload    : Payload to index
#    params     : Metadata Properties object
#    pyUtils    : Utility object for accessing app logic
#

#
# Index a path separated value as multiple field values
#
def indexPath(name, path, includeLastPart=True):
    parts = path.split("/")
    length = len(parts)
    if includeLastPart:
        length +=1
    for i in range(1, length):
        part = "/".join(parts[:i])
        if part != "":
            if part.startswith("/"):
                part = part[1:]
            rules.add(AddField(name, part))

pyUtils.registerNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
pyUtils.registerNamespace("dc", "http://purl.org/dc/terms/")
pyUtils.registerNamespace("skos", "http://www.w3.org/2004/02/skos/core#")

oid = object.getId()
pid = payload.getId()
metaPid = params.getProperty("metaPid")

print "*** metaPid: ", metaPid

if pid == metaPid:
    #XslTransform is currently broken - so just process the XML directly
    #rules.add(XslTransform(pyUtils.getResource("/xsl/DublinCoreToSolr.xsl")))

    skosPayload = object.getPayload(object.getSourceId())
    skos = pyUtils.getXmlDocument(skosPayload.open())
    skosPayload.close()
    print "*** skos: ", skos
#    nodes = dc.selectNodes("//dc:*")
#    for node in nodes:
#        name = "dc_" + node.getName()
#        text = node.getTextTrim()
#        rules.add(AddField(name, text))
    itemType = "object"
#else:
#    rules.add(New())
#    oid += "/" + pid
#    itemType = "datastream"
#    rules.add(AddField("identifier", pid))

rules.add(AddField("id", oid))
rules.add(AddField("last_modified", time.strftime("%Y-%m-%dT%H:%M:%SZ")))
rules.add(AddField("item_type", itemType))
rules.add(AddField("harvest_config", params.getProperty("jsonConfigOid")))
rules.add(AddField("harvest_rules",  params.getProperty("rulesOid")))
#
## Security
#roles = pyUtils.getRolesWithAccess(oid)
#if roles is not None:
#    for role in roles:
#        rules.add(AddField("security_filter", role))
#else:
#    # Default to guest access if Null object returned
#    schema = pyUtils.getAccessSchema("derby");
#    schema.setRecordId(oid)
#    schema.set("role", "guest")
#    pyUtils.setAccessSchema(schema, "derby")
#    rules.add(AddField("security_filter", "guest"))
#
#rules.add(AddField("repository_name", params["repository.name"]))
#rules.add(AddField("repository_type", params["repository.type"]))
#
#rules.add(CopyField("title", "dc_title"))
#rules.add(CopyField("description", "dc_description"))
