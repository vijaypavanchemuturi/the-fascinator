import time
from au.edu.usq.fascinator.indexer.rules import XslTransform, CheckField, ModifyField, DeleteField, AddField

# Available objects:
#    self: harvester
#    rules: rule manager
#    pid: registry pid
#    name: repository name
#    item: metadata item
#    dsId: datastream id or None if item is object
#

# dc to solr transform
rules.add(XslTransform(indexer.getResource("/xsl/DublinCoreToSolr.xsl")))

# at least one relation with url value
rules.add(CheckField("relation", "http.*"))

# at least one non-blank title
rules.add(CheckField("title", ".+"))

# use only the year from date field
rules.add(ModifyField("date", ".*(\\d{4}).*", "$1"))

# delete invalid dates
rules.add(DeleteField("date", "(.*[^0-9].*)|(^\\s*$)"))

# delete blank subject
rules.add(DeleteField("subject", "^\\s*$"))

# delete blank creator
rules.add(DeleteField("creator", "^\\s*$"))

# reformat subject from 'code description' to 'description (code)'
rules.add(ModifyField("subject", "^(\\d{6}) (.*)$", "$2 ($1)"))

# reformat types to MACAR standards
rules.add(ModifyField("type", "^Book \\(DEST Category A\\)$", "book"))
rules.add(ModifyField("type", "^Book Chapter \\(DEST Category B\\)$", "book chapter"))
rules.add(ModifyField("type", "^Conference or Workshop Item \\(DEST Category E\\)$", "conference item"))
rules.add(ModifyField("type", "^Article \\(DEST Category C\\)$", "journal article"))
rules.add(ModifyField("type", "^Patent$", "patent"))
rules.add(ModifyField("type", "^Report$", "report"))
rules.add(ModifyField("type", "^Thesis$", "thesis"))

rules.add(ModifyField("type", "^PeerReviewed$", "peer reviewed"))
rules.add(ModifyField("type", "^NonPeerReviewed$", "non peer reviewed"))

rules.add(ModifyField("type", "^ADT_Thesis$", "australasian digital thesis"))
rules.add(ModifyField("type", "^USQ Project$", "project"))
rules.add(ModifyField("type", "^Other$", "other"))

# repository name
rules.add(AddField("repository_name", params["repository.name"]))
rules.add(AddField("project_affiliation", params["project.affiliation"]))
rules.add(AddField("repository_type", params["repository.type"]))

if dsId is None:
    solrId = item.getId();
    itemType = "object"
else:
    solrId = item.getId() + "/" + dsId
    itemType = "datastream"
    rules.add(AddField("identifier", dsId))

# unique identifier
rules.add(AddField("id", solrId))
rules.add(AddField("last_modified", time.strftime("%Y-%m-%dT%H:%M:%SZ")))

# registry pid
rules.add(AddField("pid", pid))

# item type
rules.add(AddField("item_type", itemType))

# group access
#   default to "guest"
groupAccess = AddField("group_access", "guest")
rules.add(groupAccess)

# item class
rules.add(AddField("item_class", "document"))
