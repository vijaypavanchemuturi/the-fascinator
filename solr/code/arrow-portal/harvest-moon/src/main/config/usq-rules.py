from au.edu.usq.solr.index.rule import RuleManager
from au.edu.usq.solr.index.rule.impl import *

#
# Available objects:
#    self: harvester
#    rules: rule manager
#    pid: registry pid
#    name: repository name
#    item: metadata item
#    dsId: datastream id or None if item is object
#

# dc to solr transform
rules.add(TransformRule(self.getResource("/xsl/dc_solr.xsl")))

# at least one relation with url value
rules.add(CheckFieldRule("relation", "http.*"))

# at least one non-blank title
rules.add(CheckFieldRule("title", ".+"))

# use only the year from date field
rules.add(ModifyFieldRule("date", ".*(\\d{4}).*", "$1"))

# delete invalid dates
rules.add(DeleteFieldRule("date", "(.*[^0-9].*)|(^\\s*$)"))

# delete blank subject
rules.add(DeleteFieldRule("subject", "^\\s*$"))

# delete blank creator
rules.add(DeleteFieldRule("creator", "^\\s*$"))

# reformat subject from 'code description' to 'description (code)'
rules.add(ModifyFieldRule("subject", "^(\\d{6}) (.*)$", "$2 ($1)"))

# reformat types to MACAR standards
rules.add(ModifyFieldRule("type", ".*Book Chapter.*", "book chapter"))
rules.add(ModifyFieldRule("type", "PeerReviewed", "peer reviewed"))

# repository name
rules.add(AddFieldRule("repository_name", "USQ"))

if dsId is None:
    solrId = item.getId();
    itemType = "object"
else:
    solrId = item.getId() + "/" + dsId
    itemType = "datastream"

# unique identifier
rules.add(AddFieldRule("id", solrId))

# registry pid
rules.add(AddFieldRule("pid", pid))

# item type
rules.add(AddFieldRule("item_type", itemType))

# group access
#   default to "guest"
groupAccess = AddFieldRule("group_access", "guest")
rules.add(groupAccess)

# item class
rules.add(AddFieldRule("item_class", "document"))
