from au.edu.usq.solr.index.rule import RuleManager
from au.edu.usq.solr.index.rule.impl import *

#
# Available objects:
#    self: Harvest instance
#    rules: RuleManager instance
#    item: metadata item
#    pid: registry object pid
#    dsId: datastream id or None if item is object
#
datastreamMode = dsId is not None
if not datastreamMode:
    #
    # full processing mode starts with the dublin core document. this is
    # intended for indexing main item records.
    #
    # dc to solr transform
    rules.add(TransformRule(self.getResource("/xsl/dc_solr.xsl")))
    # at least one non-blank title
    rules.add(CheckFieldRule("title", ".+"))
    # use only the year from date field
    rules.add(ModifyFieldRule("date", ".*(\\d{4}).*", "$1"))
    # delete blank subject
    rules.add(DeleteFieldRule("subject", "^\\s*$"))
    # delete blank creator
    rules.add(DeleteFieldRule("creator", "^\\s*$"))
    # delete invalid dates
    rules.add(DeleteFieldRule("date", "(.*[^0-9].*)|(^\\s*$)"))
    
    # set repository name
    rules.add(AddFieldRule("repository_name", "UNE"))
    # set unique identifier (e.g. oai id or fedora pid)
    rules.add(AddFieldRule("id", item.getId()))
    # set registry pid
    rules.add(AddFieldRule("pid", pid))
    # set item type to object
    rules.add(AddFieldRule("item_type", "object"))
    
    # item class
    #   default to "document"
    #   set to "image" for dc:type == "still image"
    itemClass = AddFieldRule("item_class", "document")
    rules.add(itemClass)
    nodes = item.getMetadata().selectNodes("//dc:type")
    for node in nodes:
        dcType = node.getText().strip()
        if dcType == "still image":
            itemClass.setValue("image")
    
    # group access
    #   default to "guest"
    #   set to "admin" for ADT items
    #   add "on_campus" for conference proceedings
    rules.add(AddFieldRule("group_access", "guest"))
    
    # full text - any datastreams with type application/pdf
    if item.hasDatastreams():
        for ds in item.getDatastreams():
            if ds.getMimeType() == "application/pdf":
                tmpFile = self.getFullText(ds.getMimeType(), ds.getContentAsStream())
                fp = open(tmpFile)
                rules.add(AddFieldRule("full_text", fp.read()))
                fp.close()
else:
    #
    # datastream mode starts with a blank solr document. this is intended for
    # indexing datastreams.
    #
    # skip FULLTEXT datastreams
    if dsId == "FULLTEXT":
        rules.add(SkipRule("Full text datastream"))
    else:
        ds = item.getDatastream(dsId)
        # set unique id
        rules.add(AddFieldRule("id", item.getId() + "/" + dsId))
        # set registry pid
        rules.add(AddFieldRule("pid", pid))
        # set datastream id
        rules.add(AddFieldRule("identifier", dsId))
        # set label
        rules.add(AddFieldRule("title", ds.getLabel()))
        # set content type
        rules.add(AddFieldRule("format", ds.getMimeType()))
        # set item type to datastream
        rules.add(AddFieldRule("item_type", "datastream"))
        # group access
        #   default to "guest"
        #   set to "admin" for VITAL metadata
        groupAccess = AddFieldRule("group_access", "guest")
        rules.add(groupAccess)
        if dsId == "VITAL":
            groupAccess.setValue("admin")

