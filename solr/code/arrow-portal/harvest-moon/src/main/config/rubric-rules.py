from java.lang import String
from au.edu.usq.solr.index.rule import RuleManager
from au.edu.usq.solr.index.rule.impl import TransformRule, AddFieldRule

#
# Available objects:
#    self: harvester
#    pid: registry pid
#    name: harvest job name
#    item: metadata item
#    dsId: datastream id or None if item is object
#
# Return:
#    rules: RuleManager instance
#
rules = RuleManager()

# dc to solr transform
rules.add(TransformRule(self.getResource("/xsl/dc_solr.xsl")))

# repository name
rules.add(AddFieldRule("repository_name", name))

# unique identifier
solrId = item.getId();
if dsId is not None:
    solrId = solrId + "/" + dsId
rules.add(AddFieldRule("id", solrId))

# registry pid
rules.add(AddFieldRule("pid", pid))

# item type
itemType = "object"
if dsId is not None:
    itemType = "datastream"
rules.add(AddFieldRule("item_type", itemType))

# group access
#   default to "guest"
#   set to "admin" for ADT items
#   add "on_campus" for conference proceedings
groupAccess = AddFieldRule("group_access", "guest")
rules.add(groupAccess)
nodes = item.getMetadata().selectNodes("//dc:type")
for node in nodes:
    dcType = node.getText().strip()
    if dcType == "Australasian Digital Thesis":
        groupAccess.setValue("admin")
    elif dcType == "conference proceedings":
        rules.add(AddFieldRule("group_access", "on_campus"))

# item class
rules.add(AddFieldRule("item_class", "document"))

# full text - get the FULLTEXT datastream
if dsId is None and item.hasDatastreams():
    ds = item.getDatastream("FULLTEXT")
    if ds is not None:
        rules.add(AddFieldRule("full_text", String(ds.getContent())))

