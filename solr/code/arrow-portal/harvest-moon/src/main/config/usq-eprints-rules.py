from java.lang import String
from au.edu.usq.solr.index.rule import RuleManager
from au.edu.usq.solr.index.rule.impl import TransformRule, AddFieldRule

#
# Available objects:
#    self: harvester
#    pid : registry pid
#    name: harvest job name
#    item: metadata item
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
rules.add(AddFieldRule("id", item.getId()))

# registry pid
rules.add(AddFieldRule("pid", pid))

# group access
#   default to "admin"
groupAccess = AddFieldRule("group_access", "admin")
rules.add(groupAccess)

# item class
rules.add(AddFieldRule("item_class", "document"))

# item type
rules.add(AddFieldRule("item_type", "object"))
