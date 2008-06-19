from au.edu.usq.solr.harvest.rule import RuleManager
from au.edu.usq.solr.harvest.rule.impl import StylesheetRule, AddFieldRule

#
# Available objects:
#    self: harvester
#    name:harvest job name
#    item: metadata item
#    pid : registry pid
#
# Return:
#    rules: RuleManager instance
#

rules = RuleManager()

# dc to solr transform
dcToSolr = StylesheetRule(self.getResource("/xsl/dc_solr.xsl"))
dcToSolr.setName("Transform DC to Solr")
rules.addRule(dcToSolr)

# unique identifier
rules.addRule(AddFieldRule("id", item.getId()))

# registry pid
rules.addRule(AddFieldRule("pid", pid))

# group access
#   default to "guest"
groupAccess = AddFieldRule("group_access", "guest")
rules.addRule(groupAccess)

# set to "admin" for dc:type == Australasian Digital Thesis
nodes = item.getMetadata().selectNodes("//dc:type")
for node in nodes:
    dcType = node.getText().strip()
    if dcType == "Australasian Digital Thesis":
        print "access for %s set to admin" % item.getId()
        groupAccess.setValue("admin")
        break

# item class
rules.addRule(AddFieldRule("item_class", "document"))

# item type
rules.addRule(AddFieldRule("item_type", "object"))
